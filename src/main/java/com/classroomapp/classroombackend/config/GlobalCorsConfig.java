package com.classroomapp.classroombackend.config;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Global CORS filter to ensure preflight responses include PATCH and other methods.
 * This complements the security CORS config and guarantees consistent headers in dev.
 */
@Configuration
public class GlobalCorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Allow localhost dev ports; you can tighten this if needed
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://localhost:*"));
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList(
            "Authorization", "Cache-Control", "Content-Type",
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Ensure runs before Spring Security
        return bean;
    }
}


