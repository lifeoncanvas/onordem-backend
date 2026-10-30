package com.honouryourmentor.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Production-ready Automated Email Service for Honour Your Mentor (onodem.com).
 * Sends branded transactional emails from info@onodem.com:
 *   1. Account Registration Welcome & Verification
 *   2. Password Reset OTP Verification Code
 *   3. Password Reset Security Confirmation
 *   4. Dedicated Tribute Invitation (sent to mentor)
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:info@onodem.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Honour Your Mentor}")
    private String fromName;

    @Value("${app.mail.app-url:https://onodem.com}")
    private String appUrl;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    /**
     * 1. Account Registration: Welcome email whenever a new user creates an account.
     */
    @Async("mailTaskExecutor")
    public void sendWelcomeEmail(String toEmail, String name, String username) {
        String displayName = (name != null && !name.isBlank()) ? name.trim() : "Friend";
        String userHandle = (username != null && !username.isBlank()) ? username.trim() : toEmail.split("@")[0];
        String subject = "Welcome to Honour Your Mentor — Keep their memories alive";

        String htmlContent = buildEmailTemplate(
                "Welcome to Honour Your Mentor",
                "Your account is ready to celebrate those who shaped your life",
                """
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    Hello <strong style="color: #FFFFFF;">%s</strong>,
                </p>
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 20px;">
                    Welcome to <strong>Honour Your Mentor</strong>. We are honored to have you with us.
                    Your personal account has been created with username:
                    <span style="display: inline-block; background-color: #27272A; color: #8FD3FF; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-family: monospace;">@%s</span>
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
                    <a href="%s/home" style="display: inline-block; background-color: #FFFFFF; color: #09090B; font-weight: 700; font-size: 15px; text-decoration: none; padding: 14px 32px; border-radius: 10px; letter-spacing: -0.2px;">Open Honour Your Mentor</a>
                </div>
                """.formatted(escapeHtml(displayName), escapeHtml(userHandle), appUrl)
        );

        sendEmail(toEmail, subject, htmlContent);
    }

    /**
     * 2. Forgot Password: Send 5-digit verification code whenever a user requests password reset.
     */
    @Async("mailTaskExecutor")
    public void sendPasswordResetOtp(String toEmail, String name, String otpCode) {
        String displayName = (name != null && !name.isBlank()) ? name.trim() : "Mentee";
        String subject = "Your Password Reset Code: " + otpCode + " — Honour Your Mentor";

        String htmlContent = buildEmailTemplate(
                "Password Reset Request",
                "Verification code for your onodem.com account",
                """
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 16px;">
                    Hello <strong style="color: #FFFFFF;">%s</strong>,
                </p>
                <p style="font-size: 15px; color: #E4E4E7; line-height: 1.6; margin-bottom: 20px;">
                    We received a request to reset the password for your Honour Your Mentor account (<span style="color: #8FD3FF;">%s</span>).
                </p>
                <p style="font-size: 14px; color: #A1A1AA; margin-bottom: 8px;">
                    Your 5-digit verification code is:
                </p>
                <div style="background-color: #121215; border: 2px solid #3F3F46; border-radius: 14px; padding: 22px; text-align: center; margin: 18px 0 24px 0;">
                    <span style="font-family: monospace; font-size: 38px; font-weight: 800; letter-spacing: 10px; color: #FFFFFF;">%s</span>
                </div>
                <div style="background-color: #18181B; border-left: 3px solid #F59E0B; padding: 12px 16px; border-radius: 6px; margin-bottom: 20px;">
                    <p style="font-size: 13px; color: #FCD34D; margin: 0; line-height: 1.5;">
                        <strong>Security Notice:</strong> This code expires in <strong>15 minutes</strong> and can only be used once. Never share this code with anyone.
                    </p>
                </div>
                <p style="font-size: 13px; color: #71717A; line-height: 1.5;">
                    If you did not request this password reset, please disregard this email. Your password will remain unchanged and your account is secure.
                </p>
                """.formatted(escapeHtml(displayName), escapeHtml(toEmail), escapeHtml(otpCode))
        );

        sendEmail(toEmail, subject, htmlContent);
    }

    /**
     * 3. Password Reset Confirmation: Send a confirmation email once the password has been successfully changed.
     */
    @Async("mailTaskExecutor")
    public void sendPasswordResetConfirmation(String toEmail, String name) {
        String displayName = (name != null && !name.isBlank()) ? name.trim() : "Member";
        String subject = "Security Alert: Your Password Was Changed — Honour Your Mentor";
        String timestamp = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm 'UTC'"));

        String htmlContent = buildEmailTemplate(
                "Password Changed Successfully",
                "Security notification regarding your Honour Your Mentor account",
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
                    <a href="%s/login" style="display: inline-block; background-color: #FFFFFF; color: #09090B; font-weight: 700; font-size: 14px; text-decoration: none; padding: 12px 28px; border-radius: 8px;">Log In to Onodem</a>
                </div>
                <div style="background-color: #18181B; border: 1px solid #27272A; border-radius: 10px; padding: 16px; margin-top: 24px;">
                    <p style="font-size: 13px; color: #EF4444; margin: 0 0 6px 0; font-weight: 700;">
                        Did not make this change?
                    </p>
                    <p style="font-size: 12.5px; color: #A1A1AA; margin: 0; line-height: 1.5;">
                        If you did not change your password, someone may have unauthorized access. Contact our team immediately by replying to <a href="mailto:info@onodem.com" style="color: #8FD3FF; text-decoration: none;">info@onodem.com</a>.
                    </p>
                </div>
                """.formatted(escapeHtml(displayName), timestamp, appUrl)
        );

        sendEmail(toEmail, subject, htmlContent);
    }

    /**
     * 4. Post Dedication: Sends an automated email to a mentor whenever a mentee dedicates a tribute, letter, or memo to them.
     */
    @Async("mailTaskExecutor")
    public void sendDedicationEmail(String toEmail, String mentorName, String senderName, String tributeTitle, String tributeKind, String excerpt, String tributeId) {
        String greeting = (mentorName != null && !mentorName.isBlank()) ? mentorName.trim() : "Mentor";
        String author = (senderName != null && !senderName.isBlank()) ? senderName.trim() : "Someone grateful";
        String kindDisplay = (tributeKind != null && !tributeKind.isBlank()) ? tributeKind.substring(0, 1).toUpperCase() + tributeKind.substring(1) : "Tribute";
        String subject = "✨ " + author + " has dedicated a " + kindDisplay + " to you on Honour Your Mentor";

        String postLink = appUrl + "/memory-wall";
        if (tributeId != null && !tributeId.isBlank()) {
            postLink = appUrl + "/memory-detail/" + tributeId;
        }

        String excerptBlock = "";
        if (excerpt != null && !excerpt.isBlank()) {
            excerptBlock = """
            <div style="background-color: #121215; border-left: 3px solid #8FD3FF; border-radius: 8px; padding: 18px 20px; margin: 20px 0;">
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
                    <strong style="color: #FFFFFF;">%s</strong> has created and dedicated a special <strong>%s</strong> to you on <em>Honour Your Mentor</em>.
                </p>
                %s
                <p style="font-size: 14px; color: #A1A1AA; line-height: 1.6; margin-bottom: 26px;">
                    Your guidance, lessons, and presence made a lasting difference. You can read the full dedication, view attached photos and letters, and leave your thoughts on our memory wall.
                </p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="display: inline-block; background-color: #FFFFFF; color: #09090B; font-weight: 700; font-size: 15px; text-decoration: none; padding: 14px 32px; border-radius: 10px; letter-spacing: -0.2px;">Read Your Dedicated Tribute</a>
                </div>
                """.formatted(escapeHtml(greeting), escapeHtml(author), escapeHtml(kindDisplay.toLowerCase()), excerptBlock, postLink)
        );

        sendEmail(toEmail, subject, htmlContent);
    }

    /**
     * Core email dispatch method using Spring JavaMailSender with graceful logging fallback.
     */
    private void sendEmail(String toEmail, String subject, String htmlBody) {
        if (!mailEnabled) {
            log.info("[EMAIL DISABLED] Would send from {} to {}: {}", fromEmail, toEmail, subject);
            return;
        }

        if (mailSender == null) {
            log.warn("[EMAIL SENDER UNAVAILABLE] JavaMailSender is not initialized. Email from {} to {}: {}", fromEmail, toEmail, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);
            log.info("[EMAIL SENT] Successfully sent automated email from {} to {} | Subject: {}", fromEmail, toEmail, subject);
        } catch (Exception ex) {
            log.error("[EMAIL ERROR] Could not dispatch email from {} to {}: {}. Will log email content for tracking.", fromEmail, toEmail, ex.getMessage());
            // Log fallback so OTP / important messages are never completely lost if SMTP is offline
            log.info("[EMAIL BACKUP LOG] To: {} | Subject: {}\n{}", toEmail, subject, htmlBody);
        }
    }

    /**
     * Dark luxury HTML email template matching the Honour Your Mentor / Onodem design system.
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
                                                    Honour Your Mentor
                                                </span>
                                            </td>
                                            <td align="right" style="vertical-align: middle;">
                                                <span style="font-size: 11px; color: #8FD3FF; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">
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
                                        This automated email was sent from <strong>info@onodem.com</strong> by <strong style="color: #A1A1AA;">Honour Your Mentor</strong>.<br/>
                                        Preserving the stories, lessons, and impacts of those who shaped our journeys.
                                    </p>
                                    <p style="font-size: 11px; color: #52525B; margin: 0;">
                                        &copy; 2026 onodem.com &bull; <a href="%s" style="color: #8FD3FF; text-decoration: none;">Visit Website</a> &bull; <a href="mailto:info@onodem.com" style="color: #8FD3FF; text-decoration: none;">Support: info@onodem.com</a>
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
}
