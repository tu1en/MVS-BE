package com.classroomapp.classroombackend.config.seed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.StudentProgress;
import com.classroomapp.classroombackend.model.StudentProgress.ProgressType;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.StudentProgressRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;


@Component
public class StudentProgressSeeder {

    @Autowired
    private StudentProgressRepository studentProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private ClassroomEnrollmentRepository enrollmentRepository;

    private final Random random = new Random();

    public void seed() {
        if (studentProgressRepository.count() > 0) {
            System.out.println("✅ [StudentProgressSeeder] Student progress already seeded.");
            return;
        }

        System.out.println("🔄 [StudentProgressSeeder] Seeding student progress data...");

        List<User> students = userRepository.findByRoleId(1); // STUDENT role
        if (students.isEmpty()) {
            System.out.println("⚠️ [StudentProgressSeeder] No students found. Skipping progress seeding.");
            return;
        }

        int progressCount = 0;
        // Seed progress for the first 5 students
        for (int i = 0; i < Math.min(5, students.size()); i++) {
            User student = students.get(i);
            List<Classroom> enrolledClassrooms = enrollmentRepository.findByUserId(student.getId()).stream()
                    .map(e -> e.getClassroom())
                    .collect(Collectors.toList());

            if (enrolledClassrooms.isEmpty()) {
                continue;
            }

            // Create overall progress for each enrolled classroom
            for (Classroom classroom : enrolledClassrooms) {
                createOverallProgress(student.getId(), classroom.getId(),
                        new BigDecimal(50 + random.nextInt(51)), // 50-100%
                        60 + random.nextInt(120)); // 60-180 minutes
                progressCount++;
                
                // Create detailed progress for assignments within that classroom
                List<Assignment> assignments = assignmentRepository.findByClassroomId(classroom.getId());
                if (!assignments.isEmpty()) {
                    // Create progress for up to 2 assignments per class
                    for (int j = 0; j < Math.min(2, assignments.size()); j++) {
                        Assignment assignment = assignments.get(j);
                        createAssignmentProgress(student.getId(), classroom.getId(), assignment.getId(),
                                new BigDecimal(100), // Assume completed
                                new BigDecimal(70 + random.nextInt(31)), // 70-100 points
                                new BigDecimal(100), // Max points
                                30 + random.nextInt(90)); // 30-120 minutes
                        progressCount++;
                    }
                }
            }
        }
        System.out.println("✅ [StudentProgressSeeder] Created " + progressCount + " student progress records.");
    }

    private void createOverallProgress(Long studentId, Long classroomId, BigDecimal progressPercentage, Integer timeSpentMinutes) {
        StudentProgress progress = new StudentProgress();
        progress.setStudentId(studentId);
        progress.setClassroomId(classroomId);
        progress.setProgressType(ProgressType.OVERALL);
        progress.setProgressPercentage(progressPercentage);
        progress.setTimeSpentMinutes(timeSpentMinutes);
        progress.setLastAccessed(LocalDateTime.now().minusDays(random.nextInt(10)));
        studentProgressRepository.save(progress);
    }

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
        progress.setLastAccessed(LocalDateTime.now().minusDays(random.nextInt(5)));
        studentProgressRepository.save(progress);
    }
} 