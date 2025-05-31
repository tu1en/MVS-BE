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
    
    @NotEmpty(message = "Trình độ chuyên môn không được để trống")
    private String qualifications;
    
    @NotEmpty(message = "Kinh nghiệm không được để trống")
    private String experience;
    
    @NotEmpty(message = "Môn học dạy không được để trống")
    private String subjects;
    
    private String additionalInfo;
} 