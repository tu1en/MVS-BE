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
 * Test Ä‘á»ƒ kiá»ƒm tra tÃ­nh nháº¥t quÃ¡n giá»¯a cÃ¡c methods láº¥y dá»¯ liá»‡u enrollment
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
        // Láº¥y táº¥t cáº£ classrooms cÃ³ enrollments
        List<Classroom> classrooms = classroomRepository.findAll();
        
        for (Classroom classroom : classrooms) {
            Long classroomId = classroom.getId();
            
            // Method 1: Sá»­ dá»¥ng JPQL query trá»±c tiáº¿p
            Set<Long> studentIdsFromQuery = enrollmentRepository.findStudentIdsByClassroomId(classroomId);
            
            // Method 2: Sá»­ dá»¥ng findById_ClassroomId vÃ  extract user IDs
            List<ClassroomEnrollment> enrollments = enrollmentRepository.findById_ClassroomId(classroomId);
            Set<Long> studentIdsFromEnrollments = enrollments.stream()
                .map(e -> e.getUser().getId())
                .collect(java.util.stream.Collectors.toSet());
            
            // Method 3: Sá»­ dá»¥ng classroom.getStudents() (lazy loading)
            Set<User> studentsFromClassroom = classroom.getStudents();
            Set<Long> studentIdsFromClassroom = studentsFromClassroom.stream()
                .map(User::getId)
                .collect(java.util.stream.Collectors.toSet());
            
            // Kiá»ƒm tra tÃ­nh nháº¥t quÃ¡n
            System.out.println("=== Classroom ID: " + classroomId + " ===");
            System.out.println("Method 1 (JPQL Query): " + studentIdsFromQuery.size() + " students");
            System.out.println("Method 2 (Enrollments): " + studentIdsFromEnrollments.size() + " students");
            System.out.println("Method 3 (Lazy Loading): " + studentIdsFromClassroom.size() + " students");
            
            // Kiá»ƒm tra xem cÃ¡c sets cÃ³ báº±ng nhau khÃ´ng
            if (!studentIdsFromQuery.equals(studentIdsFromEnrollments)) {
                System.out.println("âŒ INCONSISTENCY: Query vs Enrollments");
                System.out.println("Query IDs: " + studentIdsFromQuery);
                System.out.println("Enrollment IDs: " + studentIdsFromEnrollments);
            }
            
            if (!studentIdsFromQuery.equals(studentIdsFromClassroom)) {
                System.out.println("âŒ INCONSISTENCY: Query vs Classroom.getStudents()");
                System.out.println("Query IDs: " + studentIdsFromQuery);
                System.out.println("Classroom IDs: " + studentIdsFromClassroom);
            }
            
            if (!studentIdsFromEnrollments.equals(studentIdsFromClassroom)) {
                System.out.println("âŒ INCONSISTENCY: Enrollments vs Classroom.getStudents()");
                System.out.println("Enrollment IDs: " + studentIdsFromEnrollments);
                System.out.println("Classroom IDs: " + studentIdsFromClassroom);
            }
            
            // Assert Ä‘á»ƒ test fail náº¿u cÃ³ inconsistency
            assertEquals(studentIdsFromQuery, studentIdsFromEnrollments, 
                "Query method vÃ  Enrollment method pháº£i tráº£ vá» cÃ¹ng káº¿t quáº£ cho classroom " + classroomId);
            assertEquals(studentIdsFromQuery, studentIdsFromClassroom, 
                "Query method vÃ  Classroom.getStudents() pháº£i tráº£ vá» cÃ¹ng káº¿t quáº£ cho classroom " + classroomId);
        }
    }
    
    @Test
    public void testSpecificClassroomConsistency() {
        // Test vá»›i classroom cá»¥ thá»ƒ tá»« screenshot (cÃ³ váº» nhÆ° classroom cÃ³ 0 há»c viÃªn nhÆ°ng cÃ³ submissions)
        List<Classroom> classrooms = classroomRepository.findAll();
        
        for (Classroom classroom : classrooms) {
            Long classroomId = classroom.getId();
            
            // Äáº¿m enrollments
            long enrollmentCount = enrollmentRepository.findById_ClassroomId(classroomId).size();
            
            // Äáº¿m students tá»« classroom entity
            int studentCount = classroom.getStudents().size();
            
            // Äáº¿m tá»« optimized query
            int queryCount = enrollmentRepository.findStudentIdsByClassroomId(classroomId).size();
            
            System.out.println("Classroom " + classroomId + ":");
            System.out.println("  - Enrollment count: " + enrollmentCount);
            System.out.println("  - Student count (lazy): " + studentCount);
            System.out.println("  - Query count: " + queryCount);
            
            if (enrollmentCount != studentCount || enrollmentCount != queryCount) {
                System.out.println("  âŒ INCONSISTENCY DETECTED!");
                
                // Debug thÃªm thÃ´ng tin
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
