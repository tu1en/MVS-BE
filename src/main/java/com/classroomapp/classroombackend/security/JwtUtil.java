package com.classroomapp.classroombackend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.classroomapp.classroombackend.constants.RoleConstants;

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

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

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

    /**
     * NEW: Generate token for parent with childIds claim
     * Based on PARENT_ROLE_SPEC.md requirements for parent authentication
     */
    public String generateParentToken(String username, Integer roleId, List<Long> childIds) {
        log.info("Generating parent token for user: {} with role ID: {} and {} children", 
                username, roleId, childIds != null ? childIds.size() : 0);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", roleId);
        claims.put("childIds", childIds); // NEW: Add childIds for parent access control
    
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
            .signWith(getSecretKeyFromString(), SignatureAlgorithm.HS512)
            .compact();
            
        log.info("Parent token generated successfully for user: {} with {} child access", 
            username, childIds != null ? childIds.size() : 0);
        
        return token;
    }

    /**
     * NEW: Generate token with custom claims (flexible method)
     */
    public String generateTokenWithClaims(String username, Integer roleId, Map<String, Object> additionalClaims) {
        log.info("Generating token with custom claims for user: {} with role ID: {}", username, roleId);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", roleId);
        
        // Add any additional claims
        if (additionalClaims != null) {
            claims.putAll(additionalClaims);
        }
    
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
            .signWith(getSecretKeyFromString(), SignatureAlgorithm.HS512)
            .compact();
            
        log.info("Token with custom claims generated successfully for user: {}", username);
        
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

    /**
     * NEW: Get childIds from parent token
     * Based on PARENT_ROLE_SPEC.md requirements
     */
    @SuppressWarnings("unchecked")
    public List<Long> getChildIdsFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKeyFromString())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            List<Integer> childIdsAsInt = claims.get("childIds", List.class);
            if (childIdsAsInt == null) {
                log.debug("No childIds found in token");
                return List.of();
            }
            
            // Convert Integer list to Long list
            List<Long> childIds = childIdsAsInt.stream()
                    .map(id -> id.longValue())
                    .toList();
            
            log.debug("Extracted childIds from token: {}", childIds);
            return childIds;
        } catch (Exception e) {
            log.error("Error getting childIds from token: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * NEW: Get all claims from token
     */
    public Claims getAllClaimsFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKeyFromString())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            log.debug("Extracted all claims from token");
            return claims;
        } catch (Exception e) {
            log.error("Error getting claims from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * NEW: Check if user has access to a specific child
     * For parent role validation
     */
    public boolean hasAccessToChild(String token, Long childId) {
        try {
            Integer roleId = getRoleFromToken(token);
            
            // Non-parent roles don't use childIds (admins, teachers, etc.)
            if (roleId == null || !RoleConstants.isParentRole(roleId)) {
                return false;
            }
            
            List<Long> childIds = getChildIdsFromToken(token);
            boolean hasAccess = childIds.contains(childId);
            
            log.debug("Parent access check for child {}: {}", childId, hasAccess);
            return hasAccess;
        } catch (Exception e) {
            log.error("Error checking child access: {}", e.getMessage());
            return false;
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
            case 6: return "TEACHING_ASSISTANT";
            case 7: return "PARENT"; // NEW: Parent role
            default: return "USER";
        }
    }

    /**
     * NEW: Check if token belongs to a parent
     */
    public boolean isParentToken(String token) {
        Integer roleId = getRoleFromToken(token);
        return RoleConstants.isParentRole(roleId);
    }

    /**
     * NEW: Validate parent has access to specific child
     * Security utility for parent endpoints
     */
    public boolean validateParentChildAccess(String token, Long childId) {
        if (!isParentToken(token)) {
            log.warn("Non-parent token attempting to access child data");
            return false;
        }
        
        return hasAccessToChild(token, childId);
    }
}