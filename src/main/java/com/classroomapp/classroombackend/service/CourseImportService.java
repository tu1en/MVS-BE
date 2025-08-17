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
                throw new IllegalArgumentException("Yêu cầu nhập dữ liệu không được để trống");
            }
            if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new IllegalArgumentException("Cần cung cấp file Excel");
            }
            if (request.getCourseName() == null || request.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Cần cung cấp tên khóa học");
            }
            
            // Teacher is optional - can be assigned later
            User teacher = null;
            if (request.getTeacherId() != null) {
                teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên với ID: " + request.getTeacherId()));
            }

            // Create course DTO
            CourseDetailsDto courseDto = new CourseDetailsDto();
            courseDto.setName(request.getCourseName());
            courseDto.setDescription(request.getDescription() != null ? request.getDescription() : "");
            courseDto.setSection(request.getSection() != null ? request.getSection() : "Default Section");
            courseDto.setSubject(request.getSubject() != null ? request.getSubject() : "");
            courseDto.setTeacher(teacher != null ? new UserDto(teacher) : null); // Teacher is optional
            courseDto.setStudents(new ArrayList<>()); // Empty initially
            courseDto.setTotalStudents(0);
            
            // Add room information if provided (for reference only - room assignment happens separately)
            if (request.getRoomId() != null) {
                // Store room info in description for now, or handle room assignment separately
                String roomInfo = request.getRoomName() != null ? request.getRoomName() : "Room ID: " + request.getRoomId();
                courseDto.setDescription(courseDto.getDescription() + " [Phòng học: " + roomInfo + "]");
            }

            // Create course with empty student list
            return courseService.createCourseWithStudents(courseDto, new ArrayList<>());

        } catch (Exception e) {
            throw new IOException("Nhập dữ liệu khóa học từ Excel thất bại: " + e.getMessage(), e);
        }
    }
}
