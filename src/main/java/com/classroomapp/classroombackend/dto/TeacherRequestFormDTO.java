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
    
    // Explicit getters and setters to resolve compilation issues
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getCvFileUrl() { return cvFileUrl; }
    public void setCvFileUrl(String cvFileUrl) { this.cvFileUrl = cvFileUrl; }
    
    public String getCvFileData() { return cvFileData; }
    public void setCvFileData(String cvFileData) { this.cvFileData = cvFileData; }
    
    public String getCvFileName() { return cvFileName; }
    public void setCvFileName(String cvFileName) { this.cvFileName = cvFileName; }
    
    public String getCvFileType() { return cvFileType; }
    public void setCvFileType(String cvFileType) { this.cvFileType = cvFileType; }
    
    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
}