package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

/**
 * Improved User Seeder theo pattern mới
 * - Không dùng hardcoded IDs
 * - Better validation và error handling
 * - Flexible configuration
 * - Comprehensive verification
 */
@Component
public class ImprovedUserSeeder extends AbstractSeeder<User> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Configuration constants
    private static final int DEFAULT_STUDENT_COUNT = 20;
    private static final int DEFAULT_TEACHER_COUNT = 5;
    private static final int DEFAULT_MANAGER_COUNT = 2;
    private static final int DEFAULT_ADMIN_COUNT = 1;
    private static final String DEFAULT_PASSWORD = "password123";
    
    @Override
    public boolean shouldSeed() {
        return userRepository.count() == 0;
    }
    
    @Override
    public List<User> getExistingData() {
        return userRepository.findAll();
    }
    
    @Override
    public int getExpectedCount() {
        return DEFAULT_STUDENT_COUNT + DEFAULT_TEACHER_COUNT + 
               DEFAULT_MANAGER_COUNT + DEFAULT_ADMIN_COUNT;
    }
    
    @Override
    @Transactional
    protected List<User> doSeed() {
        List<User> users = new ArrayList<>();
        
        logProgress("Creating core users...");
        
        // Tạo core users trước
        users.addAll(createCoreUsers());
        
        logProgress("Creating bulk students...");
        users.addAll(createBulkStudents(DEFAULT_STUDENT_COUNT));
        
        logProgress("Creating bulk teachers...");
        users.addAll(createBulkTeachers(DEFAULT_TEACHER_COUNT));
        
        logProgress("Creating managers...");
        users.addAll(createManagers(DEFAULT_MANAGER_COUNT));
        
        logSuccess("Created {} users total", users.size());
        return users;
    }
    
    /**
     * Tạo core users (admin, main teacher, main student)
     */
    private List<User> createCoreUsers() {
        List<User> coreUsers = new ArrayList<>();
        
        // Admin user
        User admin = createUser("admin", "admin@test.com", "Administrator", 
            RoleConstants.ADMIN, null, LocalDate.now());
        coreUsers.add(userRepository.save(admin));
        
        // Main teacher
        User teacher = createUser("teacher", "teacher@test.com", "Main Teacher", 
            RoleConstants.TEACHER, null, LocalDate.now());
        coreUsers.add(userRepository.save(teacher));
        
        // Main student
        User student = createUser("student", "student@test.com", "Main Student", 
            RoleConstants.STUDENT, LocalDate.now(), null);
        coreUsers.add(userRepository.save(student));
        
        return coreUsers;
    }
    
    /**
     * Tạo bulk students với generated data
     */
    private List<User> createBulkStudents(int count) {
        return IntStream.range(1, count + 1)
            .mapToObj(i -> {
                String username = "student" + String.format("%02d", i);
                String email = "student" + i + "@test.com";
                String fullName = "Sinh viên " + String.format("%02d", i);
                
                User student = createUser(username, email, fullName, 
                    RoleConstants.STUDENT, LocalDate.now().minusYears(1), null);
                student.setDepartment("Khoa Công nghệ thông tin");
                
                return userRepository.save(student);
            })
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Tạo bulk teachers với generated data
     */
    private List<User> createBulkTeachers(int count) {
        String[] subjects = {"Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh", "Sử", "Địa"};
        
        return IntStream.range(1, count + 1)
            .mapToObj(i -> {
                String subject = subjects[(i - 1) % subjects.length];
                String username = subject.toLowerCase() + "_teacher" + i;
                String email = subject.toLowerCase() + ".teacher" + i + "@test.com";
                String fullName = "Giáo viên " + subject + " " + String.format("%02d", i);
                
                User teacher = createUser(username, email, fullName, 
                    RoleConstants.TEACHER, null, LocalDate.now().minusYears(2));
                teacher.setDepartment("Khoa " + subject);
                
                return userRepository.save(teacher);
            })
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Tạo managers
     */
    private List<User> createManagers(int count) {
        return IntStream.range(1, count + 1)
            .mapToObj(i -> {
                String username = "manager" + i;
                String email = "manager" + i + "@test.com";
                String fullName = "Quản lý " + String.format("%02d", i);
                
                User manager = createUser(username, email, fullName, 
                    RoleConstants.MANAGER, null, LocalDate.now().minusYears(3));
                manager.setDepartment("Phòng Đào tạo");
                
                return userRepository.save(manager);
            })
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Helper method để tạo user với validation
     */
    private User createUser(String username, String email, String fullName, 
                           int roleId, LocalDate enrollmentDate, LocalDate hireDate) {
        
        // Validation
        validateNotNull(username, "username");
        validateNotNull(email, "email");
        validateNotNull(fullName, "fullName");
        
        if (!email.contains("@")) {
            throw new SeedingException("Invalid email format: " + email);
        }
        
        // Check duplicate username
        if (userRepository.findByUsername(username).isPresent()) {
            throw new SeedingException("Username already exists: " + username);
        }
        
        // Check duplicate email
        if (userRepository.findByEmail(email).isPresent()) {
            throw new SeedingException("Email already exists: " + email);
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRoleId(roleId);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setStatus("active");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        if (enrollmentDate != null) {
            user.setEnrollmentDate(enrollmentDate);
        }
        
        if (hireDate != null) {
            user.setHireDate(hireDate);
        }
        
        return user;
    }
    
    @Override
    protected void doAdditionalVerification(List<User> users) {
        // Verify role distribution
        long adminCount = users.stream().filter(u -> u.getRoleId() == RoleConstants.ADMIN).count();
        long teacherCount = users.stream().filter(u -> u.getRoleId() == RoleConstants.TEACHER).count();
        long studentCount = users.stream().filter(u -> u.getRoleId() == RoleConstants.STUDENT).count();
        long managerCount = users.stream().filter(u -> u.getRoleId() == RoleConstants.MANAGER).count();
        
        logProgress("Role distribution - Admin: {}, Teacher: {}, Student: {}, Manager: {}", 
            adminCount, teacherCount, studentCount, managerCount);
        
        // Verify minimum requirements
        if (adminCount == 0) {
            throw new SeedingException("No admin users created");
        }
        
        if (teacherCount == 0) {
            throw new SeedingException("No teacher users created");
        }
        
        if (studentCount == 0) {
            throw new SeedingException("No student users created");
        }
        
        // Verify all users have required fields
        for (User user : users) {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                throw new SeedingException("User without username found: " + user.getId());
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new SeedingException("User without email found: " + user.getId());
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                throw new SeedingException("User without password found: " + user.getId());
            }
        }
        
        logSuccess("Additional verification passed for {} users", users.size());
    }
    
    @Override
    public void cleanup() {
        logWarning("Cleaning up all users...");
        userRepository.deleteAll();
        logSuccess("User cleanup completed");
    }
}
