package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.CourseDetailResponse;
import com.classroomapp.classroombackend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping("/{id}")
    public CourseDetailResponse getCourseDetail(@PathVariable Long id) {
        return courseService.getCourseDetail(id);
    }
}
