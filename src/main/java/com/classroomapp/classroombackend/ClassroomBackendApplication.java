package com.classroomapp.classroombackend;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableCaching
@Configuration
@ComponentScan(basePackages = {"com.classroomapp.classroombackend", "com.doproject"})
@EntityScan(basePackages = {"com.classroomapp.classroombackend.entity", "com.classroomapp.classroombackend.model", "com.doproject.entity"})
@EnableJpaRepositories(basePackages = {"com.classroomapp.classroombackend.repository", "com.doproject.repository"})
public class ClassroomBackendApplication implements WebMvcConfigurer {
    
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        SpringApplication.run(ClassroomBackendApplication.class, args);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }
}