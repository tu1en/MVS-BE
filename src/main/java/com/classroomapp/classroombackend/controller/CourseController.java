package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseImportRequest;
import com.classroomapp.classroombackend.service.CourseImportService;
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
    private final CourseImportService courseImportService;

    @GetMapping
    public ResponseEntity<List<CourseDetailsDto>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // Compatibility endpoint: fetch lectures by course (classroom) ID
    // Delegates to LectureService which already supports fetching by classroomId
    @GetMapping("/{courseId}/lectures")
    public ResponseEntity<List<LectureDto>> getLecturesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(lectureService.getLecturesByClassroomId(courseId));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/import")
    public ResponseEntity<CourseDetailsDto> importCourse(
            @RequestPart("file") MultipartFile file,
            @RequestPart("courseName") String courseName,
            @RequestPart("description") String description,
            @RequestPart("section") String section,
            @RequestPart("subject") String subject,
            @RequestPart("teacherId") Long teacherId) throws Exception {
        
        CourseImportRequest request = new CourseImportRequest();
        request.setFile(file);
        request.setCourseName(courseName);
        request.setDescription(description);
        request.setSection(section);
        request.setSubject(subject);
        request.setTeacherId(teacherId);
        
        return ResponseEntity.ok(courseImportService.importCourseFromExcel(request));
    }
}
