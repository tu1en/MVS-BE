package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureDetailsDto {
    private Long id;
    private String title;
    private String content;
    private LocalDate lectureDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LectureMaterialDto> materials;
} 