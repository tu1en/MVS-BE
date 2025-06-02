package com.classroomapp.classroombackend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Cấu hình bảo mật cho ứng dụng
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Định nghĩa bean PasswordEncoder để mã hóa mật khẩu
     * 
     * @return BCryptPasswordEncoder để mã hóa mật khẩu an toàn
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cấu hình chuỗi bộ lọc bảo mật
     * 
     * @param http Đối tượng cấu hình HttpSecurity
     * @return SecurityFilterChain đã cấu hình
     * @throws Exception nếu có lỗi cấu hình
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Cấu hình CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Vô hiệu hóa CSRF vì đây là API REST
            .csrf(csrf -> csrf.disable())
            // Cấu hình phân quyền truy cập
            .authorizeHttpRequests(authorize -> authorize
                // Cho phép tất cả các request không cần xác thực
                .anyRequest().permitAll()
            );

        return http.build();
    }
    
    /**
     * Cấu hình CORS để cho phép frontend truy cập API
     * 
     * @return nguồn cấu hình CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép origin từ frontend React
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        // Cho phép các phương thức HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Cho phép các header HTTP
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        // Cho phép gửi thông tin xác thực
        configuration.setAllowCredentials(true);
        // Thời gian cache preflight request
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cấu hình cho tất cả các đường dẫn
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 