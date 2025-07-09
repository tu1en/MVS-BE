package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class AssignmentTestDataSeeder {

    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void seedAssignmentTestData() {
        System.out.println("📚 [AssignmentTestData] Tạo assignments test cho classroom 54...");

        Classroom classroom54 = classroomRepository.findById(54L).orElse(null);
        if (classroom54 == null) {
            System.out.println("⚠️ [AssignmentTestData] Không tìm thấy classroom 54");
            return;
        }

        // Find teacher by role (assuming TEACHER role is 2)
        User teacher = userRepository.findByRoleId(2).stream()
            .findFirst().orElse(null);
        if (teacher == null) {
            System.out.println("⚠️ [AssignmentTestData] Không tìm thấy teacher");
            return;
        }

        // Tạo assignments test nếu chưa có
        List<Assignment> existingAssignments = assignmentRepository.findByClassroomId(54L);
        if (existingAssignments.size() < 3) {
            createTestAssignments(classroom54, teacher);
        }
    }

    private void createTestAssignments(Classroom classroom, User teacher) {
        List<AssignmentData> assignmentDataList = Arrays.asList(
            new AssignmentData(
                "Thiết kế cơ sở dữ liệu cho hệ thống bán hàng",
                "Yêu cầu thiết kế CSDL cho hệ thống bán hàng online bao gồm:\n\n" +
                "1. Phân tích yêu cầu nghiệp vụ\n" +
                "2. Thiết kế mô hình thực thể - quan hệ (ERD)\n" +
                "3. Chuẩn hóa dữ liệu đến dạng chuẩn 3\n" +
                "4. Tạo script SQL tạo bảng\n" +
                "5. Viết stored procedure cơ bản\n\n" +
                "**Deliverables:**\n" +
                "- File PDF báo cáo (max 10 trang)\n" +
                "- File SQL script\n" +
                "- Sơ đồ ERD (format .png hoặc .pdf)\n\n" +
                "**Tiêu chí chấm điểm:**\n" +
                "- Phân tích yêu cầu: 20%\n" +
                "- Thiết kế ERD: 30%\n" +
                "- Chuẩn hóa dữ liệu: 25%\n" +
                "- SQL script: 25%",
                100.0,
                LocalDateTime.now().plusDays(7)
            ),
            new AssignmentData(
                "Xây dựng ứng dụng web với Spring Boot",
                "Phát triển một ứng dụng web hoàn chình sử dụng Spring Boot:\n\n" +
                "**Yêu cầu chức năng:**\n" +
                "1. Authentication & Authorization\n" +
                "2. CRUD operations cho ít nhất 3 entities\n" +
                "3. REST API với đầy đủ endpoints\n" +
                "4. Frontend với Thymeleaf hoặc React\n" +
                "5. Validation và error handling\n" +
                "6. Unit tests (coverage ≥ 70%)\n\n" +
                "**Công nghệ yêu cầu:**\n" +
                "- Spring Boot 3.x\n" +
                "- Spring Security\n" +
                "- JPA/Hibernate\n" +
                "- MySQL/PostgreSQL\n" +
                "- Maven/Gradle\n\n" +
                "**Nộp bài:**\n" +
                "- Source code (ZIP file)\n" +
                "- Database script\n" +
                "- Documentation\n" +
                "- Demo video (5-10 phút)",
                100.0,
                LocalDateTime.now().plusDays(14)
            ),
            new AssignmentData(
                "Phân tích và thiết kế hệ thống",
                "Thực hiện phân tích và thiết kế hệ thống quản lý thư viện:\n\n" +
                "**Phần 1: Phân tích hệ thống (40%)**\n" +
                "- Phân tích stakeholder\n" +
                "- Thu thập và phân tích yêu cầu\n" +
                "- Use case diagram\n" +
                "- Activity diagram\n\n" +
                "**Phần 2: Thiết kế hệ thống (60%)**\n" +
                "- Kiến trúc hệ thống\n" +
                "- Class diagram\n" +
                "- Sequence diagram\n" +
                "- Component diagram\n" +
                "- Deployment diagram\n\n" +
                "**Công cụ:**\n" +
                "- UML tools (StarUML, Lucidchart, Draw.io)\n" +
                "- Word/LaTeX cho documentation\n\n" +
                "**Format nộp bài:**\n" +
                "- PDF report (15-20 trang)\n" +
                "- UML diagrams (separate files)\n" +
                "- Presentation slides",
                100.0,
                LocalDateTime.now().plusDays(10)
            )
        );

        for (AssignmentData data : assignmentDataList) {
            // Kiểm tra xem assignment đã tồn tại chưa
            boolean exists = assignmentRepository.findByClassroomId(classroom.getId())
                .stream()
                .anyMatch(a -> a.getTitle().equals(data.title));

            if (!exists) {
                Assignment assignment = new Assignment();
                assignment.setTitle(data.title);
                assignment.setDescription(data.description);
                assignment.setPoints(data.points.intValue());
                assignment.setDueDate(data.dueDate);
                assignment.setClassroom(classroom);

                assignmentRepository.save(assignment);
                System.out.println("✅ [AssignmentTestData] Tạo assignment: " + data.title);
            }
        }
    }

    private static class AssignmentData {
        final String title;
        final String description;
        final Double points;
        final LocalDateTime dueDate;

        AssignmentData(String title, String description, Double points, LocalDateTime dueDate) {
            this.title = title;
            this.description = description;
            this.points = points;
            this.dueDate = dueDate;
        }
    }
}
