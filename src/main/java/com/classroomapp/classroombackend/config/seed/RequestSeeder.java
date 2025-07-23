package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.dto.StudentRequestFormDTO;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestSeeder {

    private final RequestRepository requestRepository;
    private final ObjectMapper objectMapper;

    public void seed() {
        if (requestRepository.count() > 0) {
            return;
        }
        try {
            // Create a teacher role request
            Request teacherRequest = new Request();
            teacherRequest.setEmail("nguyenvanA@gmail.com");
            teacherRequest.setFullName("Nguyễn Văn A");
            teacherRequest.setPhoneNumber("0987654321");
            teacherRequest.setRequestedRole("TEACHER");
            teacherRequest.setFormResponses(objectMapper.writeValueAsString(null)); // No form data for teacher
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
            studentRequest.setEmail(studentForm.getEmail() != null ? studentForm.getEmail() : "unknown@email.com");
            studentRequest.setFullName(studentForm.getFullName() != null ? studentForm.getFullName() : "Unknown");
            studentRequest.setPhoneNumber(studentForm.getPhoneNumber() != null ? studentForm.getPhoneNumber() : "0000000000");
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