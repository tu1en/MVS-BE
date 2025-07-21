package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateAssignmentDto;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.service.AssignmentService;

@Component
public class AssignmentSeeder {

    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    @Lazy
    private AssignmentService assignmentService;

    @Transactional
    public void seed() {
        if (assignmentRepository.count() == 0) {
            List<Classroom> classrooms = classroomRepository.findAll();

            if (classrooms.isEmpty()) {
                System.out.println("⚠️ [AssignmentSeeder] No classrooms found. Skipping.");
                 return;
            }

            System.out.println("🔄 [AssignmentSeeder] Creating assignments for " + classrooms.size() + " classrooms...");

            // Create assignments for EVERY classroom to ensure all students have assignments
            int totalAssignments = 0;
            for (Classroom classroom : classrooms) {
                totalAssignments += createAssignmentsForClassroom(classroom);
            }

            System.out.println("✅ [AssignmentSeeder] Created " + totalAssignments + " assignments across " + classrooms.size() + " classrooms");
        } else {
            System.out.println("✅ [AssignmentSeeder] Assignments already seeded");
        }
    }

    private int createAssignmentsForClassroom(Classroom classroom) {
        try {
            String teacherEmail = classroom.getTeacher().getEmail();
            String classroomName = classroom.getName();

            // Create 3 assignments per classroom with different due dates
            String[] assignmentTitles = {
                "Bài tập tuần 1 - " + classroomName,
                "Bài tập giữa kỳ - " + classroomName,
                "Bài tập cuối kỳ - " + classroomName
            };

            String[] assignmentDescriptions = {
                "Bài tập cơ bản để làm quen với nội dung môn học. Hoàn thành các câu hỏi lý thuyết và bài tập thực hành.",
                "Bài tập giữa kỳ tổng hợp kiến thức đã học. Yêu cầu vận dụng lý thuyết vào thực tế.",
                "Bài tập cuối kỳ đánh giá toàn diện kiến thức môn học. Bao gồm cả lý thuyết và thực hành."
            };

            int[] dueDays = {7, 14, 21}; // Due in 1, 2, 3 weeks
            int assignmentCount = 0;

            for (int i = 0; i < 3; i++) {
                CreateAssignmentDto assignmentDto = new CreateAssignmentDto();
                assignmentDto.setTitle(assignmentTitles[i]);
                assignmentDto.setDescription(assignmentDescriptions[i]);
                assignmentDto.setDueDate(LocalDateTime.now().plusDays(dueDays[i]));
                assignmentDto.setPoints(100);
                assignmentDto.setClassroomId(classroom.getId());

                assignmentService.CreateAssignment(assignmentDto, teacherEmail);
                assignmentCount++;
            }

            System.out.println("✅ [AssignmentSeeder] Created " + assignmentCount + " assignments for classroom: " + classroomName);
            return assignmentCount;

        } catch (Exception e) {
            System.err.println("⚠️ [AssignmentSeeder] Error creating assignments for classroom " + classroom.getName() + ": " + e.getMessage());
            return 0;
        }
    }
}