package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Explicit getters for compilation issues
    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }
}
