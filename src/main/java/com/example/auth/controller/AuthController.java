package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String otp = authService.register(request);
        return ResponseEntity.ok("OTP sent to email (dummy): " + otp);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest request) {
        boolean valid = authService.verifyOtp(request);
        if (valid) return ResponseEntity.ok("OTP verified successfully");
        else return ResponseEntity.badRequest().body("Invalid or expired OTP");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {
        String otp = authService.resendOtp(email);
        return ResponseEntity.ok("OTP resent (dummy): " + otp);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok(authService.logout());
    }
}
