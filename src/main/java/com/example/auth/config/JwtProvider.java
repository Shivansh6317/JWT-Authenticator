package com.example.auth.config;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtProvider {

    public String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstant.ACCESS_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, JwtConstant.SECRET)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstant.REFRESH_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, JwtConstant.SECRET)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(JwtConstant.SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(JwtConstant.SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
