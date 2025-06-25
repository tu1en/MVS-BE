package com.classroomapp.classroombackend.controller;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class DatabaseHealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> checkDatabaseHealth() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            response.put("status", "SUCCESS");
            response.put("connected", true);
            response.put("databaseProductName", metaData.getDatabaseProductName());
            response.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            response.put("driverName", metaData.getDriverName());
            response.put("driverVersion", metaData.getDriverVersion());
            response.put("url", metaData.getURL());
            response.put("username", metaData.getUserName());
            
            // Test a simple query
            boolean queryResult = connection.createStatement()
                .execute("SELECT 1 as test_connection");
            response.put("queryTest", queryResult ? "SUCCESS" : "FAILED");
            
        } catch (SQLException e) {
            response.put("status", "ERROR");
            response.put("connected", false);
            response.put("error", e.getMessage());
            response.put("sqlState", e.getSQLState());
            response.put("errorCode", e.getErrorCode());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> checkTables() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check for some key tables
            String[] tableNames = {"users", "classrooms", "schedules", "announcements"};
            Map<String, Boolean> tableStatus = new HashMap<>();
            
            for (String tableName : tableNames) {
                try (var rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    tableStatus.put(tableName, rs.next());
                }
            }
            
            response.put("status", "SUCCESS");
            response.put("tables", tableStatus);
            
        } catch (SQLException e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
