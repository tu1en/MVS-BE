package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class DataVerificationSeeder {
    
    private static final Logger log = LoggerFactory.getLogger(DataVerificationSeeder.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;
    
    @Autowired
    private LectureRepository lectureRepository;
    
    public void verifyDataIntegrity() {
        log.info("🔍 ============== DATA VERIFICATION STARTED ==============");
        
        verifyUsers();
        verifyClassrooms();
        verifyEnrollments();
        verifyLectures();
        
        log.info("✅ ============== DATA VERIFICATION COMPLETED ==============");
    }
    
    private void verifyUsers() {
        log.info("👥 Verifying Users...");
        
        // Check critical test users
        User student = userRepository.findByEmail("student@test.com").orElse(null);
        User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
        User mathTeacher = userRepository.findByEmail("math@test.com").orElse(null);
        
        if (student != null) {
            log.info("✅ Student User: ID={}, Email={}, Role={}, RoleId={}", 
                student.getId(), student.getEmail(), student.getRole(), student.getRoleId());
            
            if (student.getRoleId() != 1) {
                log.error("❌ CRITICAL: Student user has wrong role! Expected: 1 (STUDENT), Actual: {}", student.getRoleId());
            }
        } else {
            log.error("❌ CRITICAL: Student user not found!");
        }
        
        if (teacher != null) {
            log.info("✅ Main Teacher: ID={}, Email={}, Role={}, RoleId={}", 
                teacher.getId(), teacher.getEmail(), teacher.getRole(), teacher.getRoleId());
                
            if (teacher.getRoleId() != 2) {
                log.error("❌ CRITICAL: Teacher user has wrong role! Expected: 2 (TEACHER), Actual: {}", teacher.getRoleId());
            }
        } else {
            log.error("❌ CRITICAL: Main teacher user not found!");
        }
        
        if (mathTeacher != null) {
            log.info("✅ Math Teacher: ID={}, Email={}, Role={}, RoleId={}", 
                mathTeacher.getId(), mathTeacher.getEmail(), mathTeacher.getRole(), mathTeacher.getRoleId());
        } else {
            log.warn("⚠️ Math teacher user not found!");
        }
        
        // Count users by role
        List<User> students = userRepository.findByRoleId(1);
        List<User> teachers = userRepository.findByRoleId(2);
        List<User> managers = userRepository.findByRoleId(3);
        List<User> admins = userRepository.findByRoleId(4);
        
        log.info("📊 User counts: Students={}, Teachers={}, Managers={}, Admins={}", 
            students.size(), teachers.size(), managers.size(), admins.size());
    }
    
    private void verifyClassrooms() {
        log.info("🏫 Verifying Classrooms...");
        
        List<Classroom> classrooms = classroomRepository.findAll();
        log.info("📚 Found {} classrooms:", classrooms.size());
        
        for (Classroom classroom : classrooms) {
            User classroomTeacher = classroom.getTeacher();
            log.info("   - Classroom: {} (ID={}), Teacher: {} (ID={}), TeacherRole: {}", 
                classroom.getName(), 
                classroom.getId(),
                classroomTeacher != null ? classroomTeacher.getFullName() : "NULL",
                classroomTeacher != null ? classroomTeacher.getId() : "NULL",
                classroomTeacher != null ? classroomTeacher.getRole() : "NULL");
                
            if (classroomTeacher != null && classroomTeacher.getRoleId() != 2) {
                log.error("❌ CRITICAL: Classroom {} has teacher with wrong role! Expected: 2 (TEACHER), Actual: {}", 
                    classroom.getName(), classroomTeacher.getRoleId());
            }
        }
        
        // Find specific classroom for testing
        Classroom mathClass = classrooms.stream()
            .filter(c -> c.getName().contains("Toán"))
            .findFirst()
            .orElse(null);
            
        if (mathClass != null) {
            log.info("✅ Found Math classroom for testing: {} (ID={})", mathClass.getName(), mathClass.getId());
        } else {
            log.error("❌ CRITICAL: Math classroom not found! This will cause lecture loading issues.");
        }
    }
    
    private void verifyEnrollments() {
        log.info("📝 Verifying Enrollments...");
        
        User student = userRepository.findByEmail("student@test.com").orElse(null);
        if (student != null) {
            List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByUserId(student.getId());
            log.info("🎓 Student {} is enrolled in {} classrooms:", student.getEmail(), enrollments.size());
            
            for (ClassroomEnrollment enrollment : enrollments) {
                Classroom classroom = enrollment.getClassroom();
                log.info("   - Enrolled in: {} (ID={})", classroom.getName(), classroom.getId());
            }
            
            if (enrollments.isEmpty()) {
                log.error("❌ CRITICAL: Student is not enrolled in any classrooms! This will cause empty course lists.");
            }
        }
    }
    
    private void verifyLectures() {
        log.info("📖 Verifying Lectures...");
        
        List<Lecture> allLectures = lectureRepository.findAll();
        log.info("📚 Found {} total lectures", allLectures.size());
        
        // Check lectures by classroom
        List<Classroom> classrooms = classroomRepository.findAll();
        for (Classroom classroom : classrooms) {
            List<Lecture> classroomLectures = lectureRepository.findByClassroomId(classroom.getId());
            log.info("   - Classroom {} has {} lectures", classroom.getName(), classroomLectures.size());
            
            for (Lecture lecture : classroomLectures) {
                log.info("     * Lecture: {} (ID={})", lecture.getTitle(), lecture.getId());
            }
        }
        
        if (allLectures.isEmpty()) {
            log.error("❌ CRITICAL: No lectures found! This will cause empty lecture lists in frontend.");
        }
        
        // Specific check for Math classroom lectures
        Classroom mathClass = classrooms.stream()
            .filter(c -> c.getName().contains("Toán"))
            .findFirst()
            .orElse(null);

        if (mathClass != null) {
            List<Lecture> mathLectures = lectureRepository.findByClassroomId(mathClass.getId());
            log.info("🧮 Math classroom (ID={}) has {} lectures (this is what frontend will load)",
                mathClass.getId(), mathLectures.size());

            if (mathLectures.isEmpty()) {
                log.error("❌ CRITICAL: Math classroom (ID={}) has no lectures! Frontend will show empty list.", mathClass.getId());
                log.error("   This is the ROOT CAUSE of the empty lecture list issue!");
                log.error("   LectureSeeder may have failed or not run properly.");
            } else {
                log.info("✅ Math classroom lectures found:");
                for (Lecture lecture : mathLectures) {
                    log.info("   - {} (ID={}, Date={})", lecture.getTitle(), lecture.getId(), lecture.getLectureDate());
                }
            }
        } else {
            log.error("❌ CRITICAL: Math classroom not found! Available classrooms:");
            for (Classroom c : classrooms) {
                log.error("   - {} (ID={})", c.getName(), c.getId());
            }
        }
    }
    
    public void diagnoseStudentTeacherIssue() {
        log.info("🔍 ============== DIAGNOSING STUDENT/TEACHER ISSUE ==============");
        
        User student = userRepository.findByEmail("student@test.com").orElse(null);
        if (student == null) {
            log.error("❌ Student user not found!");
            return;
        }
        
        log.info("👤 Student Details:");
        log.info("   - ID: {}", student.getId());
        log.info("   - Email: {}", student.getEmail());
        log.info("   - Username: {}", student.getUsername());
        log.info("   - Role: {}", student.getRole());
        log.info("   - RoleId: {}", student.getRoleId());
        
        // Check if student is mistakenly assigned as teacher to any classroom
        List<Classroom> classroomsAsTeacher = classroomRepository.findByTeacherId(student.getId());
        if (!classroomsAsTeacher.isEmpty()) {
            log.error("❌ CRITICAL BUG: Student is assigned as TEACHER to {} classrooms!", classroomsAsTeacher.size());
            for (Classroom classroom : classroomsAsTeacher) {
                log.error("   - Student is teacher of: {} (ID={})", classroom.getName(), classroom.getId());
            }
        } else {
            log.info("✅ Student is correctly NOT assigned as teacher to any classroom");
        }
        
        // Check student enrollments
        List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByUserId(student.getId());
        log.info("📚 Student is enrolled in {} classrooms:", enrollments.size());
        for (ClassroomEnrollment enrollment : enrollments) {
            Classroom classroom = enrollment.getClassroom();
            log.info("   - Enrolled as STUDENT in: {} (ID={})", classroom.getName(), classroom.getId());
        }
        
        log.info("✅ ============== DIAGNOSIS COMPLETED ==============");
    }
}
