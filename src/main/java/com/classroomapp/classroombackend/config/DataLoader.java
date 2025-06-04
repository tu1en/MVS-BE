package com.classroomapp.classroombackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.repository.RequestRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.classroomapp.classroombackend.dto.TeacherRequestFormDTO;
import com.classroomapp.classroombackend.dto.StudentRequestFormDTO;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalDate;


/**
 * Initialize test data when application starts
 */
@Component
public class DataLoader implements CommandLineRunner {    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final AccomplishmentRepository accomplishmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    @Autowired    public DataLoader(
        UserRepository userRepository, 
        RequestRepository requestRepository,
        AccomplishmentRepository accomplishmentRepository,
        PasswordEncoder passwordEncoder,
        ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.accomplishmentRepository = accomplishmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }
      @Override
    public void run(String... args) throws Exception {
        // Clear existing data
        userRepository.deleteAll();
        requestRepository.deleteAll();
        accomplishmentRepository.deleteAll();
        
        // Create sample users
        CreateUsers();
        
        // Create sample accomplishments
        CreateAccomplishments();
        
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
    
    /**
     * Create sample accomplishments for testing
     */
    private void CreateAccomplishments() {
        // Get the student user we created
        User student = userRepository.findByUsername("student")
                .orElseThrow(() -> new RuntimeException("Student user not found"));
        
        // Create sample accomplishments
        Accomplishment math = new Accomplishment();
        math.setUser(student);
        math.setCourseTitle("Advanced Mathematics");
        math.setSubject("Mathematics");
        math.setTeacherName("Dr. John Smith");
        math.setGrade(85.5);
        math.setCompletionDate(LocalDate.now().minusDays(30));
        accomplishmentRepository.save(math);
        
        Accomplishment physics = new Accomplishment();
        physics.setUser(student);
        physics.setCourseTitle("Classical Physics");
        physics.setSubject("Physics");
        physics.setTeacherName("Prof. Jane Doe");
        physics.setGrade(92.0);
        physics.setCompletionDate(LocalDate.now().minusDays(15));
        accomplishmentRepository.save(physics);
        
        Accomplishment programming = new Accomplishment();
        programming.setUser(student);
        programming.setCourseTitle("Java Programming");
        programming.setSubject("Computer Science");
        programming.setTeacherName("Mr. Bob Wilson");
        programming.setGrade(88.5);
        programming.setCompletionDate(LocalDate.now().minusDays(7));
        accomplishmentRepository.save(programming);
    }
}