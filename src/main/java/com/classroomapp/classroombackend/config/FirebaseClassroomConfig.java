package com.classroomapp.classroombackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration cho Classroom Management
 * Cáº¥u hÃ¬nh Firebase Realtime Database cho real-time updates
 */
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class FirebaseClassroomConfig {

    @Value("${firebase.database.url:https://mve-1-default-rtdb.asia-southeast1.firebasedatabase.app/}")
    private String databaseUrl;

    @Value("${firebase.config.path:mve-1-firebase-adminsdk.json}")
    private String firebaseConfigPath;

    @Value("${firebase.bucket-name}")
    private String storageBucket;

    private static final String CLASSROOM_APP_NAME = "classroom-management";

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Kiểm tra xem FirebaseApp đã được khởi tạo chưa
            if (FirebaseApp.getApps().stream().noneMatch(app -> CLASSROOM_APP_NAME.equals(app.getName()))) {
                log.info("Firebase app '{}' not found, attempting to initialize...", CLASSROOM_APP_NAME);
                
                // Check if there's already a default Firebase app
                if (FirebaseApp.getApps().isEmpty()) {
                    log.warn("No Firebase apps found. Please ensure FirebaseConfig initializes the main Firebase app first.");
                    return;
                }
                
                // Try to get the existing app
                try {
                    FirebaseApp existingApp = FirebaseApp.getInstance(CLASSROOM_APP_NAME);
                    log.info("Firebase app '{}' is available but not in getApps() list", CLASSROOM_APP_NAME);
                    return;
                } catch (IllegalStateException e) {
                    log.warn("Firebase app '{}' not accessible: {}", CLASSROOM_APP_NAME, e.getMessage());
                    
                    // Try to initialize a new app with the same name
                    try {
                        ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
                        if (resource.exists()) {
                            try (InputStream serviceAccount = resource.getInputStream()) {
                                FirebaseOptions options = FirebaseOptions.builder()
                                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                        .setDatabaseUrl(databaseUrl)
                                        .setStorageBucket(storageBucket)
                                        .build();

                                FirebaseApp.initializeApp(options, CLASSROOM_APP_NAME);
                                log.info("Firebase app '{}' initialized successfully for Classroom Management", CLASSROOM_APP_NAME);
                            }
                        } else {
                            log.warn("Firebase config file not found: {}. Using existing Firebase app.", firebaseConfigPath);
                        }
                    } catch (Exception initError) {
                        log.error("Failed to initialize Firebase app '{}': {}", CLASSROOM_APP_NAME, initError.getMessage());
                    }
                }
            } else {
                log.info("Firebase app '{}' already exists, skipping initialization", CLASSROOM_APP_NAME);
            }
        } catch (Exception e) {
            log.error("Error during Firebase initialization check: {}", e.getMessage());
        }
    }

    @Bean(name = "classroomFirebaseDatabase")
    public FirebaseDatabase classroomFirebaseDatabase() {
        try {
            // Wait a bit for Firebase app to be initialized
            int maxAttempts = 5;
            int attempt = 0;
            FirebaseApp app = null;
            
            while (attempt < maxAttempts && app == null) {
                try {
                    app = FirebaseApp.getInstance(CLASSROOM_APP_NAME);
                    break;
                } catch (IllegalStateException e) {
                    attempt++;
                    if (attempt < maxAttempts) {
                        log.info("Attempt {}/{}: Firebase app '{}' not ready yet, waiting...", attempt, maxAttempts, CLASSROOM_APP_NAME);
                        try {
                            Thread.sleep(1000); // Wait 1 second
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
            if (app == null) {
                log.error("Failed to get Firebase app '{}' after {} attempts", CLASSROOM_APP_NAME, maxAttempts);
                // Instead of throwing exception, try to get default app
                try {
                    app = FirebaseApp.getInstance();
                    log.info("Using default Firebase app as fallback");
                } catch (IllegalStateException e) {
                    log.error("No Firebase apps available. Firebase features will be disabled.");
                    throw new RuntimeException("No Firebase apps available", e);
                }
            }
            
            FirebaseDatabase database = FirebaseDatabase.getInstance(app);
            log.info("Firebase Database bean created successfully for Classroom Management");
            return database;
        } catch (Exception e) {
            log.error("Failed to create Firebase Database bean for Classroom Management: {}", e.getMessage());
            throw new RuntimeException("Failed to create Firebase Database bean", e);
        }
    }

    /**
     * Get Firebase Database instance
     */
    public static FirebaseDatabase getDatabase() {
        try {
            FirebaseApp app = FirebaseApp.getInstance(CLASSROOM_APP_NAME);
            return FirebaseDatabase.getInstance(app);
        } catch (Exception e) {
            log.warn("ðŸ”¥ Failed to get Firebase Database instance: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if Firebase is available
     */
    public static boolean isFirebaseAvailable() {
        try {
            FirebaseApp.getInstance(CLASSROOM_APP_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
