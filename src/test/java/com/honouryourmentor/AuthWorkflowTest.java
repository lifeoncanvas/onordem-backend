package com.honouryourmentor;

import com.honouryourmentor.auth.dto.ForgotPasswordRequest;
import com.honouryourmentor.auth.dto.RegisterRequest;
import com.honouryourmentor.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("neon")
class AuthWorkflowTest {

    @Autowired
    private AuthService authService;

    @Test
    void testAuthWorkflow() {
        System.out.println("Testing forgotPassword request...");
        try {
            String res = authService.requestPasswordReset(new ForgotPasswordRequest("nonexistent_test@example.com"));
            System.out.println("Result: " + res);
        } catch (Exception e) {
            System.err.println("ForgotPassword error: ");
            e.printStackTrace();
        }
    }
}
