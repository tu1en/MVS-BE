package com.classroomapp.classroombackend.core.config;

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
 * ✅ FIXED: Spring Security configuration with proper CORS handling
 * 🎯 SINGLE SOURCE OF TRUTH for CORS configuration
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
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Authentication manager bean for Spring Security
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔧 Configuring security filter chain with CORS");
        
        http
            // ✅ FIRST: Configure CORS (must be first)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // ✅ SECOND: Disable CSRF
            .csrf(csrf -> csrf.disable())
            
            // ✅ THIRD: Add JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // ✅ FOURTH: Configure authorization rules
            .authorizeHttpRequests(authorize -> authorize
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Public endpoints - Only truly public endpoints should be here
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/google-login").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/auth/change-password").authenticated()
                .requestMatchers("/api/auth/validate").authenticated()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/v1/health").permitAll()
                .requestMatchers("/api/test").permitAll()
                .requestMatchers("/api/v1/greetings/hello").permitAll()
                .requestMatchers("/api/role-requests/**").permitAll()
                .requestMatchers("/role-requests/**").permitAll()
                
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
                .requestMatchers("/api/attendance/**").authenticated()
                .requestMatchers("/api/attendance-sessions/**").authenticated()
                .requestMatchers("/api/attendances/**").authenticated()

                // Protected endpoints - Messages system
                .requestMatchers("/api/messages/**").authenticated()
                .requestMatchers("/api/student-messages/**").authenticated()

                // Protected endpoints - File operations
                .requestMatchers("/api/files/**").authenticated()
                .requestMatchers("/files/**").authenticated()

                // Protected endpoints - Assignments
                .requestMatchers("/api/assignments/debug/**").permitAll()
                .requestMatchers("/api/assignments/classroom/**").permitAll()
                .requestMatchers("/api/assignments/**").authenticated()
                .requestMatchers("/api/timetable/**").authenticated()

                // Debug endpoints
                .requestMatchers("/api/debug/**").permitAll()

                // Notification endpoints
                .requestMatchers("/api/notifications/teacher").permitAll()
                .requestMatchers("/api/notifications/role/**").permitAll()

                // Materials endpoints
                .requestMatchers("/api/materials/**").authenticated()

                // Course endpoints
                .requestMatchers("/api/courses/**").permitAll()
                .requestMatchers("/api/classrooms/*/details").permitAll()

                // Role-based endpoints
                .requestMatchers("/api/admin/requests/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/manager/**").hasRole("MANAGER")
                .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                .requestMatchers("/api/student/**").hasRole("STUDENT")

                // HR Management endpoints
                .requestMatchers("/api/hr/**").hasAnyRole("MANAGER", "ADMIN")

                // Accountant specific endpoints
                .requestMatchers("/api/accountant/**").hasRole("ACCOUNTANT")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            
            // ✅ FIFTH: Configure session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        log.info("✅ Security filter chain configured successfully");
        return http.build();
    }

    /**
     * ✅ FIXED: CORS configuration source - SINGLE SOURCE OF TRUTH
     * 🎯 This is the ONLY place where CORS is configured
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("🔧 Configuring CORS - Single source of truth");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ FIXED: Use allowedOriginPatterns for credentials support
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",      // Any port on localhost
            "http://127.0.0.1:*",      // Any port on 127.0.0.1  
            "https://localhost:*",     // HTTPS localhost
            "https://127.0.0.1:*",     // HTTPS 127.0.0.1
            "https://mvsclassroom.com" // Production domain
        ));
        
        // ✅ Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // ✅ Allowed headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // ✅ Exposed headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Cache-Control", 
            "Content-Type", 
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // ✅ Allow credentials (required for JWT tokens)
        configuration.setAllowCredentials(true);
        
        // ✅ Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        // ✅ Apply to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("✅ CORS configured with allowedOriginPatterns and allowCredentials=true");
        return source;
    }
}