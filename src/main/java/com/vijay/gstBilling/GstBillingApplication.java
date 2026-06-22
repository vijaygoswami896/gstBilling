package com.vijay.gstBilling;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRabbit
public class GstBillingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GstBillingApplication.class, args);
	}

}
