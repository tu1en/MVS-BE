package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseMaterialDto {
    private Long id;
    private String title;
    private String description;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadDate;
    private Long classroomId;
    private Long uploadedBy;
    private String uploaderName;
    private Boolean isPublic;
    private Integer downloadCount;
    private Integer versionNumber;
}
