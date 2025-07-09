package com.classroomapp.classroombackend.config.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class UserSeeder {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seed() {
        if (userRepository.count() == 0) {
            try {
                // Allow explicit ID insertion for SQL Server
                entityManager.createNativeQuery("SET IDENTITY_INSERT users ON").executeUpdate();

                // Create student user
                User student = new User();
                student.setId(101L);
                student.setUsername("student");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setEmail("student@test.com");
                student.setFullName("Student User");
                student.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student);

                // Create main teacher user
                User teacher = new User();
                teacher.setId(201L);
                teacher.setUsername("teacher");
                teacher.setPassword(passwordEncoder.encode("teacher123"));
                teacher.setEmail("teacher@test.com");
                teacher.setFullName("Teacher User");
                teacher.setRoleId(RoleConstants.TEACHER);
                userRepository.save(teacher);

                // Create manager user
                User manager = new User();
                manager.setId(301L);
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("manager123"));
                manager.setEmail("manager@test.com");
                manager.setFullName("Manager User");
                manager.setRoleId(RoleConstants.MANAGER);
                userRepository.save(manager);

                // Create admin user
                User admin = new User();
                admin.setId(401L);
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@test.com");
                admin.setFullName("Administrator");
                admin.setRoleId(RoleConstants.ADMIN);
                userRepository.save(admin);

                // Create subject-specific teachers
                User mathTeacher = new User();
                mathTeacher.setId(202L);
                mathTeacher.setUsername("math_teacher");
                mathTeacher.setPassword(passwordEncoder.encode("teacher123"));
                mathTeacher.setEmail("math@test.com");
                mathTeacher.setFullName("Nguyễn Văn Toán");
                mathTeacher.setRoleId(RoleConstants.TEACHER);
                userRepository.save(mathTeacher);

                User litTeacher = new User();
                litTeacher.setId(203L);
                litTeacher.setUsername("lit_teacher");
                litTeacher.setPassword(passwordEncoder.encode("teacher123"));
                litTeacher.setEmail("literature@test.com");
                litTeacher.setFullName("Trần Thị Văn");
                litTeacher.setRoleId(RoleConstants.TEACHER);
                userRepository.save(litTeacher);

                User engTeacher = new User();
                engTeacher.setId(204L);
                engTeacher.setUsername("eng_teacher");
                engTeacher.setPassword(passwordEncoder.encode("teacher123"));
                engTeacher.setEmail("english@test.com");
                engTeacher.setFullName("Lê Anh");
                engTeacher.setRoleId(RoleConstants.TEACHER);
                userRepository.save(engTeacher);

                // Create additional test students
                User student1 = new User();
                student1.setId(102L);
                student1.setUsername("student1");
                student1.setPassword(passwordEncoder.encode("student123"));
                student1.setEmail("student1@test.com");
                student1.setFullName("Phạm Văn Nam");
                student1.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student1);

                User student2 = new User();
                student2.setId(103L);
                student2.setUsername("student2");
                student2.setPassword(passwordEncoder.encode("student123"));
                student2.setEmail("student2@test.com");
                student2.setFullName("Alice Johnson");
                student2.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student2);

                User student3 = new User();
                student3.setId(104L);
                student3.setUsername("student3");
                student3.setPassword(passwordEncoder.encode("student123"));
                student3.setEmail("student3@test.com");
                student3.setFullName("Bob Wilson");
                student3.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student3);

                User student4 = new User();
                student4.setId(105L);
                student4.setUsername("student4");
                student4.setPassword(passwordEncoder.encode("student123"));
                student4.setEmail("student4@test.com");
                student4.setFullName("Carol Davis");
                student4.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student4);

                User student5 = new User();
                student5.setId(106L);
                student5.setUsername("student5");
                student5.setPassword(passwordEncoder.encode("student123"));
                student5.setEmail("student5@test.com");
                student5.setFullName("David Chen");
                student5.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student5);

                // Additional teachers
                User extraTeacher1 = new User();
                extraTeacher1.setId(205L);
                extraTeacher1.setUsername("teacher2");
                extraTeacher1.setPassword(passwordEncoder.encode("teacher123"));
                extraTeacher1.setEmail("teacher2@test.com");
                extraTeacher1.setFullName("Dr. Sarah Williams");
                extraTeacher1.setRoleId(RoleConstants.TEACHER);
                userRepository.save(extraTeacher1);

                User extraTeacher2 = new User();
                extraTeacher2.setId(206L);
                extraTeacher2.setUsername("teacher3");
                extraTeacher2.setPassword(passwordEncoder.encode("teacher123"));
                extraTeacher2.setEmail("teacher3@test.com");
                extraTeacher2.setFullName("Prof. Michael Brown");
                extraTeacher2.setRoleId(RoleConstants.TEACHER);
                userRepository.save(extraTeacher2);

                System.out.println("✅ [UserSeeder] Created users with standardized, explicit IDs.");

            } finally {
                // IMPORTANT: Disable explicit ID insertion
                entityManager.createNativeQuery("SET IDENTITY_INSERT users OFF").executeUpdate();
            }
        } else {
            System.out.println("✅ [UserSeeder] Users already seeded.");
        }
    }
} 