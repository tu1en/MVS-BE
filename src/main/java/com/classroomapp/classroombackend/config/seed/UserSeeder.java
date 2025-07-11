package com.classroomapp.classroombackend.config.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import java.time.LocalDate;

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
                teacher.setFullName("Nguyễn Văn Minh");
                teacher.setRoleId(RoleConstants.TEACHER);
                teacher.setPhoneNumber("0912345678");
                teacher.setDepartment("Khoa Công Nghệ Thông Tin");
                teacher.setHireDate(LocalDate.now().minusYears(2));
                teacher.setAnnualLeaveBalance(12);
                teacher.setLeaveResetDate(LocalDate.now().plusMonths(6)); // Reset in 6 months
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
                mathTeacher.setFullName("Trần Văn Đức");
                mathTeacher.setRoleId(RoleConstants.TEACHER);
                mathTeacher.setPhoneNumber("0987654321");
                mathTeacher.setDepartment("Khoa Toán Học");
                mathTeacher.setHireDate(LocalDate.now().minusYears(3));
                mathTeacher.setAnnualLeaveBalance(4); // Đã dùng 8 ngày
                mathTeacher.setLeaveResetDate(LocalDate.now().plusMonths(8));
                userRepository.save(mathTeacher);

                User litTeacher = new User();
                litTeacher.setId(203L);
                litTeacher.setUsername("lit_teacher");
                litTeacher.setPassword(passwordEncoder.encode("teacher123"));
                litTeacher.setEmail("literature@test.com");
                litTeacher.setFullName("Phạm Thị Lan");
                litTeacher.setRoleId(RoleConstants.TEACHER);
                litTeacher.setPhoneNumber("0976543210");
                litTeacher.setDepartment("Khoa Ngữ Văn");
                litTeacher.setHireDate(LocalDate.now().minusYears(1));
                litTeacher.setAnnualLeaveBalance(-3); // Đã vượt phép (dùng 15 ngày)
                litTeacher.setLeaveResetDate(LocalDate.now().plusMonths(3));
                userRepository.save(litTeacher);

                User engTeacher = new User();
                engTeacher.setId(204L);
                engTeacher.setUsername("eng_teacher");
                engTeacher.setPassword(passwordEncoder.encode("teacher123"));
                engTeacher.setEmail("english@test.com");
                engTeacher.setFullName("Lê Hoàng Nam");
                engTeacher.setRoleId(RoleConstants.TEACHER);
                engTeacher.setPhoneNumber("0965432109");
                engTeacher.setDepartment("Khoa Ngoại Ngữ");
                engTeacher.setHireDate(LocalDate.now().minusYears(4));
                engTeacher.setAnnualLeaveBalance(8); // Đã dùng 4 ngày
                engTeacher.setLeaveResetDate(LocalDate.now().plusMonths(10));
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
                extraTeacher1.setFullName("Vũ Thị Hương");
                extraTeacher1.setRoleId(RoleConstants.TEACHER);
                extraTeacher1.setPhoneNumber("0954321098");
                extraTeacher1.setDepartment("Khoa Hóa Học");
                extraTeacher1.setHireDate(LocalDate.now().minusYears(5));
                extraTeacher1.setAnnualLeaveBalance(9); // Đã dùng 3 ngày
                extraTeacher1.setLeaveResetDate(LocalDate.now().plusMonths(4));
                userRepository.save(extraTeacher1);

                User extraTeacher2 = new User();
                extraTeacher2.setId(206L);
                extraTeacher2.setUsername("teacher3");
                extraTeacher2.setPassword(passwordEncoder.encode("teacher123"));
                extraTeacher2.setEmail("teacher3@test.com");
                extraTeacher2.setFullName("Đặng Minh Tuấn");
                extraTeacher2.setRoleId(RoleConstants.TEACHER);
                extraTeacher2.setPhoneNumber("0943210987");
                extraTeacher2.setDepartment("Khoa Vật Lý");
                extraTeacher2.setHireDate(LocalDate.now().minusMonths(6)); // Mới vào 6 tháng
                extraTeacher2.setAnnualLeaveBalance(12); // Chưa dùng ngày nào
                extraTeacher2.setLeaveResetDate(LocalDate.now().plusMonths(6));
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