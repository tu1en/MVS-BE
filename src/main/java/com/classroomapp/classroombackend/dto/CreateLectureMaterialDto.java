package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLectureMaterialDto {
    
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private boolean localFile;
}
