package com.classroomapp.classroombackend.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.CourseTemplateDto;
import com.classroomapp.classroombackend.dto.LessonTemplateDto;
import com.classroomapp.classroombackend.dto.response.PublicCourseTemplateDto;
import com.classroomapp.classroombackend.entity.LessonTemplate;
import com.classroomapp.classroombackend.mapper.CourseTemplateMapper;
import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.repository.CourseTemplateRepository;
import com.classroomapp.classroombackend.repository.LessonTemplateRepository;

@Service
public class CourseTemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseTemplateService.class);
    
    @Autowired
    private CourseTemplateRepository courseTemplateRepository;
    
    @Autowired
    private LessonTemplateRepository lessonTemplateRepository;
    
    @Autowired
    private ExcelImportService excelImportService;
    
    @Autowired
    private CourseTemplateMapper courseTemplateMapper;
    
    // Removed unused objectMapper field
    
    /**
     * Import course template from Excel file
     */
    @Transactional
    public CourseTemplateDto importCourseFromExcel(MultipartFile file, String courseName, String description, String subject, Long createdBy) {
        try {
            CourseTemplate courseTemplate = excelImportService.importCourseFromExcel(file, courseName, createdBy);
            
            // Update additional fields if provided
            if (description != null && !description.trim().isEmpty()) {
                courseTemplate.setDescription(description);
            }
            if (subject != null && !subject.trim().isEmpty()) {
                courseTemplate.setSubject(subject);
            }
            
            courseTemplate = courseTemplateRepository.save(courseTemplate);
            
            logger.info("Successfully imported course template: {}", courseName);
            return courseTemplateMapper.toDto(courseTemplate);
            
        } catch (Exception e) {
            logger.error("Error importing course template: {}", e.getMessage());
            throw new RuntimeException("Nhập mẫu khóa học thất bại: " + e.getMessage(), e);
        }
    }
    
    /**
     * Export course template to Excel
     */
    @Transactional(readOnly = true)
    public byte[] exportCourseTemplateToExcel(Long courseTemplateId) {
        try {
            return excelImportService.exportCourseTemplateToExcel(courseTemplateId);
        } catch (Exception e) {
            logger.error("Error exporting course template: {}", e.getMessage());
            throw new RuntimeException("Xuất mẫu khóa học thất bại: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a new course template manually
     */
    @Transactional
    public CourseTemplateDto createCourseTemplate(CourseTemplateDto courseTemplateDto) {
        CourseTemplate courseTemplate = courseTemplateMapper.toEntity(courseTemplateDto);
        courseTemplate.setTotalWeeks(0); // Will be updated when lessons are added
        
        courseTemplate = courseTemplateRepository.save(courseTemplate);
        
        logger.info("Created new course template: {}", courseTemplate.getName());
        return courseTemplateMapper.toDto(courseTemplate);
    }
    
    /**
     * Get all course templates
     */
    @Transactional(readOnly = true)
    public List<CourseTemplateDto> getAllCourseTemplates() {
        List<CourseTemplate> templates = courseTemplateRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        return templates.stream()
                .map(courseTemplateMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get course template by ID
     */
    @Transactional(readOnly = true)
    public CourseTemplateDto getCourseTemplateById(Long id) {
        CourseTemplate courseTemplate = courseTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu khóa học với id: " + id));
        return courseTemplateMapper.toDto(courseTemplate);
    }
    
    /**
     * Update course template
     */
    @Transactional
    public CourseTemplateDto updateCourseTemplate(Long id, CourseTemplateDto courseTemplateDto) {
        CourseTemplate existingCourse = courseTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu khóa học với id: " + id));
        
        existingCourse.setName(courseTemplateDto.getName());
        existingCourse.setDescription(courseTemplateDto.getDescription());
        existingCourse.setSubject(courseTemplateDto.getSubject());
        // new editable fields
        if (courseTemplateDto.getIsPublic() != null) {
            existingCourse.setIsPublic(courseTemplateDto.getIsPublic());
        }
        if (courseTemplateDto.getEnrollmentFee() != null) {
            existingCourse.setEnrollmentFee(courseTemplateDto.getEnrollmentFee());
        }
        if (courseTemplateDto.getMaxStudentsPerTemplate() != null) {
            existingCourse.setMaxStudentsPerTemplate(courseTemplateDto.getMaxStudentsPerTemplate());
        }
        
        existingCourse = courseTemplateRepository.save(existingCourse);
        logger.info("Updated course template: {}", existingCourse.getName());
        return courseTemplateMapper.toDto(existingCourse);
    }
    
    /**
     * Soft delete course template
     */
    @Transactional
    public void deleteCourseTemplate(Long id) {
        CourseTemplate courseTemplate = courseTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu khóa học với id: " + id));
        
        courseTemplate.setIsActive(false);
        courseTemplateRepository.save(courseTemplate);
        
        logger.info("Soft deleted course template: {}", courseTemplate.getName());
    }
    
    /**
     * Add lesson to course template
     */
    @Transactional
    public LessonTemplateDto addLessonToCourseTemplate(Long courseTemplateId, LessonTemplateDto lessonTemplateDto) {
        CourseTemplate courseTemplate = courseTemplateRepository.findById(courseTemplateId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu khóa học với id: " + courseTemplateId));
        
        LessonTemplate lessonTemplate = new LessonTemplate();
        lessonTemplate.setCourseTemplate(courseTemplate);
        lessonTemplate.setWeekNumber(lessonTemplateDto.getWeekNumber());
        lessonTemplate.setTopicName(lessonTemplateDto.getTopicName());
        lessonTemplate.setLessonType(lessonTemplateDto.getLessonType());
        lessonTemplate.setObjectives(lessonTemplateDto.getObjectives());
        lessonTemplate.setRequirements(lessonTemplateDto.getRequirements());
        lessonTemplate.setPreparations(lessonTemplateDto.getPreparations());
        lessonTemplate.setDurationMinutes(lessonTemplateDto.getDurationMinutes());
        lessonTemplate.setSortOrder(lessonTemplateRepository.findByCourseTemplateId(courseTemplateId).size());
        
        lessonTemplate = lessonTemplateRepository.save(lessonTemplate);
        
        // Update total weeks
        Integer maxWeek = lessonTemplateRepository.findMaxWeekNumberByCourseTemplateId(courseTemplateId);
        courseTemplate.setTotalWeeks(maxWeek);
        courseTemplateRepository.save(courseTemplate);
        
        return courseTemplateMapper.lessonToDto(lessonTemplate);
    }
    
    /**
     * Get lessons for course template
     */
    @Transactional(readOnly = true)
    public List<LessonTemplateDto> getLessonsByCourseTemplate(Long courseTemplateId) {
        List<LessonTemplate> lessons = lessonTemplateRepository.findByCourseTemplateIdOrderByWeekNumberAscSortOrderAsc(courseTemplateId);
        return lessons.stream()
                .map(courseTemplateMapper::lessonToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Update lesson
     */
    @Transactional
    public LessonTemplateDto updateLesson(Long id, LessonTemplateDto lessonTemplateDto) {
        LessonTemplate lessonTemplate = lessonTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu bài học với id: " + id));
        
        lessonTemplate.setWeekNumber(lessonTemplateDto.getWeekNumber());
        lessonTemplate.setTopicName(lessonTemplateDto.getTopicName());
        lessonTemplate.setLessonType(lessonTemplateDto.getLessonType());
        lessonTemplate.setObjectives(lessonTemplateDto.getObjectives());
        lessonTemplate.setRequirements(lessonTemplateDto.getRequirements());
        lessonTemplate.setPreparations(lessonTemplateDto.getPreparations());
        lessonTemplate.setDurationMinutes(lessonTemplateDto.getDurationMinutes());
        
        lessonTemplate = lessonTemplateRepository.save(lessonTemplate);
        
        // Update total weeks for course
        CourseTemplate courseTemplate = lessonTemplate.getCourseTemplate();
        Integer maxWeek = lessonTemplateRepository.findMaxWeekNumberByCourseTemplateId(courseTemplate.getId());
        courseTemplate.setTotalWeeks(maxWeek);
        courseTemplateRepository.save(courseTemplate);
        
        return courseTemplateMapper.lessonToDto(lessonTemplate);
    }
    
    /**
     * Delete lesson
     */
    @Transactional
    public void deleteLesson(Long id) {
        LessonTemplate lessonTemplate = lessonTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu bài học với id: " + id));
        
        CourseTemplate courseTemplate = lessonTemplate.getCourseTemplate();
        lessonTemplateRepository.delete(lessonTemplate);
        
        // Update total weeks
        Integer maxWeek = lessonTemplateRepository.findMaxWeekNumberByCourseTemplateId(courseTemplate.getId());
        courseTemplate.setTotalWeeks(maxWeek != null ? maxWeek : 0);
        courseTemplateRepository.save(courseTemplate);
    }
    
    /**
     * Search course templates
     */
    @Transactional(readOnly = true)
    public List<CourseTemplateDto> searchCourseTemplates(String keyword) {
        return courseTemplateRepository.searchByNameOrSubject(keyword).stream()
                .map(courseTemplateMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get course templates by subject
     */
    @Transactional(readOnly = true)
    public List<CourseTemplateDto> getCourseTemplatesBySubject(String subject) {
        return courseTemplateRepository.findBySubjectContainingIgnoreCaseAndIsActiveTrue(subject).stream()
                .map(courseTemplateMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get course template count
     */
    @Transactional(readOnly = true)
    public long getCourseTemplateCount() {
        return courseTemplateRepository.countByIsActiveTrue();
    }

    /**
     * Get course templates by teacher
     */
    public List<CourseTemplateDto> getCourseTemplatesByTeacher(Long teacherId) {
        logger.debug("Getting course templates for teacher: {}", teacherId);
        List<CourseTemplate> templates = courseTemplateRepository.findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(teacherId);
        return templates.stream()
                .map(courseTemplateMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Create Excel template
     */
    @Transactional(readOnly = true)
    public byte[] createExcelTemplate() throws IOException {
        return excelImportService.createExcelTemplate();
    }
    
    /**
     * Get all public course templates
     */
    @Transactional(readOnly = true)
    public List<PublicCourseTemplateDto> getPublicCourseTemplates() {
        List<CourseTemplate> templates = courseTemplateRepository.findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc();
        return templates.stream()
                .map(this::convertToPublicDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get public course templates with filtering
     */
    @Transactional(readOnly = true)
    public List<PublicCourseTemplateDto> getPublicCourseTemplatesWithFilter(String search, String category, String level) {
        List<CourseTemplate> templates = courseTemplateRepository.findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc();
        
        return templates.stream()
                .map(this::convertToPublicDto)
                .filter(dto -> {
                    // Apply search filter
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.toLowerCase();
                        if (!dto.getName().toLowerCase().contains(searchLower) &&
                            !dto.getDescription().toLowerCase().contains(searchLower) &&
                            (dto.getSubject() == null || !dto.getSubject().toLowerCase().contains(searchLower))) {
                            return false;
                        }
                    }
                    
                    // Apply category filter
                    if (category != null && !category.trim().isEmpty() && !"all".equals(category)) {
                        if (dto.getSubject() == null || !dto.getSubject().toLowerCase().equals(category.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // Apply level filter
                    if (level != null && !level.trim().isEmpty() && !"all".equals(level)) {
                        // For now, we'll use a simple mapping based on totalWeeks
                        String courseLevel = "basic";
                        if (dto.getTotalWeeks() != null) {
                            if (dto.getTotalWeeks() > 16) {
                                courseLevel = "advanced";
                            } else if (dto.getTotalWeeks() > 8) {
                                courseLevel = "intermediate";
                            }
                        }
                        if (!courseLevel.equals(level.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get public course template detail by ID
     */
    @Transactional(readOnly = true)
    public PublicCourseTemplateDto getPublicCourseTemplateDetail(Long id) {
        CourseTemplate courseTemplate = courseTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu khóa học với id: " + id));
                
        if (!Boolean.TRUE.equals(courseTemplate.getIsPublic()) || !Boolean.TRUE.equals(courseTemplate.getIsActive())) {
            throw new RuntimeException("Mẫu khóa học không có sẵn để đăng ký công khai");
        }
        
        return convertToPublicDto(courseTemplate);
    }
    
    private PublicCourseTemplateDto convertToPublicDto(CourseTemplate courseTemplate) {
        // Get creator name
        String createdByName = "Unknown";
        if (courseTemplate.getCreatedBy() != null) {
            try {
                // This would need UserRepository to get the creator's name
                createdByName = "Admin"; // Simplified for now
            } catch (Exception e) {
                logger.warn("Could not fetch creator name for course template {}", courseTemplate.getId());
            }
        }
        
        return PublicCourseTemplateDto.builder()
                .id(courseTemplate.getId())
                .name(courseTemplate.getName())
                .description(courseTemplate.getDescription())
                .subject(courseTemplate.getSubject())
                .totalWeeks(courseTemplate.getTotalWeeks())
                .enrollmentFee(courseTemplate.getEnrollmentFee())
                .maxStudentsPerTemplate(courseTemplate.getMaxStudentsPerTemplate())
                .createdAt(courseTemplate.getCreatedAt())
                .createdByName(createdByName)
                .materials(Collections.emptyList()) // Simplified for now
                .build();
    }
}