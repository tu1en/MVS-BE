package com.classroomapp.classroombackend.config; 
 
import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = {
    "com.classroomapp.classroombackend.repository",
    "com.classroomapp.classroombackend.repository.usermanagement",
    "com.classroomapp.classroombackend.repository.requestmanagement",
    "com.classroomapp.classroombackend.repository.classroommanagement",
    "com.classroomapp.classroombackend.repository.assignmentmanagement",
    "com.classroomapp.classroombackend.repository.attendancemanagement",
    "com.classroomapp.classroombackend.accountant.repository"
})
@EntityScan(basePackages = {
    "com.classroomapp.classroombackend.model",
    "com.classroomapp.classroombackend.model.usermanagement",
    "com.classroomapp.classroombackend.model.requestmanagement",
    "com.classroomapp.classroombackend.model.classroommanagement",
    "com.classroomapp.classroombackend.model.assignmentmanagement",
    "com.classroomapp.classroombackend.model.attendancemanagement",
    "com.classroomapp.classroombackend.accountant.model"
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

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=SchoolManagementDB;encrypt=true;trustServerCertificate=true;characterEncoding=UTF-8;useUnicode=true;sendTimeAsDateTime=false;");
        dataSource.setUsername("sa");
        dataSource.setPassword("123");
        dataSource.setAutoCommit(false);
        dataSource.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        return dataSource;
    }
}
