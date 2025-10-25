package com.example.auth.service;

import com.example.auth.config.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;

    public String generateAccessToken(String email) {
        return jwtProvider.generateAccessToken(email);
    }

    public String generateRefreshToken(String email) {
        return jwtProvider.generateRefreshToken(email);
    }

    public boolean validateToken(String token) {
        return jwtProvider.validateToken(token);
    }

    public String getEmailFromToken(String token) {
        return jwtProvider.getEmailFromToken(token);
    }
}
