package com.classroomapp.classroombackend.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.classroomapp.classroombackend.filter.JwtAuthenticationFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security configuration for the application
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    /**
     * Password encoder bean for password hashing
     * 
     * @return password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Authentication manager bean for Spring Security
     * 
     * @param authenticationConfiguration authentication configuration
     * @return authentication manager
     * @throws Exception if authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");
        http
            // First configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Then disable CSRF
            .csrf(csrf -> csrf.disable())
            // Add the JWT filter before the standard authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Then configure authorization rules
            .authorizeHttpRequests(authorize -> authorize
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/test/**").permitAll() // Allow test endpoints for debugging
                .requestMatchers("/api/v1/greetings/**").permitAll() // Allow greeting endpoints
                .requestMatchers("/api/greetings/**").permitAll() // Allow greeting endpoints
                .requestMatchers("/api/role-requests/**").permitAll()
                .requestMatchers("/role-requests/**").permitAll() // Allow both with and without /api prefix
                .requestMatchers("/api/files/**").permitAll()
                .requestMatchers("/files/**").permitAll() // Allow both with and without /api prefix
                .requestMatchers("/api/timetable/**").permitAll() // Allow access to timetable for testing
                .requestMatchers("/api/assignments/classroom/**").permitAll() // Allow access to assignments for testing
                .requestMatchers("/api/assignments").permitAll() // Allow access to assignments for testing
                .requestMatchers("/api/assignments/create-samples/**").permitAll() // Allow creating sample assignments
                .requestMatchers("/api/assignments/*").permitAll() // Allow access to assignment detail for testing
                .requestMatchers("/api/assignments/*/submissions").permitAll() // Allow access to submissions for testing
                .requestMatchers("/api/assignments/*/submissions-debug").permitAll() // Allow access to debug for testing
                .requestMatchers("/api/assignments/upload").permitAll() // Allow file upload for testing
                
                // Blog endpoints
                .requestMatchers("/api/blogs").permitAll()
                .requestMatchers("/api/blogs/published").permitAll()
                .requestMatchers("/api/blogs/{id:[\\d]+}").permitAll()
                .requestMatchers("/api/blogs/search").permitAll()
                .requestMatchers("/api/blogs/tag/**").permitAll()
                .requestMatchers("/api/blogs/author/**").permitAll()
                .requestMatchers("/api/blogs/{id:[\\d]+}/publish").authenticated()
                .requestMatchers("/api/blogs/{id:[\\d]+}/unpublish").authenticated()
                
                // Protected endpoints
                .requestMatchers("/api/admin/requests/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/manager/**").hasRole("MANAGER")
                .requestMatchers("/api/teacher/**").hasAuthority("ROLE_TEACHER")
                .requestMatchers("/api/student/**").hasRole("STUDENT")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        log.info("Security filter chain configured successfully");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép origin từ frontend React và các nguồn khác
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*", 
            "https://localhost:*"
        ));
        
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000", 
            "http://localhost:3001", 
            "http://localhost:8088", 
            "http://localhost", 
            "https://mvsclassroom.com"
        ));
        
        // Cho phép các phương thức HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        
        // Cho phép các header HTTP
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Expose headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Cache-Control", 
            "Content-Type", 
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Cho phép gửi thông tin xác thực
        configuration.setAllowCredentials(true);
        
        // Thời gian cache preflight request
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cấu hình cho tất cả các đường dẫn
        source.registerCorsConfiguration("/**", configuration);
        log.info("CORS configuration registered with enhanced settings");
        return source;
    }
}
