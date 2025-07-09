package com.classroomapp.classroombackend.config.seed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.StudentProgress;
import com.classroomapp.classroombackend.model.StudentProgress.ProgressType;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.StudentProgressRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

/**
 * Seeder để tạo dữ liệu tiến độ học tập mẫu cho sinh viên
 * Bao gồm tiến độ tổng thể và tiến độ cho từng bài tập cụ thể
 */
@Component
public class StudentProgressSeeder {

    @Autowired
    private StudentProgressRepository studentProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    public void seed() {
        if (studentProgressRepository.count() == 0) {
            System.out.println("🔄 [StudentProgressSeeder] Seeding student progress data...");

            // Lấy danh sách sinh viên (roleId = 1 cho STUDENT)
            List<User> students = userRepository.findByRoleId(1);
            if (students.isEmpty()) {
                System.out.println("⚠️ [StudentProgressSeeder] No students found. Skipping progress seeding.");
                return;
            }

            // Lấy assignments để dùng cho progress chi tiết
            List<Assignment> assignments = assignmentRepository.findAll();
            if (assignments.isEmpty()) {
                System.out.println("⚠️ [StudentProgressSeeder] No assignments found for detailed progress.");
            }

            // Đảm bảo có ít nhất một số sinh viên cụ thể để thêm dữ liệu
            User student1 = students.size() > 0 ? students.get(0) : null;
            User student2 = students.size() > 1 ? students.get(1) : null;
            User student3 = students.size() > 2 ? students.get(2) : null;

            if (student1 != null) {
                // Overall progress cho student 1
                createOverallProgress(student1.getId(), 1L, new BigDecimal("75.50"), 120);
                createOverallProgress(student1.getId(), 2L, new BigDecimal("45.25"), 90);
                createOverallProgress(student1.getId(), 3L, new BigDecimal("88.00"), 150);
                
                // Assignment-specific progress
                if (!assignments.isEmpty()) {
                    Assignment assignment1 = assignments.get(0);
                    Assignment assignment2 = assignments.size() > 1 ? assignments.get(1) : null;
                    Assignment assignment3 = assignments.size() > 2 ? assignments.get(2) : null;
                    
                    if (assignment1 != null) {
                        createAssignmentProgress(student1.getId(), 1L, assignment1.getId(), 
                                                new BigDecimal("100.00"), new BigDecimal("95.00"), 
                                                new BigDecimal("100.00"), 60);
                    }
                    
                    if (assignment2 != null) {
                        createAssignmentProgress(student1.getId(), 1L, assignment2.getId(), 
                                                new BigDecimal("85.00"), new BigDecimal("85.00"), 
                                                new BigDecimal("100.00"), 45);
                    }
                    
                    if (assignment3 != null) {
                        createAssignmentProgress(student1.getId(), 2L, assignment3.getId(), 
                                                new BigDecimal("70.00"), new BigDecimal("70.00"), 
                                                new BigDecimal("100.00"), 30);
                    }
                }
            }

            if (student2 != null) {
                // Overall progress cho student 2
                createOverallProgress(student2.getId(), 1L, new BigDecimal("92.00"), 180);
                createOverallProgress(student2.getId(), 2L, new BigDecimal("67.50"), 105);
            }

            if (student3 != null) {
                // Overall progress cho student 3
                createOverallProgress(student3.getId(), 1L, new BigDecimal("55.75"), 85);
                createOverallProgress(student3.getId(), 3L, new BigDecimal("78.25"), 140);
            }

            System.out.println("✅ [StudentProgressSeeder] Student progress data seeded successfully");
        } else {
            System.out.println("✅ [StudentProgressSeeder] Student progress already seeded");
        }
    }

    /**
     * Tạo tiến độ tổng thể cho sinh viên trong lớp học
     */
    private void createOverallProgress(Long studentId, Long classroomId, BigDecimal progressPercentage, Integer timeSpentMinutes) {
        StudentProgress progress = new StudentProgress();
        progress.setStudentId(studentId);
        progress.setClassroomId(classroomId);
        progress.setProgressType(ProgressType.OVERALL);
        progress.setProgressPercentage(progressPercentage);
        progress.setTimeSpentMinutes(timeSpentMinutes);
        progress.setLastAccessed(LocalDateTime.now());
        studentProgressRepository.save(progress);
    }

    /**
     * Tạo tiến độ chi tiết cho bài tập cụ thể của sinh viên
     */
    private void createAssignmentProgress(Long studentId, Long classroomId, Long assignmentId, 
                                         BigDecimal progressPercentage, BigDecimal pointsEarned, 
                                         BigDecimal maxPoints, Integer timeSpentMinutes) {
        StudentProgress progress = new StudentProgress();
        progress.setStudentId(studentId);
        progress.setClassroomId(classroomId);
        progress.setAssignmentId(assignmentId);
        progress.setProgressType(ProgressType.ASSIGNMENT);
        progress.setProgressPercentage(progressPercentage);
        progress.setPointsEarned(pointsEarned);
        progress.setMaxPoints(maxPoints);
        progress.setTimeSpentMinutes(timeSpentMinutes);
        progress.setLastAccessed(LocalDateTime.now());
        studentProgressRepository.save(progress);
    }
} 