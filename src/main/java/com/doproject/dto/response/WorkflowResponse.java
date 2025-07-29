package com.doproject.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Response cho Workflow operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowResponse {
    
    private Long id;
    private String name;
    private String description;
    private String jsonData;
    private Integer version;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
