package com.example.auth.service;

import com.example.auth.config.JwtProvider;
import com.example.auth.dto.*;
import com.example.auth.entity.User;
import com.example.auth.exception.CustomException;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtProvider jwtProvider;

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("Email already registered", HttpStatus.CONFLICT);
        }
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new CustomException("Invalid email format", HttpStatus.BAD_REQUEST);
        }
        try {
            String domain = request.getEmail().substring(request.getEmail().indexOf("@") + 1);
            java.net.InetAddress.getByName(domain);
        } catch (Exception e) {
            throw new CustomException("Email domain does not exist", HttpStatus.BAD_REQUEST);
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .verified(false)
                .build();

        userRepository.save(user);

        return otpService.generateOtp(user.getEmail());
    }


    public boolean verifyOtp(OtpRequest request) {
        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp());

        if (valid) {
            userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
                u.setVerified(true);
                userRepository.save(u);
            });
        }

        return valid;
    }


    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            throw new RuntimeException("User already verified");
        }

        return otpService.generateOtp(email);
    }


    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->  new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED));


        if (!user.isVerified()) {
            throw new CustomException("User not verified. Please verify your email before logging in.", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtProvider.validateToken(request.getRefreshToken())) {
            throw new CustomException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        String email = jwtProvider.getEmailFromToken(request.getRefreshToken());
        String accessToken = jwtProvider.generateAccessToken(email);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .build();
    }


    public void logout(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new CustomException("No active session or invalid request", HttpStatus.BAD_REQUEST);
        }

        String token = tokenHeader.substring(7);
        if (!jwtProvider.validateToken(token)) {
            throw new CustomException("Invalid or expired token. Please login again.", HttpStatus.UNAUTHORIZED);
        }


    }

}
