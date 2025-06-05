package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for application startup events
 */
@Configuration
@Slf4j
public class ApplicationStartupConfig {

    /**
     * Event listener that runs when the application context is refreshed (on startup)
     * Clears any security context to ensure no user is automatically logged in
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Application started: Clearing security context to ensure no auto-login occurs");
        SecurityContextHolder.clearContext();
    }
} 