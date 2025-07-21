package com.doproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho request tạo/cập nhật workflow
 */
@Data
public class WorkflowRequest {
    
    @NotBlank(message = "Tên workflow không được để trống")
    @Size(max = 255, message = "Tên workflow không được vượt quá 255 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    @NotBlank(message = "Dữ liệu JSON không được để trống")
    private String jsonData;
    
    private Boolean isActive;
    
    private String createdBy;
}
