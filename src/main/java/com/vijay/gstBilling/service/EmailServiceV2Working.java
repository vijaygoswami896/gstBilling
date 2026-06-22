//package com.vijay.gstBilling.service;
//
//import com.resend.Resend;
//import com.resend.core.exception.ResendException;
//import com.resend.services.emails.model.CreateEmailOptions;
//import com.resend.services.emails.model.CreateEmailResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//public class EmailServiceV2Working {
//
//    private final Resend resend;
////    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromAddress;
//
////    public EmailService(JavaMailSender mailSender) {
////        this.mailSender = mailSender;
////    }
//
//    public EmailServiceV2Working(@Value("${RESEND_API_KEY}") String apiKey)  {
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
//        String subject = "Welcome, " + name + "! Complete your registration setup";
//
//        // Since it's going through Gmail SMTP, you can use standard clean HTML layouts without issues!
//        String htmlBody = """
//                <!DOCTYPE html>
//                <html>
//                <body style="font-family: Arial, sans-serif; color: #333333; max-width: 600px; margin: 0 auto; padding: 20px;">
//                    <h2 style="color: #4A90E2;">Welcome aboard!</h2>
//                    <p>Hi %s,</p>
//                    <p>Thank you for signing up. Please click the button below to verify your email address and activate your account:</p>
//
//                    <p style="margin: 30px 0;">
//                        <a href="%s" style="background-color: #4A90E2; color: white; padding: 12px 25px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;">Verify Email Address</a>
//                    </p>
//
//                    <p style="font-size: 12px; color: #6c757d; margin-top: 40px;">
//                        If the button doesn't work, copy and paste this link into your browser:<br>
//                        <span style="color: #e83e8c;">%s</span>
//                    </p>
//                </body>
//                </html>
//                """.formatted(name, verifyLink,verifyLink);
//
//        sendViaResend(to, subject, htmlBody);
////        sendHtml(to, subject, htmlBody);
//    }
//
//    private void sendViaResend(String to, String subject, String htmlBody) {
//        try {
//            // NOTE: On Resend's free tier, you must use "onboarding@resend.dev"
//            // until you verify your own custom domain ownership.
//            CreateEmailOptions request = CreateEmailOptions.builder()
//                    .from("onboarding@resend.dev")
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
//
////    private void sendHtml(String to, String subject, String htmlBody) {
////        try {
////            MimeMessage message = mailSender.createMimeMessage();
////            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
////            helper.setFrom(fromAddress);
////            helper.setTo(to);
////            helper.setSubject(subject);
////            helper.setText(htmlBody, true);
////            mailSender.send(message);
////            log.info("Email sent to: {}", to);
////        } catch (MessagingException e) {
////            log.error("Failed to send email to {}: {}", to, e.getMessage());
////            // Don't rethrow — registration already succeeded; email failure is non-fatal
////        }
////    }
//}