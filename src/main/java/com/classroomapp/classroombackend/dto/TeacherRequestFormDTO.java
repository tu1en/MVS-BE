package com.classroomapp.classroombackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class TeacherRequestFormDTO {
    @NotEmpty(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotEmpty(message = "Họ tên không được để trống")
    private String fullName;
    
    @NotEmpty(message = "Số điện thoại không được để trống")
    private String phoneNumber;
    
    // Bỏ ràng buộc @NotEmpty vì trường này được tạo sau khi upload file
    private String cvFileUrl;
    
    // Các trường cho upload file base64
    @NotEmpty(message = "File CV không được để trống")
    private String cvFileData;
    private String cvFileName;
    private String cvFileType;
    
    private String additionalInfo;
} 