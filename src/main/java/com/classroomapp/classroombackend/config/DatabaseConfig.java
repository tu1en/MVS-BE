package com.classroomapp.classroombackend.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
    "com.classroomapp.classroombackend.repository",
    "com.classroomapp.classroombackend.repository.usermanagement",
    "com.classroomapp.classroombackend.repository.requestmanagement",
    "com.classroomapp.classroombackend.repository.classroommanagement",
    "com.classroomapp.classroombackend.repository.assignmentmanagement",
    "com.classroomapp.classroombackend.repository.attendancemanagement"
})
@EntityScan(basePackages = {
    "com.classroomapp.classroombackend.model",
    "com.classroomapp.classroombackend.model.usermanagement",
    "com.classroomapp.classroombackend.model.requestmanagement",
    "com.classroomapp.classroombackend.model.classroommanagement",
    "com.classroomapp.classroombackend.model.assignmentmanagement",
    "com.classroomapp.classroombackend.model.attendancemanagement"
})
@ComponentScan(basePackages = {
    "com.classroomapp.classroombackend",
    "com.classroomapp.classroombackend.service",
    "com.classroomapp.classroombackend.service.impl",
    "com.classroomapp.classroombackend.controller",
    "com.classroomapp.classroombackend.config",
    "com.classroomapp.classroombackend.security"
})
public class DatabaseConfig {

    // Configuration is handled by application.properties
    // No hardcoded DataSource bean needed
}
