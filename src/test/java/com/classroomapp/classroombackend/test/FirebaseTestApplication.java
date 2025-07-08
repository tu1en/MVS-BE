package com.classroomapp.classroombackend.test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.google.firebase.FirebaseApp;

@SpringBootApplication
@ComponentScan(basePackages = "com.classroomapp.classroombackend")
public class FirebaseTestApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(FirebaseTestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Firebase Test Application ===");
        
        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("❌ Firebase is NOT initialized!");
        } else {
            System.out.println("✅ Firebase is initialized successfully!");
            System.out.println("📱 Number of Firebase apps: " + FirebaseApp.getApps().size());
            
            for (FirebaseApp app : FirebaseApp.getApps()) {
                System.out.println("📱 App name: " + app.getName());
                System.out.println("📱 Project ID: " + app.getOptions().getProjectId());
                System.out.println("📱 Storage bucket: " + app.getOptions().getStorageBucket());
            }
        }
        
        System.out.println("=== Test completed ===");
        System.exit(0);
    }
}
