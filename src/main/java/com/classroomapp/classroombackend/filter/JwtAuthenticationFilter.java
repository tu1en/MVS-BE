package com.classroomapp.classroombackend.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.classroomapp.classroombackend.security.CustomUserDetailsService;
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

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

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
                // Lấy subject từ token (đã được cấu hình là email)
                String subject = jwtUtil.getSubjectFromToken(token);
                if (subject == null) {
                    log.warn("JWT Filter - Subject from token is null");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                log.info("JWT Filter - Subject (email) from token: {}", subject);
                
                // Tải thông tin người dùng đầy đủ từ database bằng email
                org.springframework.security.core.userdetails.UserDetails userDetails = 
                        customUserDetailsService.loadUserByUsername(subject); // Custom service handles email lookup

                // Kiểm tra nếu người dùng hợp lệ và có thể xác thực
                if (userDetails != null) {
                    // Tạo đối tượng xác thực với UserDetails làm principal
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    
                    // Set request details in authentication object
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set the authentication in the Security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.info("JWT Filter - Authentication set for user: {} with roles: {}", 
                        subject, userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.joining(", ")));
                } else {
                    log.warn("JWT Filter - Could not find user with subject (email): {}", subject);
                }
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