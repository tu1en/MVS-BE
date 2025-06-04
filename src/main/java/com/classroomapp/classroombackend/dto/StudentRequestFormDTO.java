package com.classroomapp.classroombackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class StudentRequestFormDTO {
    @NotEmpty(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotEmpty(message = "Họ tên không được để trống")
    private String fullName;
    
    @NotEmpty(message = "Số điện thoại không được để trống")
    private String phoneNumber;
    
    @NotEmpty(message = "Lớp không được để trống")
    private String grade;
    
    @NotEmpty(message = "Thông tin phụ huynh không được để trống")
    private String parentContact;
    
    private String additionalInfo;
} 