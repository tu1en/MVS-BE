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

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
public class FirebaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.bucket-name}")
    private String storageBucket;
    
    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                logger.info("Initializing Firebase for project with bucket: {}", storageBucket);

                // This file should be in src/main/resources
                String serviceAccountPath = "mve-1-firebase-adminsdk.json";
                ClassPathResource resource = new ClassPathResource(serviceAccountPath);

                if (!resource.exists()) {
                    logger.error("!!! CRITICAL: Firebase service account file not found at classpath: {}. Firebase features will fail.", serviceAccountPath);
                    return;
                }
                
                try (InputStream serviceAccount = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(storageBucket)
                        .build();
                    
                    FirebaseApp.initializeApp(options);
                    logger.info(">>>> Firebase has been initialized successfully! <<<<");
                }
            } else {
                logger.info("Firebase is already initialized.");
            }
        } catch (Exception e) {
            logger.error("!!! CRITICAL: Failed to initialize Firebase. All Firebase-dependent features will fail.", e);
        }
    }
}

