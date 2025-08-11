package com.classroomapp.classroombackend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ClassDto;
import com.classroomapp.classroombackend.dto.response.PublicCourseTemplateDto;
import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.service.CourseTemplateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for public course template endpoints
 * These endpoints are accessible without authentication
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public/courses")
@RequiredArgsConstructor
@Slf4j
public class PublicCourseController {
    
    private final CourseTemplateService courseTemplateService;
    private final ClassRepository classRepository;
    
    /**
     * Get all public course templates with optional filtering
     */
    @GetMapping
    public ResponseEntity<List<PublicCourseTemplateDto>> getPublicCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level) {
        log.info("Fetching public course templates with search: '{}', category: '{}', level: '{}'", search, category, level);
        
        try {
            List<PublicCourseTemplateDto> courses = courseTemplateService.getPublicCourseTemplatesWithFilter(search, category, level);
            log.info("Found {} public course templates", courses.size());
            
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Error fetching public courses", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get course catalog for homepage/marketing
     */
    @GetMapping("/catalog")
    public ResponseEntity<List<PublicCourseTemplateDto>> getCourseCatalog() {
        log.info("Fetching course catalog for homepage");
        
        try {
            List<PublicCourseTemplateDto> courses = courseTemplateService.getPublicCourseTemplates();
            log.info("Found {} courses for catalog", courses.size());
            
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Error fetching course catalog", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Public list of classes (no auth). Used for catalog by class with tuition fee.
     */
    @GetMapping("/by-class")
    public ResponseEntity<List<ClassDto>> getPublicClasses() {
        try {
            List<ClassEntity> classes = classRepository.findByIsPublicTrueOrderByCreatedAtDesc();
            List<ClassDto> result = classes.stream().map(c -> {
                ClassDto dto = new ClassDto();
                dto.setId(c.getId());
                dto.setCourseTemplateId(c.getCourseTemplate() != null ? c.getCourseTemplate().getId() : null);
                dto.setCourseTemplateName(c.getCourseTemplate() != null ? c.getCourseTemplate().getName() : null);
                dto.setClassName(c.getClassName());
                dto.setDescription(c.getDescription());
                dto.setMaxStudents(c.getMaxStudents());
                dto.setCurrentStudents(c.getCurrentStudents());
                dto.setStatus(c.getStatus().name());
                dto.setCreatedAt(c.getCreatedAt());
                dto.setIsPublic(c.getIsPublic());
                dto.setTuitionFee(c.getTuitionFee());
                return dto;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching public classes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Public class detail by ID
     */
    @GetMapping("/by-class/{id}")
    public ResponseEntity<ClassDto> getPublicClassDetail(@PathVariable Long id) {
        try {
            ClassEntity c = classRepository.findByIdAndIsPublicTrue(id)
                    .orElseThrow(() -> new RuntimeException("Class not found or not public"));
            ClassDto dto = new ClassDto();
            dto.setId(c.getId());
            dto.setCourseTemplateId(c.getCourseTemplate() != null ? c.getCourseTemplate().getId() : null);
            dto.setCourseTemplateName(c.getCourseTemplate() != null ? c.getCourseTemplate().getName() : null);
            dto.setClassName(c.getClassName());
            dto.setDescription(c.getDescription());
            dto.setMaxStudents(c.getMaxStudents());
            dto.setCurrentStudents(c.getCurrentStudents());
            dto.setStatus(c.getStatus().name());
            dto.setCreatedAt(c.getCreatedAt());
            dto.setIsPublic(c.getIsPublic());
            dto.setTuitionFee(c.getTuitionFee());
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            log.warn("Public class not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching public class detail", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get public course template detail by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PublicCourseTemplateDto> getCourseDetail(@PathVariable Long id) {
        log.info("Fetching public course template detail for ID: {}", id);
        
        try {
            PublicCourseTemplateDto course = courseTemplateService.getPublicCourseTemplateDetail(id);
            log.info("Found public course template: {}", course.getName());
            
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            log.warn("Course template not found or not public: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching course detail for ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}