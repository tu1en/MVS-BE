package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;

public interface CourseService {
    List<CourseDetailsDto> getAllCourses();
} 