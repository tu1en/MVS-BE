package com.classroomapp.classroombackend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicCourseTemplateDto {
    private Long id;
    private String name;
    private String description;
    private String subject;
    private Integer totalWeeks;
    private BigDecimal enrollmentFee;
    private Integer maxStudentsPerTemplate;
    private LocalDateTime createdAt;
    private String createdByName; // Full name of creator
    
    // Materials/resources available for preview
    private List<CourseMaterialPreviewDto> materials;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CourseMaterialPreviewDto {
    private String name;
    private String description;
    private String materialType; // DOCUMENT, VIDEO, LINK, etc.
}