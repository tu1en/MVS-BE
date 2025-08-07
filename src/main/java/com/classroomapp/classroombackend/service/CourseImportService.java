package com.classroomapp.classroombackend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseImportRequest;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseImportService {

    private final CourseService courseService;
    private final UserRepository userRepository;

    public CourseDetailsDto importCourseFromExcel(CourseImportRequest request) throws IOException {
        try {
            // Validate request
            if (request == null) {
                throw new IllegalArgumentException("Import request cannot be null");
            }
            if (request.getFile() == null || request.getFile().isEmpty()) {
                throw new IllegalArgumentException("Excel file is required");
            }
            if (request.getCourseName() == null || request.getCourseName().trim().isEmpty()) {
                throw new IllegalArgumentException("Course name is required");
            }
            if (request.getTeacherId() == null) {
                throw new IllegalArgumentException("Teacher ID is required");
            }

            // Validate teacher exists
            User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found with ID: " + request.getTeacherId()));

            // Create course DTO
            CourseDetailsDto courseDto = new CourseDetailsDto();
            courseDto.setName(request.getCourseName());
            courseDto.setDescription(request.getDescription() != null ? request.getDescription() : "");
            courseDto.setSection(request.getSection() != null ? request.getSection() : "Default Section");
            courseDto.setSubject(request.getSubject() != null ? request.getSubject() : "");
            courseDto.setTeacher(new UserDto(teacher));
            courseDto.setStudents(new ArrayList<>()); // Empty initially
            courseDto.setTotalStudents(0);

            // Create course with empty student list
            return courseService.createCourseWithStudents(courseDto, new ArrayList<>());

        } catch (Exception e) {
            throw new IOException("Failed to import course from Excel: " + e.getMessage(), e);
        }
    }
}
