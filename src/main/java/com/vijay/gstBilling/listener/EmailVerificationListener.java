// listener/EmailVerificationListener.java
package com.vijay.gstBilling.listener;

import com.vijay.gstBilling.config.RabbitMQConfig;
import com.vijay.gstBilling.dto.email.EmailVerificationMessage;
import com.vijay.gstBilling.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailVerificationListener {

    private final EmailService emailService;

    public EmailVerificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailVerification(EmailVerificationMessage message) {
        log.info("Consumed email verification message for: {}", message.getTo());

        // This is where the actual slow SMTP call happens —
        // but now it's OFF the request thread, doesn't block the client
        emailService.sendVerificationEmail(
                message.getTo(),
                message.getName(),
                message.getVerifyLink()
        );
    }
}