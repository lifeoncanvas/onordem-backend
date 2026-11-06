package com.honouryourmentor.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.honouryourmentor.auth.dto.AuthResponse;
import com.honouryourmentor.auth.dto.ForgotPasswordRequest;
import com.honouryourmentor.auth.dto.LoginRequest;
import com.honouryourmentor.auth.dto.RefreshTokenRequest;
import com.honouryourmentor.auth.dto.RegisterRequest;
import com.honouryourmentor.auth.dto.ResetPasswordRequest;
import com.honouryourmentor.auth.dto.VerifyOtpRequest;
import com.honouryourmentor.auth.entity.PasswordResetToken;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    /** Wrong-code attempts allowed per issued OTP before it is burned and a new one must be requested. */
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long OTP_TTL_MINUTES = 15;
    private static final String INVALID_CODE_MESSAGE = "Invalid or expired verification code";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
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

        User user = User.builder()
                .name(request.getName().trim())
                .username(finalUsername)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .enabled(true)
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

        String accessToken = jwtService.generateAccessToken(user, user.getId());
        String refreshToken = jwtService.generateRefreshToken(user);

        UserProfileDto userDto = userService.getProfileByUserId(user.getId());

        // Send automated welcome & verification email from info@onodem.com
        emailService.sendWelcomeEmail(user.getEmail(), user.getName(), user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmailOrUsername().trim();

        User user = findUserByIdentifier(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid email/username or password"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword()));

        String accessToken = jwtService.generateAccessToken(user, user.getId());
        String refreshToken = jwtService.generateRefreshToken(user);

        UserProfileDto userDto = userService.getProfileByUserId(user.getId());

        // Send automated welcome email whenever user signs in as requested
        emailService.sendWelcomeEmail(user.getEmail(), user.getName(), user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
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

    @Transactional
    public String requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String genericMessage = "If an account exists for " + email
                + ", we sent a 5-digit reset code to it. The code expires in " + OTP_TTL_MINUTES + " minutes.";

        // Same response whether or not the account exists, to avoid account enumeration.
        // Only real accounts get a token and an email, so this endpoint can't be used to spam arbitrary inboxes.
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return genericMessage;
        }

        String otp = String.format("%05d", SECURE_RANDOM.nextInt(100000));

        // Only the newest code is ever checked (see findActiveToken), so issuing a new one supersedes older ones.
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
     * Checks a code without consuming it. Counts wrong guesses, so it must run in a writable transaction.
     * noRollbackFor keeps the attempt counter even though a wrong code throws.
     */
    @Transactional(noRollbackFor = BadRequestException.class)
    public boolean verifyOtp(VerifyOtpRequest request) {
        checkCode(request.getEmail().trim().toLowerCase(), request.getCode().trim());
        return true;
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        PasswordResetToken token = checkCode(email, request.getCode().trim());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(INVALID_CODE_MESSAGE));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        resetTokenRepository.save(token);

        // Send automated security confirmation email from info@onodem.com
        emailService.sendPasswordResetConfirmation(user.getEmail(), user.getName());
    }

    /**
     * Validates the code against the newest unused token for this email.
     * A wrong code increments the token's failure counter; after MAX_OTP_ATTEMPTS the token is burned,
     * so a 5-digit code can't be brute-forced.
     */
    private PasswordResetToken checkCode(String email, String code) {
        PasswordResetToken token = resetTokenRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException(INVALID_CODE_MESSAGE));

        if (token.getExpiresAt().isBefore(Instant.now()) || token.getFailedAttempts() >= MAX_OTP_ATTEMPTS) {
            token.setUsed(true);
            resetTokenRepository.save(token);
            throw new BadRequestException(INVALID_CODE_MESSAGE);
        }

        boolean matches = MessageDigest.isEqual(
                token.getOtpCode().getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8));

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
