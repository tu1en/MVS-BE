package com.classroomapp.classroombackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.UserRepository;

/**
 * Initialize test data when application starts
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Clear existing data
        userRepository.deleteAll();
        
        // Create sample users
        CreateUsers();
    }
    
    /**
     * Create sample users for testing
     */
    private void CreateUsers() {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@classroomapp.com");
        admin.setFullName("Administrator");
        admin.setRole("ADMIN");
        userRepository.save(admin);
        
        // Create teacher user
        User teacher = new User();
        teacher.setUsername("teacher");
        teacher.setPassword(passwordEncoder.encode("teacher123"));
        teacher.setEmail("teacher@classroomapp.com");
        teacher.setFullName("Jane Doe");
        teacher.setRole("TEACHER");
        userRepository.save(teacher);
        
        // Create student user
        User student = new User();
        student.setUsername("student");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setEmail("student@classroomapp.com");
        student.setFullName("John Smith");
        student.setRole("STUDENT");
        userRepository.save(student);
    }
} 