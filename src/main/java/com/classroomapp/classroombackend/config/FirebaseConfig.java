package com.classroomapp.classroombackend.config;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import com.google.firebase.database.FirebaseDatabase;

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
public class FirebaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.bucket-name}")
    private String storageBucket;
    
    @Value("${firebase.database.url:https://mve-1-default-rtdb.asia-southeast1.firebasedatabase.app/}")
    private String databaseUrl;
    
    @PostConstruct
    public void init() {
        try {
            // Kiểm tra xem FirebaseApp đã được khởi tạo chưa
            if (FirebaseApp.getApps().stream().noneMatch(app -> "classroom-management".equals(app.getName()))) {
                logger.info("Initializing Firebase for project with bucket: {}", storageBucket);
                
                // Kiểm tra xem có cần thiết để khởi tạo không
                if (storageBucket == null || storageBucket.trim().isEmpty()) {
                    logger.warn("Storage bucket is not configured. Firebase initialization skipped.");
                    return;
                }
                
                try (InputStream serviceAccount = new ClassPathResource("mve-1-firebase-adminsdk.json").getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(storageBucket)
                        .setDatabaseUrl(databaseUrl)
                        .build();
                    
                    FirebaseApp.initializeApp(options, "classroom-management");
                    logger.info(">>>> Firebase has been initialized successfully with name: classroom-management <<<<");
                }
            } else {
                logger.info("Firebase app 'classroom-management' is already initialized.");
            }
            
            // Log all available Firebase apps
            logger.info("Available Firebase apps: {}", 
                FirebaseApp.getApps().stream()
                    .map(app -> String.format("%s (name: %s)", app.getOptions().getProjectId(), app.getName()))
                    .collect(java.util.stream.Collectors.joining(", ")));
                    
        } catch (Exception e) {
            logger.error("!!! CRITICAL: Failed to initialize Firebase. All Firebase-dependent features will fail.", e);
        }
    }
    
    /**
     * Configure FirebaseMessaging bean for dependency injection
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            // Ensure FirebaseApp is initialized first
            if (FirebaseApp.getApps().isEmpty()) {
                logger.warn("No Firebase apps found. FirebaseMessaging bean creation skipped.");
                throw new IllegalStateException("FirebaseApp has not been initialized. Please check your Firebase configuration file and bucket name configuration.");
            }
            
            // Try to get the classroom-management app
            FirebaseApp firebaseApp;
            try {
                firebaseApp = FirebaseApp.getInstance("classroom-management");
                logger.info("Using Firebase app 'classroom-management' for FirebaseMessaging");
            } catch (IllegalStateException e) {
                // Fallback to default app if classroom-management not found
                logger.warn("Firebase app 'classroom-management' not found, using default app");
                firebaseApp = FirebaseApp.getInstance();
            }
            
            FirebaseMessaging messaging = FirebaseMessaging.getInstance(firebaseApp);
            logger.info("FirebaseMessaging bean created successfully for app: {}", firebaseApp.getName());
            return messaging;
        } catch (Exception e) {
            logger.error("Failed to create FirebaseMessaging bean: {}", e.getMessage());
            // Instead of throwing exception, return null to allow application to start
            // Services that need Firebase will handle this gracefully
            logger.warn("FirebaseMessaging bean creation failed. Firebase-dependent services may not work properly.");
            return null;
        }
    }
    
    /**
     * Configure FirebaseDatabase bean for dependency injection
     * This provides a default FirebaseDatabase instance for services that don't need specific configuration
     */
    @Bean
    public FirebaseDatabase firebaseDatabase() {
        try {
            // Ensure FirebaseApp is initialized first
            if (FirebaseApp.getApps().isEmpty()) {
                logger.warn("No Firebase apps found. FirebaseDatabase bean creation skipped.");
                throw new IllegalStateException("FirebaseApp has not been initialized. Please check your Firebase configuration file and bucket name configuration.");
            }
            
            // Try to get the classroom-management app
            FirebaseApp firebaseApp;
            try {
                firebaseApp = FirebaseApp.getInstance("classroom-management");
                logger.info("Using Firebase app 'classroom-management' for FirebaseDatabase");
            } catch (IllegalStateException e) {
                // Fallback to default app if classroom-management not found
                logger.warn("Firebase app 'classroom-management' not found, using default app");
                firebaseApp = FirebaseApp.getInstance();
            }
            
            // Check if the app has database URL configured
            if (firebaseApp.getOptions().getDatabaseUrl() == null) {
                logger.warn("Firebase app '{}' does not have database URL configured. Using default database URL.", firebaseApp.getName());
                // Create a new FirebaseDatabase instance with default URL
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                logger.info("FirebaseDatabase bean created successfully with default configuration");
                return database;
            }
            
            FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseApp);
            logger.info("FirebaseDatabase bean created successfully for app: {}", firebaseApp.getName());
            return database;
        } catch (Exception e) {
            logger.error("Failed to create FirebaseDatabase bean: {}", e.getMessage());
            // Instead of throwing exception, return null to allow application to start
            // Services that need Firebase will handle this gracefully
            logger.warn("FirebaseDatabase bean creation failed. Firebase-dependent services may not work properly.");
            return null;
        }
    }
}

