package com.vijay.gstBilling.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;

import java.util.List;

@Slf4j
@Service
public class EmailService {

    private final TransactionalEmailsApi apiInstance;

//    @Value("${app.mail.from}")
    private String fromAddress = "gurusacademy2021@gmail.com";

//    @Value("${app.mail.from-name}")
    private String fromName = "Guru";

    public EmailService(@Value("${BREVO_API_KEY}") String apiKey) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth auth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        auth.setApiKey(apiKey);
        this.apiInstance = new TransactionalEmailsApi();
    }

    public void sendVerificationEmail(String to, String name, String verifyLink) {
        String subject = "Welcome, " + name + "! Complete your registration setup";
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
                        If the button doesn't work, copy and paste this link:<br>
                        <span style="color: #e83e8c;">%s</span>
                    </p>
                </body>
                </html>
                """.formatted(name, verifyLink, verifyLink);

        sendViaBrevo(to, name, subject, htmlBody);
    }

    private void sendViaBrevo(String to, String toName, String subject, String htmlBody) {
        try {
            SendSmtpEmail email = new SendSmtpEmail();
            email.setSender(new SendSmtpEmailSender().name(fromName).email(fromAddress));
            email.setTo(List.of(new SendSmtpEmailTo().email(to).name(toName)));
            email.setSubject(subject);
            email.setHtmlContent(htmlBody);

            CreateSmtpEmail result = apiInstance.sendTransacEmail(email);
            log.info("Email sent via Brevo to {}. MessageId: {}", to, result.getMessageId());

        } catch (Exception e) {
            log.error("Failed to send email to {} via Brevo: {}", to, e.getMessage());
            throw new RuntimeException("Brevo email sending failed", e);
        }
    }
}