package com.classroomapp.classroombackend.dto.assignmentmanagement;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentAttachmentDto {
    
    private Long id;
    
    private String fileName;
    
    private String fileUrl;
    
    private String downloadUrl;
    
    private String fileType;
    
    private long fileSize;
    
    private Long assignmentId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
