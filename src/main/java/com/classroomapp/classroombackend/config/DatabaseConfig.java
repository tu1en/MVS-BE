package com.classroomapp.classroombackend.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
              .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
              .url("jdbc:sqlserver://localhost:1433;databaseName=SchoolManagementDB;encrypt=true;trustServerCertificate=true;characterEncoding=UTF-8;useUnicode=true;")
                .username("sa")
                .password("123456")
                .build();
    }
}
