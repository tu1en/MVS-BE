package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.RequestDTO;
import com.classroomapp.classroombackend.dto.RequestResponseDTO;
import com.classroomapp.classroombackend.dto.StudentRequestFormDTO;
import com.classroomapp.classroombackend.dto.TeacherRequestFormDTO;
import com.classroomapp.classroombackend.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/role-requests")
@RequiredArgsConstructor
public class RoleRequestController {
    private final RequestService requestService;
    private final ObjectMapper objectMapper;

    /**
     * Xử lý đăng ký yêu cầu làm giáo viên
     */
    @PostMapping("/teacher")
    public ResponseEntity<RequestResponseDTO> submitTeacherRequest(
            @Valid @RequestBody TeacherRequestFormDTO teacherForm) {
        
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setEmail(teacherForm.getEmail());
        requestDTO.setFullName(teacherForm.getFullName());
        requestDTO.setPhoneNumber(teacherForm.getPhoneNumber());
        requestDTO.setRequestedRole("TEACHER");
        
        try {
            // Chuyển đổi form data thành JSON để lưu trữ
            String formDataJson = objectMapper.writeValueAsString(teacherForm);
            requestDTO.setFormResponses(formDataJson);
            
            return ResponseEntity.ok(requestService.createRequest(requestDTO));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý yêu cầu đăng ký giáo viên", e);
        }
    }

    /**
     * Xử lý đăng ký yêu cầu làm học sinh
     */
    @PostMapping("/student")
    public ResponseEntity<RequestResponseDTO> submitStudentRequest(
            @Valid @RequestBody StudentRequestFormDTO studentForm) {
        
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setEmail(studentForm.getEmail());
        requestDTO.setFullName(studentForm.getFullName());
        requestDTO.setPhoneNumber(studentForm.getPhoneNumber());
        requestDTO.setRequestedRole("STUDENT");
        
        try {
            // Chuyển đổi form data thành JSON để lưu trữ
            String formDataJson = objectMapper.writeValueAsString(studentForm);
            requestDTO.setFormResponses(formDataJson);
            
            return ResponseEntity.ok(requestService.createRequest(requestDTO));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý yêu cầu đăng ký học sinh", e);
        }
    }
    
    /**
     * Kiểm tra trạng thái yêu cầu qua email và role
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkActiveRequest(
            @RequestParam String email,
            @RequestParam String role) {
        return ResponseEntity.ok(requestService.hasActiveRequest(email, role));
    }

    /**
     * Lấy các yêu cầu của một người dùng
     */
    @GetMapping("/my-requests")
    public ResponseEntity<List<RequestResponseDTO>> getMyRequests(@RequestParam String email) {
        return ResponseEntity.ok(requestService.getRequestsByEmail(email));
    }
} 