package com.classroomapp.classroombackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.RequestRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.classroomapp.classroombackend.dto.TeacherRequestFormDTO;
import com.classroomapp.classroombackend.dto.StudentRequestFormDTO;

import java.time.LocalDateTime;

/**
 * Initialize test data when application starts
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public DataLoader(
        UserRepository userRepository, 
        RequestRepository requestRepository,
        PasswordEncoder passwordEncoder,
        ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Clear existing data
        userRepository.deleteAll();
        requestRepository.deleteAll();
        
        // Create sample users
        CreateUsers();
        
        // Create sample requests
        // CreateRequests();
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
        admin.setRoleId(RoleConstants.ADMIN);
        userRepository.save(admin);
        
        // Create teacher user
        User manager = new User();
        manager.setUsername("manager");
        manager.setPassword(passwordEncoder.encode("manager123"));
        manager.setEmail("manager@classroomapp.com");
        manager.setFullName("Nigga Cheese");
        manager.setRoleId(RoleConstants.MANAGER);
        userRepository.save(manager);

        // Create teacher user
        User teacher = new User();
        teacher.setUsername("teacher");
        teacher.setPassword(passwordEncoder.encode("teacher123"));
        teacher.setEmail("teacher@classroomapp.com");
        teacher.setFullName("Butt Slapper");
        teacher.setRoleId(RoleConstants.TEACHER);
        userRepository.save(teacher);
        
        // Create student user
        User student = new User();
        student.setUsername("student");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setEmail("student@classroomapp.com");
        student.setFullName("Ass Cracker");
        student.setRoleId(RoleConstants.STUDENT);
        userRepository.save(student);
    }
}