package com.classroomapp.classroombackend.config;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * UTF-8 Encoding Configuration for Vietnamese Text Support
 * 
 * This configuration ensures that all HTTP requests and responses
 * use UTF-8 encoding to properly handle Vietnamese characters.
 */
@Slf4j
@Configuration
public class UTF8EncodingConfig {

    /**
     * Configure UTF-8 character encoding filter
     * This filter ensures all requests and responses use UTF-8 encoding
     */
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilter() {
        log.info("🔧 Configuring UTF-8 character encoding filter for Vietnamese text support");
        
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding(StandardCharsets.UTF_8.name());
        filter.setForceEncoding(true);
        filter.setForceRequestEncoding(true);
        filter.setForceResponseEncoding(true);
        
        FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // Highest priority
        registrationBean.setName("UTF8EncodingFilter");
        
        log.info("✅ UTF-8 character encoding filter configured successfully");
        log.info("📝 Filter will apply UTF-8 encoding to all requests and responses");
        
        return registrationBean;
    }

    /**
     * System property configuration for UTF-8 support
     * This method sets JVM system properties to ensure UTF-8 is used throughout the application
     */
    @Bean
    public UTF8SystemPropertiesInitializer utf8SystemPropertiesInitializer() {
        return new UTF8SystemPropertiesInitializer();
    }

    /**
     * Inner class to initialize UTF-8 system properties
     */
    public static class UTF8SystemPropertiesInitializer {
        
        public UTF8SystemPropertiesInitializer() {
            log.info("🔧 Initializing UTF-8 system properties for Vietnamese text support");
            
            // Set file encoding to UTF-8
            System.setProperty("file.encoding", StandardCharsets.UTF_8.name());
            
            // Set default charset for HTTP connections
            System.setProperty("http.agent.charset", StandardCharsets.UTF_8.name());
            
            // Set default charset for URL encoding
            System.setProperty("url.encoding", StandardCharsets.UTF_8.name());
            
            // Set console encoding to UTF-8 (for Windows compatibility)
            System.setProperty("console.encoding", StandardCharsets.UTF_8.name());
            
            // Set SQL Server specific properties for Unicode support
            System.setProperty("sqlserver.charset", StandardCharsets.UTF_8.name());
            System.setProperty("sqlserver.sendStringParametersAsUnicode", "true");
            
            // Log current encoding settings
            logEncodingSettings();
            
            log.info("✅ UTF-8 system properties initialized successfully");
        }
        
        private void logEncodingSettings() {
            log.info("📊 Current encoding settings:");
            log.info("   - file.encoding: {}", System.getProperty("file.encoding"));
            log.info("   - Default charset: {}", java.nio.charset.Charset.defaultCharset());
            log.info("   - JVM default charset: {}", System.getProperty("file.encoding"));
            log.info("   - Console encoding: {}", System.getProperty("console.encoding"));
            
            // Test Vietnamese character support
            String testVietnamese = "Chúng tôi vui mừng thông báo hệ thống mới";
            byte[] utf8Bytes = testVietnamese.getBytes(StandardCharsets.UTF_8);
            String reconstructed = new String(utf8Bytes, StandardCharsets.UTF_8);
            
            boolean encodingWorking = testVietnamese.equals(reconstructed);
            log.info("   - Vietnamese encoding test: {} ({})", 
                encodingWorking ? "✅ PASSED" : "❌ FAILED",
                encodingWorking ? "Characters preserved" : "Characters corrupted"
            );
            
            if (!encodingWorking) {
                log.error("❌ Vietnamese character encoding test failed!");
                log.error("   Original: {}", testVietnamese);
                log.error("   Reconstructed: {}", reconstructed);
                log.error("   UTF-8 bytes length: {}", utf8Bytes.length);
            }
        }
    }
}
