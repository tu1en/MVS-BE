package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import com.classroomapp.classroombackend.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001", "http://localhost:8088", "http://localhost", "https://mvsclassroom.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        log.info("CORS configuration registered");
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
