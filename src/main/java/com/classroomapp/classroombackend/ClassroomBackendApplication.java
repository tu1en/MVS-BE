package com.classroomapp.classroombackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
    "com.classroomapp.classroombackend.repository.usermanagement",
    "com.classroomapp.classroombackend.repository.classroommanagement",
    "com.classroomapp.classroombackend.repository.attendancemanagement",
    "com.classroomapp.classroombackend.repository.assignmentmanagement",
    "com.classroomapp.classroombackend.repository.requestmanagement"
})
public class ClassroomBackendApplication {

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        SpringApplication.run(ClassroomBackendApplication.class, args);
    }

    @Configuration
    public class EncodingConfig implements WebMvcConfigurer {
        @Bean
        public HttpMessageConverter<String> responseBodyConverter() {
            StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
            converter.setWriteAcceptCharset(false);
            return converter;
        }

        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.add(0, responseBodyConverter());
        }
    }
} 