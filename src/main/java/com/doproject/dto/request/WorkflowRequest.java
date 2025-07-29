package com.doproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request cho Workflow operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowRequest {
    
    @NotBlank(message = "Tên workflow không được để trống")
    @Size(max = 255, message = "Tên workflow không được vượt quá 255 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    @NotBlank(message = "JSON data không được để trống")
    private String jsonData;
    
    private Boolean isActive;
    
    private String createdBy;
}
// package com.classroomapp.classroombackend.dto;

// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Size;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// /**
//  * DTO Request cho Workflow operations
//  * Validation annotations để đảm bảo dữ liệu hợp lệ
//  */
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class WorkflowRequest {
    
//     @NotBlank(message = "Tên workflow không được để trống")
//     @Size(min = 1, max = 255, message = "Tên workflow phải từ 1-255 ký tự")
//     private String name;
    
//     @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
//     private String description;
    
//     @NotNull(message = "JSON data không được để trống")
//     @NotBlank(message = "JSON data không được để trống")
//     private String jsonData;
    
//     @Builder.Default
//     private Boolean isActive = true;
    
//     @Size(max = 100, message = "Tên người tạo không được vượt quá 100 ký tự")
//     private String createdBy;
    
//     /**
//      * Validate JSON structure (basic validation)
//      * Detailed validation should be done in service layer
//      */
//     public boolean hasValidJsonStructure() {
//         if (jsonData == null || jsonData.trim().isEmpty()) {
//             return false;
//         }
        
//         // Basic check for JSON structure
//         return jsonData.trim().startsWith("{") && jsonData.trim().endsWith("}");
//     }
// }