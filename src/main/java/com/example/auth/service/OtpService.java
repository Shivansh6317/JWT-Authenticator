package com.example.auth.service;

import com.example.auth.entity.Otp;
import com.example.auth.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    @Value("${MAIL_PROVIDER:gmail}")
    private String mailProvider;

    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;
    @Transactional
    public String generateOtp(String email) {
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        Otp otp = Otp.builder()
                .email(email)
                .otp(otpCode)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .attempts(0)
                .build();

        otpRepository.deleteByEmail(email);
        otpRepository.save(otp);


        if ("resend".equalsIgnoreCase(mailProvider)) {
            sendWithResend(email, otpCode);
        } else {
            sendWithGmail(email, otpCode);
        }
        return otpCode;
    }
    private void sendWithGmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }

    private void sendWithResend(String toEmail, String otp) {
        try {
            String url = "https://api.resend.com/emails";
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> body = new HashMap<>();
            body.put("from", "E-Learning Platform <no-reply@yourdomain.com>");
            body.put("to", new String[]{toEmail});
            body.put("subject", "Your OTP Code");
            body.put("html", "<p>Your OTP is: <b>" + otp + "</b></p>");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP via Resend API", e);
        }
    }

    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        return otpRepository.findByEmail(email)
                .filter(o -> o.getExpiryTime().isAfter(LocalDateTime.now()))
                .filter(o -> o.getOtp().equals(otpCode))
                .map(o -> {
                    otpRepository.delete(o);
                    return true;
                }).orElse(false);
    }
}
