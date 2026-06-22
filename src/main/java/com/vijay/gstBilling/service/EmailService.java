package com.vijay.gstBilling.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String name, String verifyLink) {
//        String subject = "Verify your GST Billing account";
//        String body = """
//            <html><body>
//            <p>Hi %s,</p>
//            <p>Click the link below to verify your email address:</p>
//            <p><a href="%s">Verify Email</a></p>
//            <p>This link expires in 24 hours.</p>
//            <p>If you did not register, ignore this email.</p>
//            </body></html>
//            """.formatted(name, verifyLink);

        String subject = "Welcome, " + name + "! Complete your registration setup";

        // Since it's going through Gmail SMTP, you can use standard clean HTML layouts without issues!
        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; color: #333333; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #4A90E2;">Welcome aboard!</h2>
                    <p>Hi %s,</p>
                    <p>Thank you for signing up. Please click the button below to verify your email address and activate your account:</p>
                    
                    <p style="margin: 30px 0;">
                        <a href="%s" style="background-color: #4A90E2; color: white; padding: 12px 25px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;">Verify Email Address</a>
                    </p>
                    
                    <p style="font-size: 12px; color: #6c757d; margin-top: 40px;">
                        If the button doesn't work, copy and paste this link into your browser:<br>
                        <span style="color: #e83e8c;">%s</span>
                    </p>
                </body>
                </html>
                """.formatted(name, verifyLink,verifyLink);

        sendHtml(to, subject, htmlBody);
    }


    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Don't rethrow — registration already succeeded; email failure is non-fatal
        }
    }
}