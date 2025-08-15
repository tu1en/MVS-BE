package com.classroomapp.classroombackend.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.CourseTemplateDto;
import com.classroomapp.classroombackend.dto.LessonTemplateDto;
import com.classroomapp.classroombackend.service.CourseTemplateService;
import com.classroomapp.classroombackend.service.QuickCourseGeneratorService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/course-templates")
@CrossOrigin(origins = "*")
public class CourseTemplateController {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseTemplateController.class);
    
    @Autowired
    private CourseTemplateService courseTemplateService;
    
    @Autowired
    private QuickCourseGeneratorService quickGeneratorService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Import course template from Excel
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<String>> importCourseTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseName") String courseName,
            @RequestParam("description") String description,
            @RequestParam("subject") String subject,
            @RequestParam("createdBy") Long createdBy) {
        
        try {
            // Validate input
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File không được để trống"));
            }
            
            if (!isExcelFile(file)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File phải có định dạng Excel (.xlsx, .xls)"));
            }
            
            long startTime = System.currentTimeMillis();
            
            CourseTemplateDto courseTemplate = courseTemplateService.importCourseFromExcel(
                    file, courseName, description, subject, createdBy);
            
            long endTime = System.currentTimeMillis();
            logger.info("Imported course template '{}' in {} ms", courseName, endTime - startTime);
            
            String courseDetails = String.format("Khóa học '%s' đã được import thành công với %d tuần học", 
                    courseName, courseTemplate.getTotalWeeks());
            
            return ResponseEntity.ok(ApiResponse.success(courseDetails));
            
        } catch (Exception e) {
            logger.error("Error importing course template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi import file Excel: " + e.getMessage()));
        }
    }
    
    /**
     * Export course template to Excel
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<?> exportToExcel(@PathVariable Long id) {
        try {
            byte[] excelBytes = courseTemplateService.exportCourseTemplateToExcel(id);
            
            CourseTemplateDto courseTemplate = courseTemplateService.getCourseTemplateById(id);
            
            ByteArrayResource resource = new ByteArrayResource(excelBytes);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           String.format("attachment; filename=template_%s.xlsx", sanitizeFileName(courseTemplate.getName())))
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("Error exporting course template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi export template: " + e.getMessage()));
        }
    }
    
    /**
     * Get all course templates
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseTemplateDto>>> getAllCourseTemplates() {
        try {
            List<CourseTemplateDto> templates = courseTemplateService.getAllCourseTemplates();
            return ResponseEntity.ok(ApiResponse.success(templates));
        } catch (Exception e) {
            logger.error("Error getting course templates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy danh sách khóa học: " + e.getMessage()));
        }
    }
    
    /**
     * Get course template by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseTemplateDto>> getCourseTemplateById(@PathVariable Long id) {
        try {
            CourseTemplateDto template = courseTemplateService.getCourseTemplateById(id);
            return ResponseEntity.ok(ApiResponse.success(template));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(ApiResponse.error("Không tìm thấy khóa học"));
        } catch (Exception e) {
            logger.error("Error getting course template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy chi tiết khóa học: " + e.getMessage()));
        }
    }
    
    /**
     * Create new course template
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CourseTemplateDto>> createCourseTemplate(@RequestBody CourseTemplateDto courseTemplateDto) {
        try {
            if (courseTemplateDto.getName() == null || courseTemplateDto.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên khóa học không được để trống"));
            }
            
            CourseTemplateDto createdTemplate = courseTemplateService.createCourseTemplate(courseTemplateDto);
            return ResponseEntity.ok(ApiResponse.success(createdTemplate, "Tạo khóa học thành công"));
            
        } catch (Exception e) {
            logger.error("Error creating course template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error("Lỗi tạo khóa học: " + e.getMessage()));
        }
    }
    
    /**
     * Update course template
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseTemplateDto>> updateCourseTemplate(@PathVariable Long id, @RequestBody CourseTemplateDto courseTemplateDto) {
        try {
            if (courseTemplateDto.getName() == null || courseTemplateDto.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên khóa học không được để trống"));
            }
            
            CourseTemplateDto updatedTemplate = courseTemplateService.updateCourseTemplate(id, courseTemplateDto);
            return ResponseEntity.ok(ApiResponse.success(updatedTemplate, "Cập nhật khóa học thành công"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(ApiResponse.error("Không tìm thấy khóa học"));
        } catch (Exception e) {
            logger.error("Error updating course template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error("Lỗi cập nhật khóa học: " + e.getMessage()));
        }
    }
    
    /**
     * Delete course template
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCourseTemplate(@PathVariable Long id) {
        try {
            courseTemplateService.deleteCourseTemplate(id);
            return ResponseEntity.ok(ApiResponse.success("Khóa học đã được xóa"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(ApiResponse.error("Không tìm thấy khóa học"));
        } catch (Exception e) {
            logger.error("Error deleting course template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error("Lỗi xóa khóa học: " + e.getMessage()));
        }
    }
    
    /**
     * Get lessons for a course template
     */
    @GetMapping("/{id}/lessons")
    public ResponseEntity<ApiResponse<List<LessonTemplateDto>>> getLessonsByCourseTemplate(@PathVariable Long id) {
        try {
            List<LessonTemplateDto> lessons = courseTemplateService.getLessonsByCourseTemplate(id);
            return ResponseEntity.ok(ApiResponse.success(lessons));
        } catch (Exception e) {
            logger.error("Error getting lessons: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy danh sách bài học: " + e.getMessage()));
        }
    }
    
    /**
     * Add lesson to course template
     */
    @PostMapping("/{id}/lessons")
    public ResponseEntity<ApiResponse<LessonTemplateDto>> addLessonToCourseTemplate(@PathVariable Long id, @RequestBody LessonTemplateDto lessonDto) {
        try {
            if (lessonDto.getTopicName() == null || lessonDto.getTopicName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên bài học không được để trống"));
            }
            
            LessonTemplateDto createdLesson = courseTemplateService.addLessonToCourseTemplate(id, lessonDto);
            return ResponseEntity.ok(ApiResponse.success(createdLesson, "Thêm bài học thành công"));
            
        } catch (Exception e) {
            logger.error("Error adding lesson: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error("Lỗi thêm bài học: " + e.getMessage()));
        }
    }
    
    /**
     * Update lesson
     */
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonTemplateDto>> updateLesson(@PathVariable Long lessonId, @RequestBody LessonTemplateDto lessonDto) {
        try {
            if (lessonDto.getTopicName() == null || lessonDto.getTopicName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên bài học không được để trống"));
            }
            
            LessonTemplateDto updatedLesson = courseTemplateService.updateLesson(lessonId, lessonDto);
            return ResponseEntity.ok(ApiResponse.success(updatedLesson, "Cập nhật bài học thành công"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(ApiResponse.error("Không tìm thấy bài học"));
        } catch (Exception e) {
            logger.error("Error updating lesson: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error("Lỗi cập nhật bài học: " + e.getMessage()));
        }
    }
    
    /**
     * Delete lesson
     */
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<String>> deleteLesson(@PathVariable Long lessonId) {
        try {
            courseTemplateService.deleteLesson(lessonId);
            return ResponseEntity.ok(ApiResponse.success("Bài học đã được xóa"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(ApiResponse.error("Không tìm thấy bài học"));
        } catch (Exception e) {
            logger.error("Error deleting lesson: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error("Lỗi xóa bài học: " + e.getMessage()));
        }
    }
    
    /**
     * Download Excel template
     */
    @GetMapping("/download-template")
    public ResponseEntity<?> downloadTemplate() {
        try {
            byte[] excelBytes = courseTemplateService.createExcelTemplate();
            
            ByteArrayResource resource = new ByteArrayResource(excelBytes);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_template.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("Error downloading template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi tải template: " + e.getMessage()));
        }
    }
    
    /**
     * Search course templates
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CourseTemplateDto>>> searchTemplates(@RequestParam String keyword) {
        try {
            List<CourseTemplateDto> templates = courseTemplateService.searchCourseTemplates(keyword);
            return ResponseEntity.ok(ApiResponse.success(templates));
        } catch (Exception e) {
            logger.error("Error searching templates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi tìm kiếm khóa học: " + e.getMessage()));
        }
    }
    
    /**
     * Get course templates by subject
     */
    @GetMapping("/subject/{subject}")
    public ResponseEntity<ApiResponse<List<CourseTemplateDto>>> getBySubject(@PathVariable String subject) {
        try {
            List<CourseTemplateDto> templates = courseTemplateService.getCourseTemplatesBySubject(subject);
            return ResponseEntity.ok(ApiResponse.success(templates));
        } catch (Exception e) {
            logger.error("Error getting templates by subject: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy khóa học theo môn học: " + e.getMessage()));
        }
    }
    
    /**
     * Get course template counts
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCourseCount() {
        try {
            long count = courseTemplateService.getCourseTemplateCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            logger.error("Error getting course count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi đếm khóa học: " + e.getMessage()));
        }
    }
    
    private boolean isExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            return contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                   contentType.equals("application/vnd.ms-excel");
        }
        return false;
    }
    
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\-_ ]", "").trim().replace(" ", "_");
    }
    
    /**
     * ===============================
     * QUICK TEMPLATE GENERATION APIs
     * ===============================
     */
    
    /**
     * Lấy danh sách template có sẵn để tạo nhanh
     */
    @GetMapping("/quick-templates")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAvailableQuickTemplates() {
        try {
            Map<String, String> templates = quickGeneratorService.getAvailableTemplates();
            return ResponseEntity.ok(ApiResponse.success(templates, "Danh sách template tạo nhanh"));
        } catch (Exception e) {
            logger.error("Error getting quick templates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy danh sách template: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo nhanh template Toán lớp 12
     */
    @GetMapping("/quick-generate/math")
    public ResponseEntity<?> generateMathTemplate() {
        try {
            byte[] excelBytes = quickGeneratorService.generateMathGrade12Template();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Toan_Lop_12_Template.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new org.springframework.core.io.ByteArrayResource(excelBytes));
                    
        } catch (Exception e) {
            logger.error("Error generating Math template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi tạo template Toán: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo nhanh template Ngữ văn lớp 12
     */
    @GetMapping("/quick-generate/literature")
    public ResponseEntity<?> generateLiteratureTemplate() {
        try {
            byte[] excelBytes = quickGeneratorService.generateLiteratureGrade12Template();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Ngu_Van_Lop_12_Template.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new org.springframework.core.io.ByteArrayResource(excelBytes));
                    
        } catch (Exception e) {
            logger.error("Error generating Literature template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi tạo template Ngữ văn: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo nhanh template Tiếng Anh lớp 12
     */
    @GetMapping("/quick-generate/english")
    public ResponseEntity<?> generateEnglishTemplate() {
        try {
            byte[] excelBytes = quickGeneratorService.generateEnglishGrade12Template();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Tieng_Anh_Lop_12_Template.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new org.springframework.core.io.ByteArrayResource(excelBytes));
                    
        } catch (Exception e) {
            logger.error("Error generating English template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi tạo template Tiếng Anh: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo nhanh template Vật lý lớp 12
     */
    @GetMapping("/quick-generate/physics")
    public ResponseEntity<?> generatePhysicsTemplate() {
        try {
            // Tạo template Vật lý tùy chỉnh
            byte[] excelBytes = quickGeneratorService.generateCustomTemplate(
                "Vật lý lớp 12", 16, "Lý thuyết + Thí nghiệm", 90);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Vat_Ly_Lop_12_Template.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new org.springframework.core.io.ByteArrayResource(excelBytes));
                    
        } catch (Exception e) {
            logger.error("Error generating Physics template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi tạo template Vật lý: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo template tùy chỉnh
     */
    @PostMapping("/quick-generate/custom")
    public ResponseEntity<?> generateCustomTemplate(
            @RequestParam("courseName") String courseName,
            @RequestParam("totalWeeks") int totalWeeks,
            @RequestParam(value = "lessonType", defaultValue = "Lý thuyết + Thực hành") String lessonType,
            @RequestParam(value = "duration", defaultValue = "120") int duration) {
        
        try {
            // Validate input
            if (courseName == null || courseName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên khóa học không được để trống"));
            }
            
            if (totalWeeks < 1 || totalWeeks > 52) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Số tuần phải từ 1-52"));
            }
            
            if (duration < 30 || duration > 480) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Thời lượng phải từ 30-480 phút"));
            }
            
            byte[] excelBytes = quickGeneratorService.generateCustomTemplate(
                courseName, totalWeeks, lessonType, duration);
            
            String fileName = sanitizeFileName(courseName) + "_Template.xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new org.springframework.core.io.ByteArrayResource(excelBytes));
                    
        } catch (Exception e) {
            logger.error("Error generating custom template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi tạo template tùy chỉnh: " + e.getMessage()));
        }
    }
}