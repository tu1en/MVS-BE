package com.classroomapp.classroombackend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=SchoolManagementDB;encrypt=false;trustServerCertificate=true;";
        String username = "sa"; // Default
        String password = "123456"; // Default
        
        // Override with command-line arguments if provided
        if (args.length >= 2) {
            username = args[0];
            password = args[1];
        }
        
        System.out.println("Testing SQL Server connection...");
        System.out.println("Username: " + username);
        
        try {
            // Load the driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("✓ SQL Server driver loaded successfully");
            
            // Test connection
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("✓ Connected to SQL Server successfully!");
            
            // Test a simple query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT @@VERSION as version");
            
            if (resultSet.next()) {
                System.out.println("✓ SQL Server Version: " + resultSet.getString("version"));
            }
            
            // Test database exists
            ResultSet dbResult = statement.executeQuery("SELECT DB_NAME() as current_db");
            if (dbResult.next()) {
                System.out.println("✓ Current Database: " + dbResult.getString("current_db"));
            }
            
            // Check some tables
            String[] tables = {"users", "classrooms", "schedules", "announcements"};
            for (String table : tables) {
                try {
                    ResultSet tableResult = statement.executeQuery(
                        "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + table + "'"
                    );
                    if (tableResult.next() && tableResult.getInt("count") > 0) {
                        System.out.println("✓ Table '" + table + "' exists");
                    } else {
                        System.out.println("⚠ Table '" + table + "' does not exist");
                    }
                } catch (Exception e) {
                    System.out.println("✗ Error checking table '" + table + "': " + e.getMessage());
                }
            }
            
            connection.close();
            System.out.println("✓ Connection closed successfully");
            
        } catch (Exception e) {
            System.out.println("✗ Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
