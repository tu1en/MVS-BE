package com.classroomapp.classroombackend.dto.classroommanagement;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class CourseImportRequest {
    private MultipartFile file;
    private String courseName;
    private String description;
    private String section;
    private String subject;
    private Long teacherId; // Optional - can be null
    private Long roomId; // Optional room assignment
    private String roomName; // Optional room name for reference
}
