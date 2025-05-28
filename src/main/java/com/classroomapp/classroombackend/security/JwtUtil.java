package com.classroomapp.classroombackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final javax.crypto.SecretKey SECRET_KEY = io.jsonwebtoken.security.Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60; // 24 hours

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private int expiration;

    // Getter cho SECRET_KEY để các class khác có thể sử dụng
    public javax.crypto.SecretKey getSecretKey() {
        return SECRET_KEY;
    }

    public String generateToken(String username, Integer roleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", roleId);
    
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            System.out.println("JWT Filter - Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("JWT Filter - Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT Filter - JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT Filter - JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT Filter - JWT claims string is empty: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("JWT Filter - Unknown error: " + e.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            System.out.println("JWT Filter - Error getting username: " + e.getMessage());
            return null;
        }
    }
    
    public Integer getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token).getBody();
            
            return claims.get("role", Integer.class);
        } catch (Exception e) {
            System.out.println("JWT Filter - Error getting role: " + e.getMessage());
            return null;
        }
    }
    
    // Phương thức hỗ trợ chuyển đổi roleId thành roleName
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