package com.classroomapp.classroombackend.service;

import java.util.List;
import java.util.Optional;

import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;

public interface CourseService {
    List<CourseDetailsDto> getAllCourses();
    Optional<CourseDetailsDto> findById(Long id);
} 