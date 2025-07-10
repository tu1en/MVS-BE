package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureDto {
    
    private Long id;
    private String title;
    private String content;
    private String description;
    private Long classroomId;
    private Long courseId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String type;
    private String roomLocation;
    private Integer maxAttendees;
    private Boolean isRecordingEnabled;
    private String meetingUrl;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDate lectureDate;
    private List<LectureMaterialDto> materials = new ArrayList<>();
    
    public LectureDto(Long id, String title, String content, Long classroomId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.classroomId = classroomId;
        this.materials = new ArrayList<>();
    }
    
    public void addMaterial(LectureMaterialDto material) {
        if (this.materials == null) {
            this.materials = new ArrayList<>();
        }
        this.materials.add(material);
    }
}
