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
import com.example.auth.entity.RefreshToken;
import com.example.auth.repository.RefreshTokenRepository;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

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


    public String resendOtp(ResendOtpRequest request) {
        String email = request.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException("User not found with email: " + email, HttpStatus.NOT_FOUND));
        if (user.isVerified()) {
            throw new CustomException("User is already verified", HttpStatus.BAD_REQUEST);
        }
        return otpService.generateOtp(email);
    }


    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED));


        if (!user.isVerified()) {
            throw new CustomException("User not verified. Please verify your email before logging in.", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshTokenString = jwtProvider.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(user)
                .expiryDate(Instant.now().plusMillis(604800000)) // 7 days
                .build();
        refreshTokenRepository.save(refreshToken);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .build();
    }


    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken savedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new CustomException("Please Login again", HttpStatus.UNAUTHORIZED));

        if (savedToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(savedToken);
            throw new CustomException("Refresh token expired. Please login again.", HttpStatus.UNAUTHORIZED);
        }
        if (!jwtProvider.validateToken(request.getRefreshToken())) {
            throw new CustomException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        User user = savedToken.getUser();
        String accessToken = jwtProvider.generateAccessToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .build();
    }


    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException("No active session. Please Login again", HttpStatus.BAD_REQUEST));

        refreshTokenRepository.delete(token);
    }
}
