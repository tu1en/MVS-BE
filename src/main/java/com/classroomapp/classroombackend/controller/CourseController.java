package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.service.ClassroomService;
import com.classroomapp.classroombackend.service.CourseService;
import com.classroomapp.classroombackend.service.LectureService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {

    private final CourseService courseService;
    private final LectureService lectureService;
    private final ClassroomService classroomService;

    @GetMapping
    public ResponseEntity<List<CourseDetailsDto>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // Get course (classroom) details by ID
    // Since frontend treats courses as classrooms, we delegate to ClassroomService
    @GetMapping("/{courseId}")
    public ResponseEntity<ClassroomDto> getCourseById(@PathVariable Long courseId) {
        return ResponseEntity.ok(classroomService.GetClassroomById(courseId));
    }

    // Compatibility endpoint: fetch lectures by course (classroom) ID
    // Delegates to LectureService which already supports fetching by classroomId
    @GetMapping("/{courseId}/lectures")
    public ResponseEntity<List<LectureDto>> getLecturesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(lectureService.getLecturesByClassroomId(courseId));
    }

    // NOTE: All other mock-data-based endpoints are removed.
    // They should be re-implemented properly using the service and repository layers
    // if their functionality is still required.
    // (createLecture, updateLecture, deleteLecture, getLecturesByCourse, etc.)
}
