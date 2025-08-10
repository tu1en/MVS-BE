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
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ClassLessonRepository;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.repository.CourseTemplateRepository;
import com.classroomapp.classroombackend.repository.LessonTemplateRepository;
import com.classroomapp.classroombackend.repository.RoomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    // ✅ FIX: Inject WebSocket notification service
    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    @Autowired
    private LessonTemplateRepository lessonTemplateRepository;
    
    @Autowired
    private com.classroomapp.classroombackend.repository.LectureRepository lectureRepository;
    
    @Autowired  
    private com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository classroomRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
        
        // Nếu endDate chưa truyền, tự tính dự kiến dựa vào số bài học và số buổi/tuần đã chọn
        if (request.getEndDate() == null) {
            try {
                long lessonCount = lessonTemplateRepository.countByCourseTemplateId(courseTemplate.getId());
                int lessons = lessonCount > 0
                    ? (int) lessonCount
                    : Math.max(1, courseTemplate.getTotalWeeks());
                int sessionsPerWeek = 1;
                if (request.getSchedule() != null) {
                    JsonNode node = objectMapper.readTree(request.getSchedule());
                    if (node.has("days") && node.get("days").isArray()) {
                        sessionsPerWeek = Math.max(1, node.get("days").size());
                    }
                }
                int weeksNeeded = (int) Math.ceil((double) lessons / (double) sessionsPerWeek);
                request.setEndDate(request.getStartDate().plusWeeks(weeksNeeded - 1));
            } catch (Exception ignored) {}
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
        classEntity.setTeacher(teacher);
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
        
        // Convert to DTO
        ClassDto classDto = convertToDto(classEntity);
        
        // ✅ FIX: Send WebSocket notification with full data
        try {
            webSocketNotificationService.notifyClassCreated(classDto);
            logger.info("Sent WebSocket notification for class creation: {}", classDto.getClassName());
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification for class creation", e);
            // Don't fail the whole operation if notification fails
        }
        
        return classDto;
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
        
        ClassDto newClass = createClassFromTemplate(createRequest);
        
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
        
        ClassEntity.ClassStatus oldStatus = classEntity.getStatus();
        classEntity.setStatus(ClassEntity.ClassStatus.valueOf(status));
        classEntity = classRepository.save(classEntity);
        
        ClassDto classDto = convertToDto(classEntity);
        
        // Send WebSocket notification for status update
        try {
            webSocketNotificationService.notifyClassUpdated(classDto);
            logger.info("Sent WebSocket notification for class status update: {} -> {}", 
                       oldStatus, status);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification for class status update", e);
        }
        
        return classDto;
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
        
        if (request.getStartDate() == null) {
            throw new RuntimeException("Start date is required");
        }
        
        if (request.getEndDate() != null && request.getStartDate().isAfter(request.getEndDate())) {
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
            
            classLesson = classLessonRepository.save(classLesson);
            
            // Tự động tạo 1 bài giảng mặc định cho mỗi bài học
            createDefaultLectureForLesson(classLesson, template);
        }
    }
    
    private void createDefaultLectureForLesson(ClassLesson classLesson, LessonTemplate template) {
        try {
            // ✅ FIXED: Tạo 1 bài giảng mặc định cho mỗi bài học như yêu cầu
            // Tìm hoặc tạo Classroom tương ứng với ClassEntity này
            Classroom targetClassroom = findOrCreateClassroomForClass(classLesson.getClassEntity());
            
            if (targetClassroom != null) {
                Lecture lecture = new Lecture();
                lecture.setTitle(template.getTopicName());
                lecture.setContent("Nội dung bài giảng: " + template.getTopicName() + "\n\n" + 
                                 (template.getObjectives() != null ? template.getObjectives() : ""));
                lecture.setClassroom(targetClassroom);
                
                // Sử dụng ngày dự kiến từ classLesson nếu có
                if (classLesson.getActualDate() != null) {
                    lecture.setLectureDate(classLesson.getActualDate());
                }
                
                lecture = lectureRepository.save(lecture);
                logger.info("✅ Created default lecture '{}' for lesson '{}' in classroom '{}'", 
                    lecture.getTitle(), template.getTopicName(), targetClassroom.getName());
            } else {
                logger.warn("❌ Could not find/create classroom for class '{}', skipping lecture creation", 
                    classLesson.getClassEntity().getClassName());
            }
        } catch (Exception e) {
            logger.warn("❌ Failed to create default lecture for lesson '{}': {}", template.getTopicName(), e.getMessage());
        }
    }
    
    private Classroom findOrCreateClassroomForClass(ClassEntity classEntity) {
        try {
            // Tìm classroom có tên giống với class
            List<Classroom> existingClassrooms = classroomRepository.findAll();
            Classroom found = existingClassrooms.stream()
                .filter(c -> c.getName() != null && c.getName().equals(classEntity.getClassName()))
                .findFirst()
                .orElse(null);
                
            if (found != null) {
                return found;
            }
            
            // Tạo mới classroom nếu chưa có
            Classroom newClassroom = new Classroom();
            newClassroom.setName(classEntity.getClassName());
            newClassroom.setDescription("Classroom for " + classEntity.getClassName());
            
            return classroomRepository.save(newClassroom);
        } catch (Exception e) {
            logger.error("Error finding/creating classroom for class '{}': {}", classEntity.getClassName(), e.getMessage());
            return null;
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