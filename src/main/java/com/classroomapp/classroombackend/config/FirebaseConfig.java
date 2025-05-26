package com.classroomapp.classroombackend.config;
import com.google.firebase.FirebaseApp;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {
    
    @PostConstruct
    public void init() {
        try {
            FirebaseApp.initializeApp();
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
   
}