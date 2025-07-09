package com.classroomapp.classroombackend.dto.classroommanagement;

import java.util.List;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDetailsDto;

import lombok.Data;


@Data
public class ClassroomDetailsDto {
    private Long id;
    private String name;
    private String description;
    
    // Thông tin lồng nhau
    private CourseDetailsDto course;
    private UserDetailsDto teacher;
    
    // Danh sách các bài giảng
    private List<LectureDto> lectures;
}
