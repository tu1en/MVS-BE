package com.classroomapp.classroombackend.config;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.model.StudentParent.RelationType;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data seeder for Zalo notification testing
 * Creates test data including classroom, students, parents, and relationships
 * Only runs in 'dev' or 'test' profiles
 */
@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class ZaloNotificationTestSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    // Test data constants
    private static final String TEST_CLASSROOM_NAME = "Lớp Test Zalo Notification";
    private static final String TEST_TEACHER_USERNAME = "teacher_zalo_test";
    private static final String TEST_STUDENT1_USERNAME = "student_zalo_test_1";
    private static final String TEST_STUDENT2_USERNAME = "student_zalo_test_2";
    private static final String TEST_PARENT1_USERNAME = "parent_zalo_test_1";
    private static final String TEST_PARENT2_USERNAME = "parent_zalo_test_2";
    
    // Real phone numbers for Zalo testing
    private static final String PARENT1_PHONE = "0971335989";
    private static final String PARENT2_PHONE = "0859326040";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("🚀 Starting Zalo Notification Test Data Seeder...");
        
        try {
            // Check if test data already exists
            if (isTestDataExists()) {
                log.info("✅ Zalo test data already exists, skipping seeder");
                return;
            }
            
            // Create test data
            User teacher = createTestTeacher();
            Classroom classroom = createTestClassroom(teacher);
            
            User student1 = createTestStudent1();
            User student2 = createTestStudent2();
            
            Parent parent1 = createTestParent1();
            Parent parent2 = createTestParent2();
            
            // Create relationships
            createStudentParentRelationship(student1, parent1);
            createStudentParentRelationship(student2, parent2);
            
            // Enroll students in classroom
            enrollStudentInClassroom(student1, classroom);
            enrollStudentInClassroom(student2, classroom);
            
            log.info("✅ Zalo Notification Test Data Seeder completed successfully!");
            logTestDataSummary(classroom, teacher, student1, student2, parent1, parent2);
            
        } catch (Exception e) {
            log.error("❌ Error in Zalo Notification Test Data Seeder: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean isTestDataExists() {
        return classroomRepository.findByNameContainingIgnoreCase(TEST_CLASSROOM_NAME).size() > 0 ||
               userRepository.findByUsername(TEST_TEACHER_USERNAME).isPresent();
    }

    private User createTestTeacher() {
        Optional<User> existingTeacher = userRepository.findByUsername(TEST_TEACHER_USERNAME);
        if (existingTeacher.isPresent()) {
            log.info("📚 Test teacher already exists: {}", TEST_TEACHER_USERNAME);
            return existingTeacher.get();
        }

        User teacher = new User();
        teacher.setUsername(TEST_TEACHER_USERNAME);
        teacher.setPassword(passwordEncoder.encode("password123"));
        teacher.setEmail("teacher.zalo.test@mvs.edu.vn");
        teacher.setFullName("Nguyễn Văn Minh");
        teacher.setPhoneNumber("0912345678");
        teacher.setRoleId(RoleConstants.TEACHER);
        teacher.setGender("MALE");
        teacher.setHireDate(LocalDate.now());
        teacher.setDepartment("Toán học");
        teacher.setCreatedAt(LocalDateTime.now());
        teacher.setUpdatedAt(LocalDateTime.now());
        teacher.setStatus("active");

        teacher = userRepository.save(teacher);
        log.info("👨‍🏫 Created test teacher: {} (ID: {})", teacher.getFullName(), teacher.getId());
        return teacher;
    }

    private Classroom createTestClassroom(User teacher) {
        // Check if classroom already exists
        Optional<Classroom> existingClassroom = classroomRepository
                .findByNameContainingIgnoreCase(TEST_CLASSROOM_NAME)
                .stream()
                .findFirst();
        
        if (existingClassroom.isPresent()) {
            log.info("🏫 Test classroom already exists: {}", TEST_CLASSROOM_NAME);
            return existingClassroom.get();
        }

        Classroom classroom = new Classroom();
        classroom.setName(TEST_CLASSROOM_NAME);
        classroom.setDescription("Lớp học test cho chức năng thông báo Zalo qua n8n workflow");
        classroom.setSubject("Toán học");
        classroom.setSection("8A");
        classroom.setTeacher(teacher);

        classroom = classroomRepository.save(classroom);
        log.info("🏫 Created test classroom: {} (ID: {})", classroom.getName(), classroom.getId());
        return classroom;
    }

    private User createTestStudent1() {
        Optional<User> existingStudent = userRepository.findByUsername(TEST_STUDENT1_USERNAME);
        if (existingStudent.isPresent()) {
            log.info("👨‍🎓 Test student 1 already exists: {}", TEST_STUDENT1_USERNAME);
            return existingStudent.get();
        }

        User student = new User();
        student.setUsername(TEST_STUDENT1_USERNAME);
        student.setPassword(passwordEncoder.encode("password123"));
        student.setEmail("tran.van.nam@student.mvs.edu.vn");
        student.setFullName("Trần Văn Nam");
        student.setPhoneNumber("0987654321");
        student.setRoleId(RoleConstants.STUDENT);
        student.setGender("MALE");
        student.setBirthDate(LocalDate.of(2010, 5, 15));
        student.setSchool("Trường THCS ABC");
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        student.setStatus("active");

        student = userRepository.save(student);
        log.info("👨‍🎓 Created test student 1: {} (ID: {})", student.getFullName(), student.getId());
        return student;
    }

    private User createTestStudent2() {
        Optional<User> existingStudent = userRepository.findByUsername(TEST_STUDENT2_USERNAME);
        if (existingStudent.isPresent()) {
            log.info("👨‍🎓 Test student 2 already exists: {}", TEST_STUDENT2_USERNAME);
            return existingStudent.get();
        }

        User student = new User();
        student.setUsername(TEST_STUDENT2_USERNAME);
        student.setPassword(passwordEncoder.encode("password123"));
        student.setEmail("le.van.minh@student.mvs.edu.vn");
        student.setFullName("Lê Văn Minh");
        student.setPhoneNumber("0976543210");
        student.setRoleId(RoleConstants.STUDENT);
        student.setGender("MALE");
        student.setBirthDate(LocalDate.of(2010, 8, 20));
        student.setSchool("Trường THCS XYZ");
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        student.setStatus("active");

        student = userRepository.save(student);
        log.info("👨‍🎓 Created test student 2: {} (ID: {})", student.getFullName(), student.getId());
        return student;
    }

    private Parent createTestParent1() {
        Optional<User> existingParentUser = userRepository.findByUsername(TEST_PARENT1_USERNAME);
        if (existingParentUser.isPresent()) {
            Optional<Parent> existingParent = parentRepository.findByUserId(existingParentUser.get().getId());
            if (existingParent.isPresent()) {
                log.info("👩‍👧‍👦 Test parent 1 already exists: {}", TEST_PARENT1_USERNAME);
                return existingParent.get();
            }
        }

        // Create parent user account
        User parentUser = new User();
        parentUser.setUsername(TEST_PARENT1_USERNAME);
        parentUser.setPassword(passwordEncoder.encode("password123"));
        parentUser.setEmail("tran.thi.huong@parent.mvs.edu.vn");
        parentUser.setFullName("Trần Thị Hương");
        parentUser.setPhoneNumber(PARENT1_PHONE);
        parentUser.setRoleId(RoleConstants.PARENT);
        parentUser.setGender("FEMALE");
        parentUser.setCreatedAt(LocalDateTime.now());
        parentUser.setUpdatedAt(LocalDateTime.now());
        parentUser.setStatus("active");

        parentUser = userRepository.save(parentUser);

        // Create parent profile
        Parent parent = new Parent();
        parent.setUserId(parentUser.getId());
        parent.setName("Trần Thị Hương");
        parent.setPhone(PARENT1_PHONE);
        parent.setEmail("tran.thi.huong@parent.mvs.edu.vn");
        parent.setStatus(Parent.ParentStatus.ACTIVE);
        parent.setCreatedAt(LocalDateTime.now());
        parent.setUpdatedAt(LocalDateTime.now());

        parent = parentRepository.save(parent);
        log.info("👩‍👧‍👦 Created test parent 1: {} (ID: {}, Phone: {})", 
                parent.getName(), parent.getId(), parent.getPhone());
        return parent;
    }

    private Parent createTestParent2() {
        Optional<User> existingParentUser = userRepository.findByUsername(TEST_PARENT2_USERNAME);
        if (existingParentUser.isPresent()) {
            Optional<Parent> existingParent = parentRepository.findByUserId(existingParentUser.get().getId());
            if (existingParent.isPresent()) {
                log.info("👩‍👧‍👦 Test parent 2 already exists: {}", TEST_PARENT2_USERNAME);
                return existingParent.get();
            }
        }

        // Create parent user account
        User parentUser = new User();
        parentUser.setUsername(TEST_PARENT2_USERNAME);
        parentUser.setPassword(passwordEncoder.encode("password123"));
        parentUser.setEmail("le.thi.mai@parent.mvs.edu.vn");
        parentUser.setFullName("Lê Thị Mai");
        parentUser.setPhoneNumber(PARENT2_PHONE);
        parentUser.setRoleId(RoleConstants.PARENT);
        parentUser.setGender("FEMALE");
        parentUser.setCreatedAt(LocalDateTime.now());
        parentUser.setUpdatedAt(LocalDateTime.now());
        parentUser.setStatus("active");

        parentUser = userRepository.save(parentUser);

        // Create parent profile
        Parent parent = new Parent();
        parent.setUserId(parentUser.getId());
        parent.setName("Lê Thị Mai");
        parent.setPhone(PARENT2_PHONE);
        parent.setEmail("le.thi.mai@parent.mvs.edu.vn");
        parent.setStatus(Parent.ParentStatus.ACTIVE);
        parent.setCreatedAt(LocalDateTime.now());
        parent.setUpdatedAt(LocalDateTime.now());

        parent = parentRepository.save(parent);
        log.info("👩‍👧‍👦 Created test parent 2: {} (ID: {}, Phone: {})",
                parent.getName(), parent.getId(), parent.getPhone());
        return parent;
    }

    private void createStudentParentRelationship(User student, Parent parent) {
        // Check if relationship already exists
        Optional<StudentParent> existingRelationship = studentParentRepository
                .findActiveRelationship(parent.getId(), student.getId());

        if (existingRelationship.isPresent()) {
            log.info("👨‍👩‍👧‍👦 Student-Parent relationship already exists: {} -> {}",
                    student.getFullName(), parent.getName());
            return;
        }

        StudentParent relationship = new StudentParent();
        relationship.setStudentId(student.getId());
        relationship.setParentId(parent.getId());
        relationship.setRelationType(RelationType.MOTHER); // Default to MOTHER
        relationship.setIsPrimary(true);
        relationship.setLegalGuardian(true);
        relationship.setStartAt(LocalDate.now());
        relationship.setCreatedAt(LocalDateTime.now());
        relationship.setUpdatedAt(LocalDateTime.now());

        relationship = studentParentRepository.save(relationship);
        log.info("👨‍👩‍👧‍👦 Created student-parent relationship: {} -> {} (ID: {})",
                student.getFullName(), parent.getName(), relationship.getId());
    }

    private void enrollStudentInClassroom(User student, Classroom classroom) {
        // Check if enrollment already exists
        Optional<ClassroomEnrollment> existingEnrollment = classroomEnrollmentRepository
                .findByClassroomAndUser(classroom, student);

        if (existingEnrollment.isPresent()) {
            log.info("📚 Student already enrolled: {} in {}",
                    student.getFullName(), classroom.getName());
            return;
        }

        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroom.getId(), student.getId());

        ClassroomEnrollment enrollment = new ClassroomEnrollment();
        enrollment.setId(enrollmentId);
        enrollment.setClassroom(classroom);
        enrollment.setUser(student);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        enrollment = classroomEnrollmentRepository.save(enrollment);
        log.info("📚 Enrolled student: {} in {} (Enrollment ID: {}-{})",
                student.getFullName(), classroom.getName(),
                enrollment.getId().getClassroomId(), enrollment.getId().getUserId());
    }

    private void logTestDataSummary(Classroom classroom, User teacher,
                                   User student1, User student2,
                                   Parent parent1, Parent parent2) {
        log.info("📋 ===== ZALO NOTIFICATION TEST DATA SUMMARY =====");
        log.info("🏫 Classroom: {} (ID: {})", classroom.getName(), classroom.getId());
        log.info("👨‍🏫 Teacher: {} (ID: {})", teacher.getFullName(), teacher.getId());
        log.info("👨‍🎓 Student 1: {} (ID: {})", student1.getFullName(), student1.getId());
        log.info("👨‍🎓 Student 2: {} (ID: {})", student2.getFullName(), student2.getId());
        log.info("👩‍👧‍👦 Parent 1: {} (ID: {}, Phone: {})", parent1.getName(), parent1.getId(), parent1.getPhone());
        log.info("👩‍👧‍👦 Parent 2: {} (ID: {}, Phone: {})", parent2.getName(), parent2.getId(), parent2.getPhone());
        log.info("📱 Zalo Test Phones: {}, {}", PARENT1_PHONE, PARENT2_PHONE);
        log.info("🎯 Ready for Zalo notification testing!");
        log.info("💡 To test: Submit attendance for classroom ID {} and check Zalo messages", classroom.getId());
        log.info("📋 ============================================");
    }
}
