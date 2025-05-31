package com.classroomapp.classroombackend.config;

import com.classroomapp.classroombackend.service.EmailService;
import com.classroomapp.classroombackend.service.impl.EmailServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmailConfig {
    
    @Bean
    @Primary
    public EmailService emailService(EmailServiceImpl emailService) {
        return emailService;
    }
} 