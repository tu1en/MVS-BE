package com.classroomapp.classroombackend.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Filter that runs before JwtAuthenticationFilter to check for suspicious auto-login
 * attempts and clear them
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // Ensure this runs before other filters
@Slf4j
public class SessionClearingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Kiểm tra nếu đây là request đầu tiên hoặc không phải là request login
        String requestURI = request.getRequestURI();
        if (!requestURI.contains("/api/auth/login")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // Nếu authentication đã được thiết lập từ trước nhưng không phải từ request login
            if (auth != null && (auth.getCredentials() == null || auth.getCredentials().toString().isEmpty())) {
                log.warn("Detected potential auto-login. Clearing security context.");
                SecurityContextHolder.clearContext();
                
                // Xóa cookie JWT nếu có
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("jwt".equals(cookie.getName()) || "token".equals(cookie.getName())) {
                            cookie.setMaxAge(0);
                            cookie.setValue("");
                            response.addCookie(cookie);
                        }
                    }
                }
                
                // Thêm header để ngăn chặn lưu cache của trình duyệt
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
            }
        }
        
        filterChain.doFilter(request, response);
    }
} 