//package com.vijay.gstBilling.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//public class EmailServiceGmail {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromAddress;
//
//    public EmailServiceGmail(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//    public void sendVerificationEmail(String to, String name, String verifyLink) {
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
//
//        sendHtml(to, subject, body);
//    }
//
//    private void sendHtml(String to, String subject, String htmlBody) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            helper.setFrom(fromAddress);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(htmlBody, true);
//            mailSender.send(message);
//            log.info("Email sent to: {}", to);
//        } catch (MessagingException e) {
//            log.error("Failed to send email to {}: {}", to, e.getMessage());
//            // Don't rethrow — registration already succeeded; email failure is non-fatal
//        }
//    }
//}


//===========================================custom
//package com.vijay.gstBilling.service;
//
//import com.resend.Resend;
//import com.resend.core.exception.ResendException;
//import com.resend.services.emails.model.CreateEmailOptions;
//import com.resend.services.emails.model.CreateEmailResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import com.resend.*;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//
//@Slf4j
//@Service
//public class EmailService {
//
//    private final Resend resend;
//
//    public EmailService(@Value("${RESEND_API_KEY}") String apiKey)  {
//        this.resend = new Resend((apiKey));
//    }
//
//    public void sendVerificationEmail(String to, String name, String verifyLink) {
////        String subject = "Verify your GST Billing account";
////        String body = """
////            <html><body>
////            <p>Hi %s,</p>
////            <p>Click the link below to verify your email address:</p>
////            <p><a href="%s">Verify Email</a></p>
////            <p>This link expires in 24 hours.</p>
////            <p>If you did not register, ignore this email.</p>
////            </body></html>
////            """.formatted(name, verifyLink);
//
//        // 1. A perfectly friendly, conversational subject line to bypass financial spam triggers
//        String subject = "Welcome, " + name + "! Complete your registration setup";
//
//        // 2. Clear HTML Structure with proper styling so Mail-tester gets a perfect layout balance
//        String htmlBody = """
//        <!DOCTYPE html>
//        <html>
//        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333333; max-width: 600px; margin: 0 auto; padding: 20px;">
//            <h2 style="color: #4A90E2; margin-bottom: 20px;">Welcome aboard!</h2>
//            <p>Hi %s,</p>
//            <p>Thank you for signing up. To complete your account registration and secure your profile, please copy and paste the following web address directly into your browser's address bar:</p>
//
//            <div style="background-color: #f8f9fa; border: 1px solid #e9ecef; padding: 15px; margin: 20px 0; border-radius: 4px; word-break: break-all;">
//                <code style="font-family: monospace; color: #e83e8c;">%s</code>
//            </div>
//
//            <p style="font-size: 13px; color: #6c757d; margin-top: 30px;">
//                Please note: This link is valid for the next 24 hours.<br>
//                If you did not request this registration setup, you can safely delete this email.
//            </p>
//        </body>
//        </html>
//        """.formatted(name, verifyLink);
//
//        // Execute the HTTP REST API call via Resend SDK
//        sendViaResend(to, subject, htmlBody);
//
////        sendHtml(to, subject, body);
//
//    }
//
//    private void sendViaResend(String to, String subject, String htmlBody) {
//        try {
//            // NOTE: On Resend's free tier, you must use "onboarding@resend.dev"
//            // until you verify your own custom domain ownership.
//            CreateEmailOptions request = CreateEmailOptions.builder()
//                    .from("reply@vijaygoswami896.publicvm.com")
//                    .to(to)
//                    .subject(subject)
//                    .html(htmlBody)
//                    .build();
//
//            CreateEmailResponse response = resend.emails().send(request);
//            log.info("Email sent successfully via Resend to {}! Message ID: {}", to, response.getId());
//
//        } catch (ResendException e) {
//            log.error("Failed to send email to {} via Resend API: {}", to, e.getMessage());
//
//            // CRITICAL: We MUST rethrow this exception so that your RabbitMQ
//            // RetryInterceptor knows a failure happened, allowing it to retry 3 times
//            // and gracefully dead-letter the message to your DLQ if Resend is down!
//            throw new RuntimeException("Resend communication error", e);
//        }
//    }
//}
