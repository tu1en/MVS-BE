package com.classroomapp.classroombackend.config;
import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

import com.google.firebase.FirebaseApp;

@Configuration
public class FirebaseConfig {
    
    @PostConstruct
    public void init() {
        try {
            // Thêm cấu hình Firebase từ file JSON
            java.io.FileInputStream serviceAccount =
                new java.io.FileInputStream("src/main/resources/sep490-e5896-firebase-adminsdk-fbsvc-402079bade.json");
              com.google.firebase.FirebaseOptions options = com.google.firebase.FirebaseOptions.builder()
                .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount))
                .build();
                
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}