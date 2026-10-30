package com.honouryourmentor.auth.service;

import com.honouryourmentor.auth.dto.*;
import com.honouryourmentor.auth.entity.PasswordResetToken;
import com.honouryourmentor.auth.repository.PasswordResetTokenRepository;
import com.honouryourmentor.common.exception.BadRequestException;
import com.honouryourmentor.common.exception.ResourceNotFoundException;
import com.honouryourmentor.common.exception.UnauthorizedException;
import com.honouryourmentor.security.JwtService;
import com.honouryourmentor.user.dto.UserProfileDto;
import com.honouryourmentor.user.entity.User;
import com.honouryourmentor.user.entity.UserProfile;
import com.honouryourmentor.user.repository.UserProfileRepository;
import com.honouryourmentor.user.repository.UserRepository;
import com.honouryourmentor.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
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
                .avatarUri("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80")
                .coverColor("#54000E")
                .build();
        userProfileRepository.save(profile);

        String accessToken = jwtService.generateAccessToken(user, user.getId());
        String refreshToken = jwtService.generateRefreshToken(user);

        UserProfileDto userDto = userService.getProfileByUserId(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmailOrUsername().trim();

        User user = userRepository.findByEmail(identifier.toLowerCase())
                .or(() -> userRepository.findByUsername(identifier.toLowerCase()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email/username or password"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );

        String accessToken = jwtService.generateAccessToken(user, user.getId());
        String refreshToken = jwtService.generateRefreshToken(user);

        UserProfileDto userDto = userService.getProfileByUserId(user.getId());

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

    @Transactional
    public String requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        // Check if user exists (silent if not to avoid enumeration)
        User user = userRepository.findByEmail(email).orElse(null);

        // Generate 5-digit OTP matching the frontend flow
        String otp = String.format("%05d", new Random().nextInt(100000));
        // If testing demo email, default to 12345
        if ("sarita@example.com".equalsIgnoreCase(email)) {
            otp = "12345";
        }

        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .otpCode(otp)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .used(false)
                .build();
        resetTokenRepository.save(token);

        return "We sent a 5-digit reset code to " + email + ". Use code: " + otp;
    }

    @Transactional(readOnly = true)
    public boolean verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode().trim();

        PasswordResetToken token = resetTokenRepository
                .findTopByEmailAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(email, code)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification code"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Verification code has expired");
        }

        return true;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode().trim();

        PasswordResetToken token = resetTokenRepository
                .findTopByEmailAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(email, code)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification code"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Verification code has expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        resetTokenRepository.save(token);
    }
}
