package com.classroomapp.classroombackend.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    
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
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) // For H2 console
            // Then configure authorization rules
            .authorizeHttpRequests(authorize -> authorize
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Public endpoints
                .requestMatchers("/h2-console/**").permitAll()  // Allow H2 console
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/test/**").permitAll() // Allow test endpoints for debugging
                .requestMatchers("/api/role-requests/**").permitAll()
                .requestMatchers("/role-requests/**").permitAll() // Allow both with and without /api prefix
                .requestMatchers("/api/files/**").permitAll()
                .requestMatchers("/files/**").permitAll() // Allow both with and without /api prefix
                .requestMatchers("/api/timetable/**").permitAll() // Allow access to timetable for testing
                .requestMatchers("/api/classrooms/**").permitAll() // Allow access to classrooms for testing
                
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
                .requestMatchers("/api/teacher/**").hasRole("TEACHER")
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
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000", 
            "http://localhost:3001", 
            "http://localhost:8088", 
            "http://localhost", 
            "https://mvsclassroom.com"
        ));
        
        // Cho phép các phương thức HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Cho phép các header HTTP
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Cache-Control", 
            "Content-Type", 
            "Accept", 
            "X-Requested-With"
        ));
        
        // Cho phép gửi thông tin xác thực
        configuration.setAllowCredentials(true);
        
        // Thời gian cache preflight request
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cấu hình cho tất cả các đường dẫn
        source.registerCorsConfiguration("/**", configuration);
        log.info("CORS configuration registered");
        return source;
    }
}
