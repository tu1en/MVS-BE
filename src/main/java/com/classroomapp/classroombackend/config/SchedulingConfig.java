package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable Spring's scheduling capabilities
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // This configuration enables @Scheduled annotations to work
}
