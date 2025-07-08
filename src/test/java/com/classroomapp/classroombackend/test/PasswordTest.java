package com.classroomapp.classroombackend.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String storedHash = "$2a$10$N.zmdr9k7uOCQb97.ZB5..6pNmLHWQKmKu8lPNrXMROGgGnVcmxDC";
        
        System.out.println("Testing password: " + password);
        System.out.println("Stored hash: " + storedHash);
        System.out.println("Match: " + encoder.matches(password, storedHash));
        
        // Generate a new hash
        String newHash = encoder.encode(password);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(password, newHash));
    }
}
