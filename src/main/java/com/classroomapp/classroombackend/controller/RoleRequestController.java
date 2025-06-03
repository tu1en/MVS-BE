package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.RequestDTO;
import com.classroomapp.classroombackend.dto.RequestResponseDTO;
import com.classroomapp.classroombackend.dto.StudentRequestFormDTO;
import com.classroomapp.classroombackend.dto.TeacherRequestFormDTO;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.classroomapp.classroombackend.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/role-requests")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:5173"}, allowedHeaders = "*", allowCredentials = "true")
public class RoleRequestController {
    private final RequestService requestService;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;
    
    private static final Logger logger = LoggerFactory.getLogger(RoleRequestController.class);

    /**
     * Xử lý đăng ký yêu cầu làm giáo viên
     */
    @PostMapping("/teacher")
    public ResponseEntity<RequestResponseDTO> submitTeacherRequest(
            @Valid @RequestBody TeacherRequestFormDTO teacherForm) {
        
        logger.info("Received teacher registration request: {}", teacherForm.getEmail());
        
        try {
            // Xử lý file CV dạng base64
            if (teacherForm.getCvFileData() != null && !teacherForm.getCvFileData().isEmpty()) {
                logger.info("Processing CV file data: {}, type: {}", 
                        teacherForm.getCvFileName(), teacherForm.getCvFileType());
                
                try {
                    // Tách tiền tố data:image/jpeg;base64, từ chuỗi base64
                    String base64Data = teacherForm.getCvFileData();
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.split(",")[1];
                    }
                    
                    // Giải mã base64 thành byte array
                    byte[] fileBytes = Base64.getDecoder().decode(base64Data);
                    
                    // Tạo MultipartFile giả từ byte array
                    String originalFilename = teacherForm.getCvFileName() != null ? 
                            teacherForm.getCvFileName() : "cv-file-" + UUID.randomUUID() + ".pdf";
                    String contentType = teacherForm.getCvFileType() != null ? 
                            teacherForm.getCvFileType() : "application/pdf";
                    
                    MultipartFile multipartFile = new MockMultipartFile(
                            "file", 
                            originalFilename, 
                            contentType, 
                            fileBytes);
                    
                    // Upload lên Firebase Storage
                    try {
                        String fileUrl = fileStorageService.uploadFile(multipartFile, "cv-files");
                        logger.info("CV file uploaded successfully to: {}", fileUrl);
                        
                        // Cập nhật URL cho form
                        teacherForm.setCvFileUrl(fileUrl);
                    } catch (Exception e) {
                        logger.error("Firebase upload failed: {}", e.getMessage());
                        // Tạo một URL tạm thời khi không thể upload
                        String tempFileUrl = "pending://" + UUID.randomUUID() + "/" + originalFilename;
                        teacherForm.setCvFileUrl(tempFileUrl);
                    }
                    
                    // Xóa dữ liệu base64 để tránh lưu vào database
                    teacherForm.setCvFileData(null);
                } catch (Exception e) {
                    logger.error("Failed to process CV file: {}", e.getMessage(), e);
                    // Tạo URL tạm nếu xảy ra lỗi
                    String tempFileUrl = "error://" + UUID.randomUUID() + "/error-processing-file.pdf";
                    teacherForm.setCvFileUrl(tempFileUrl);
                    teacherForm.setCvFileData(null);
                }
            } else {
                logger.warn("No CV file data received");
                throw new RuntimeException("Thiếu file CV, vui lòng upload lại");
            }
            
            // Tiếp tục xử lý form như bình thường
            RequestDTO requestDTO = new RequestDTO();
            requestDTO.setEmail(teacherForm.getEmail());
            requestDTO.setFullName(teacherForm.getFullName());
            requestDTO.setPhoneNumber(teacherForm.getPhoneNumber());
            requestDTO.setRequestedRole("TEACHER");
            
            // Chuyển đổi form data thành JSON để lưu trữ
            String formDataJson = objectMapper.writeValueAsString(teacherForm);
            requestDTO.setFormResponses(formDataJson);
            
            RequestResponseDTO response = requestService.createRequest(requestDTO);
            logger.info("Teacher registration successful for: {}", teacherForm.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing teacher request", e);
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