package com.doproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Configuration for Jackson ObjectMapper and CORS
 * Production: update allowed origins and methods
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Configure ObjectMapper for JSON serialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print for development
        return mapper;
    }
    
    /**
     * Configure CORS for cross-origin requests
     * Development: Allow localhost origins
     * Production: Update with actual frontend domains
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:3001", 
                    "http://127.0.0.1:3000",
                    "http://127.0.0.1:3001"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight response for 1 hour
    }
}
