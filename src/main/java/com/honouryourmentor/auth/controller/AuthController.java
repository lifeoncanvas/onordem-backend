package com.honouryourmentor.auth.controller;

import com.honouryourmentor.auth.dto.*;
import com.honouryourmentor.auth.service.AuthService;
import com.honouryourmentor.common.ApiResponse;
import com.honouryourmentor.user.dto.UserProfileDto;
import com.honouryourmentor.user.entity.User;
import com.honouryourmentor.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, OTP verification, login, JWT refresh and password reset")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final com.honouryourmentor.notification.service.EmailService emailService;

    @PostMapping("/register")
    @Operation(summary = "Register new user and send 6-digit OTP verification email")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Verification code sent to " + request.getEmail(), response));
    }

    @PostMapping("/verify-registration-otp")
    @Operation(summary = "Verify 6-digit registration OTP and activate user account")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyRegistrationOtp(@Valid @RequestBody VerifyRegistrationOtpRequest request) {
        AuthResponse response = authService.verifyRegistrationOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Account verified and activated successfully", response));
    }

    @PostMapping("/resend-registration-otp")
    @Operation(summary = "Resend 6-digit registration OTP verification email")
    public ResponseEntity<ApiResponse<String>> resendRegistrationOtp(@Valid @RequestBody ResendOtpRequest request) {
        String msg = authService.resendRegistrationOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(msg, null));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email or username")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request 6-digit password reset OTP code (expires in 10 minutes)")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String msg = authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok(msg, null));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify 6-digit password reset OTP")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean valid = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified successfully", valid));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Set new password using verified 6-digit OTP")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully. You can now login.", null));
    }

    @GetMapping("/test-email")
    @Operation(summary = "Diagnostic test to verify outbound email delivery from info@onodem.com")
    public ResponseEntity<ApiResponse<String>> testEmail(@RequestParam(defaultValue = "info@onodem.com") String to) {
        String result = emailService.testEmailDelivery(to);
        return ResponseEntity.ok(ApiResponse.ok(result, to));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        UserProfileDto profile = userService.getProfileByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }
}
