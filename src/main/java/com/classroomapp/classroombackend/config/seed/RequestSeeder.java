package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.dto.StudentRequestFormDTO;
import com.classroomapp.classroombackend.dto.TeacherRequestFormDTO;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RequestSeeder {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public RequestSeeder(RequestRepository requestRepository, UserRepository userRepository, EmailService emailService, ObjectMapper objectMapper) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    public void seed() {
        if (requestRepository.count() > 0) {
            return;
        }
        try {
            // Create a teacher role request
            TeacherRequestFormDTO teacherForm = new TeacherRequestFormDTO();
            teacherForm.setEmail("nguyenvanA@gmail.com");
            teacherForm.setFullName("Nguyễn Văn A");
            teacherForm.setPhoneNumber("0987654321");
            teacherForm.setCvFileName("nguyen_van_a_cv.pdf");
            teacherForm.setCvFileType("application/pdf");
            teacherForm.setCvFileData(
                    "U2FtcGxlIENWIGZpbGUgY29udGVudC4gSW4gcmVhbCBpbXBsZW1lbnRhdGlvbiwgdGhpcyB3b3VsZCBiZSBhIGJhc2U2NCBlbmNvZGVkIHN0cmluZyBvZiBhIFBERiBmaWxlLg=="); // Sample base64 data
            teacherForm.setCvFileUrl("/files/teachers/nguyen_van_a_cv.pdf");
            teacherForm.setAdditionalInfo(
                    "Tôi đã có 5 năm kinh nghiệm giảng dạy Toán cấp trung học. Tôi từng làm việc tại trường THPT Chu Văn An và là giáo viên dạy thêm tại nhiều trung tâm luyện thi.");

            Request teacherRequest = new Request();
            teacherRequest.setEmail(teacherForm.getEmail());
            teacherRequest.setFullName(teacherForm.getFullName());
            teacherRequest.setPhoneNumber(teacherForm.getPhoneNumber());
            teacherRequest.setRequestedRole("TEACHER");
            teacherRequest.setFormResponses(objectMapper.writeValueAsString(teacherForm));
            teacherRequest.setStatus("PENDING");
            teacherRequest.setCreatedAt(LocalDateTime.now().minusDays(3));
            requestRepository.save(teacherRequest);

            // Create a student role request
            StudentRequestFormDTO studentForm = new StudentRequestFormDTO();
            studentForm.setEmail("tranvanB@gmail.com");
            studentForm.setFullName("Trần Văn B");
            studentForm.setPhoneNumber("0987123456");
            studentForm.setGrade("Lớp 11");
            studentForm.setParentContact("Phụ huynh: Trần Thị C, SĐT: 0912345678");
            studentForm.setAdditionalInfo("Em muốn đăng ký học thêm môn Toán và Vật lý để chuẩn bị cho kỳ thi quốc gia.");

            Request studentRequest = new Request();
            studentRequest.setEmail(studentForm.getEmail());
            studentRequest.setFullName(studentForm.getFullName());
            studentRequest.setPhoneNumber(studentForm.getPhoneNumber());
            studentRequest.setRequestedRole("STUDENT");
            studentRequest.setFormResponses(objectMapper.writeValueAsString(studentForm));
            studentRequest.setStatus("PENDING");
            studentRequest.setCreatedAt(LocalDateTime.now().minusDays(1));
            requestRepository.save(studentRequest);
            System.out.println("✅ [RequestSeeder] Created 2 sample role requests.");
        } catch (Exception e) {
            System.err.println("❌ Error creating sample requests: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 