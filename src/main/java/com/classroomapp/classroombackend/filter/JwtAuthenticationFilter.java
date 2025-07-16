package com.classroomapp.classroombackend.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.classroomapp.classroombackend.security.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT authentication filter for validating tokens in request header
 * This filter is executed for every request to validate JWT tokens
 * and set up security context if the token is valid.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            String method = request.getMethod();
            boolean isMaterialDownload = requestURI.contains("/materials/download/");

            // Enhanced logging for material downloads
            if (isMaterialDownload) {
                log.info("🔽 JWT Filter - MATERIAL DOWNLOAD REQUEST: {} {}", method, requestURI);
            } else {
                log.info("JWT Filter - Processing request: {} {}", method, requestURI);
            }

            String token = getTokenFromRequest(request);
            log.info("JWT Filter - Token present: {}", token != null && !token.isEmpty());

            // Enhanced header debugging for material downloads, approve/reject, and test endpoints
            if (isMaterialDownload || requestURI.contains("/approve") || requestURI.contains("/reject") || requestURI.contains("/test")) {
                String logPrefix = isMaterialDownload ? "🔽 MATERIAL DOWNLOAD" : "DEBUG";
                log.info("===== {} REQUEST HEADERS FOR {} =====", logPrefix, requestURI);

                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    // Skip logging cookies and other sensitive headers
                    if (!headerName.toLowerCase().contains("cookie")) {
                        String headerValue = request.getHeader(headerName);
                        // Truncate Authorization header for security
                        if ("authorization".equalsIgnoreCase(headerName) && headerValue != null && headerValue.length() > 30) {
                            headerValue = headerValue.substring(0, 30) + "...";
                        }
                        log.info("Header: {} = {}", headerName, headerValue);
                    }
                }
                log.info("===== END {} HEADERS =====", logPrefix);
            }

            if (token != null && !token.isEmpty()) {
                String tokenPreview = token.substring(0, Math.min(token.length(), 20)) + "...";
                if (isMaterialDownload) {
                    log.info("🔽 JWT Filter - Material download token (first 20 chars): {}", tokenPreview);
                } else {
                    log.info("JWT Filter - Token (first 20 chars): {}", tokenPreview);
                }
            }

            // Token validation and authentication
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // Get subject from token (configured as email)
                String subject = jwtUtil.getSubjectFromToken(token);
                if (subject == null) {
                    if (isMaterialDownload) {
                        log.warn("🔽 JWT Filter - MATERIAL DOWNLOAD: Subject from token is null");
                    } else {
                        log.warn("JWT Filter - Subject from token is null");
                    }
                    filterChain.doFilter(request, response);
                    return;
                }

                log.info("JWT Filter - Subject from token: {}", subject);
                log.info("JWT Filter - Token validation successful, loading user details...");

                // Tải thông tin người dùng đầy đủ từ database
                // CustomUserDetailsService sẽ xử lý việc tìm user bằng email hoặc username
                org.springframework.security.core.userdetails.UserDetails userDetails =
                        userDetailsService.loadUserByUsername(subject);

                if (isMaterialDownload) {
                    log.info("JWT Filter - MATERIAL DOWNLOAD: Subject from token: {}", subject);
                    log.info("JWT Filter - MATERIAL DOWNLOAD: Token validation successful, loading user details...");
                } else {
                    log.info("JWT Filter - Subject from token: {}", subject);
                    log.info("JWT Filter - Token validation successful, loading user details...");
                }

                // Load full user information from database
                // CustomUserDetailsService will handle finding user by email or username

                // Check if user is valid and can be authenticated
                if (userDetails != null) {
                    // Create authentication object with UserDetails as principal
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // Set request details in authentication object
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the authentication in the Security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    String authorities = userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.joining(", "));

                    if (isMaterialDownload) {
                        // Extract material ID from URI for enhanced logging
                        String materialId = extractMaterialIdFromUri(requestURI);
                        log.info("🔽 JWT Filter - MATERIAL DOWNLOAD: Authentication set for user: {} with roles: {}", subject, authorities);
                        log.info("🔽 JWT Filter - MATERIAL DOWNLOAD: User {} attempting to download material ID: {}", subject, materialId);

                        // Log additional context for material downloads
                        if (userDetails instanceof org.springframework.security.core.userdetails.User) {
                            log.info("🔽 JWT Filter - MATERIAL DOWNLOAD: User authorities: {}", authorities);
                            log.info("🔽 JWT Filter - MATERIAL DOWNLOAD: User enabled: {}", userDetails.isEnabled());
                            log.info("🔽 JWT Filter - MATERIAL DOWNLOAD: User account non-expired: {}", userDetails.isAccountNonExpired());
                        }
                    } else {
                        log.info("JWT Filter - Authentication set for user: {} with roles: {}", subject, authorities);
                    }
                } else {
                    if (isMaterialDownload) {
                        log.warn("🔽 JWT Filter - MATERIAL DOWNLOAD: Could not find user with subject (email): {}", subject);
                    } else {
                        log.warn("JWT Filter - Could not find user with subject (email): {}", subject);
                    }
                }
            } else if (StringUtils.hasText(token)) {
                if (isMaterialDownload) {
                    log.warn("🔽 JWT Filter - MATERIAL DOWNLOAD: Invalid token for material download request");
                } else {
                    log.warn("JWT Filter - Invalid token");
                }
            } else if (isMaterialDownload) {
                log.warn("🔽 JWT Filter - MATERIAL DOWNLOAD: No token provided for material download! This will cause a 403 error.");
            } else if (requestURI.contains("/approve") || requestURI.contains("/reject")) {
                log.warn("JWT Filter - No token for approve/reject endpoint! This will cause a 403 error.");
            }
        } catch (Exception e) {
            String requestURI = request.getRequestURI();
            boolean isMaterialDownload = requestURI.contains("/materials/download/");

            if (isMaterialDownload) {
                log.error("🔽 JWT Filter - MATERIAL DOWNLOAD: Exception during authentication: {}", e.getMessage(), e);
                String materialId = extractMaterialIdFromUri(requestURI);
                log.error("🔽 JWT Filter - MATERIAL DOWNLOAD: Failed for material ID: {}", materialId);
            } else {
                log.error("JWT Filter - Exception: {}", e.getMessage(), e);
            }
            // Don't throw exception to ensure request continues processing
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Extract material ID from URI for enhanced logging
     * @param requestURI the request URI
     * @return material ID or "unknown" if not found
     */
    private String extractMaterialIdFromUri(String requestURI) {
        try {
            // Pattern: /api/materials/download/{materialId}
            if (requestURI.contains("/materials/download/")) {
                String[] parts = requestURI.split("/");
                for (int i = 0; i < parts.length; i++) {
                    if ("download".equals(parts[i]) && i + 1 < parts.length) {
                        return parts[i + 1];
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract material ID from URI: {}", requestURI);
        }
        return "unknown";
    }
}