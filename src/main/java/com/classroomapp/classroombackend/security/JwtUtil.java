package com.classroomapp.classroombackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {

    // Sử dụng một secret key cố định từ application.properties
    @Value("${jwt.secret:defaultSecretKeyForDevThatShouldBeChangedInProduction}")
    private String jwtSecret;

    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60; // 24 hours

    @Value("${jwt.expiration:86400}")
    private int expiration;

    // Tạo SecretKey từ chuỗi secret để đảm bảo nhất quán
    public SecretKey getSecretKeyFromString() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Integer roleId) {
        log.info("Generating token for user: {} with role ID: {}", username, roleId);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", roleId);
    
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
            .signWith(SignatureAlgorithm.HS512, getSecretKeyFromString())
            .compact();
            
        log.info("Token generated successfully for user: {} (first 20 chars): {}", 
            username, token.substring(0, Math.min(20, token.length())));
        
        return token;
    }

    public boolean validateToken(String token) {
        if (token == null) {
            log.error("JWT validation failed: token is null");
            return false;
        }
        
        try {
            log.debug("Validating JWT token");
            Jwts.parser().setSigningKey(getSecretKeyFromString()).parseClaimsJws(token);
            log.debug("JWT token validated successfully");
            return true;
        } catch (SignatureException e) {
            log.error("JWT validation failed: Invalid signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT validation failed: Malformed token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT validation failed: Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT validation failed: Unsupported token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT validation failed: Empty claims: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation failed: Unknown error: {}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        try {
            String username = Jwts.parser().setSigningKey(getSecretKeyFromString())
                    .parseClaimsJws(token).getBody().getSubject();
            log.debug("Extracted username from token: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Error getting username from token: {}", e.getMessage());
            return null;
        }
    }
    
    public Integer getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(getSecretKeyFromString())
                    .parseClaimsJws(token).getBody();
            
            Integer roleId = claims.get("role", Integer.class);
            log.debug("Extracted role ID from token: {}", roleId);
            return roleId;
        } catch (Exception e) {
            log.error("Error getting role from token: {}", e.getMessage());
            return null;
        }
    }
    
    // Convert roleId to roleName for easier reference
    public String convertRoleIdToName(Integer roleId) {
        if (roleId == null) return "USER";
        
        switch (roleId) {
            case 1: return "STUDENT";
            case 2: return "TEACHER";
            case 3: return "MANAGER";
            case 4: return "ADMIN";
            case 5: return "ACCOUNTANT";
            default: return "USER";
        }
    }
}