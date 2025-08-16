package com.classroomapp.classroombackend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.ClassDto;
import com.classroomapp.classroombackend.dto.CloneClassRequest;
import com.classroomapp.classroombackend.dto.CreateClassRequest;
import com.classroomapp.classroombackend.dto.RescheduleRequest;
import com.classroomapp.classroombackend.dto.RoomDto;
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

    @Autowired
    private ClassScheduleSyncService classScheduleSyncService;

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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mẫu khóa học"));
        
        // Validate teacher and room
        User teacher = null;
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));
        }
        // Fallback: if teacher not provided, use current authenticated user if role is TEACHER
        if (teacher == null) {
            try {
                Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                    ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
                String identity;
                if (principal instanceof UserDetails) identity = ((UserDetails) principal).getUsername();
                else identity = (principal != null) ? principal.toString() : null;
                if (identity != null) {
                    final String idStr = identity;
                    User u = userRepository.findByEmail(idStr)
                        .orElseGet(() -> userRepository.findByUsername(idStr).orElse(null));
                    if (u != null && u.getRoleId() != null && u.getRoleId() == 2) {
                        teacher = u;
                    }
                }
            } catch (Exception ignored) {}
        }
        
        Room room = null;
        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
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
        // default class public & tuition from template if available
        try {
            classEntity.setIsPublic(courseTemplate.getIsPublic());
            classEntity.setTuitionFee(courseTemplate.getEnrollmentFee());
        } catch (Exception ignored) {}
        
        classEntity = classRepository.save(classEntity);

        // Ensure a corresponding Classroom exists and is linked to the assigned teacher
        try {
            findOrCreateClassroomForClass(classEntity);
        } catch (Exception e) {
            logger.warn("Could not ensure Classroom mapping for class '{}': {}", classEntity.getClassName(), e.getMessage());
        }

        // Create class lessons from lesson templates
        createClassLessonsFromTemplate(classEntity);

        // Generate timetable events asynchronously so the request returns fast
        classScheduleSyncService.generateTimetableForClassAsync(classEntity.getId());
        
        logger.info("Created class " + classEntity.getClassName() + " with " + courseTemplate.getLessonTemplates().size() + " lessons");
        
        // Convert to DTO
        ClassDto classDto = convertToDto(classEntity);
        
        // ✅ FIX: Send WebSocket notification with full data
        try {
            webSocketNotificationService.notifyClassCreated(classDto);
            logger.info("Sent WebSocket notification for class creation: {}", classDto.getClassName());
        } catch (Exception e) {
            logger.error("Gửi thông báo WebSocket cho việc tạo lớp học thất bại", e);
            // Don't fail the whole operation if notification fails
        }
        
        return classDto;
    }

    /**
     * Tìm phòng trống trong khoảng ngày/giờ theo danh sách ngày trong tuần
     */
    public List<RoomDto> findFreeRooms(java.time.LocalDate startDate,
                                       java.time.LocalDate endDate,
                                       java.time.LocalTime startTime,
                                       java.time.LocalTime endTime,
                                       List<String> days) {
        try {
            List<Room> allActive = roomRepository.findAllActiveRooms();
            List<RoomDto> result = new ArrayList<>();

            // Lấy tất cả lớp đang hoạt động để kiểm tra trùng slot
            List<ClassEntity> classes = classRepository.findActiveAndPlanningClasses();

            Set<Integer> dayIndexes = new HashSet<>();
            if (days != null) {
                for (String d : days) dayIndexes.add(mapDayStringToIndex(d));
            }
            if (dayIndexes.isEmpty()) dayIndexes.add(0);

            for (Room r : allActive) {
                boolean occupied = false;
                for (ClassEntity c : classes) {
                    if (c.getRoom() == null || c.getRoom().getId() == null) continue;
                    if (!c.getRoom().getId().equals(r.getId())) continue;
                    if (c.getEndDate() != null && c.getEndDate().isBefore(startDate)) continue;
                    if (c.getStartDate() != null && c.getStartDate().isAfter(endDate)) continue;

                    // Kiểm tra chồng lấn ngày trong tuần + giờ
                    if (isTimeOverlapOnDays(c.getScheduleJson(), startTime, endTime, dayIndexes)) {
                        occupied = true;
                        break;
                    }
                }
                if (!occupied) {
                    RoomDto dto = new RoomDto();
                    dto.setId(r.getId());
                    dto.setRoomCode(r.getRoomCode());
                    dto.setRoomName(r.getRoomName());
                    dto.setCapacity(r.getCapacity());
                    dto.setLocation(r.getLocation());
                    dto.setFacilities(r.getFacilities());
                    dto.setIsActive(r.getIsActive());
                    dto.setStatus(r.getIsActive() != null && r.getIsActive() ? "active" : "inactive");
                    dto.setName(r.getRoomName());
                    dto.setDescription(r.getFacilities());
                    result.add(dto);
                }
            }
            // Ưu tiên theo capacity tăng dần
            result.sort(java.util.Comparator.comparing(RoomDto::getCapacity));
            return result;
        } catch (Exception e) {
            logger.error("findFreeRooms error: {}", e.getMessage());
            throw new RuntimeException("Không thể tìm phòng trống: " + e.getMessage());
        }
    }

    private boolean isTimeOverlapOnDays(String scheduleJson,
                                        java.time.LocalTime newStart,
                                        java.time.LocalTime newEnd,
                                        Set<Integer> targetDayIndexes) {
        if (scheduleJson == null || scheduleJson.isBlank()) return false;
        try {
            JsonNode node = objectMapper.readTree(scheduleJson);
            java.time.LocalTime st = node.has("startTime") ? java.time.LocalTime.parse(node.get("startTime").asText()) : null;
            java.time.LocalTime en = node.has("endTime") ? java.time.LocalTime.parse(node.get("endTime").asText()) : null;
            Set<Integer> classDays = new HashSet<>();
            if (node.has("days") && node.get("days").isArray()) {
                for (JsonNode d : node.get("days")) classDays.add(mapDayStringToIndex(d.asText()));
            }
            if (classDays.isEmpty()) classDays.add(0);

            boolean dayOverlap = classDays.stream().anyMatch(targetDayIndexes::contains);
            if (!dayOverlap) return false;
            if (st == null || en == null) return false;
            return !(en.isBefore(newStart) || !st.isBefore(newEnd));
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Reschedule lớp và tái tạo lesson + timetable, kèm auto chọn phòng nếu yêu cầu
     */
    @Transactional
    public ClassDto rescheduleClass(Long classId, RescheduleRequest request) {
        ClassEntity entity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));

        // Nếu đổi theo buổi (selectedLessonIds != null && !empty): xử lý riêng
        if (request.getSelectedLessonIds() != null && !request.getSelectedLessonIds().isEmpty()) {
            if (request.getTargetDate() == null || request.getTargetStartTime() == null || request.getTargetEndTime() == null) {
                throw new RuntimeException("Thiếu targetDate/targetStartTime/targetEndTime cho đổi lịch theo buổi");
            }
            java.time.LocalTime newStart = java.time.LocalTime.parse(request.getTargetStartTime());
            java.time.LocalTime newEnd = java.time.LocalTime.parse(request.getTargetEndTime());

            // Check conflicts GV+HS cho ngày/giờ đích
            String singleSlot = String.format("{\"startTime\":\"%s\",\"endTime\":\"%s\",\"days\":[\"%s\"]}",
                    newStart.toString(), newEnd.toString(), request.getTargetDate().getDayOfWeek().name().toLowerCase());
            List<ScheduleConflict> conflictsSlot = scheduleConflictService.checkScheduleConflicts(
                    entity.getId(), null,
                    entity.getTeacher() != null ? entity.getTeacher().getId() : null,
                    singleSlot,
                    request.getTargetDate(), request.getTargetDate()
            );
            if (!conflictsSlot.isEmpty()) throw new ScheduleConflictException("Schedule conflicts detected", conflictsSlot);

            // Auto-assign room nếu bật, lấy phòng trống theo đúng 1 ngày
            Room assignedRoom = entity.getRoom();
            if (Boolean.TRUE.equals(request.getAutoAssignRoom())) {
                List<String> days = java.util.List.of(request.getTargetDate().getDayOfWeek().name().toLowerCase());
                List<RoomDto> free = findFreeRooms(request.getTargetDate(), request.getTargetDate(), newStart, newEnd, days);
                if (request.getPreferRoomId() != null) {
                    RoomDto preferred = free.stream().filter(r -> r.getId().equals(request.getPreferRoomId())).findFirst().orElse(null);
                    if (preferred != null) assignedRoom = roomRepository.findById(preferred.getId()).orElse(assignedRoom);
                }
                if (assignedRoom == null && !free.isEmpty()) {
                    int classSize = entity.getCurrentStudents() != null ? entity.getCurrentStudents() : 0;
                    RoomDto chosen = free.stream().filter(r -> r.getCapacity() != null && r.getCapacity() >= classSize)
                            .findFirst().orElse(free.get(0));
                    assignedRoom = roomRepository.findById(chosen.getId()).orElse(null);
                }
                if (assignedRoom != null) entity.setRoom(assignedRoom);
            }

            // Cập nhật các ClassLesson được chọn
            List<ClassLesson> lessons = classLessonRepository.findAllById(request.getSelectedLessonIds());
            for (ClassLesson l : lessons) {
                if (!l.getClassEntity().getId().equals(entity.getId())) continue;
                l.setActualDate(request.getTargetDate());
                l.setActualStartTime(newStart);
                l.setActualEndTime(newEnd);
                classLessonRepository.save(l);
            }

            // Regenerate timetable (đơn giản: gọi async cho cả lớp)
            classScheduleSyncService.generateTimetableForClassAsync(entity.getId());
            ClassDto dto = convertToDto(entity);
            try { webSocketNotificationService.notifyClassUpdated(dto); } catch (Exception ignored) {}
            return dto;
        }

        // Trường hợp nâng cao: cập nhật theo danh sách lessonUpdates (mỗi buổi một thời gian riêng)
        if (request.getLessonUpdates() != null && !request.getLessonUpdates().isEmpty()) {
            // Kiểm tra conflict nội bộ trong lớp trước khi xử lý
            Map<java.time.LocalDate, java.util.List<java.util.Map.Entry<java.time.LocalTime, java.time.LocalTime>>> dateTimeMap = new java.util.HashMap<>();
            
            for (var up : request.getLessonUpdates()) {
                java.time.LocalDate d = up.getNewDate();
                java.time.LocalTime st = java.time.LocalTime.parse(up.getNewStartTime());
                java.time.LocalTime en = java.time.LocalTime.parse(up.getNewEndTime());
                
                // Kiểm tra xem có buổi nào khác trong cùng ngày có thời gian chồng lấn không
                if (dateTimeMap.containsKey(d)) {
                    for (var existingTimeRange : dateTimeMap.get(d)) {
                        java.time.LocalTime existingStart = existingTimeRange.getKey();
                        java.time.LocalTime existingEnd = existingTimeRange.getValue();
                        
                        // Kiểm tra overlap: (start1 < end2) && (start2 < end1)
                        boolean hasOverlap = st.isBefore(existingEnd) && existingStart.isBefore(en);
                        
                        if (hasOverlap) {
                            throw new RuntimeException("Không thể đổi lịch: Buổi học #" + up.getLessonId() + 
                                " trùng lịch với buổi khác trong cùng ngày " + d + 
                                " (thời gian: " + st + "-" + en + " chồng lấn với " + existingStart + "-" + existingEnd + ")");
                        }
                    }
                }
                
                // Thêm vào map để kiểm tra
                dateTimeMap.computeIfAbsent(d, k -> new java.util.ArrayList<>())
                    .add(new java.util.AbstractMap.SimpleEntry<>(st, en));
            }
            
            for (var up : request.getLessonUpdates()) {
                ClassLesson l = classLessonRepository.findById(up.getLessonId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy buổi: " + up.getLessonId()));
                if (!l.getClassEntity().getId().equals(entity.getId())) continue;
                java.time.LocalDate d = up.getNewDate();
                java.time.LocalTime st = java.time.LocalTime.parse(up.getNewStartTime());
                java.time.LocalTime en = java.time.LocalTime.parse(up.getNewEndTime());

                // Check conflicts theo slot từng buổi
                String one = String.format("{\"startTime\":\"%s\",\"endTime\":\"%s\",\"days\":[\"%s\"]}",
                        st.toString(), en.toString(), d.getDayOfWeek().name().toLowerCase());
                List<ScheduleConflict> cfs = scheduleConflictService.checkScheduleConflicts(entity.getId(), null,
                        entity.getTeacher() != null ? entity.getTeacher().getId() : null, one, d, d);
                if (!cfs.isEmpty()) throw new ScheduleConflictException("Schedule conflicts detected", cfs);

                // Auto-assign room nếu cần (áp dụng cho cả lớp — phòng classEntity)
                if (Boolean.TRUE.equals(request.getAutoAssignRoom())) {
                    List<RoomDto> free = findFreeRooms(d, d, st, en, java.util.List.of(d.getDayOfWeek().name().toLowerCase()));
                    if (request.getPreferRoomId() != null) {
                        RoomDto preferred = free.stream().filter(r -> r.getId().equals(request.getPreferRoomId())).findFirst().orElse(null);
                        if (preferred != null) entity.setRoom(roomRepository.findById(preferred.getId()).orElse(entity.getRoom()));
                    } else if (entity.getRoom() == null && !free.isEmpty()) {
                        int classSize = entity.getCurrentStudents() != null ? entity.getCurrentStudents() : 0;
                        RoomDto chosen = free.stream().filter(r -> r.getCapacity() != null && r.getCapacity() >= classSize)
                                .findFirst().orElse(free.get(0));
                        entity.setRoom(roomRepository.findById(chosen.getId()).orElse(null));
                    }
                }

                l.setActualDate(d);
                l.setActualStartTime(st);
                l.setActualEndTime(en);
                classLessonRepository.save(l);
            }

            // Lưu entity nếu phòng thay đổi và sync timetable
            classRepository.save(entity);
            classScheduleSyncService.generateTimetableForClassAsync(entity.getId());
            ClassDto dto = convertToDto(entity);
            try { webSocketNotificationService.notifyClassUpdated(dto); } catch (Exception ignored) {}
            return dto;
        }

        // Ngược lại: đổi toàn bộ theo khoảng ngày/tuần như trước
        List<ScheduleConflict> conflicts = scheduleConflictService.checkScheduleConflicts(
                entity.getId(),
                null,
                entity.getTeacher() != null ? entity.getTeacher().getId() : null,
                request.getSchedule(),
                request.getStartDate(),
                request.getEndDate()
        );
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException("Schedule conflicts detected", conflicts);
        }

        // Auto-assign room nếu bật
        Room assignedRoom = entity.getRoom();
        if (Boolean.TRUE.equals(request.getAutoAssignRoom())) {
            List<String> days = new ArrayList<>();
            try {
                JsonNode node = objectMapper.readTree(request.getSchedule());
                if (node.has("days") && node.get("days").isArray()) {
                    for (JsonNode d : node.get("days")) days.add(d.asText());
                }
            } catch (Exception ignored) {}

            java.time.LocalTime newStart;
            java.time.LocalTime newEnd;
            try {
                JsonNode sn = objectMapper.readTree(request.getSchedule());
                newStart = java.time.LocalTime.parse(sn.get("startTime").asText());
                newEnd = java.time.LocalTime.parse(sn.get("endTime").asText());
            } catch (Exception ex) {
                throw new RuntimeException("Schedule không hợp lệ: thiếu startTime/endTime");
            }

            List<RoomDto> free = findFreeRooms(request.getStartDate(), request.getEndDate(), newStart, newEnd, days);
            if (request.getPreferRoomId() != null) {
                RoomDto preferred = free.stream().filter(r -> r.getId().equals(request.getPreferRoomId())).findFirst().orElse(null);
                if (preferred != null) {
                    assignedRoom = roomRepository.findById(preferred.getId()).orElse(assignedRoom);
                }
            }
            if (assignedRoom == null && !free.isEmpty()) {
                // Ưu tiên capacity >= sĩ số
                int classSize = entity.getCurrentStudents() != null ? entity.getCurrentStudents() : 0;
                RoomDto chosen = free.stream()
                        .filter(r -> r.getCapacity() != null && r.getCapacity() >= classSize)
                        .findFirst().orElse(free.get(0));
                assignedRoom = roomRepository.findById(chosen.getId()).orElse(null);
            }
        }

        // Cập nhật entity
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setScheduleJson(request.getSchedule());
        if (Boolean.TRUE.equals(request.getAutoAssignRoom())) {
            entity.setRoom(assignedRoom);
        }
        entity = classRepository.save(entity);

        // Xóa lesson cũ và tạo lại theo lịch mới
        try {
            classLessonRepository.deleteByClassEntityId(entity.getId());
        } catch (Exception ignored) {}
        createClassLessonsFromTemplate(entity);

        // Regenerate timetable in background
        classScheduleSyncService.generateTimetableForClassAsync(entity.getId());

        // WebSocket thông báo cập nhật
        ClassDto classDto = convertToDto(entity);
        try {
            webSocketNotificationService.notifyClassUpdated(classDto);
        } catch (Exception ignored) {}
        return classDto;
    }
    
    /**
     * Clone class
     */
    @Transactional
    public ClassDto cloneClass(Long sourceClassId, CloneClassRequest request) {
        ClassEntity sourceClass = classRepository.findById(sourceClassId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp nguồn"));
        
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp với id: " + id));
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
     * Lấy danh sách buổi học của lớp
     */
    public List<com.classroomapp.classroombackend.dto.ClassLessonDto> getClassLessons(Long classId) {
        List<ClassLesson> list = classLessonRepository.findByClassEntityIdOrderByActualDateAsc(classId);
        List<com.classroomapp.classroombackend.dto.ClassLessonDto> result = new ArrayList<>();
        for (ClassLesson l : list) {
            com.classroomapp.classroombackend.dto.ClassLessonDto dto = new com.classroomapp.classroombackend.dto.ClassLessonDto();
            dto.setId(l.getId());
            dto.setClassId(l.getClassEntity() != null ? l.getClassEntity().getId() : null);
            dto.setLessonTemplateId(l.getLessonTemplate() != null ? l.getLessonTemplate().getId() : null);
            dto.setLessonTopic(l.getLessonTemplate() != null ? l.getLessonTemplate().getTopicName() : null);
            dto.setLessonType(l.getLessonTemplate() != null ? l.getLessonTemplate().getLessonType() : null);
            dto.setActualDate(l.getActualDate());
            dto.setActualStartTime(l.getActualStartTime() != null ? l.getActualStartTime().toString() : null);
            dto.setActualEndTime(l.getActualEndTime() != null ? l.getActualEndTime().toString() : null);
            dto.setStatus(l.getStatus() != null ? l.getStatus().name() : null);
            dto.setAttendanceCount(l.getAttendanceCount());
            result.add(dto);
        }
        return result;
    }

    /**
     * Cập nhật nhanh một số trường đơn giản của lớp
     */
    @Transactional
    public ClassDto updateClassPartial(Long id, java.util.Map<String, Object> payload) {
        ClassEntity entity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp với id: " + id));

        if (payload.containsKey("isPublic")) {
            try {
                entity.setIsPublic(Boolean.valueOf(payload.get("isPublic").toString()));
            } catch (Exception ignored) {}
        }
        if (payload.containsKey("tuitionFee")) {
            try {
                java.math.BigDecimal fee = new java.math.BigDecimal(payload.get("tuitionFee").toString());
                entity.setTuitionFee(fee);
            } catch (Exception ignored) {}
        }

        entity = classRepository.save(entity);
        return convertToDto(entity);
    }
    
    /**
     * Update class status
     */
    @Transactional
    public ClassDto updateClassStatus(Long id, String status) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp với id: " + id));
        
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
            logger.error("Gửi thông báo WebSocket cho cập nhật trạng thái lớp học thất bại", e);
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
            throw new RuntimeException("Tên lớp là bắt buộc");
        }
        
        if (request.getStartDate() == null) {
            throw new RuntimeException("Ngày bắt đầu là bắt buộc");
        }
        
        if (request.getEndDate() != null && request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
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

        // Parse schedule json once
        java.time.LocalTime parsedStart = null;
        java.time.LocalTime parsedEnd = null;
        java.util.List<Integer> dayIndexes = new java.util.ArrayList<>();
        if (classEntity.getScheduleJson() != null && !classEntity.getScheduleJson().isBlank()) {
            try {
                JsonNode node = objectMapper.readTree(classEntity.getScheduleJson());
                if (node.has("startTime")) parsedStart = java.time.LocalTime.parse(node.get("startTime").asText());
                if (node.has("endTime")) parsedEnd = java.time.LocalTime.parse(node.get("endTime").asText());
                if (node.has("days") && node.get("days").isArray()) {
                    for (JsonNode d : node.get("days")) dayIndexes.add(mapDayStringToIndex(d.asText()));
                }
            } catch (Exception ignored) {}
        }
        if (dayIndexes.isEmpty()) dayIndexes.add(0); // default Monday
        java.time.LocalTime st = parsedStart != null ? parsedStart : java.time.LocalTime.of(7, 30);
        java.time.LocalTime en = parsedEnd != null ? parsedEnd : st.plusMinutes(120);

        for (int i = 0; i < lessonTemplates.size(); i++) {
            LessonTemplate template = lessonTemplates.get(i);

            ClassLesson classLesson = new ClassLesson();
            classLesson.setClassEntity(classEntity);
            classLesson.setLessonTemplate(template);
            classLesson.setStatus(ClassLesson.LessonStatus.SCHEDULED);

            // Calculate date per week and align to first configured day
            java.time.LocalDate targetDate = classEntity.getStartDate().plusWeeks(i);
            int desired = dayIndexes.get(0); // 0=Mon..6=Sun
            int current = targetDate.getDayOfWeek().getValue() - 1; // 0=Mon..6=Sun
            int delta = desired - current;
            if (delta < 0) delta += 7;
            targetDate = targetDate.plusDays(delta);

            classLesson.setActualDate(targetDate);
            classLesson.setActualStartTime(st);
            classLesson.setActualEndTime(en);

            classLesson = classLessonRepository.save(classLesson);

            // Tạo bài giảng mặc định
            createDefaultLectureForLesson(classLesson, template);

            // Sự kiện sẽ được sinh ở background bởi ClassScheduleSyncService
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
            logger.warn("❌ Tạo bài giảng mặc định cho bài học '{}' thất bại: {}", template.getTopicName(), e.getMessage());
        }
    }

    // generateTimetableEventsForClass moved to ClassScheduleSyncService (async)

    private int mapDayStringToIndex(String day) {
        if (day == null) return 0;
        String d = day.trim().toLowerCase();
        switch (d) {
            case "mon": case "monday": case "mon_day": case "thu2": case "monday_vi": return 0;
            case "tue": case "tuesday": case "thu3": return 1;
            case "wed": case "wednesday": case "thu4": return 2;
            case "thu": case "thursday": case "thu5": return 3;
            case "fri": case "friday": case "thu6": return 4;
            case "sat": case "saturday": case "thu7": return 5;
            case "sun": case "sunday": case "cn": return 6;
            default: return 0;
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
                // Gán teacher nếu chưa có hoặc khác
                if (classEntity.getTeacher() != null && (found.getTeacher() == null || !found.getTeacher().getId().equals(classEntity.getTeacher().getId()))) {
                    found.setTeacher(classEntity.getTeacher());
                }
                // Bổ sung subject/description nếu trống
                if ((found.getSubject() == null || found.getSubject().isBlank()) && classEntity.getCourseTemplate() != null) {
                    found.setSubject(classEntity.getCourseTemplate().getSubject());
                }
                if (found.getDescription() == null || found.getDescription().isBlank()) {
                    found.setDescription(classEntity.getDescription() != null ? classEntity.getDescription() : ("Classroom for " + classEntity.getClassName()));
                }
                return classroomRepository.save(found);
            }
            
            // Tạo mới classroom nếu chưa có
            Classroom newClassroom = new Classroom();
            newClassroom.setName(classEntity.getClassName());
            newClassroom.setDescription(classEntity.getDescription() != null ? classEntity.getDescription() : ("Classroom for " + classEntity.getClassName()));
            if (classEntity.getCourseTemplate() != null) {
                newClassroom.setSubject(classEntity.getCourseTemplate().getSubject());
            }
            if (classEntity.getTeacher() != null) {
                newClassroom.setTeacher(classEntity.getTeacher());
            }
            
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
        dto.setIsPublic(classEntity.getIsPublic());
        dto.setTuitionFee(classEntity.getTuitionFee());
        
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