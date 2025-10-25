package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl() {
            @Override
            public void send(SimpleMailMessage message) {
                System.out.println("\n=== Dummy Email ===");
                System.out.println("To: " + message.getTo()[0]);
                System.out.println("Subject: " + message.getSubject());
                System.out.println("Text: " + message.getText());
                System.out.println("==================\n");
            }
        };
    }
}
