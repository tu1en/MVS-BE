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

            // Find specific classes by name (partial match)
            Classroom mathClass = findClassroomByPartialName(classrooms, "Toán");
            Classroom litClass = findClassroomByPartialName(classrooms, "Văn");
            Classroom engClass = findClassroomByPartialName(classrooms, "Anh");
            Classroom csClass = findClassroomByPartialName(classrooms, "Công nghệ");
            
            // Create Math assignments
            if (mathClass != null) {
                // Assignment 1 for Math
                CreateAssignmentDto mathAssignment1Dto = new CreateAssignmentDto();
                mathAssignment1Dto.setTitle("Bài tập về Đạo hàm và Tích phân");
                mathAssignment1Dto.setDescription("Giải các bài tập từ 1-10 trong giáo trình Toán cao cấp A1, chương 3.");
                mathAssignment1Dto.setDueDate(LocalDateTime.now().plusDays(7));
                mathAssignment1Dto.setPoints(100);
                mathAssignment1Dto.setClassroomId(mathClass.getId());
                assignmentService.CreateAssignment(mathAssignment1Dto, mathClass.getTeacher().getEmail());

                // Assignment 2 for Math
                CreateAssignmentDto mathAssignment2Dto = new CreateAssignmentDto();
                mathAssignment2Dto.setTitle("Bài tập về Ma trận và Định thức");
                mathAssignment2Dto.setDescription("Tính các định thức và ma trận nghịch đảo. Giải các hệ phương trình tuyến tính.");
                mathAssignment2Dto.setDueDate(LocalDateTime.now().plusDays(14));
                mathAssignment2Dto.setPoints(100);
                mathAssignment2Dto.setClassroomId(mathClass.getId());
                assignmentService.CreateAssignment(mathAssignment2Dto, mathClass.getTeacher().getEmail());

                System.out.println("✅ [AssignmentSeeder] Created 2 assignments for Math class");
            }
            
            // Create Literature assignments
            if (litClass != null) {
                // Assignment 1 for Literature
                CreateAssignmentDto litAssignment1Dto = new CreateAssignmentDto();
                litAssignment1Dto.setTitle("Phân tích tác phẩm Truyện Kiều");
                litAssignment1Dto.setDescription("Phân tích nhân vật Thúy Kiều và giá trị nhân đạo trong tác phẩm.");
                litAssignment1Dto.setDueDate(LocalDateTime.now().plusDays(10));
                litAssignment1Dto.setPoints(100);
                litAssignment1Dto.setClassroomId(litClass.getId());
                assignmentService.CreateAssignment(litAssignment1Dto, litClass.getTeacher().getEmail());
                
                // Assignment 2 for Literature
                CreateAssignmentDto litAssignment2Dto = new CreateAssignmentDto();
                litAssignment2Dto.setTitle("So sánh các tác phẩm thơ của Hồ Xuân Hương");
                litAssignment2Dto.setDescription("Phân tích và so sánh phong cách, chủ đề trong các bài thơ nổi tiếng của Hồ Xuân Hương.");
                litAssignment2Dto.setDueDate(LocalDateTime.now().plusDays(15));
                litAssignment2Dto.setPoints(100);
                litAssignment2Dto.setClassroomId(litClass.getId());
                assignmentService.CreateAssignment(litAssignment2Dto, litClass.getTeacher().getEmail());
                
                System.out.println("✅ [AssignmentSeeder] Created 2 assignments for Literature class");
            }
            
            // Create English assignments
            if (engClass != null) {
                // Assignment 1 for English
                CreateAssignmentDto engAssignment1Dto = new CreateAssignmentDto();
                engAssignment1Dto.setTitle("Presentation on Cultural Differences");
                engAssignment1Dto.setDescription("Prepare a 5-minute presentation about cultural differences between Vietnam and an English-speaking country.");
                engAssignment1Dto.setDueDate(LocalDateTime.now().plusDays(9));
                engAssignment1Dto.setPoints(100);
                engAssignment1Dto.setClassroomId(engClass.getId());
                assignmentService.CreateAssignment(engAssignment1Dto, engClass.getTeacher().getEmail());

                // Assignment 2 for English
                CreateAssignmentDto engAssignment2Dto = new CreateAssignmentDto();
                engAssignment2Dto.setTitle("Essay Writing: My Future Career");
                engAssignment2Dto.setDescription("Write a 500-word essay about your future career aspirations and the steps to achieve them.");
                engAssignment2Dto.setDueDate(LocalDateTime.now().plusDays(12));
                engAssignment2Dto.setPoints(100);
                engAssignment2Dto.setClassroomId(engClass.getId());
                assignmentService.CreateAssignment(engAssignment2Dto, engClass.getTeacher().getEmail());

                System.out.println("✅ [AssignmentSeeder] Created 2 assignments for English class");
            }
            
            // Create CS assignments
            if (csClass != null) {
                // Assignment 1 for CS
                CreateAssignmentDto csAssignment1Dto = new CreateAssignmentDto();
                csAssignment1Dto.setTitle("Xây dựng ứng dụng quản lý sinh viên");
                csAssignment1Dto.setDescription("Thiết kế và xây dựng ứng dụng quản lý sinh viên đơn giản với Java Swing hoặc JavaFX.");
                csAssignment1Dto.setDueDate(LocalDateTime.now().plusDays(20));
                csAssignment1Dto.setPoints(100);
                csAssignment1Dto.setClassroomId(csClass.getId());
                assignmentService.CreateAssignment(csAssignment1Dto, csClass.getTeacher().getEmail());

                // Assignment 2 for CS
                CreateAssignmentDto csAssignment2Dto = new CreateAssignmentDto();
                csAssignment2Dto.setTitle("Thiết kế cơ sở dữ liệu cho hệ thống bán hàng");
                csAssignment2Dto.setDescription("Phân tích yêu cầu và thiết kế cơ sở dữ liệu cho hệ thống quản lý bán hàng đơn giản.");
                csAssignment2Dto.setDueDate(LocalDateTime.now().plusDays(15));
                csAssignment2Dto.setPoints(100);
                csAssignment2Dto.setClassroomId(csClass.getId());
                assignmentService.CreateAssignment(csAssignment2Dto, csClass.getTeacher().getEmail());

                System.out.println("✅ [AssignmentSeeder] Created 2 assignments for CS class");
            }
            
            System.out.println("✅ [AssignmentSeeder] Created assignments for available classes");
        } else {
            System.out.println("✅ [AssignmentSeeder] Assignments already seeded");
        }
    }
    
    private Classroom findClassroomByPartialName(List<Classroom> classrooms, String partialName) {
        return classrooms.stream()
                .filter(c -> c.getName().contains(partialName))
                .findFirst()
                .orElse(null);
    }
} 