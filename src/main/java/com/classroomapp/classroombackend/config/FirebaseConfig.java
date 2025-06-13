<<<<<<< HEAD
=======
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
    
    @Value("${firebase.bucket-name}")
    private String storageBucket;
    
    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing Firebase with bucket: {}", storageBucket);
            
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                // Load the service account JSON file
                FileInputStream serviceAccount = new FileInputStream("src/main/resources/sep490-e5896-firebase-adminsdk-fbsvc-402079bade.json");
                
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
            logger.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
>>>>>>> master
