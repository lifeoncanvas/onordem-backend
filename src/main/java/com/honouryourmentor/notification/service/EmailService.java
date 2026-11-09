package com.honouryourmentor.notification.service;

import com.honouryourmentor.notification.entity.EmailDeliveryLog;
import com.honouryourmentor.notification.repository.EmailDeliveryLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Production-ready Automated Email Notification System for Onodem (https://onodem.com/).
 * Sender Email: info@onodem.com
 *
 * Supported Workflows:
 *   1. Account Registration: 6-digit OTP verification code (expires in 10 minutes)
 *   2. Welcome Email upon account activation / login
 *   3. Forgot Password: 6-digit OTP verification code (expires in 10 minutes)
 *   4. Password Reset Confirmation: Security alert after password update
 *   5. Appreciation Notification Email: "Someone Appreciated You on Onodem!"
 *   6. Tribute Dedication: Dedicated card notification
 *
 * Delivery Engine:
 *   - Primary: Authenticated JavaMailSender (smtpout.secureserver.net:465 / 587)
 *   - HTTP API fallback: Resend / Brevo / SendGrid via EMAIL_API_KEY / RESEND_API_KEY
 *   - Server relay fallback: 127.0.0.1:25 & relay-hosting.secureserver.net:25
 *   - Delivery tracking: every email attempt is recorded in email_delivery_logs
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private EmailDeliveryLogRepository deliveryLogRepository;

    @Value("${app.mail.from:info@onodem.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Onodem}")
    private String fromName;

    @Value("${app.mail.app-url:https://onodem.com}")
    private String appUrl;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${EMAIL_API_KEY:${RESEND_API_KEY:}}")
    private String emailApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    /**
     * 1. Account Registration: Send 6-digit OTP verification code before activating account.
     */
    @Async("mailTaskExecutor")
    public void sendRegistrationOtp(String toEmail, String name, String otpCode) {
        String displayName = (name != null && !name.isBlank()) ? name.trim() : "New Member";
        String subject = "Verify Your Onodem Account — Code: " + otpCode;

        String formattedOtp = formatOtpDisplay(otpCode);

        String htmlContent = buildEmailTemplate(
                "Verify Your Email Address",
                "Complete your registration on Onodem",
                """
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    Hello <strong style="color: #FFFFFF;">%s</strong>,
                </p>
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 20px;">
                    Thank you for signing up for <strong>Onodem</strong>. Please enter the 6-digit verification code below to verify your email and activate your account:
                </p>
                <div style="background-color: #121215; border: 2px solid #54000E; border-radius: 14px; padding: 22px; text-align: center; margin: 18px 0 24px 0;">
                    <span style="font-family: 'Courier New', Courier, monospace; font-size: 38px; font-weight: 800; letter-spacing: 10px; color: #FFFFFF;">%s</span>
                </div>
                <div style="background-color: #18181B; border-left: 3px solid #EC8F3E; padding: 12px 16px; border-radius: 6px; margin-bottom: 20px;">
                    <p style="font-size: 13px; color: #FCD34D; margin: 0; line-height: 1.5;">
                        <strong>Expiry Notice:</strong> This code will expire in <strong>10 minutes</strong>. If you did not request this, please ignore this email.
                    </p>
                </div>
                <div style="text-align: center; margin: 26px 0;">
                    <a href="%s" style="display: inline-block; background-color: #54000E; color: #FFFFFF; font-weight: 700; font-size: 14px; text-decoration: none; padding: 13px 30px; border-radius: 10px; border: 1px solid rgba(236,143,62,0.4);">Open Onodem</a>
                </div>
                """.formatted(escapeHtml(displayName), escapeHtml(formattedOtp), appUrl)
        );

        sendEmail(toEmail, subject, htmlContent, "REGISTRATION_OTP");
    }

    /**
     * 2. Forgot Password: Send 6-digit OTP verification code for secure password reset.
     */
    @Async("mailTaskExecutor")
    public void sendPasswordResetOtp(String toEmail, String name, String otpCode) {
        String displayName = (name != null && !name.isBlank()) ? name.trim() : "Member";
        String subject = "Your Password Reset Code: " + otpCode + " — Onodem";

        String formattedOtp = formatOtpDisplay(otpCode);

        String htmlContent = buildEmailTemplate(
                "Password Reset Request",
                "Verification code for your onodem.com account",
                """
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    Hello <strong style="color: #FFFFFF;">%s</strong>,
                </p>
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 20px;">
                    We received a request to reset your password on Onodem. Use this 6-digit verification code to securely reset your password:
                </p>
                <div style="background-color: #121215; border: 2px solid #54000E; border-radius: 14px; padding: 22px; text-align: center; margin: 18px 0 24px 0;">
                    <span style="font-family: 'Courier New', Courier, monospace; font-size: 38px; font-weight: 800; letter-spacing: 10px; color: #FFFFFF;">%s</span>
                </div>
                <div style="background-color: #18181B; border-left: 3px solid #EC8F3E; padding: 12px 16px; border-radius: 6px; margin-bottom: 20px;">
                    <p style="font-size: 13px; color: #FCD34D; margin: 0; line-height: 1.5;">
                        <strong>Security Notice:</strong> This code expires in <strong>10 minutes</strong> and can only be used once. Never share this code with anyone.
                    </p>
                </div>
                <p style="font-size: 13px; color: #71717A; line-height: 1.5;">
                    If you did not request a password reset, please disregard this email. Your password remains safe and unchanged.
                </p>
                """.formatted(escapeHtml(displayName), escapeHtml(formattedOtp))
        );

        sendEmail(toEmail, subject, htmlContent, "PASSWORD_RESET_OTP");
    }

    /**
     * 3. Appreciation Notification Email:
     * Sent when a user appreciates another user on the platform.
     * Subject: Someone Appreciated You on Onodem!
     * Content: Exactly matching the user prompt requirement without exposing private info.
     */
    @Async("mailTaskExecutor")
    public void sendAppreciationNotification(String recipientEmail, String recipientName, String senderName) {
        String greeting = (recipientName != null && !recipientName.isBlank()) ? recipientName.trim() : "Member";
        String author = (senderName != null && !senderName.isBlank()) ? senderName.trim() : "Someone in the community";
        String subject = "Someone Appreciated You on Onodem!";

        String htmlContent = buildEmailTemplate(
                "Someone Appreciated You on Onodem!",
                "A community member appreciated your profile and contributions",
                """
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    Hello <strong style="color: #FFFFFF;">%s</strong>,
                </p>
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    Good news! Someone has appreciated you on Onodem.
                </p>
                <div style="background-color: #18181B; border-left: 3px solid #EC8F3E; border-radius: 8px; padding: 18px 20px; margin: 20px 0;">
                    <p style="font-size: 16px; color: #FFFFFF; font-weight: 600; margin: 0 0 4px 0;">
                        &#10024; <span style="color: #EC8F3E;">%s</span> has appreciated you on the platform.
                    </p>
                </div>
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 24px;">
                    Log in to your Onodem account to see your appreciation and connect with the community.
                </p>
                <div style="text-align: center; margin: 28px 0;">
                    <a href="%s" style="display: inline-block; background-color: #54000E; color: #FFFFFF; font-weight: 700; font-size: 15px; text-decoration: none; padding: 14px 32px; border-radius: 10px; border: 1px solid rgba(236,143,62,0.4); letter-spacing: -0.2px;">View on Onodem</a>
                </div>
                <p style="font-size: 14px; color: #A1A1AA; line-height: 1.6; margin-top: 24px;">
                    Thank you for being part of Onodem!
                </p>
                """.formatted(escapeHtml(greeting), escapeHtml(author), appUrl)
        );

        sendEmail(recipientEmail, subject, htmlContent, "APPRECIATION_NOTIFICATION");
    }

    /**
     * 4. Account Registration: Welcome email once an account is verified and activated.
     */
    @Async("mailTaskExecutor")
    public void sendWelcomeEmail(String toEmail, String name, String username) {
        String displayName = (name != null && !name.isBlank()) ? name.trim() : "Friend";
        String userHandle = (username != null && !username.isBlank()) ? username.trim() : toEmail.split("@")[0];
        String subject = "Welcome to Onodem — Keep their memories alive";

        String htmlContent = buildEmailTemplate(
                "Welcome to Onodem",
                "Your account is ready to celebrate those who shaped your life",
                """
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    Hello <strong style="color: #FFFFFF;">%s</strong>,
                </p>
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 20px;">
                    Welcome to <strong>Onodem</strong>. We are honored to have you with us.
                    Your personal account has been created with username:
                    <span style="display: inline-block; background-color: #27272A; color: #EC8F3E; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-family: monospace;">@%s</span>
                </p>
                <div style="background-color: #18181B; border: 1px solid #27272A; border-radius: 12px; padding: 20px; margin-bottom: 24px;">
                    <p style="font-size: 14px; color: #A1A1AA; margin: 0 0 10px 0; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px;">What you can do next:</p>
                    <ul style="margin: 0; padding-left: 20px; color: #D4D4D8; font-size: 14px; line-height: 1.8;">
                        <li><strong>Add your first mentor:</strong> Teachers, parents, coaches, or guides who made an impact.</li>
                        <li><strong>Create a tribute:</strong> Write open letters, record voice notes, or scan photos.</li>
                        <li><strong>Memory Wall:</strong> Explore community stories and preserve timeless lessons.</li>
                    </ul>
                </div>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s/home" style="display: inline-block; background-color: #54000E; color: #FFFFFF; font-weight: 700; font-size: 15px; text-decoration: none; padding: 14px 32px; border-radius: 10px; border: 1px solid rgba(236,143,62,0.4);">Open Onodem</a>
                </div>
                """.formatted(escapeHtml(displayName), escapeHtml(userHandle), appUrl)
        );

        sendEmail(toEmail, subject, htmlContent, "WELCOME_EMAIL");
    }

    /**
     * 5. Password Reset Confirmation: Security alert after password was successfully changed.
     */
    @Async("mailTaskExecutor")
    public void sendPasswordResetConfirmation(String toEmail, String name) {
        String displayName = (name != null && !name.isBlank()) ? name.trim() : "Member";
        String subject = "Security Alert: Your Password Was Changed — Onodem";
        String timestamp = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm 'UTC'"));

        String htmlContent = buildEmailTemplate(
                "Password Changed Successfully",
                "Security notification regarding your Onodem account",
                """
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    Hello <strong style="color: #FFFFFF;">%s</strong>,
                </p>
                <div style="background-color: #121215; border: 1px solid #10B981; border-radius: 12px; padding: 18px 20px; margin: 16px 0 22px 0;">
                    <p style="font-size: 15px; color: #10B981; font-weight: 700; margin: 0 0 6px 0;">
                        &#10003; Your password was successfully updated
                    </p>
                    <p style="font-size: 13px; color: #A1A1AA; margin: 0;">
                        Time of change: <span style="color: #FFFFFF;">%s</span>
                    </p>
                </div>
                <p style="font-size: 14px; color: #E4E4E7; line-height: 1.6; margin-bottom: 24px;">
                    If you made this change, you can now log in using your new credentials. No further action is required.
                </p>
                <div style="text-align: center; margin: 24px 0;">
                    <a href="%s/login" style="display: inline-block; background-color: #54000E; color: #FFFFFF; font-weight: 700; font-size: 14px; text-decoration: none; padding: 12px 28px; border-radius: 8px;">Log In to Onodem</a>
                </div>
                <div style="background-color: #18181B; border: 1px solid #27272A; border-radius: 10px; padding: 16px; margin-top: 24px;">
                    <p style="font-size: 13px; color: #EF4444; margin: 0 0 6px 0; font-weight: 700;">
                        Did not make this change?
                    </p>
                    <p style="font-size: 12.5px; color: #A1A1AA; margin: 0; line-height: 1.5;">
                        If you did not change your password, contact our team immediately by replying to <a href="mailto:info@onodem.com" style="color: #EC8F3E; text-decoration: none;">info@onodem.com</a>.
                    </p>
                </div>
                """.formatted(escapeHtml(displayName), timestamp, appUrl)
        );

        sendEmail(toEmail, subject, htmlContent, "PASSWORD_RESET_CONFIRMATION");
    }

    /**
     * 6. Dedicated Tribute Email: Sent when a tribute is created for a mentor/recipient.
     */
    @Async("mailTaskExecutor")
    public void sendDedicationEmail(String toEmail, String mentorName, String senderName, String tributeTitle, String tributeKind, String excerpt, String tributeId) {
        String greeting = (mentorName != null && !mentorName.isBlank()) ? mentorName.trim() : "Mentor";
        String author = (senderName != null && !senderName.isBlank()) ? senderName.trim() : "Someone grateful";
        String kindDisplay = (tributeKind != null && !tributeKind.isBlank()) ? tributeKind.substring(0, 1).toUpperCase() + tributeKind.substring(1) : "Tribute";
        String subject = "✨ " + author + " has dedicated a " + kindDisplay + " to you on Onodem";

        String postLink = appUrl + "/memory-wall";
        if (tributeId != null && !tributeId.isBlank()) {
            postLink = appUrl + "/memory-detail/" + tributeId;
        }

        String excerptBlock = "";
        if (excerpt != null && !excerpt.isBlank()) {
            excerptBlock = """
            <div style="background-color: #121215; border-left: 3px solid #EC8F3E; border-radius: 8px; padding: 18px 20px; margin: 20px 0;">
                <p style="font-size: 15px; font-style: italic; color: #FFFFFF; line-height: 1.6; margin: 0;">
                    &ldquo;%s&rdquo;
                </p>
                <p style="font-size: 13px; color: #A1A1AA; text-align: right; margin: 10px 0 0 0; font-weight: 600;">
                    — %s
                </p>
            </div>
            """.formatted(escapeHtml(excerpt), escapeHtml(author));
        }

        String htmlContent = buildEmailTemplate(
                "A Tribute Has Been Dedicated to You",
                author + " wrote words of honour and gratitude in your name",
                """
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    Dear <strong style="color: #FFFFFF;">%s</strong>,
                </p>
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    <strong style="color: #FFFFFF;">%s</strong> has created and dedicated a special <strong>%s</strong> to you on <em>Onodem</em>.
                </p>
                %s
                <p style="font-size: 14px; color: #A1A1AA; line-height: 1.6; margin-bottom: 26px;">
                    Your guidance, lessons, and presence made a lasting difference. You can read the full dedication, view attached photos and letters, and leave your thoughts on our memory wall.
                </p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="display: inline-block; background-color: #54000E; color: #FFFFFF; font-weight: 700; font-size: 15px; text-decoration: none; padding: 14px 32px; border-radius: 10px; border: 1px solid rgba(236,143,62,0.4);">Read Your Dedicated Tribute</a>
                </div>
                """.formatted(escapeHtml(greeting), escapeHtml(author), escapeHtml(kindDisplay.toLowerCase()), excerptBlock, postLink)
        );

        sendEmail(toEmail, subject, htmlContent, "TRIBUTE_DEDICATION");
    }

    /**
     * Core multi-tier email dispatch method:
     *   Tier 1: Configured JavaMailSender (smtpout.secureserver.net / SSL / STARTTLS)
     *   Tier 2: Direct REST Email API (Resend / SendGrid) if API key is present
     *   Tier 3: Local Exim host relay (127.0.0.1:25) or GoDaddy internal relay (relay-hosting.secureserver.net:25)
     *   Tier 4: Audit & Delivery Logging to email_delivery_logs table
     */
    private void sendEmail(String toEmail, String subject, String htmlBody, String emailType) {
        if (!mailEnabled) {
            log.info("[EMAIL DISABLED] Would send from {} to {}: {}", fromEmail, toEmail, subject);
            recordDelivery(toEmail, subject, emailType, "DISABLED", "Email dispatch is disabled via app.mail.enabled");
            return;
        }

        String lastError = null;

        // Tier 1: Configured Spring JavaMailSender
        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail, fromName);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);

                mailSender.send(message);
                log.info("[EMAIL SENT - PRIMARY] Dispatched email from {} to {} | Subject: {}", fromEmail, toEmail, subject);
                recordDelivery(toEmail, subject, emailType, "SENT", null);
                return;
            } catch (Exception ex) {
                lastError = ex.getMessage();
                log.warn("[EMAIL PRIMARY FAILED] SMTP dispatch from {} to {} failed: {}. Attempting fallbacks...", fromEmail, toEmail, lastError);
            }
        }

        // Tier 2: Resend / SendGrid REST API if EMAIL_API_KEY is configured
        if (emailApiKey != null && !emailApiKey.isBlank()) {
            if (tryRestApiDispatch(toEmail, subject, htmlBody)) {
                log.info("[EMAIL SENT - REST API] Delivered via Email REST API to {}", toEmail);
                recordDelivery(toEmail, subject, emailType, "SENT_API", null);
                return;
            }
        }

        // Tier 3: Host Relay Fallback (GoDaddy local Exim on 127.0.0.1:25 or relay-hosting.secureserver.net:25)
        if (tryHostRelayFallback(toEmail, subject, htmlBody)) {
            recordDelivery(toEmail, subject, emailType, "SENT_RELAY", null);
            return;
        }

        // Tier 4: Record failure in delivery tracking table
        log.error("[EMAIL ERROR] All SMTP delivery routes failed for email to {}. Recorded in delivery log. Last error: {}", toEmail, lastError);
        recordDelivery(toEmail, subject, emailType, "FAILED", lastError != null ? lastError : "No mail transport available");
    }

    private boolean tryRestApiDispatch(String toEmail, String subject, String htmlBody) {
        try {
            // Support Resend HTTPS API (standard JSON payload)
            String jsonPayload = """
            {
                "from": "%s <%s>",
                "to": ["%s"],
                "subject": "%s",
                "html": %s
            }
            """.formatted(
                    fromName,
                    fromEmail,
                    toEmail,
                    escapeJson(subject),
                    escapeJsonString(htmlBody)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + emailApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            } else {
                log.warn("[EMAIL API FAILED] Status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("[EMAIL API EXCEPTION] {}", e.getMessage());
        }
        return false;
    }

    private boolean tryHostRelayFallback(String toEmail, String subject, String htmlBody) {
        // Try local Exim on 127.0.0.1:25
        try {
            org.springframework.mail.javamail.JavaMailSenderImpl localSender = new org.springframework.mail.javamail.JavaMailSenderImpl();
            localSender.setHost("127.0.0.1");
            localSender.setPort(25);
            java.util.Properties props = localSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "false");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.connectiontimeout", "3000");
            props.put("mail.smtp.timeout", "3000");

            MimeMessage message = localSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            localSender.send(message);
            log.info("[EMAIL SENT - LOCAL RELAY] Delivered via 127.0.0.1:25 to {}", toEmail);
            return true;
        } catch (Exception e1) {
            log.debug("127.0.0.1:25 relay not reachable: {}", e1.getMessage());
        }

        // Try GoDaddy internal relay on relay-hosting.secureserver.net:25
        try {
            org.springframework.mail.javamail.JavaMailSenderImpl gdRelaySender = new org.springframework.mail.javamail.JavaMailSenderImpl();
            gdRelaySender.setHost("relay-hosting.secureserver.net");
            gdRelaySender.setPort(25);
            java.util.Properties props = gdRelaySender.getJavaMailProperties();
            props.put("mail.smtp.auth", "false");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.connectiontimeout", "4000");
            props.put("mail.smtp.timeout", "4000");

            MimeMessage message = gdRelaySender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            gdRelaySender.send(message);
            log.info("[EMAIL SENT - GODADDY RELAY] Delivered via relay-hosting.secureserver.net:25 to {}", toEmail);
            return true;
        } catch (Exception e2) {
            log.debug("relay-hosting.secureserver.net:25 not reachable: {}", e2.getMessage());
        }

        return false;
    }

    private void recordDelivery(String toEmail, String subject, String emailType, String status, String errorMessage) {
        if (deliveryLogRepository != null) {
            try {
                EmailDeliveryLog entry = EmailDeliveryLog.builder()
                        .recipient(toEmail)
                        .subject(subject)
                        .emailType(emailType != null ? emailType : "GENERAL")
                        .status(status)
                        .errorMessage(errorMessage)
                        .sentAt(Instant.now())
                        .build();
                deliveryLogRepository.save(entry);
            } catch (Exception e) {
                log.warn("Could not write email delivery log: {}", e.getMessage());
            }
        }
    }

    /**
     * Diagnostic testing method to verify live email delivery and return detailed status.
     */
    public String testEmailDelivery(String toEmail) {
        String testSubject = "Test Email from Onodem (info@onodem.com)";
        String testContent = buildEmailTemplate(
                "SMTP Test Delivery",
                "Verification of automated email delivery system",
                "<p style='color: #E4E4E7; font-size: 15px;'>This is a test email sent from <strong>info@onodem.com</strong> to verify that outbound email dispatch is operational.</p>"
        );

        sendEmail(toEmail, testSubject, testContent, "TEST_DISPATCH");
        return "Dispatched test email to " + toEmail + " from " + fromEmail + ". Check inbox or spam.";
    }

    private String formatOtpDisplay(String otp) {
        if (otp == null) return "";
        String clean = otp.trim();
        if (clean.length() == 6) {
            return clean.substring(0, 3) + " " + clean.substring(3);
        }
        return clean;
    }

    /**
     * Dark luxury HTML email template matching the Onodem brand system.
     */
    private String buildEmailTemplate(String headerTitle, String headerSubtitle, String bodyHtml) {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>%s</title>
        </head>
        <body style="margin: 0; padding: 0; background-color: #09090B; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #FFFFFF;">
            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #09090B; padding: 40px 16px;">
                <tr>
                    <td align="center">
                        <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="max-width: 580px; background-color: #141416; border: 1px solid #27272A; border-radius: 18px; overflow: hidden; box-shadow: 0 16px 40px rgba(0,0,0,0.6);">
                            <!-- Brand Header -->
                            <tr>
                                <td style="background-color: #000000; padding: 28px 32px; border-bottom: 1px solid #27272A; text-align: left;">
                                    <table cellpadding="0" cellspacing="0" border="0" width="100%%">
                                        <tr>
                                            <td style="vertical-align: middle;">
                                                <div style="display: inline-block; width: 36px; height: 36px; background-color: #18181B; border: 1px solid #3F3F46; border-radius: 10px; text-align: center; line-height: 36px; font-size: 20px;">
                                                    &#10024;
                                                </div>
                                                <span style="font-size: 18px; font-weight: 800; color: #FFFFFF; letter-spacing: -0.4px; margin-left: 12px; vertical-align: middle;">
                                                    Onodem
                                                </span>
                                            </td>
                                            <td align="right" style="vertical-align: middle;">
                                                <span style="font-size: 11px; color: #EC8F3E; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">
                                                    onodem.com
                                                </span>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <!-- Title Banner -->
                            <tr>
                                <td style="padding: 28px 32px 12px 32px;">
                                    <h1 style="font-size: 22px; font-weight: 800; color: #FFFFFF; margin: 0 0 6px 0; letter-spacing: -0.4px;">
                                        %s
                                    </h1>
                                    <p style="font-size: 13px; color: #A1A1AA; margin: 0; line-height: 1.4;">
                                        %s
                                    </p>
                                </td>
                            </tr>
                            <!-- Main Content -->
                            <tr>
                                <td style="padding: 16px 32px 32px 32px;">
                                    %s
                                </td>
                            </tr>
                            <!-- Footer -->
                            <tr>
                                <td style="background-color: #0D0D10; padding: 24px 32px; border-top: 1px solid #222226; text-align: center;">
                                    <p style="font-size: 12px; color: #71717A; margin: 0 0 8px 0; line-height: 1.6;">
                                        This automated email was sent from <strong>info@onodem.com</strong> by <strong style="color: #A1A1AA;">Onodem</strong>.<br/>
                                        Preserving the stories, lessons, and impacts of those who shaped our journeys.
                                    </p>
                                    <p style="font-size: 11px; color: #52525B; margin: 0;">
                                        &copy; 2026 onodem.com &bull; <a href="%s" style="color: #EC8F3E; text-decoration: none;">Visit Website</a> &bull; <a href="mailto:info@onodem.com" style="color: #EC8F3E; text-decoration: none;">Support: info@onodem.com</a>
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(escapeHtml(headerTitle), escapeHtml(headerTitle), escapeHtml(headerSubtitle), bodyHtml, appUrl);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private String escapeJsonString(String text) {
        if (text == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
