package com.classroomapp.classroombackend.config; 
 
import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.classroomapp.classroombackend.repository")
@EntityScan(basePackages = "com.classroomapp.classroombackend.model")
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=SchoolManagementDB;encrypt=true;trustServerCertificate=true;characterEncoding=UTF-8;useUnicode=true;sendTimeAsDateTime=false;");
        dataSource.setUsername("sa");
        dataSource.setPassword("Hoangduc02@");
        dataSource.setAutoCommit(false);
        dataSource.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        return dataSource;
    }
}
