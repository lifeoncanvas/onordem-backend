package com.honouryourmentor;

import com.honouryourmentor.auth.dto.*;
import com.honouryourmentor.auth.entity.AccountVerificationToken;
import com.honouryourmentor.auth.entity.PasswordResetToken;
import com.honouryourmentor.auth.repository.AccountVerificationTokenRepository;
import com.honouryourmentor.auth.repository.PasswordResetTokenRepository;
import com.honouryourmentor.auth.service.AuthService;
import com.honouryourmentor.engagement.repository.AppreciationNotificationRepository;
import com.honouryourmentor.notification.entity.EmailDeliveryLog;
import com.honouryourmentor.notification.repository.EmailDeliveryLogRepository;
import com.honouryourmentor.user.entity.User;
import com.honouryourmentor.user.repository.UserRepository;
import com.honouryourmentor.user.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("neon")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComprehensiveEmailWorkflowsTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountVerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private AppreciationNotificationRepository appreciationNotificationRepository;

    @Autowired
    private EmailDeliveryLogRepository deliveryLogRepository;

    private static final String TEST_USER_1_EMAIL = "onodem.recipient.test@gmail.com";
    private static final String TEST_USER_2_EMAIL = "onodem.sender.test@gmail.com";

    @Test
    @Order(1)
    void testWorkflow1_AccountRegistrationAndOtpVerification() {
        System.out.println("=== WORKFLOW 1: ACCOUNT REGISTRATION & 6-DIGIT OTP VERIFICATION ===");

        // Cleanup previous test state if present
        userRepository.findByEmail(TEST_USER_1_EMAIL).ifPresent(u -> userRepository.delete(u));

        // Step 1: Register new account
        RegisterRequest registerReq = RegisterRequest.builder()
                .name("Recipient User")
                .email(TEST_USER_1_EMAIL)
                .password("SecurePass2026!")
                .build();

        AuthResponse regResponse = authService.register(registerReq);
        assertNotNull(regResponse);
        assertTrue(regResponse.isRequiresVerification(), "Registration must require OTP verification");
        assertEquals(TEST_USER_1_EMAIL, regResponse.getEmail());
        assertNull(regResponse.getAccessToken(), "Access token should not be granted before OTP verification");

        // Verify user is in unactivated state
        User unactivatedUser = userRepository.findByEmail(TEST_USER_1_EMAIL).orElse(null);
        assertNotNull(unactivatedUser);
        assertFalse(unactivatedUser.isEnabled(), "Account must be disabled prior to OTP verification");

        // Step 2: Verify 6-digit OTP was generated with 10-minute expiry
        AccountVerificationToken token = verificationTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_USER_1_EMAIL)
                .orElse(null);
        assertNotNull(token);
        assertEquals(6, token.getOtpCode().length(), "OTP must be exactly 6 digits");
        assertFalse(token.isUsed());
        assertTrue(token.getExpiresAt().isAfter(Instant.now().plus(4, ChronoUnit.MINUTES)), "OTP must expire in 5-10 minutes");

        System.out.println("Generated 6-digit registration OTP: " + token.getOtpCode());

        // Step 3: Test resend OTP functionality
        String resendMsg = authService.resendRegistrationOtp(new ResendOtpRequest(TEST_USER_1_EMAIL));
        assertTrue(resendMsg.contains("6-digit verification code"));

        AccountVerificationToken newToken = verificationTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_USER_1_EMAIL)
                .orElse(null);
        assertNotNull(newToken);
        assertEquals(6, newToken.getOtpCode().length());

        // Step 4: Verify OTP and activate account
        VerifyRegistrationOtpRequest verifyReq = VerifyRegistrationOtpRequest.builder()
                .email(TEST_USER_1_EMAIL)
                .code(newToken.getOtpCode())
                .build();

        AuthResponse verifyResponse = authService.verifyRegistrationOtp(verifyReq);
        assertNotNull(verifyResponse);
        assertFalse(verifyResponse.isRequiresVerification());
        assertNotNull(verifyResponse.getAccessToken(), "Access token must be returned upon successful activation");
        assertNotNull(verifyResponse.getUser());
        assertEquals("Recipient User", verifyResponse.getUser().getName());

        // Verify account is now enabled
        User activatedUser = userRepository.findByEmail(TEST_USER_1_EMAIL).orElse(null);
        assertNotNull(activatedUser);
        assertTrue(activatedUser.isEnabled(), "Account must be enabled after OTP verification");

        System.out.println("✅ Workflow 1 verified: Registration, 6-digit OTP, resend, and account activation successful!");
    }

    @Test
    @Order(2)
    void testWorkflow2_ForgotPasswordAndOtpVerification() {
        System.out.println("=== WORKFLOW 2: FORGOT PASSWORD & 6-DIGIT OTP VERIFICATION ===");

        // Step 1: Request password reset
        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest(TEST_USER_1_EMAIL);
        String forgotMsg = authService.requestPasswordReset(forgotReq);
        assertTrue(forgotMsg.contains("6-digit reset code"));
        assertTrue(forgotMsg.contains("10 minutes"));

        // Step 2: Verify non-existent email gives the EXACT same generic message (prevents account enumeration)
        String nonExistentMsg = authService.requestPasswordReset(new ForgotPasswordRequest("totally.unknown@onodem.com"));
        assertEquals(
                "If an account exists for totally.unknown@onodem.com, we sent a 6-digit reset code to it. The code expires in 10 minutes.",
                nonExistentMsg
        );

        // Step 3: Verify 6-digit OTP was stored
        PasswordResetToken token = resetTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_USER_1_EMAIL)
                .orElse(null);
        assertNotNull(token);
        assertEquals(6, token.getOtpCode().length(), "Password reset OTP must be exactly 6 digits");
        assertFalse(token.isUsed());
        assertTrue(token.getExpiresAt().isAfter(Instant.now().plus(4, ChronoUnit.MINUTES)));

        System.out.println("Generated 6-digit password reset OTP: " + token.getOtpCode());

        // Step 4: Verify the code via verifyOtp endpoint
        VerifyOtpRequest verifyOtpReq = new VerifyOtpRequest(TEST_USER_1_EMAIL, token.getOtpCode());
        boolean isValid = authService.verifyOtp(verifyOtpReq);
        assertTrue(isValid);

        // Step 5: Reset password with verified code
        ResetPasswordRequest resetReq = ResetPasswordRequest.builder()
                .email(TEST_USER_1_EMAIL)
                .code(token.getOtpCode())
                .newPassword("BrandNewPassword2026!")
                .build();

        authService.resetPassword(resetReq);

        // Verify token burned
        PasswordResetToken burnedToken = resetTokenRepository.findById(token.getId()).orElse(null);
        assertNotNull(burnedToken);
        assertTrue(burnedToken.isUsed());

        // Step 6: Verify can login with new password
        AuthResponse loginResponse = authService.login(LoginRequest.builder()
                .emailOrUsername(TEST_USER_1_EMAIL)
                .password("BrandNewPassword2026!")
                .build());
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getAccessToken());

        System.out.println("✅ Workflow 2 verified: Forgot password, 6-digit OTP, security enumeration prevention, and password reset successful!");
    }

    @Test
    @Order(3)
    void testWorkflow3_AppreciationNotificationAndDeduplication() {
        System.out.println("=== WORKFLOW 3: APPRECIATION NOTIFICATION & DEDUPLICATION ===");

        // Setup Sender User
        userRepository.findByEmail(TEST_USER_2_EMAIL).ifPresent(u -> userRepository.delete(u));

        RegisterRequest senderReq = RegisterRequest.builder()
                .name("Sender User")
                .email(TEST_USER_2_EMAIL)
                .password("SenderPass2026!")
                .build();
        authService.register(senderReq);
        AccountVerificationToken senderToken = verificationTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_USER_2_EMAIL)
                .orElseThrow();
        authService.verifyRegistrationOtp(new VerifyRegistrationOtpRequest(TEST_USER_2_EMAIL, senderToken.getOtpCode()));

        User sender = userRepository.findByEmail(TEST_USER_2_EMAIL).orElseThrow();
        User recipient = userRepository.findByEmail(TEST_USER_1_EMAIL).orElseThrow();

        // Send appreciation from Sender to Recipient
        String apprecResult1 = userService.appreciateUser(sender.getId(), recipient.getId());
        assertTrue(apprecResult1.contains("Appreciation notification sent"));

        // Test Deduplication: Immediate second appreciation must be suppressed to avoid spamming recipient
        String apprecResult2 = userService.appreciateUser(sender.getId(), recipient.getId());
        assertTrue(apprecResult2.contains("already appreciated"));

        // Verify delivery audit logs are tracking every dispatch
        List<EmailDeliveryLog> logs = deliveryLogRepository.findByRecipientOrderBySentAtDesc(recipient.getEmail());
        assertFalse(logs.isEmpty(), "Email delivery logs must record every outbound email");

        System.out.println("Recorded email delivery logs for recipient: " + logs.size());
        for (EmailDeliveryLog logEntry : logs) {
            System.out.printf("  [Audit] Type: %s | Status: %s | Subject: %s%n",
                    logEntry.getEmailType(), logEntry.getStatus(), logEntry.getSubject());
        }

        System.out.println("✅ Workflow 3 verified: Appreciation notification dispatched, deduplication enforced, and delivery tracked!");
    }
}

