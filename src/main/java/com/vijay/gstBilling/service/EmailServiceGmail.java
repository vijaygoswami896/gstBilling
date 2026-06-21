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
