

package com.classroomapp.classroombackend.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
      @Value("${firebase.bucket-name:}")
    private String storageBucket;
    
    @PostConstruct
    public void init() {
        try {
            logger.info("Attempting to initialize Firebase with bucket: {}", storageBucket);
            
            // Skip Firebase initialization if bucket name is not configured
            if (storageBucket == null || storageBucket.trim().isEmpty()) {
                logger.info("Firebase bucket name not configured. Skipping Firebase initialization.");
                return;
            }
            
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                // Try to load the service account JSON file
                String serviceAccountPath = "src/main/resources/sep490-e5896-firebase-adminsdk-fbsvc-402079bade.json";
                
                // Check if file exists before trying to load
                java.io.File serviceAccountFile = new java.io.File(serviceAccountPath);
                if (!serviceAccountFile.exists()) {
                    logger.warn("Firebase service account file not found at: {}. Firebase will not be initialized.", serviceAccountPath);
                    logger.warn("Firebase-dependent features (file upload) may not work properly.");
                    return; // Skip Firebase initialization
                }
                
                FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
                
                // Build Firebase options with storage bucket
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(storageBucket)
                    .build();
                
                // Initialize the Firebase app
                FirebaseApp.initializeApp(options);
                logger.info("Firebase has been initialized successfully");
            } else {
                logger.info("Firebase is already initialized");
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase: {}", e.getMessage());
            logger.warn("Firebase will not be available. File upload features may not work.");
            // Don't throw exception, just log the error to allow app to start
        } catch (Exception e) {
            logger.error("Unexpected error during Firebase initialization: {}", e.getMessage());
            logger.warn("Firebase will not be available. File upload features may not work.");
        }
    }
}

