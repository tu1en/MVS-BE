package com.classroomapp.classroombackend.filter;

import com.classroomapp.classroombackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            
            // Thêm logging để debug
            System.out.println("JWT Filter - Request URI: " + request.getRequestURI());
            System.out.println("JWT Filter - Token present: " + (token != null && !token.isEmpty()));

            if (token != null && !token.isEmpty()) {
                System.out.println("JWT Filter - Token (first 20 chars): " + token.substring(0, Math.min(token.length(), 20)) + "...");
            }

            // Kiểm tra và xác thực token
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // Lấy subject từ token (có thể là username hoặc email)
                String subject = jwtUtil.getUsernameFromToken(token);
                if (subject == null) {
                    System.out.println("JWT Filter - Subject is null");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                System.out.println("JWT Filter - Subject: " + subject);
                
                // Lấy role từ token
                Integer roleId = null;
                try {
                    roleId = jwtUtil.getRoleFromToken(token);
                    System.out.println("JWT Filter - Role ID: " + roleId);
                } catch (Exception e) {
                    System.out.println("JWT Filter - Error getting role: " + e.getMessage());
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
                
                System.out.println("JWT Filter - Role Name: " + role);
                
                // Tạo danh sách quyền từ role
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role));
                
                // Log danh sách quyền
                System.out.println("JWT Filter - Authorities: " + authorities.stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.joining(", ")));
                
                // Tạo đối tượng Authentication và đặt vào SecurityContext
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                System.out.println("JWT Filter - Authentication set: " + subject + " with role: " + role);
            } else if (StringUtils.hasText(token)) {
                System.out.println("JWT Filter - Invalid token");
            }
        } catch (Exception e) {
            System.out.println("JWT Filter - Exception: " + e.getMessage());
            e.printStackTrace();
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