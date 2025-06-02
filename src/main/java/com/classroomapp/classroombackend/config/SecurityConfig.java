package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.classroomapp.classroombackend.filter.JwtAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
    .addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            // Cho phép truy cập công khai vào API blog đọc
            .requestMatchers("/api/blogs").permitAll()
            .requestMatchers("/api/blogs/published").permitAll()
            .requestMatchers("/api/blogs/{id:[\\d]+}").permitAll()
            .requestMatchers("/api/blogs/search").permitAll()
            .requestMatchers("/api/blogs/tag/**").permitAll()
            .requestMatchers("/api/blogs/author/**").permitAll()
            // Duy trì bảo mật cho các API quản lý
            .requestMatchers("/api/blogs/{id:[\\d]+}/publish").authenticated()
            .requestMatchers("/api/blogs/{id:[\\d]+}/unpublish").authenticated()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/manager/**").hasRole("MANAGER")
            .requestMatchers("/api/teacher/**").hasRole("TEACHER")
            .requestMatchers("/api/student/**").hasRole("STUDENT")
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .csrf().disable();
    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
