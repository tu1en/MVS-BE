package com.classroomapp.classroombackend.util;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Simple database connection test
 * This will run on application startup to verify SQL Server connection
 */
@Component
@Slf4j
public class DatabaseConnectionTest implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("🔍 Testing SQL Server connection...");
        
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            String username = connection.getMetaData().getUserName();
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            String databaseProductVersion = connection.getMetaData().getDatabaseProductVersion();
            
            log.info("✅ SQL Server connection successful!");
            log.info("📊 Database: {}", databaseProductName);
            log.info("🔢 Version: {}", databaseProductVersion);
            log.info("🔗 URL: {}", url);
            log.info("👤 User: {}", username);
            log.info("🗄️ Hibernate will now handle schema creation/updates...");
            
        } catch (Exception e) {
            log.error("❌ SQL Server connection failed: {}", e.getMessage());
            log.error("💡 Please check:");
            log.error("   1. SQL Server is running");
            log.error("   2. Database credentials are correct");
            log.error("   3. Database 'SchoolManagementDB' exists");
            log.error("   4. SQL Server Authentication is enabled");
            throw e;
        }
    }
}
