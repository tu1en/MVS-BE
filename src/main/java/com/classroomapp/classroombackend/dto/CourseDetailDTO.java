package com.classroomapp.classroombackend.dto;

import com.classroomapp.classroombackend.dto.classroommanagement.SyllabusDto;
import lombok.Data;

import java.util.List;

@Data
public class CourseDetailDTO {
    private CourseDTO course;
    private SyllabusDto syllabus;
    private List<CourseTeacherDTO> teachers;
}