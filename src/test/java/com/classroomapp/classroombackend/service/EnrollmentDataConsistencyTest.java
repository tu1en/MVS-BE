package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test để kiểm tra tính nhất quán giữa các methods lấy dữ liệu enrollment
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EnrollmentDataConsistencyTest {

    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private ClassroomEnrollmentRepository enrollmentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testEnrollmentDataConsistency() {
        // Lấy tất cả classrooms có enrollments
        List<Classroom> classrooms = classroomRepository.findAll();
        
        for (Classroom classroom : classrooms) {
            Long classroomId = classroom.getId();
            
            // Method 1: Sử dụng JPQL query trực tiếp
            Set<Long> studentIdsFromQuery = enrollmentRepository.findStudentIdsByClassroomId(classroomId);
            
            // Method 2: Sử dụng findById_ClassroomId và extract user IDs
            List<ClassroomEnrollment> enrollments = enrollmentRepository.findById_ClassroomId(classroomId);
            Set<Long> studentIdsFromEnrollments = enrollments.stream()
                .map(e -> e.getUser().getId())
                .collect(java.util.stream.Collectors.toSet());
            
            // Method 3: Sử dụng classroom.getStudents() (lazy loading)
            Set<User> studentsFromClassroom = classroom.getStudents();
            Set<Long> studentIdsFromClassroom = studentsFromClassroom.stream()
                .map(User::getId)
                .collect(java.util.stream.Collectors.toSet());
            
            // Kiểm tra tính nhất quán
            System.out.println("=== Classroom ID: " + classroomId + " ===");
            System.out.println("Method 1 (JPQL Query): " + studentIdsFromQuery.size() + " students");
            System.out.println("Method 2 (Enrollments): " + studentIdsFromEnrollments.size() + " students");
            System.out.println("Method 3 (Lazy Loading): " + studentIdsFromClassroom.size() + " students");
            
            // Kiểm tra xem các sets có bằng nhau không
            if (!studentIdsFromQuery.equals(studentIdsFromEnrollments)) {
                System.out.println("❌ INCONSISTENCY: Query vs Enrollments");
                System.out.println("Query IDs: " + studentIdsFromQuery);
                System.out.println("Enrollment IDs: " + studentIdsFromEnrollments);
            }
            
            if (!studentIdsFromQuery.equals(studentIdsFromClassroom)) {
                System.out.println("❌ INCONSISTENCY: Query vs Classroom.getStudents()");
                System.out.println("Query IDs: " + studentIdsFromQuery);
                System.out.println("Classroom IDs: " + studentIdsFromClassroom);
            }
            
            if (!studentIdsFromEnrollments.equals(studentIdsFromClassroom)) {
                System.out.println("❌ INCONSISTENCY: Enrollments vs Classroom.getStudents()");
                System.out.println("Enrollment IDs: " + studentIdsFromEnrollments);
                System.out.println("Classroom IDs: " + studentIdsFromClassroom);
            }
            
            // Assert để test fail nếu có inconsistency
            assertEquals(studentIdsFromQuery, studentIdsFromEnrollments, 
                "Query method và Enrollment method phải trả về cùng kết quả cho classroom " + classroomId);
            assertEquals(studentIdsFromQuery, studentIdsFromClassroom, 
                "Query method và Classroom.getStudents() phải trả về cùng kết quả cho classroom " + classroomId);
        }
    }
    
    @Test
    public void testSpecificClassroomConsistency() {
        // Test với classroom cụ thể từ screenshot (có vẻ như classroom có 0 học viên nhưng có submissions)
        List<Classroom> classrooms = classroomRepository.findAll();
        
        for (Classroom classroom : classrooms) {
            Long classroomId = classroom.getId();
            
            // Đếm enrollments
            long enrollmentCount = enrollmentRepository.findById_ClassroomId(classroomId).size();
            
            // Đếm students từ classroom entity
            int studentCount = classroom.getStudents().size();
            
            // Đếm từ optimized query
            int queryCount = enrollmentRepository.findStudentIdsByClassroomId(classroomId).size();
            
            System.out.println("Classroom " + classroomId + ":");
            System.out.println("  - Enrollment count: " + enrollmentCount);
            System.out.println("  - Student count (lazy): " + studentCount);
            System.out.println("  - Query count: " + queryCount);
            
            if (enrollmentCount != studentCount || enrollmentCount != queryCount) {
                System.out.println("  ❌ INCONSISTENCY DETECTED!");
                
                // Debug thêm thông tin
                List<ClassroomEnrollment> enrollments = enrollmentRepository.findById_ClassroomId(classroomId);
                System.out.println("  - Enrollment details:");
                for (ClassroomEnrollment enrollment : enrollments) {
                    System.out.println("    * User ID: " + enrollment.getUser().getId() + 
                                     ", Name: " + enrollment.getUser().getFullName());
                }
            }
        }
    }
}
