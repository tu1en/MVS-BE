package com.classroomapp.classroombackend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.ClassDto;
import com.classroomapp.classroombackend.dto.CloneClassRequest;
import com.classroomapp.classroombackend.dto.CreateClassRequest;
import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.entity.ClassLesson;
import com.classroomapp.classroombackend.entity.LessonTemplate;
import com.classroomapp.classroombackend.entity.Room;
import com.classroomapp.classroombackend.entity.ScheduleConflict;
import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ClassLessonRepository;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.repository.CourseTemplateRepository;
import com.classroomapp.classroombackend.repository.RoomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Service
public class ClassService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassService.class);
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private CourseTemplateRepository courseTemplateRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    
    @Autowired
    private ClassLessonRepository classLessonRepository;
    
    @Autowired
    private ScheduleConflictService scheduleConflictService;
    
    
    /**
     * Create a new class from course template
     */
    @Transactional
    public ClassDto createClassFromTemplate(CreateClassRequest request) {
        validateCreateClassRequest(request);
        
        // Check if class name already exists
        if (classRepository.existsByClassName(request.getClassName())) {
            throw new RuntimeException("Class name already exists: " + request.getClassName());
        }
        
        // Find course template
        CourseTemplate courseTemplate = courseTemplateRepository.findById(request.getCourseTemplateId())
                .orElseThrow(() -> new RuntimeException("Course template not found"));
        
        // Validate teacher and room
        User teacher = null;
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
        }
        
        Room room = null;
        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));
        }
        
        // Check schedule conflicts
        List<ScheduleConflict> conflicts = scheduleConflictService.checkScheduleConflicts(
            request.getRoomId(), 
            request.getTeacherId(),
            request.getSchedule(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException("Schedule conflicts detected", conflicts);
        }
        
        // Create class
        ClassEntity classEntity = new ClassEntity();
        classEntity.setCourseTemplate(courseTemplate);
        classEntity.setClassName(request.getClassName());
        classEntity.setDescription(request.getDescription());
        classEntity.setTeacher(teacher); // Fixed: removed duplicate "setTeacher"
        classEntity.setRoom(room);
        classEntity.setStartDate(request.getStartDate());
        classEntity.setEndDate(request.getEndDate());
        classEntity.setScheduleJson(request.getSchedule());
        classEntity.setMaxStudents(request.getMaxStudents());
        classEntity.setCreatedBy(request.getCreatedBy());
        classEntity.setStatus(ClassEntity.ClassStatus.PLANNING);
        
        classEntity = classRepository.save(classEntity);
        
        // Create class lessons from lesson templates
        createClassLessonsFromTemplate(classEntity);
        
        logger.info("Created class " + classEntity.getClassName() + " with " + courseTemplate.getLessonTemplates().size() + " lessons");
        
        return convertToDto(classEntity);
    }
    
    /**
     * Clone class
     */
    @Transactional
    public ClassDto cloneClass(Long sourceClassId, CloneClassRequest request) {
        ClassEntity sourceClass = classRepository.findById(sourceClassId)
                .orElseThrow(() -> new RuntimeException("Source class not found"));
        
        // Create new class request
        CreateClassRequest createRequest = new CreateClassRequest();
        createRequest.setCourseTemplateId(sourceClass.getCourseTemplate().getId());
        createRequest.setClassName(request.getNewClassName());
        createRequest.setDescription(sourceClass.getDescription());
        createRequest.setTeacherId(request.getNewTeacherId());
        createRequest.setRoomId(request.getNewRoomId());
        createRequest.setStartDate(request.getNewStartDate());
        createRequest.setEndDate(request.getNewEndDate());
        createRequest.setSchedule(request.getNewSchedule());
        createRequest.setMaxStudents(sourceClass.getMaxStudents());
        createRequest.setCreatedBy(request.getCreatedBy());
        
        ClassDto newClass = createClassFromTemplate(createRequest); // Fixed: return ClassDto, not ClassEntity
        
        // Copy materials from template (if needed)
        // copyMaterialsFromTemplate(sourceClass, newClass);
        
        return newClass;
    }
    
    /**
     * Get all classes
     */
    public List<ClassDto> getAllClasses() {
        List<ClassEntity> classes = classRepository.findAll();
        return classes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get class by ID
     */
    public ClassDto getClassById(Long id) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
        return convertToDto(classEntity);
    }
    
    /**
     * Get classes by course template
     */
    public List<ClassDto> getClassesByCourseTemplate(Long courseTemplateId) {
        List<ClassEntity> classes = classRepository.findByCourseTemplateIdOrderByCreatedAtDesc(courseTemplateId);
        return classes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get classes by teacher
     */
    public List<ClassDto> getClassesByTeacher(Long teacherId) {
        List<ClassEntity> classes = classRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
        return classes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get classes by room
     */
    public List<ClassDto> getClassesByRoom(Long roomId) {
        List<ClassEntity> classes = classRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        return classes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Update class status
     */
    @Transactional
    public ClassDto updateClassStatus(Long id, String status) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));
        
        classEntity.setStatus(ClassEntity.ClassStatus.valueOf(status));
        classEntity = classRepository.save(classEntity);
        
        return convertToDto(classEntity);
    }
    
    /**
     * Check template availability
     */
    public boolean checkTemplateAvailability(Long courseTemplateId) {
        CourseTemplate courseTemplate = courseTemplateRepository.findById(courseTemplateId)
                .orElse(null);
        return courseTemplate != null && courseTemplate.getIsActive();
    }
    
    // Helper methods
    
    private void validateCreateClassRequest(CreateClassRequest request) {
        if (request.getClassName() == null || request.getClassName().trim().isEmpty()) {
            throw new RuntimeException("Class name is required");
        }
        
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new RuntimeException("Start and end dates are required");
        }
        
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }
        
        if (request.getMaxStudents() == null || request.getMaxStudents() <= 0) {
            request.setMaxStudents(30);
        }
    }
    
    private void createClassLessonsFromTemplate(ClassEntity classEntity) {
        List<LessonTemplate> lessonTemplates = classEntity.getCourseTemplate().getLessonTemplates()
                .stream()
                .sorted(java.util.Comparator.comparing(LessonTemplate::getWeekNumber)
                        .thenComparing(LessonTemplate::getSortOrder))
                .collect(Collectors.toList());
        
        for (int i = 0; i < lessonTemplates.size(); i++) {
            LessonTemplate template = lessonTemplates.get(i);
            
            ClassLesson classLesson = new ClassLesson();
            classLesson.setClassEntity(classEntity);
            classLesson.setLessonTemplate(template);
            classLesson.setStatus(ClassLesson.LessonStatus.SCHEDULED);
            
            classLessonRepository.save(classLesson);
        }
    }
    
    private ClassDto convertToDto(ClassEntity classEntity) {
        ClassDto dto = new ClassDto();
        dto.setId(classEntity.getId());
        dto.setCourseTemplateId(classEntity.getCourseTemplate().getId());
        dto.setCourseTemplateName(classEntity.getCourseTemplate().getName());
        dto.setClassName(classEntity.getClassName());
        dto.setDescription(classEntity.getDescription());
        
        if (classEntity.getTeacher() != null) {
            dto.setTeacherId(classEntity.getTeacher().getId());
            dto.setTeacherName(classEntity.getTeacher().getFullName());
        }
        
        if (classEntity.getRoom() != null) {
            dto.setRoomId(classEntity.getRoom().getId());
            dto.setRoomCode(classEntity.getRoom().getRoomCode());
            dto.setRoomName(classEntity.getRoom().getRoomName());
        }
        
        dto.setStartDate(classEntity.getStartDate());
        dto.setEndDate(classEntity.getEndDate());
        dto.setSchedule(classEntity.getScheduleJson());
        dto.setMaxStudents(classEntity.getMaxStudents());
        dto.setCurrentStudents(classEntity.getCurrentStudents());
        dto.setStatus(classEntity.getStatus().name());
        dto.setCreatedBy(classEntity.getCreatedBy());
        dto.setCreatedAt(classEntity.getCreatedAt());
        
        return dto;
    }
    
    public static class ScheduleConflictException extends RuntimeException {
        private final List<ScheduleConflict> conflicts;
        
        public ScheduleConflictException(String message, List<ScheduleConflict> conflicts) {
            super(message);
            this.conflicts = conflicts;
        }
        
        public List<ScheduleConflict> getConflicts() {
            return conflicts;
        }
    }
}