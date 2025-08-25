package com.classroomapp.classroombackend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;

import java.util.List;
import java.util.Optional;

/**
 * Utility class để debug enrollment issues
 */
@Component
public class EnrollmentDebugUtil {
    
    private static final Logger log = LoggerFactory.getLogger(EnrollmentDebugUtil.class);
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;
    
    /**
     * Debug thông tin chi tiết về classroom và student cho enrollment
     */
    public void debugEnrollmentRequest(Long classroomId, Long studentId) {
        log.info("🔍 ENROLLMENT DEBUG: Starting debug for ClassroomId: {}, StudentId: {}", classroomId, studentId);
        
        // 1. Kiểm tra classroom
        debugClassroomInfo(classroomId);
        
        // 2. Kiểm tra student
        debugStudentInfo(studentId);
        
        // 3. Kiểm tra existing enrollments
        debugExistingEnrollments(classroomId, studentId);
        
        // 4. Kiểm tra database statistics
        debugDatabaseStats();
    }
    
    private void debugClassroomInfo(Long classroomId) {
        log.info("🔍 CLASSROOM DEBUG: Checking classroom with ID: {}", classroomId);
        
        try {
            Optional<Classroom> classroomOpt = classroomRepository.findById(classroomId);
            
            if (classroomOpt.isPresent()) {
                Classroom classroom = classroomOpt.get();
                log.info("✅ CLASSROOM FOUND: ID={}, Name='{}', CourseId={}, TeacherId={}", 
                    classroom.getId(), classroom.getName(), classroom.getCourseId(), 
                    classroom.getTeacher() != null ? classroom.getTeacher().getId() : "NULL");
                
                if (classroom.getTeacher() != null) {
                    log.info("✅ TEACHER INFO: ID={}, Name='{}', Username='{}'", 
                        classroom.getTeacher().getId(), 
                        classroom.getTeacher().getFullName(),
                        classroom.getTeacher().getUsername());
                } else {
                    log.warn("⚠️ CLASSROOM WARNING: No teacher assigned to classroom {}", classroomId);
                }
                
                if (classroom.getCourseId() == null) {
                    log.warn("⚠️ CLASSROOM WARNING: No courseId set for classroom {}", classroomId);
                }
                
            } else {
                log.error("❌ CLASSROOM NOT FOUND: ID={}", classroomId);
                
                // Kiểm tra classrooms gần đó
                List<Classroom> nearbyClassrooms = classroomRepository.findAll();
                log.error("❌ AVAILABLE CLASSROOM IDs: {}", 
                    nearbyClassrooms.stream()
                        .map(Classroom::getId)
                        .sorted()
                        .toList());
            }
            
        } catch (Exception e) {
            log.error("❌ CLASSROOM ERROR: Exception while checking classroom {}: {}", classroomId, e.getMessage(), e);
        }
    }
    
    private void debugStudentInfo(Long studentId) {
        log.info("🔍 STUDENT DEBUG: Checking student with ID: {}", studentId);
        
        try {
            Optional<User> studentOpt = userRepository.findById(studentId);
            
            if (studentOpt.isPresent()) {
                User student = studentOpt.get();
                log.info("✅ STUDENT FOUND: ID={}, Username='{}', FullName='{}', Role={}", 
                    student.getId(), student.getUsername(), student.getFullName(), student.getRoleId());
                
                if (student.getRoleId() != 1) {
                    log.warn("⚠️ STUDENT WARNING: User {} has role {} (expected role 1 for student)", 
                        studentId, student.getRoleId());
                }
                
            } else {
                log.error("❌ STUDENT NOT FOUND: ID={}", studentId);
                
                // Kiểm tra students có sẵn
                List<User> students = userRepository.findByRoleId(1);
                log.error("❌ AVAILABLE STUDENT IDs: {}", 
                    students.stream()
                        .map(User::getId)
                        .sorted()
                        .limit(10)
                        .toList());
            }
            
        } catch (Exception e) {
            log.error("❌ STUDENT ERROR: Exception while checking student {}: {}", studentId, e.getMessage(), e);
        }
    }
    
    private void debugExistingEnrollments(Long classroomId, Long studentId) {
        log.info("🔍 ENROLLMENT DEBUG: Checking existing enrollments");
        
        try {
            // Kiểm tra enrollment hiện tại của classroom
            var classroomEnrollments = classroomEnrollmentRepository.findByClassroomId(classroomId);
            log.info("📊 CLASSROOM ENROLLMENTS: Classroom {} has {} enrollments", 
                classroomId, classroomEnrollments.size());
            
            // Kiểm tra enrollment hiện tại của student
            var studentEnrollments = classroomEnrollmentRepository.findByUserId(studentId);
            log.info("📊 STUDENT ENROLLMENTS: Student {} is enrolled in {} classrooms", 
                studentId, studentEnrollments.size());
            
            // Kiểm tra specific enrollment
            var specificEnrollment = classroomEnrollmentRepository.findByClassroomIdAndUserId(classroomId, studentId);
            if (specificEnrollment.isPresent()) {
                log.info("⚠️ ALREADY ENROLLED: Student {} is already enrolled in classroom {}", 
                    studentId, classroomId);
            } else {
                log.info("✅ NOT ENROLLED: Student {} is not yet enrolled in classroom {}", 
                    studentId, classroomId);
            }
            
        } catch (Exception e) {
            log.error("❌ ENROLLMENT ERROR: Exception while checking enrollments: {}", e.getMessage(), e);
        }
    }
    
    private void debugDatabaseStats() {
        log.info("🔍 DATABASE DEBUG: Checking database statistics");
        
        try {
            long totalClassrooms = classroomRepository.count();
            long totalUsers = userRepository.count();
            long totalStudents = userRepository.countByRoleId(1);
            long totalEnrollments = classroomEnrollmentRepository.count();
            
            log.info("📊 DATABASE STATS: Classrooms={}, Users={}, Students={}, Enrollments={}", 
                totalClassrooms, totalUsers, totalStudents, totalEnrollments);
                
        } catch (Exception e) {
            log.error("❌ DATABASE ERROR: Exception while checking database stats: {}", e.getMessage(), e);
        }
    }
}
