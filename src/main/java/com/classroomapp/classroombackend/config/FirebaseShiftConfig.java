package com.classroomapp.classroombackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration cho Shift Management
 * Cấu hình Firebase Realtime Database và Firebase Cloud Messaging
 */
@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class FirebaseShiftConfig {

    @Value("${firebase.database.url:https://mve-1-default-rtdb.asia-southeast1.firebasedatabase.app}")
    private String firebaseDatabaseUrl;

    @Value("${firebase.config.path:mve-1-firebase-adminsdk.json}")
    private String firebaseConfigPath;

    @Value("${firebase.project.id:mve-1}")
    private String firebaseProjectId;

    /**
     * Initialize Firebase App for Shift Management
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        log.info("Initializing Firebase App for Shift Management");

        try {
            // Check if Firebase app already exists
            FirebaseApp existingApp = null;
            try {
                existingApp = FirebaseApp.getInstance();
                log.info("Firebase App already exists, using existing instance");
                return existingApp;
            } catch (IllegalStateException e) {
                log.debug("No existing Firebase App found, creating new instance");
            }

            // Load Firebase credentials
            InputStream serviceAccount = new ClassPathResource(firebaseConfigPath).getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            // Configure Firebase options
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setDatabaseUrl(firebaseDatabaseUrl)
                .setProjectId(firebaseProjectId)
                .build();

            // Initialize Firebase App
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Successfully initialized Firebase App: {}", app.getName());

            return app;

        } catch (IOException e) {
            log.error("Error initializing Firebase App: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase App", e);
        }
    }

    /**
     * Firebase Realtime Database instance
     */
    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        log.info("Creating Firebase Realtime Database instance");

        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseApp);
            
            // Configure database settings
            database.setPersistenceEnabled(true);
            database.setPersistenceCacheSizeBytes(10 * 1024 * 1024); // 10MB cache
            
            log.info("Successfully created Firebase Realtime Database instance");
            return database;

        } catch (Exception e) {
            log.error("Error creating Firebase Realtime Database: {}", e.getMessage());
            throw new RuntimeException("Failed to create Firebase Realtime Database", e);
        }
    }

    /**
     * Firebase Cloud Messaging instance
     */
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        log.info("Creating Firebase Cloud Messaging instance");

        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance(firebaseApp);
            log.info("Successfully created Firebase Cloud Messaging instance");
            return messaging;

        } catch (Exception e) {
            log.error("Error creating Firebase Cloud Messaging: {}", e.getMessage());
            throw new RuntimeException("Failed to create Firebase Cloud Messaging", e);
        }
    }

    /**
     * Firebase Database Rules (for reference - should be deployed via Firebase Console)
     */
    public String getFirebaseDatabaseRules() {
        return """
            {
              "rules": {
                "shift-assignments": {
                  ".read": "auth != null",
                  ".write": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER')",
                  "$assignmentId": {
                    ".read": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER' || auth.token.role == 'ACCOUNTANT' || data.child('employeeId').val() == auth.uid)",
                    ".write": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER')"
                  }
                },
                "shift-schedules": {
                  ".read": "auth != null",
                  ".write": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER')"
                },
                "shift-swap-requests": {
                  ".read": "auth != null",
                  ".write": "auth != null",
                  "$requestId": {
                    ".read": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER' || data.child('requesterId').val() == auth.uid || data.child('targetEmployeeId').val() == auth.uid)",
                    ".write": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER' || data.child('requesterId').val() == auth.uid || data.child('targetEmployeeId').val() == auth.uid)"
                  }
                },
                "shift-templates": {
                  ".read": "auth != null",
                  ".write": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER')"
                },
                "shift-notifications": {
                  "$userId": {
                    ".read": "auth != null && auth.uid == $userId",
                    ".write": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER' || auth.uid == $userId)"
                  }
                },
                "employee-shifts": {
                  "$employeeId": {
                    ".read": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER' || auth.token.role == 'ACCOUNTANT' || auth.uid == $employeeId)",
                    ".write": "auth != null && (auth.token.role == 'ADMIN' || auth.token.role == 'MANAGER')"
                  }
                }
              }
            }
            """;
    }

    /**
     * Firebase Database Indexes (for reference - should be deployed via Firebase Console)
     */
    public String getFirebaseDatabaseIndexes() {
        return """
            {
              "rules": {
                "shift-assignments": {
                  ".indexOn": ["employeeId", "assignmentDate", "status", "attendanceStatus"]
                },
                "shift-schedules": {
                  ".indexOn": ["status", "scheduleType", "startDate", "endDate", "createdById"]
                },
                "shift-swap-requests": {
                  ".indexOn": ["requesterId", "targetEmployeeId", "status", "priority", "isEmergency", "createdAt"]
                },
                "shift-templates": {
                  ".indexOn": ["isActive", "sortOrder", "templateCode"]
                },
                "employee-shifts": {
                  "$employeeId": {
                    ".indexOn": ["date", "status"]
                  }
                }
              }
            }
            """;
    }
}

/**
 * Firebase Security Rules Documentation
 * 
 * Authentication Requirements:
 * - All reads/writes require authentication (auth != null)
 * - Role-based access control using custom claims in JWT tokens
 * 
 * Role Permissions:
 * - ADMIN: Full access to all shift data
 * - MANAGER: Full access to shift assignments, schedules, templates
 * - TEACHER: Read own assignments, create/respond to swap requests
 * - ACCOUNTANT: Read-only access for payroll calculations
 * 
 * Data Structure:
 * /shift-assignments/{assignmentId}
 * /shift-schedules/{scheduleId}
 * /shift-swap-requests/{requestId}
 * /shift-templates/{templateId}
 * /shift-notifications/{userId}/{notificationId}
 * /employee-shifts/{employeeId}/{date}/{assignmentId}
 * 
 * Security Features:
 * - User can only read their own notifications
 * - Employees can only read their own shift assignments
 * - Swap requests are readable by requester, target, and managers
 * - All write operations require appropriate role permissions
 * 
 * Performance Optimizations:
 * - Indexes on frequently queried fields
 * - Denormalized employee-shifts for mobile app efficiency
 * - Persistence enabled with 10MB cache
 * 
 * Deployment Instructions:
 * 1. Deploy security rules via Firebase Console
 * 2. Deploy database indexes via Firebase Console
 * 3. Configure custom claims in Firebase Auth
 * 4. Test security rules with Firebase Emulator
 */
