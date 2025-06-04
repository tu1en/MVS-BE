package com.classroomapp.classroombackend.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
// import com.classroomapp.classroombackend.security.CustomUserDetailsService; // Unused import

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security configuration for the application
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)  // Giữ cài đặt mở rộng từ phiên bản local
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
        .addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .headers(headers -> headers.frameOptions().disable())  // Required for H2 console
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers("/h2-console/**").permitAll()  // Allow H2 console
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/role-requests/teacher").permitAll()
            .requestMatchers("/api/role-requests/student").permitAll()
            .requestMatchers("/api/role-requests/check").permitAll()
            .requestMatchers("/role-requests/teacher").permitAll()
            .requestMatchers("/role-requests/student").permitAll()
            .requestMatchers("/role-requests/check").permitAll()
            
            // Blog endpoints từ phiên bản cũ
            .requestMatchers("/api/blogs").permitAll()
            .requestMatchers("/api/blogs/published").permitAll()
            .requestMatchers("/api/blogs/{id:[\\d]+}").permitAll()
            .requestMatchers("/api/blogs/search").permitAll()
            .requestMatchers("/api/blogs/tag/**").permitAll()
            .requestMatchers("/api/blogs/author/**").permitAll()
            .requestMatchers("/api/blogs/{id:[\\d]+}/publish").authenticated()
            .requestMatchers("/api/blogs/{id:[\\d]+}/unpublish").authenticated()
            
            // Test endpoints for development
            .requestMatchers("/api/admin/requests/*/approve-simple").permitAll()
            
            // Protected endpoints
            .requestMatchers("/api/admin/requests/**").hasAnyRole("ADMIN", "MANAGER")
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/manager/**").hasRole("MANAGER")
            .requestMatchers("/api/teacher/**").hasRole("TEACHER")
            .requestMatchers("/api/student/**").hasRole("STUDENT")
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/h2-console/**")  // Disable CSRF for H2 console
            .disable()
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
