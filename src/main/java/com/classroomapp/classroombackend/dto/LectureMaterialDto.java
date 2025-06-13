package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureMaterialDto {
    
    private Long id;
    private String fileName;
    private String contentType;
    private String downloadUrl;
    private Long lectureId;
}
