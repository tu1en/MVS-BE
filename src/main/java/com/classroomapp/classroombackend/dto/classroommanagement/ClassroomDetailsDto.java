package com.classroomapp.classroombackend.dto.classroommanagement;

import java.util.List;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDetailsDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDetailsDto {
    private Long id;
    private String name;
    private String description;
    
    // Thông tin lồng nhau
    private CourseDetailsDto course;
    private UserDetailsDto teacher;
    
    // Danh sách các bài giảng
    private List<LectureDto> lectures;
    
    // Constructor for simple creation (id, name, description, lectures)
    public ClassroomDetailsDto(Long id, String name, String description, List<Object> lectures) {
        this.id = id;
        this.name = name;
        this.description = description;
        // Convert Object list to LectureDto if needed
        // this.lectures = lectures.stream().map(obj -> (LectureDto) obj).collect(Collectors.toList());
    }
}