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
            // Then configure authorization rules
            .authorizeHttpRequests(authorize -> authorize
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Public endpoints - Only truly public endpoints should be here
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/google-login").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/auth/change-password").authenticated() // Requires authentication
                .requestMatchers("/api/auth/validate").authenticated() // Requires authentication for token validation
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/health").permitAll() // Health check endpoint
                .requestMatchers("/api/v1/health").permitAll() // Health check endpoint v1
                .requestMatchers("/api/test").permitAll() // Test endpoint
                .requestMatchers("/api/v1/greetings/hello").permitAll() // Only allow hello endpoint for health check
                .requestMatchers("/api/role-requests/**").permitAll()
                .requestMatchers("/role-requests/**").permitAll() // Allow both with and without /api prefix
                
                // Blog endpoints
                .requestMatchers("/api/blogs").permitAll()
                .requestMatchers("/api/blogs/published").permitAll()
                .requestMatchers("/api/blogs/{id:[\\d]+}").permitAll()
                .requestMatchers("/api/blogs/search").permitAll()
                .requestMatchers("/api/blogs/tag/**").permitAll()
                .requestMatchers("/api/blogs/author/**").permitAll()
                .requestMatchers("/api/blogs/{id:[\\d]+}/publish").authenticated()
                .requestMatchers("/api/blogs/{id:[\\d]+}/unpublish").authenticated()
                
                // Protected endpoints - Attendance system
                .requestMatchers("/api/v1/attendance/**").authenticated()
                .requestMatchers("/api/attendance-sessions/**").authenticated()
                .requestMatchers("/api/attendances/**").authenticated()

                // Protected endpoints - File operations
                .requestMatchers("/api/files/**").authenticated()
                .requestMatchers("/files/**").authenticated()

                // Protected endpoints - Assignments
                .requestMatchers("/api/assignments/**").authenticated()
                .requestMatchers("/api/timetable/**").authenticated()

                // Role-based endpoints
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
