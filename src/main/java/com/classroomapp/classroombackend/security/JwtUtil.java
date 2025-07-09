package com.classroomapp.classroombackend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60; // 24 hours

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
            .signWith(getSecretKeyFromString(), SignatureAlgorithm.HS512)
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
            Jwts.parserBuilder()
                .setSigningKey(getSecretKeyFromString())
                .build()
                .parseClaimsJws(token);
            log.debug("JWT token validated successfully");
            return true;
        } catch (SecurityException e) {
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

    public String getSubjectFromToken(String token) {
        try {
            String subject = Jwts.parserBuilder()
                    .setSigningKey(getSecretKeyFromString())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            log.debug("Extracted subject from token: {}", subject);
            return subject;
        } catch (Exception e) {
            log.error("Error getting subject from token: {}", e.getMessage());
            return null;
        }
    }
    
    public Integer getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKeyFromString())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
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
            default: return "USER";
        }
    }
}