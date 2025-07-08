package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;
import com.classroomapp.classroombackend.service.CourseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public List<CourseDetailsDto> getAllCourses() {
        ModelMapper modelMapper = new ModelMapper();
        return courseRepository.findAll().stream()
                .map(course -> modelMapper.map(course, CourseDetailsDto.class))
                .collect(Collectors.toList());
    }
} 