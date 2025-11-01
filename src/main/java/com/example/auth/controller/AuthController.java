package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.exception.CustomException;
import com.example.auth.service.AuthService;
import com.example.auth.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ImageService imageService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String otp = authService.register(request);
        return ResponseEntity.ok("OTP sent to email : " + otp);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest request) {
        boolean valid = authService.verifyOtp(request);
        if (valid) return ResponseEntity.ok("OTP verified successfully");
        else return ResponseEntity.badRequest().body("Invalid or expired OTP");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody ResendOtpRequest request) {
        String otp=authService.resendOtp(request);
        return ResponseEntity.ok("OTP resent successfully  " +otp);
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
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    @PostMapping("/upload-profile")
    public ResponseEntity<Map<String, String>> uploadProfile(@RequestParam("file") MultipartFile file) {
        String imageUrl = imageService.uploadImage(file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }


}
