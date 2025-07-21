package com.classroomapp.classroombackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Configuration cho Classroom Management
 * Cấu hình Firebase Realtime Database cho real-time updates
 */
@Configuration
@Slf4j
public class FirebaseClassroomConfig {

    @Value("${firebase.database.url:https://mve-1-default-rtdb.asia-southeast1.firebasedatabase.app/}")
    private String databaseUrl;

    @Value("${firebase.config.path:mve-1-firebase-adminsdk.json}")
    private String firebaseConfigPath;

    private static final String CLASSROOM_APP_NAME = "classroom-management";

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Kiểm tra xem FirebaseApp đã được khởi tạo chưa
            if (FirebaseApp.getApps().stream().noneMatch(app -> CLASSROOM_APP_NAME.equals(app.getName()))) {
                ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
                
                if (!resource.exists()) {
                    log.warn("🔥 Firebase config file not found: {}. Firebase features will be disabled.", firebaseConfigPath);
                    return;
                }

                try (InputStream serviceAccount = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setDatabaseUrl(databaseUrl)
                            .build();

                    FirebaseApp.initializeApp(options, CLASSROOM_APP_NAME);
                    log.info("🔥 Firebase initialized successfully for Classroom Management");
                } catch (IOException e) {
                    log.error("🔥 Failed to initialize Firebase for Classroom Management", e);
                }
            } else {
                log.info("🔥 Firebase already initialized for Classroom Management");
            }
        } catch (Exception e) {
            log.error("🔥 Error during Firebase initialization for Classroom Management", e);
        }
    }

    @Bean(name = "classroomFirebaseDatabase")
    public FirebaseDatabase classroomFirebaseDatabase() {
        try {
            FirebaseApp app = FirebaseApp.getInstance(CLASSROOM_APP_NAME);
            FirebaseDatabase database = FirebaseDatabase.getInstance(app);
            log.info("🔥 Firebase Database bean created for Classroom Management");
            return database;
        } catch (Exception e) {
            log.warn("🔥 Failed to create Firebase Database bean for Classroom Management: {}", e.getMessage());
            return null;
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
            log.warn("🔥 Failed to get Firebase Database instance: {}", e.getMessage());
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
