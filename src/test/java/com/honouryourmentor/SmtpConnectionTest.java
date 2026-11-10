package com.honouryourmentor;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class SmtpConnectionTest {

    @Test
    void testGoDaddySmtp() {
        System.out.println("--- TESTING GODADDY SECURESERVER SMTP ---");
        try {
            System.out.println("Trying smtpout.secureserver.net:465 with info@onodem.com and app password...");
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost("smtpout.secureserver.net");
            sender.setPort(465);
            sender.setUsername("info@onodem.com");
            sender.setPassword("sbxbcmsrmyyjpvym");

            Properties props = sender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.debug", "true");

            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom("info@onodem.com", "Onodem");
            helper.setTo("sharonshelke1@gmail.com");
            helper.setSubject("Test Email from Onodem GoDaddy");
            helper.setText("Test message", false);

            sender.send(msg);
            System.out.println(">>> SUCCESS ON GODADDY! <<<");
        } catch (Exception e) {
            System.out.println("GoDaddy failed: " + e.getMessage());
        }
    }
}
