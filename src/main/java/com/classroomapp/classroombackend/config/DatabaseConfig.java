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
        dataSource.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=SchoolManagementDB;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=true;characterEncoding=UTF-8;useUnicode=true;collation=Vietnamese_CI_AS;sendTimeAsDateTime=false;");
        dataSource.setUsername("sa");
        dataSource.setPassword("12345678");
        // dataSource.setAutoCommit(false); // This can prevent seeders from committing data. Let Spring's @Transactional manage commits.
        dataSource.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        
        // Additional Unicode support properties
        dataSource.addDataSourceProperty("useUnicode", "true");
        dataSource.addDataSourceProperty("characterEncoding", "UTF-8");
        dataSource.addDataSourceProperty("sendStringParametersAsUnicode", "true");
        dataSource.addDataSourceProperty("collation", "Vietnamese_CI_AS");
        
        return dataSource;
    }
}
