package com.classroomapp.classroombackend.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * 🎯 COMPLETE WEB CONFIG - Merged configuration
 * ✅ FIXED: Single CORS configuration using allowedOriginPatterns
 * ✅ Combines: UTF-8 encoding + CORS + Jackson + Static resources
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    /**
     * ✅ Configure Jackson ObjectMapper for JSON serialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        log.info("🔧 Configuring Jackson ObjectMapper with UTF-8 and Vietnamese support");
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print for development
        
        return mapper;
    }
    
    /**
     * ✅ Configure UTF-8 String converter bean
     */
    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converter.setWriteAcceptCharset(false);
        return converter;
    }

    /**
     * ✅ Configure message converters for UTF-8 support (Vietnamese text)
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("🔧 Configuring UTF-8 message converters for Vietnamese text support");

        // Clear existing converters to ensure our UTF-8 converters take precedence
        converters.clear();

        // Add UTF-8 String converter with highest priority
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);
        converters.add(stringConverter);

        // Add UTF-8 JSON converter with explicit charset
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);

        // Set supported media types with UTF-8 charset
        jsonConverter.setSupportedMediaTypes(List.of(
            new MediaType("application", "json", StandardCharsets.UTF_8),
            new MediaType("application", "*+json", StandardCharsets.UTF_8),
            new MediaType("text", "json", StandardCharsets.UTF_8)
        ));

        converters.add(jsonConverter);

        log.info("✅ UTF-8 message converters configured successfully");
    }

    /**
     * ✅ Configure content negotiation with UTF-8 default
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        log.info("🔧 Configuring content negotiation with UTF-8 default");
        
        configurer
            .defaultContentType(new MediaType("application", "json", StandardCharsets.UTF_8))
            .mediaType("json", new MediaType("application", "json", StandardCharsets.UTF_8))
            .mediaType("xml", new MediaType("application", "xml", StandardCharsets.UTF_8));
    }
    
    /**
     * ✅ FIXED: Configure CORS for cross-origin requests
     * 🚫 REMOVED: This CORS config to avoid conflict with SecurityConfig
     * CORS will be handled ONLY in SecurityConfig.java
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("🚫 CORS configuration DISABLED in WebConfig - handled by SecurityConfig");
        // DO NOT configure CORS here to avoid conflicts
    }
    
    /**
     * ✅ Configure static resource handlers
     * 🎯 IMPORTANT: Excludes /api/files/** from static resource handling
     * ⚠️ CONFLICT RESOLUTION: /api/files/upload is REST endpoint, not static resource
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("🔧 Configuring static resource handlers...");
        
        // ✅ 1. Handle uploaded files ONLY for /files/serve/** (not /api/files/**)
        registry.addResourceHandler("/files/serve/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600); // 1 hour cache
        
        log.info("📁 Static file serving configured: /files/serve/** -> {}", uploadDir);
        
        // ✅ 2. Handle thumbnails if needed
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:" + uploadDir + "/thumbnails/")
                .setCachePeriod(86400); // 24 hours cache
        
        // ✅ 3. Handle other static resources (CSS, JS, etc.)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400);
        
        log.info("✅ Resource handlers configured successfully");
        log.info("⚠️ IMPORTANT: /api/files/** is reserved for REST endpoints, not static resources");
    }
}