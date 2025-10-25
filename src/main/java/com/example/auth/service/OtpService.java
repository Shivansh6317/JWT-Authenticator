package com.example.auth.service;

import com.example.auth.entity.Otp;
import com.example.auth.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otpCode);
        mailSender.send(message);

        return otpCode;
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
