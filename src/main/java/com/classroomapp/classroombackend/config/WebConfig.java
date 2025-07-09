package com.classroomapp.classroombackend.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // Remove CORS configuration from here since it's handled in SecurityConfig
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     // Moved to SecurityConfig to avoid conflicts
    // }
    
    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converter.setWriteAcceptCharset(false);
        return converter;
    }

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

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        log.info("🔧 Configuring content negotiation with UTF-8 default");
        configurer
            .defaultContentType(new MediaType("application", "json", StandardCharsets.UTF_8))
            .mediaType("json", new MediaType("application", "json", StandardCharsets.UTF_8))
            .mediaType("xml", new MediaType("application", "xml", StandardCharsets.UTF_8));
    }
}