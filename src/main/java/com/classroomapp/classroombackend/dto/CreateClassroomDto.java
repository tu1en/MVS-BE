package com.classroomapp.classroombackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object cho việc tạo mới Classroom
 * DTO hợp nhất từ 2 phiên bản trước đó với đầy đủ validation và utility methods
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassroomDto {

    @NotBlank(message = "Tên classroom không được để trống")
    @Size(min = 3, max = 255, message = "Tên classroom phải có độ dài từ 3 đến 255 ký tự")
    private String name;
    
    // Alias để tương thích với version classroommanagement
    public String getClassroomName() {
        return name;
    }
    
    public void setClassroomName(String classroomName) {
        this.name = classroomName;
    }

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    private String section;

    private String subject;

    @NotNull(message = "Teacher là bắt buộc")
    private Long teacherId;

    @NotNull(message = "Course là bắt buộc") 
    private Long courseId;

    /**
     * Kiểm tra tính hợp lệ của tên classroom
     */
    public boolean isValidName() {
        return name != null && !name.trim().isEmpty() && name.length() >= 3 && name.length() <= 255;
    }

    /**
     * Lấy tên classroom đã được trim
     */
    public String getTrimmedName() {
        return name != null ? name.trim() : null;
    }

    /**
     * Kiểm tra xem có mô tả hay không
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }
    
    /**
     * Lấy mô tả đã được trim
     */
    public String getTrimmedDescription() {
        return description != null ? description.trim() : null;
    }
}