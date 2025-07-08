package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class ComprehensiveGradingSeeder {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private ClassroomEnrollmentRepository enrollmentRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;

    private final Random random = new Random();

    @Transactional
    public void seedGradingData() {
        System.out.println("🎯 [GradingSeeder] Bắt đầu tạo dữ liệu chấm điểm toàn diện...");

        // Lấy classroom 54 (Công nghệ thông tin)
        Classroom classroom54 = classroomRepository.findById(54L).orElse(null);
        if (classroom54 == null) {
            System.out.println("⚠️ [GradingSeeder] Không tìm thấy classroom 54. Đang thoát...");
            return;
        }

        // Tạo sinh viên mới nếu cần
        List<User> students = createStudentsIfNeeded();
        
        // Đăng ký sinh viên vào lớp 54
        enrollStudentsInClassroom(students, classroom54);
        
        // Lấy assignments hiện tại trong classroom 54
        List<Assignment> assignments = assignmentRepository.findByClassroomId(54L);
        if (assignments.isEmpty()) {
            System.out.println("⚠️ [GradingSeeder] Không có assignments trong classroom 54");
            return;
        }

        // Tạo submissions với các trạng thái khác nhau
        for (Assignment assignment : assignments) {
            createDiverseSubmissions(assignment, students);
        }

        System.out.println("✅ [GradingSeeder] Hoàn thành tạo dữ liệu chấm điểm!");
    }

    private List<User> createStudentsIfNeeded() {
        System.out.println("📚 [GradingSeeder] Tạo sinh viên test...");
        
        List<String> studentNames = Arrays.asList(
            "Nguyễn Văn An", "Trần Thị Bích", "Lê Minh Cường", "Phạm Thu Dung",
            "Hoàng Văn Em", "Vũ Thị Phương", "Đặng Minh Giang", "Bùi Thị Hạnh",
            "Ngô Văn Inh", "Lý Thị Kim", "Đinh Văn Long", "Trịnh Thị Mai",
            "Dương Văn Nam", "Lương Thị Oanh", "Tạ Văn Phúc", "Cao Thị Quỳnh"
        );

        // Find existing students by role (assuming STUDENT role is 1)
        List<User> existingStudents = userRepository.findByRoleId(1);
        
        // Nếu đã có đủ sinh viên thì dùng lại
        if (existingStudents.size() >= 12) {
            return existingStudents.subList(0, 12);
        }

        // Tạo sinh viên mới
        for (int i = existingStudents.size(); i < 12; i++) {
            User student = new User();
            student.setEmail("student" + (i + 1) + "@school.edu");
            student.setUsername("student" + (i + 1));
            student.setPassword("$2a$10$defaultpassword"); // Encrypted password
            student.setFullName(studentNames.get(i % studentNames.size()));
            student.setPhoneNumber("098765" + String.format("%04d", i + 1));
            student.setRoleId(1); // Student role
            student.setDepartment("Công nghệ thông tin");
            
            userRepository.save(student);
            existingStudents.add(student);
        }

        return existingStudents.subList(0, 12);
    }

    private void enrollStudentsInClassroom(List<User> students, Classroom classroom) {
        System.out.println("📝 [GradingSeeder] Đăng ký sinh viên vào lớp...");
        
        for (User student : students) {
            ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId();
            enrollmentId.setClassroomId(classroom.getId());
            enrollmentId.setUserId(student.getId());

            // Kiểm tra xem đã đăng ký chưa
            if (!enrollmentRepository.existsById(enrollmentId)) {
                ClassroomEnrollment enrollment = new ClassroomEnrollment();
                enrollment.setId(enrollmentId);
                enrollment.setClassroom(classroom);
                enrollment.setUser(student);
                
                enrollmentRepository.save(enrollment);
            }
        }
    }

    private void createDiverseSubmissions(Assignment assignment, List<User> students) {
        System.out.println("📋 [GradingSeeder] Tạo submissions cho assignment: " + assignment.getTitle());

        List<String> submissionContents = Arrays.asList(
            "# Báo cáo Thiết kế Cơ sở Dữ liệu\n\n" +
            "## 1. Phân tích yêu cầu\n" +
            "Hệ thống bán hàng cần quản lý thông tin khách hàng, sản phẩm, đơn hàng...\n\n" +
            "## 2. Thiết kế ERD\n" +
            "[Diagram được đính kèm]\n\n" +
            "## 3. Tạo bảng SQL\n" +
            "```sql\nCREATE TABLE customers (\n  id INT PRIMARY KEY,\n  name VARCHAR(100)\n);\n```",
            
            "Thiết kế CSDL cho hệ thống bán hàng:\n\n" +
            "1. Bảng KHACH_HANG (customer_id, ten, email, sdt)\n" +
            "2. Bảng SAN_PHAM (product_id, ten_sp, gia, mo_ta)\n" +
            "3. Bảng DON_HANG (order_id, customer_id, ngay_dat, tong_tien)\n" +
            "4. Bảng CHI_TIET_DH (order_id, product_id, so_luong, gia)\n\n" +
            "Quan hệ: 1-n giữa KHACH_HANG và DON_HANG, n-n giữa DON_HANG và SAN_PHAM",
            
            "Database Design Document\n\n" +
            "Entity Analysis:\n" +
            "- Customer: Stores customer information\n" +
            "- Product: Product catalog\n" +
            "- Order: Sales transactions\n" +
            "- OrderItem: Order details\n\n" +
            "Normalization: Applied 3NF to eliminate redundancy\n" +
            "Indexes: Created on foreign keys and frequently queried columns"
        );

        // Tạo submission cho mỗi sinh viên với tỷ lệ khác nhau
        for (int i = 0; i < students.size(); i++) {
            User student = students.get(i);
            
            // 80% sinh viên nộp bài
            if (random.nextDouble() < 0.8) {
                Submission submission = new Submission();
                submission.setAssignment(assignment);
                submission.setStudent(student);
                
                // Nội dung submission
                String content = submissionContents.get(random.nextInt(submissionContents.size()));
                submission.setComment(content);
                
                // Thời gian nộp (một số trễ hạn)
                LocalDateTime submissionTime;
                if (random.nextDouble() < 0.15) { // 15% nộp trễ
                    submissionTime = assignment.getDueDate().plusDays(random.nextInt(7) + 1);
                } else {
                    submissionTime = assignment.getDueDate().minusHours(random.nextInt(48) + 1);
                }
                submission.setSubmittedAt(submissionTime);
                
                // Một số bài đã được chấm điểm
                if (random.nextDouble() < 0.4) { // 40% đã chấm
                    submission.setScore(60 + random.nextInt(40)); // Điểm từ 60-100
                    submission.setFeedback(generateFeedback(submission.getScore()));
                    submission.setGradedAt(submissionTime.plusDays(random.nextInt(5) + 1));
                }
                
                submissionRepository.save(submission);
            }
        }
    }

    private String generateFeedback(Integer score) {
        if (score >= 90) {
            return "Xuất sắc! Bài làm rất tốt, ERD thiết kế chính xác, SQL viết đúng chuẩn. " +
                   "Phân tích yêu cầu chi tiết và logic. Tiếp tục phát huy!";
        } else if (score >= 80) {
            return "Tốt! Bài làm đạt yêu cầu, thiết kế CSDL hợp lý. " +
                   "Một số điểm cần cải thiện: chuẩn hóa dữ liệu và tối ưu hóa query.";
        } else if (score >= 70) {
            return "Khá! Nắm được kiến thức cơ bản về thiết kế CSDL. " +
                   "Cần chú ý thêm về quan hệ giữa các bảng và ràng buộc dữ liệu.";
        } else {
            return "Cần cải thiện! Bài làm chưa đạt yêu cầu tối thiểu. " +
                   "Hãy xem lại tài liệu về ERD và chuẩn hóa CSDL. Liên hệ thầy nếu cần hỗ trợ.";
        }
    }
}
