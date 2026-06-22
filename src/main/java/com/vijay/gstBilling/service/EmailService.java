package com.vijay.gstBilling.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.resend.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
public class EmailService {

    private final Resend resend;

    public EmailService(@Value("${RESEND_API_KEY}") String apiKey)  {
        this.resend = new Resend((apiKey));
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

        // Converted to plain-text layout to avoid HTML anchor tag spam flags
//        String body = """
//        Hi %s,
//
//        Thank you for registering. Please copy and paste the following web address into your browser's address bar to verify your email address:
//
//        %s
//
//        This link will expire in 24 hours.
//        If you did not create an account, you can safely ignore this email.
//        """.formatted(name, verifyLink);

        // Change the subject to something friendly and casual (removes "Billing/GST" triggers)
        String subject = "Welcome to the App, " + name + "! Confirm your sign up";

        // Completely rewrite the text body to look like a personal greeting rather than a machine-generated template
        String body = """
        Hi %s,

        Welcome! To complete setting up your profile, please copy the verification web link below and open it directly in your web browser:

        %s

        Thank you!
        """.formatted(name, verifyLink);

        // Execute the HTTP REST API call via Resend SDK
        sendViaResend(to, subject, body);

//        sendHtml(to, subject, body);

    }

    private void sendViaResend(String to, String subject, String htmlBody) {
        try {
            // NOTE: On Resend's free tier, you must use "onboarding@resend.dev"
            // until you verify your own custom domain ownership.
            CreateEmailOptions request = CreateEmailOptions.builder()
                    .from("reply@vijaygoswami896.publicvm.com")
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            CreateEmailResponse response = resend.emails().send(request);
            log.info("Email sent successfully via Resend to {}! Message ID: {}", to, response.getId());

        } catch (ResendException e) {
            log.error("Failed to send email to {} via Resend API: {}", to, e.getMessage());

            // CRITICAL: We MUST rethrow this exception so that your RabbitMQ
            // RetryInterceptor knows a failure happened, allowing it to retry 3 times
            // and gracefully dead-letter the message to your DLQ if Resend is down!
            throw new RuntimeException("Resend communication error", e);
        }
    }
}
