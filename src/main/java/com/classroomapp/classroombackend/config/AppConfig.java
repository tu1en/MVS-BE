package com.classroomapp.classroombackend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        
        // Configure ModelMapper to be more lenient
        modelMapper.getConfiguration()
            .setSkipNullEnabled(true)
            .setAmbiguityIgnored(true);
            
        return modelMapper;
    }
} 