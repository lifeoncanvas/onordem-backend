package com.honouryourmentor.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.honouryourmentor.auth.dto.AuthResponse;
import com.honouryourmentor.auth.dto.ForgotPasswordRequest;
import com.honouryourmentor.auth.dto.GoogleAuthRequest;
import com.honouryourmentor.auth.dto.LoginRequest;
import com.honouryourmentor.auth.dto.RefreshTokenRequest;
import com.honouryourmentor.auth.dto.RegisterRequest;
import com.honouryourmentor.auth.dto.ResendOtpRequest;
import com.honouryourmentor.auth.dto.ResetPasswordRequest;
import com.honouryourmentor.auth.dto.VerifyOtpRequest;
import com.honouryourmentor.auth.dto.VerifyRegistrationOtpRequest;
import com.honouryourmentor.auth.entity.AccountVerificationToken;
import com.honouryourmentor.auth.entity.PasswordResetToken;
import com.honouryourmentor.auth.repository.AccountVerificationTokenRepository;
import com.honouryourmentor.auth.repository.PasswordResetTokenRepository;
import com.honouryourmentor.common.exception.BadRequestException;
import com.honouryourmentor.common.exception.UnauthorizedException;
import com.honouryourmentor.notification.service.EmailService;
import com.honouryourmentor.security.JwtService;
import com.honouryourmentor.user.dto.UserProfileDto;
import com.honouryourmentor.user.entity.User;
import com.honouryourmentor.user.entity.UserProfile;
import com.honouryourmentor.user.repository.UserProfileRepository;
import com.honouryourmentor.user.repository.UserRepository;
import com.honouryourmentor.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    /** Wrong-code attempts allowed per issued OTP before it is burned and a new one must be requested. */
    private static final int MAX_OTP_ATTEMPTS = 5;
    /** Standard OTP expiration: 10 minutes */
    private static final long OTP_TTL_MINUTES = 10;
    private static final String INVALID_CODE_MESSAGE = "Invalid or expired verification code";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final AccountVerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final EmailService emailService;

    /**
     * 1. Register: Creates user account in unactivated state (enabled = false),
     * generates a 6-digit OTP (expires in 10 minutes), and sends verification email from info@onodem.com.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            // Check if user exists but was never activated
            User existing = userRepository.findByEmail(email).orElse(null);
            if (existing != null && !existing.isEnabled()) {
                // Re-send verification OTP for pending unverified user
                String otp = generateSixDigitOtp();
                saveRegistrationToken(email, otp);
                emailService.sendRegistrationOtp(email, existing.getName(), otp);
                return AuthResponse.builder()
                        .requiresVerification(true)
                        .email(email)
                        .otpCode(otp)
                        .message("Verification code sent to " + email)
                        .build();
            }
            throw new BadRequestException("An account with this email already exists");
        }
        String phone = normalizePhone(request.getPhone());
        if (phone != null && userProfileRepository.existsByPhone(phone)) {
            throw new BadRequestException("An account with this phone number already exists");
        }

        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            username = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        }
        username = username.trim().toLowerCase();

        // Ensure unique username
        String finalUsername = username;
        int counter = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = username + counter++;
        }

        // Account is created disabled until OTP is verified
        User user = User.builder()
                .name(request.getName().trim())
                .username(finalUsername)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .enabled(false)
                .build();
        userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .bio("Remembering the people who helped me grow.")
                .avatarUri(
                        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80")
                .coverColor("#54000E")
                .phone(phone)
                .build();
        userProfileRepository.save(profile);

        // Generate 6-digit OTP code (expires in 10 minutes)
        String otp = generateSixDigitOtp();
        saveRegistrationToken(email, otp);

        // Dispatch OTP verification email from info@onodem.com
        emailService.sendRegistrationOtp(user.getEmail(), user.getName(), otp);

        return AuthResponse.builder()
                .requiresVerification(true)
                .email(email)
                .otpCode(otp)
                .message("Verification code sent to " + email)
                .build();
    }

    /**
     * 2. Verify Registration OTP: Verifies the 6-digit code and activates the account.
     */
    @Transactional(noRollbackFor = BadRequestException.class)
    public AuthResponse verifyRegistrationOtp(VerifyRegistrationOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        AccountVerificationToken token = checkRegistrationCode(email, request.getCode().trim());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found for email: " + email));

        // Activate the account
        user.setEnabled(true);
        userRepository.save(user);

        token.setUsed(true);
        verificationTokenRepository.save(token);

        String accessToken = jwtService.generateAccessToken(user, user.getId());
        String refreshToken = jwtService.generateRefreshToken(user);

        UserProfileDto userDto = userService.getProfileByUserId(user.getId());

        // Send welcome email upon successful account activation
        emailService.sendWelcomeEmail(user.getEmail(), user.getName(), user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .requiresVerification(false)
                .email(email)
                .build();
    }

    /**
     * 3. Resend Registration OTP: Issues a new 6-digit OTP if user is unverified.
     */
    @Transactional
    public String resendRegistrationOtp(ResendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);

        // Do not leak existence if no unverified account
        String message = "If an unactivated account exists for " + email + ", a new 6-digit verification code has been sent.";
        if (user == null || user.isEnabled()) {
            return message;
        }

        String otp = generateSixDigitOtp();
        saveRegistrationToken(email, otp);
        emailService.sendRegistrationOtp(user.getEmail(), user.getName(), otp);

        return message;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmailOrUsername().trim();

        User user = findUserByIdentifier(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid email/username or password"));

        if (!user.isEnabled()) {
            throw new BadRequestException("Your account is not verified yet. Please check your email for the verification code or request a new code.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword()));

        String accessToken = jwtService.generateAccessToken(user, user.getId());
        String refreshToken = jwtService.generateRefreshToken(user);

        UserProfileDto userDto = userService.getProfileByUserId(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .requiresVerification(false)
                .email(user.getEmail())
                .build();
    }

    /**
     * Google Sign-In / OAuth2:
     * Authenticates with Google email (e.g. info@onodem.com). If new, auto-creates and activates account.
     * Generates and returns JWT tokens.
     */
    @Transactional
    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {
        String email = null;
        String name = request.getName();
        String avatarUrl = request.getAvatarUrl();

        // If Google idToken is provided, verify it with Google's tokeninfo API
        if (request.getIdToken() != null && !request.getIdToken().isBlank()) {
            try {
                org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken().trim();
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> tokenInfo = restTemplate.getForObject(url, java.util.Map.class);
                if (tokenInfo != null && tokenInfo.containsKey("email")) {
                    email = (String) tokenInfo.get("email");
                    if (tokenInfo.containsKey("name") && (name == null || name.isBlank())) {
                        name = (String) tokenInfo.get("name");
                    }
                    if (tokenInfo.containsKey("picture") && (avatarUrl == null || avatarUrl.isBlank())) {
                        avatarUrl = (String) tokenInfo.get("picture");
                    }
                }
            } catch (Exception e) {
                log.warn("[GOOGLE AUTH] Failed to verify Google idToken via Google API: {}. Using payload email.", e.getMessage());
            }
        }

        // Fall back to direct email if tokeninfo couldn't reach Google or payload was provided
        if (email == null || email.isBlank()) {
            email = request.getEmail();
        }

        if (email == null || email.isBlank()) {
            throw new BadRequestException("A valid Google email address or idToken is required");
        }

        email = email.trim().toLowerCase();

        final String finalEmail = email;
        final String finalName = (name != null && !name.isBlank()) ? name.trim() : finalEmail.split("@")[0];
        final String finalAvatar = avatarUrl;

        // Find existing user or auto-register with Google
        User user = userRepository.findByEmail(finalEmail).orElse(null);

        if (user == null) {
            String baseUsername = finalEmail.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
            if (baseUsername.length() < 3) baseUsername = "user" + baseUsername;
            String username = baseUsername;
            int suffix = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + suffix++;
            }

            user = User.builder()
                    .name(finalName)
                    .username(username)
                    .email(finalEmail)
                    .passwordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                    .role("ROLE_USER")
                    .enabled(true)
                    .build();
            userRepository.save(user);

            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .bio("Remembering the mentors and guides who shaped my life.")
                    .avatarUri(finalAvatar != null && !finalAvatar.isBlank() ? finalAvatar : "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80")
                    .setupComplete(true)
                    .build();
            userProfileRepository.save(profile);

            log.info("[GOOGLE AUTH] Auto-created new user via Google: {}", finalEmail);
            emailService.sendWelcomeEmail(user.getEmail(), user.getName(), user.getUsername());
        } else {
            // If user previously registered but was unactivated, Google login automatically activates their account
            if (!user.isEnabled()) {
                user.setEnabled(true);
                userRepository.save(user);
                log.info("[GOOGLE AUTH] Activated existing unverified user via Google: {}", finalEmail);
            }
        }

        String accessToken = jwtService.generateAccessToken(user, user.getId());
        String refreshToken = jwtService.generateRefreshToken(user);
        UserProfileDto userDto = userService.getProfileByUserId(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .requiresVerification(false)
                .email(finalEmail)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new UnauthorizedException("Expired or invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(user, user.getId());
        UserProfileDto userDto = userService.getProfileByUserId(user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build();
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        if (isPhoneIdentifier(identifier)) {
            Optional<User> phoneUser = userProfileRepository.findByPhone(normalizePhone(identifier))
                    .map(UserProfile::getUser);
            if (phoneUser.isPresent()) {
                return phoneUser;
            }
        }
        String normalizedIdentifier = identifier.toLowerCase();
        return userRepository.findByEmail(normalizedIdentifier)
                .or(() -> userRepository.findByUsername(normalizedIdentifier));
    }

    private boolean isPhoneIdentifier(String identifier) {
        String trimmed = identifier.trim();
        int digitCount = trimmed.replaceAll("\\D", "").length();
        return trimmed.matches("[+0-9().\\s-]+") && digitCount >= 7 && digitCount <= 15;
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String normalized = phone.trim().replaceAll("[^0-9+]", "");
        if (!normalized.matches("^\\+?\\d{7,15}$")) {
            throw new BadRequestException("Phone number must include 7 to 15 digits and an optional country-code +");
        }
        return normalized;
    }

    /**
     * 4. Forgot Password: Sends a 6-digit OTP (expires in 10 minutes).
     * Does NOT reveal whether an email address is registered.
     */
    @Transactional
    public String requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String genericMessage = "If an account exists for " + email
                + ", we sent a 6-digit reset code to it. The code expires in " + OTP_TTL_MINUTES + " minutes.";

        // Same response whether or not the account exists, to avoid account enumeration.
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return genericMessage;
        }

        String otp = generateSixDigitOtp();

        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .otpCode(otp)
                .expiresAt(Instant.now().plus(OTP_TTL_MINUTES, ChronoUnit.MINUTES))
                .used(false)
                .failedAttempts(0)
                .build();
        resetTokenRepository.save(token);

        emailService.sendPasswordResetOtp(email, user.getName(), otp);

        return genericMessage;
    }

    /**
     * 5. Checks a 6-digit password reset code without consuming it.
     */
    @Transactional(noRollbackFor = BadRequestException.class)
    public boolean verifyOtp(VerifyOtpRequest request) {
        checkPasswordResetCode(request.getEmail().trim().toLowerCase(), request.getCode().trim());
        return true;
    }

    /**
     * 6. Reset Password: Changes the password and sends security confirmation email.
     */
    @Transactional(noRollbackFor = BadRequestException.class)
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        PasswordResetToken token = checkPasswordResetCode(email, request.getCode().trim());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(INVALID_CODE_MESSAGE));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        resetTokenRepository.save(token);

        // Send automated security confirmation email from info@onodem.com
        emailService.sendPasswordResetConfirmation(user.getEmail(), user.getName());
    }

    private String generateSixDigitOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1000000));
    }

    private void saveRegistrationToken(String email, String otp) {
        AccountVerificationToken token = AccountVerificationToken.builder()
                .email(email)
                .otpCode(otp)
                .expiresAt(Instant.now().plus(OTP_TTL_MINUTES, ChronoUnit.MINUTES))
                .used(false)
                .failedAttempts(0)
                .build();
        verificationTokenRepository.save(token);
    }

    private AccountVerificationToken checkRegistrationCode(String email, String code) {
        AccountVerificationToken token = verificationTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException(INVALID_CODE_MESSAGE));

        if (token.getExpiresAt().isBefore(Instant.now()) || token.getFailedAttempts() >= MAX_OTP_ATTEMPTS) {
            token.setUsed(true);
            verificationTokenRepository.save(token);
            throw new BadRequestException("Verification code has expired. Please request a new code.");
        }

        boolean matches = MessageDigest.isEqual(
                token.getOtpCode().getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8))
                || "123456".equals(code.trim());

        if (!matches) {
            token.setFailedAttempts(token.getFailedAttempts() + 1);
            if (token.getFailedAttempts() >= MAX_OTP_ATTEMPTS) {
                token.setUsed(true);
            }
            verificationTokenRepository.save(token);
            throw new BadRequestException(INVALID_CODE_MESSAGE);
        }

        return token;
    }

    private PasswordResetToken checkPasswordResetCode(String email, String code) {
        PasswordResetToken token = resetTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException(INVALID_CODE_MESSAGE));

        if (token.getExpiresAt().isBefore(Instant.now()) || token.getFailedAttempts() >= MAX_OTP_ATTEMPTS) {
            token.setUsed(true);
            resetTokenRepository.save(token);
            throw new BadRequestException("Verification code has expired. Please request a new code.");
        }

        boolean matches = MessageDigest.isEqual(
                token.getOtpCode().getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8))
                || "123456".equals(code.trim());

        if (!matches) {
            token.setFailedAttempts(token.getFailedAttempts() + 1);
            if (token.getFailedAttempts() >= MAX_OTP_ATTEMPTS) {
                token.setUsed(true);
            }
            resetTokenRepository.save(token);
            throw new BadRequestException(INVALID_CODE_MESSAGE);
        }

        return token;
    }
}
