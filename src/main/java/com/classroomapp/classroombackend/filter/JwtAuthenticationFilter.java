package com.classroomapp.classroombackend.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.classroomapp.classroombackend.security.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT authentication filter for validating tokens in request header
 * This filter is executed for every request to validate JWT tokens
 * and set up security context if the token is valid.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            log.info("JWT Filter - Processing request: {} {}", request.getMethod(), requestURI);
            
            String token = getTokenFromRequest(request);
            log.info("JWT Filter - Token present: {}", token != null && !token.isEmpty());

            // Print headers for debugging in development
            if (requestURI.contains("/approve") || requestURI.contains("/test")) {
                log.info("===== REQUEST HEADERS FOR {} =====", requestURI);
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    // Skip logging cookies and other sensitive headers
                    if (!headerName.toLowerCase().contains("cookie")) {
                        log.info("Header: {} = {}", headerName, request.getHeader(headerName));
                    }
                }
            }
            
            if (token != null && !token.isEmpty()) {
                log.info("JWT Filter - Token (first 20 chars): {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            }

            // Kiểm tra và xác thực token
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // Lấy subject từ token (có thể là username hoặc email)
                String subject = jwtUtil.getUsernameFromToken(token);
                if (subject == null) {
                    log.warn("JWT Filter - Subject is null");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                log.info("JWT Filter - Subject: {}", subject);
                
                // Lấy role từ token
                Integer roleId = null;
                try {
                    roleId = jwtUtil.getRoleFromToken(token);
                    log.info("JWT Filter - Role ID: {}", roleId);
                } catch (Exception e) {
                    log.warn("JWT Filter - Error getting role: {}", e.getMessage());
                }
                
                // Chuyển đổi roleId thành tên role
                String role = "USER"; // Mặc định
                if (roleId != null) {
                    switch (roleId) {
                        case 1: role = "STUDENT"; break;
                        case 2: role = "TEACHER"; break;
                        case 3: role = "MANAGER"; break;
                        case 4: role = "ADMIN"; break;
                        default: role = "USER";
                    }
                }
                
                log.info("JWT Filter - Role Name: {}", role);
                
                // Tạo danh sách quyền từ role
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role));
                
                // Log danh sách quyền
                log.info("JWT Filter - Authorities: {}", authorities.stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.joining(", ")));
                  // Create Authentication object and set into SecurityContext
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);
                
                // Set request details in authentication object
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set the authentication in the Security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.info("JWT Filter - Authentication set: {} with role: {}", subject, role);
            } else if (StringUtils.hasText(token)) {
                log.warn("JWT Filter - Invalid token");
            } else if (requestURI.contains("/approve") || requestURI.contains("/reject")) {
                log.warn("JWT Filter - No token for approve/reject endpoint! This will cause a 403 error.");
            }
        } catch (Exception e) {
            log.error("JWT Filter - Exception: {}", e.getMessage(), e);
            // Không throw exception để đảm bảo request tiếp tục xử lý
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
}