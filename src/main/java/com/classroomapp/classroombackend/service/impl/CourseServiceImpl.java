package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.model.classroommanagement.Course;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.CourseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final UserRepository userRepository;

    @Override
    public List<CourseDetailsDto> getAllCourses() {
        ModelMapper modelMapper = new ModelMapper();
        return courseRepository.findAll().stream()
                .map(course -> modelMapper.map(course, CourseDetailsDto.class))
                .collect(Collectors.toList());
    }
    @Override
    public CourseDetailsDto createCourseWithStudents(CourseDetailsDto dto, List<Long> studentIds) {
        ModelMapper modelMapper = new ModelMapper();
        
        // Tạo course entity từ DTO
        Course course = modelMapper.map(dto, Course.class);
        course = courseRepository.save(course);
        
        // // Enroll students vào course
        // if (studentIds != null && !studentIds.isEmpty()) {
        //     List<ClassroomEnrollment> enrollments = studentIds.stream()
        //         .map(studentId -> {
        //             ClassroomEnrollment enrollment = new ClassroomEnrollment();
        //             enrollment.setClassroom(course);
        //             enrollment.setUser(new User(studentId));
        //             enrollment.setEnrollmentDate(LocalDateTime.now());
        //             return enrollment;
        //         })
        //         .collect(Collectors.toList());
            
        //     classroomEnrollmentRepository.saveAll(enrollments);
        // }
        
        // Map lại sang DTO để trả về
        return modelMapper.map(course, CourseDetailsDto.class);
    }

}
