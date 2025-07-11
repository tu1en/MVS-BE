package com.classroomapp.classroombackend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // Cấu hình bổ sung cho ModelMapper nếu cần
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        return modelMapper;
    }
}