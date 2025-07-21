package com.classroomapp.classroombackend.core.config;

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
                .requestMatchers("/api/attendance/**").authenticated() // Added for new attendance endpoints
                .requestMatchers("/api/attendance-sessions/**").authenticated()
                .requestMatchers("/api/attendances/**").authenticated()

                // Protected endpoints - Messages system
                .requestMatchers("/api/messages/**").authenticated()
                .requestMatchers("/api/student-messages/**").authenticated()

                // Protected endpoints - File operations
                .requestMatchers("/api/files/**").authenticated()
                .requestMatchers("/files/**").authenticated()

                // Protected endpoints - Assignments (with debug exception)
                .requestMatchers("/api/assignments/debug/**").permitAll() // Debug endpoints
                .requestMatchers("/api/assignments/classroom/**").permitAll() // Temporarily allow for debugging
                .requestMatchers("/api/assignments/**").authenticated()
                .requestMatchers("/api/timetable/**").authenticated()

                // Debug endpoints - allow all for debugging
                .requestMatchers("/api/debug/**").permitAll() // Debug endpoints

                // Notification endpoints - temporarily allow for debugging
                .requestMatchers("/api/notifications/teacher").permitAll() // Debug teacher notifications
                .requestMatchers("/api/notifications/role/**").permitAll() // Debug role notifications

                // Materials endpoints - require authentication for all operations
                .requestMatchers("/api/materials/**").authenticated() // All material operations need auth

                // Course endpoints - temporarily allow for debugging
                .requestMatchers("/api/courses/**").permitAll() // Temporarily allow for debugging
                .requestMatchers("/api/classrooms/*/details").permitAll() // Temporarily allow for debugging

                // Protected endpoints - Assignments
                .requestMatchers("/api/assignments/**").authenticated()
                .requestMatchers("/api/timetable/**").authenticated()

                // Role-based endpoints
                .requestMatchers("/api/admin/requests/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/manager/**").hasRole("MANAGER")
                .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                .requestMatchers("/api/student/**").hasRole("STUDENT")

                // HR Management endpoints - Only Manager and Admin can access
                .requestMatchers("/api/hr/**").hasAnyRole("MANAGER", "ADMIN")

                // Accountant specific endpoints
                .requestMatchers("/api/accountant/**").hasRole("ACCOUNTANT")
                
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
        
        // Use allowedOriginPatterns instead of allowedOrigins when allowCredentials is true
        // This fixes the "When allowCredentials is true, allowedOrigins cannot contain the special value '*'" error
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001", 
            "http://localhost:5173",
            "http://localhost:8088",
            "http://localhost",
            "https://mvsclassroom.com"
        ));
        
        // Remove setAllowedOrigins() completely to avoid conflicts with allowCredentials(true)
        // configuration.setAllowedOrigins() - REMOVED
        
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
        log.info("CORS configuration registered with allowedOriginPatterns and allowCredentials");
        return source;
    }
}
