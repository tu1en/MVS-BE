package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Cho phép truy cập đến H2 console trong môi trường phát triển
        // Trong thực tế, nên bỏ cấu hình này khi triển khai production
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/h2-console/**", "/api/**").permitAll()  // Tạm thời cho phép tất cả API
                // Các API yêu cầu xác thực sẽ được cấu hình sau
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions().disable()); // Cần thiết cho H2 console

        return http.build();
    }
    
    /**
     * Define password encoder for secure password storage
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 