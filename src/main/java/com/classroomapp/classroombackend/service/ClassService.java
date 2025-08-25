package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.ClassDto;
import com.classroomapp.classroombackend.dto.ClassLessonDto;
import com.classroomapp.classroombackend.dto.CloneClassRequest;
import com.classroomapp.classroombackend.dto.CreateClassRequest;
import com.classroomapp.classroombackend.dto.RescheduleRequest;
import com.classroomapp.classroombackend.dto.RoomDto;
import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.entity.ClassLesson;
import com.classroomapp.classroombackend.entity.LessonTemplate;
import com.classroomapp.classroombackend.entity.Room;
import com.classroomapp.classroombackend.entity.ScheduleConflict;
import com.classroomapp.classroombackend.entity.EnrollmentRequest;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.TimetableEvent;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ClassLessonRepository;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.repository.CourseTemplateRepository;
import com.classroomapp.classroombackend.repository.LessonTemplateRepository;
import com.classroomapp.classroombackend.repository.RoomRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.repository.EnrollmentRequestRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Isolation;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
    private LectureRepository lectureRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ClassScheduleSyncService classScheduleSyncService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;

    @Autowired
    private EnrollmentRequestRepository enrollmentRequestRepository;

    @Autowired
    private TimetableEventRepository timetableEventRepository;

    // Global semaphore for auto-sync operations
    private final Semaphore autoSyncSemaphore = new Semaphore(1);

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Create a new class from course template
     */
    @Transactional
    public ClassDto createClassFromTemplate(CreateClassRequest request) {
        validateCreateClassRequest(request);

        // ✅ REMOVED: Class name uniqueness validation - Allow duplicate class names
        // Multiple classes can have the same name as long as they don't have resource conflicts

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
            throw new ScheduleConflictException("Phát hiện xung đột lịch học", conflicts);
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

        // 🔄 AUTO-SYNC: Tự động đồng bộ sang Classroom system khi tạo class mới
        autoSyncClassToClassroom(classEntity.getId(), "class creation");

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
            if (!conflictsSlot.isEmpty()) throw new ScheduleConflictException("Phát hiện xung đột lịch học", conflictsSlot);

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

            // 🔄 AUTO-SYNC: Đồng bộ sang Classroom system sau khi reschedule
            autoSyncClassToClassroom(entity.getId(), "reschedule");

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
                if (!cfs.isEmpty()) throw new ScheduleConflictException("Phát hiện xung đột lịch học", cfs);

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

            // 🔄 AUTO-SYNC: Đồng bộ sang Classroom system sau khi reschedule (advanced)
            autoSyncClassToClassroom(entity.getId(), "advanced reschedule");

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
            throw new ScheduleConflictException("Phát hiện xung đột lịch học", conflicts);
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

        // 🔄 AUTO-SYNC: Đồng bộ sang Classroom system sau khi reschedule with new schedule
        autoSyncClassToClassroom(entity.getId(), "reschedule with new schedule");

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

        // 🔄 AUTO-SYNC: Đồng bộ sang Classroom system sau khi clone class
        autoSyncClassToClassroom(Long.valueOf(newClass.getId()), "class cloning");

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
        // introVideoUrl field removed - not available in ClassEntity

        entity = classRepository.save(entity);

        // 🔄 AUTO-SYNC: Đồng bộ sang Classroom system sau khi update partial
        autoSyncClassToClassroom(entity.getId(), "partial update");

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

        // 🔄 AUTO-SYNC: Đồng bộ sang Classroom system sau khi update status
        autoSyncClassToClassroom(classEntity.getId(), "status update (" + oldStatus + " -> " + status + ")");

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
            logger.info("🔍 Finding/creating classroom for class: '{}' with teacher: {}",
                classEntity.getClassName(),
                classEntity.getTeacher() != null ? classEntity.getTeacher().getFullName() : "NULL");

            // Tìm classroom có tên giống với class
            List<Classroom> existingClassrooms = classroomRepository.findAll();
            logger.info("🔍 Found {} existing classrooms in database", existingClassrooms.size());

            Classroom found = existingClassrooms.stream()
                .filter(c -> c.getName() != null && c.getName().equals(classEntity.getClassName()))
                .findFirst()
                .orElse(null);

            if (found != null) {
                logger.info("🔍 Found existing classroom: '{}' with teacher: {}",
                    found.getName(),
                    found.getTeacher() != null ? found.getTeacher().getFullName() : "NULL");

                // Gán teacher nếu chưa có hoặc khác
                if (classEntity.getTeacher() != null && (found.getTeacher() == null || !found.getTeacher().getId().equals(classEntity.getTeacher().getId()))) {
                    logger.info("🔄 Updating teacher for classroom '{}' from {} to {}",
                        found.getName(),
                        found.getTeacher() != null ? found.getTeacher().getFullName() : "NULL",
                        classEntity.getTeacher().getFullName());
                    found.setTeacher(classEntity.getTeacher());
                }
                // Bổ sung subject/description/courseId nếu trống
                if ((found.getSubject() == null || found.getSubject().isBlank()) && classEntity.getCourseTemplate() != null) {
                    found.setSubject(classEntity.getCourseTemplate().getSubject());
                }
                // ✅ FIX: Set courseId nếu chưa có
                if (found.getCourseId() == null && classEntity.getCourseTemplate() != null) {
                    found.setCourseId(classEntity.getCourseTemplate().getId());
                    logger.info("🔄 Set courseId: {}", classEntity.getCourseTemplate().getId());
                }
                if (found.getDescription() == null || found.getDescription().isBlank()) {
                    found.setDescription(classEntity.getDescription() != null ? classEntity.getDescription() : ("Classroom for " + classEntity.getClassName()));
                }
                Classroom saved = classroomRepository.save(found);
                logger.info("✅ Updated existing classroom: '{}' with teacher: {}",
                    saved.getName(),
                    saved.getTeacher() != null ? saved.getTeacher().getFullName() : "NULL");
                return saved;
            }

            // Tạo mới classroom nếu chưa có
            logger.info("🆕 Creating new classroom for class: '{}'", classEntity.getClassName());
            Classroom newClassroom = new Classroom();
            newClassroom.setName(classEntity.getClassName());
            newClassroom.setDescription(classEntity.getDescription() != null ? classEntity.getDescription() : ("Classroom for " + classEntity.getClassName()));
            if (classEntity.getCourseTemplate() != null) {
                newClassroom.setSubject(classEntity.getCourseTemplate().getSubject());
                // ✅ FIX: Set courseId cho classroom mới
                newClassroom.setCourseId(classEntity.getCourseTemplate().getId());
                logger.info("🔄 Set subject: {} and courseId: {}",
                    classEntity.getCourseTemplate().getSubject(),
                    classEntity.getCourseTemplate().getId());
            }
            if (classEntity.getTeacher() != null) {
                newClassroom.setTeacher(classEntity.getTeacher());
                logger.info("🔄 Set teacher: {}", classEntity.getTeacher().getFullName());
            }

            Classroom saved = classroomRepository.save(newClassroom);
            logger.info("✅ Created new classroom: '{}' (ID: {}) with teacher: {}",
                saved.getName(), saved.getId(),
                saved.getTeacher() != null ? saved.getTeacher().getFullName() : "NULL");
            return saved;
        } catch (Exception e) {
            logger.error("Error finding/creating classroom for class '{}': {}", classEntity.getClassName(), e.getMessage());
            return null;
        }
    }

    /**
     * Helper method để auto-sync class với error handling và concurrency control
     * FIXED: Added semaphore-based synchronization and retry mechanism to prevent race conditions
     */
    private void autoSyncClassToClassroom(Long classId, String operation) {
        // Use semaphore to limit concurrent sync operations globally
        try {
            if (!autoSyncSemaphore.tryAcquire(10, TimeUnit.SECONDS)) {
                logger.warn("⚠️ AUTO-SYNC: Timeout waiting for sync semaphore for class {} after {}", classId, operation);
                return;
            }
            try {
                ClassEntity classEntity = classRepository.findById(classId).orElse(null);
                if (classEntity == null) {
                    logger.warn("⚠️ Cannot auto-sync: Class with ID {} not found", classId);
                    return;
                }

                logger.info("🔄 Auto-syncing class '{}' after {} (Thread: {})",
                    classEntity.getClassName(), operation, Thread.currentThread().getName());

                // Add retry mechanism for deadlock handling
                retryAutoSync(classId, operation, 3);

                logger.info("✅ Auto-synced class '{}' to Classroom system after {} (Thread: {})",
                    classEntity.getClassName(), operation, Thread.currentThread().getName());
            } catch (Exception e) {
                logger.error("❌ Failed to auto-sync class after {}: {}", operation, e.getMessage(), e);
                // Don't throw exception to avoid breaking the main operation
            } finally {
                autoSyncSemaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("❌ AUTO-SYNC: Interrupted while waiting for semaphore for class {} after {}", classId, operation);
        }
    }

    /**
     * Retry mechanism for auto-sync operations to handle deadlocks
     */
    private void retryAutoSync(Long classId, String operation, int maxRetries) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                attempts++;
                logger.info("🔄 AUTO-SYNC: Attempt {} of {} for class {} after {}",
                    attempts, maxRetries, classId, operation);

                // Add small delay to prevent race conditions
                if (attempts > 1) {
                    Thread.sleep(attempts * 500); // Exponential backoff
                }

                syncClassToClassroom(classId);
                return; // Success, exit retry loop

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("❌ AUTO-SYNC: Interrupted during retry for class {} after {}", classId, operation);
                throw new RuntimeException("Auto-sync interrupted", e);
            } catch (Exception e) {
                if (attempts >= maxRetries) {
                    logger.error("❌ AUTO-SYNC: All {} attempts failed for class {} after {}: {}",
                        maxRetries, classId, operation, e.getMessage());
                    throw new RuntimeException("Auto-sync failed after " + maxRetries + " attempts", e);
                }

                // Check if it's a deadlock or concurrency issue
                String errorMsg = e.getMessage().toLowerCase();
                if (errorMsg.contains("deadlock") || errorMsg.contains("timeout") ||
                    errorMsg.contains("lock") || errorMsg.contains("constraint")) {
                    logger.warn("⚠️ AUTO-SYNC: Database concurrency issue detected, retrying (attempt {} of {}): {}",
                        attempts, maxRetries, e.getMessage());
                } else {
                    logger.error("❌ AUTO-SYNC: Non-retryable error for class {} after {}: {}",
                        classId, operation, e.getMessage());
                    throw e; // Non-retryable error
                }
            }
        }
    }

    /**
     * Đồng bộ hoàn chỉnh dữ liệu từ ClassEntity sang Classroom
     * Bao gồm: Classroom record, Lectures, Schedules, Student enrollments
     * FIXED: Added proper transaction isolation to prevent deadlocks
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 30)
    public void syncClassToClassroom(Long classId) {
        try {
            ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp với id: " + classId));

            logger.info("🔄 Bắt đầu đồng bộ lớp '{}' sang Classroom", classEntity.getClassName());

            // 1. Tạo hoặc cập nhật Classroom record
            Classroom classroom = findOrCreateClassroomForClass(classEntity);
            if (classroom == null) {
                logger.error("❌ Không thể tạo Classroom cho lớp '{}'", classEntity.getClassName());
                return;
            }

            // 2. Đồng bộ Lectures từ ClassLessons
            syncLecturesFromClassLessons(classEntity, classroom);

            // 3. Đồng bộ Schedules từ schedule JSON
            syncSchedulesFromClassEntity(classEntity, classroom);

            // 4. Đồng bộ Student enrollments (nếu có)
            syncStudentEnrollments(classEntity, classroom);

            logger.info("✅ Hoàn thành đồng bộ lớp '{}' sang Classroom với {} lectures",
                classEntity.getClassName(), classEntity.getClassLessons().size());

        } catch (Exception e) {
            logger.error("❌ Lỗi đồng bộ lớp {} sang Classroom: {}", classId, e.getMessage(), e);
        }
    }

    /**
     * Đồng bộ tất cả ClassEntity sang Classroom
     */
    @Transactional
    public void syncAllClassesToClassrooms() {
        logger.info("🔄 Bắt đầu đồng bộ tất cả lớp sang Classroom...");

        List<ClassEntity> allClasses = classRepository.findAll();
        int successCount = 0;
        int errorCount = 0;
        int skippedCount = 0;

        for (ClassEntity classEntity : allClasses) {
            try {
                // Skip classes that are cancelled or have invalid data
                if (classEntity.getStatus() == ClassEntity.ClassStatus.CANCELLED) {
                    logger.debug("⏭️ Skipping cancelled class: {}", classEntity.getClassName());
                    skippedCount++;
                    continue;
                }

                if (classEntity.getClassName() == null || classEntity.getClassName().trim().isEmpty()) {
                    logger.warn("⚠️ Skipping class with empty name: ID {}", classEntity.getId());
                    skippedCount++;
                    continue;
                }

                syncClassToClassroom(classEntity.getId());
                successCount++;

                // Add small delay to prevent overwhelming the database
                if (successCount % 10 == 0) {
                    Thread.sleep(100);
                }

            } catch (Exception e) {
                logger.error("❌ Lỗi đồng bộ lớp '{}': {}", classEntity.getClassName(), e.getMessage());
                errorCount++;
            }
        }

        logger.info("✅ Hoàn thành đồng bộ: {} thành công, {} lỗi, {} bỏ qua", successCount, errorCount, skippedCount);

        // 🔄 SYNC: Also sync all existing lectures to TimetableEvents
        logger.info("🔄 Starting additional sync of Lectures to TimetableEvents...");
        syncAllLecturesToTimetableEvents();
    }

    /**
     * Đồng bộ Lectures từ ClassLessons
     */
    private void syncLecturesFromClassLessons(ClassEntity classEntity, Classroom classroom) {
        try {
            // Xóa lectures cũ của classroom này
            List<Lecture> existingLectures = lectureRepository.findByClassroomId(classroom.getId());
            if (!existingLectures.isEmpty()) {
                lectureRepository.deleteAll(existingLectures);
                logger.debug("🗑️ Đã xóa {} lectures cũ của classroom '{}'", existingLectures.size(), classroom.getName());
            }

            // Tạo lectures mới từ ClassLessons
            List<ClassLesson> classLessons = classEntity.getClassLessons();
            if (classLessons.isEmpty()) {
                logger.warn("⚠️ Không có ClassLessons để đồng bộ cho lớp '{}'", classEntity.getClassName());
                return;
            }

            for (ClassLesson classLesson : classLessons) {
                try {
                    Lecture lecture = new Lecture();
                    lecture.setClassroom(classroom);

                    // Lấy thông tin từ LessonTemplate
                    LessonTemplate template = classLesson.getLessonTemplate();
                    String title = template != null && template.getTopicName() != null ?
                        template.getTopicName() : "Bài học " + (template != null ? template.getWeekNumber() : "");
                    String content = template != null && template.getObjectives() != null ?
                        template.getObjectives() : "";
                    Integer weekNumber = template != null ? template.getWeekNumber() : 1;

                    lecture.setTitle(title);
                    lecture.setContent(content);
                    lecture.setLectureDate(classLesson.getActualDate() != null ? classLesson.getActualDate() :
                        calculateLectureDateFromWeek(classEntity, weekNumber));
                    lecture.setCreatedAt(LocalDateTime.now());

                    lectureRepository.save(lecture);
                    logger.debug("📚 Tạo lecture '{}' cho tuần {} của classroom '{}'",
                        lecture.getTitle(), weekNumber, classroom.getName());

                    // 🔄 SYNC: Create corresponding TimetableEvent for this lecture
                    syncLectureToTimetableEvent(lecture, classroom);

                } catch (Exception e) {
                    Integer weekNum = classLesson.getLessonTemplate() != null ? classLesson.getLessonTemplate().getWeekNumber() : null;
                    logger.error("❌ Lỗi tạo lecture từ ClassLesson tuần {}: {}", weekNum, e.getMessage());
                }
            }

            logger.info("✅ Đồng bộ {} lectures cho classroom '{}'", classLessons.size(), classroom.getName());

        } catch (Exception e) {
            logger.error("❌ Lỗi đồng bộ lectures cho classroom '{}': {}", classroom.getName(), e.getMessage());
        }
    }

    /**
     * Tính toán ngày lecture từ tuần số và ngày bắt đầu lớp
     */
    private LocalDate calculateLectureDateFromWeek(ClassEntity classEntity, Integer weekNumber) {
        if (classEntity.getStartDate() == null || weekNumber == null) {
            return LocalDate.now();
        }
        return classEntity.getStartDate().plusWeeks(weekNumber - 1);
    }

    /**
     * 🔄 SYNC: Convert Lecture to TimetableEvent for teacher timetable display
     */
    private void syncLectureToTimetableEvent(Lecture lecture, Classroom classroom) {
        try {
            // Get schedule information for time
            LocalTime startTime = LocalTime.of(8, 0); // Default start time
            LocalTime endTime = LocalTime.of(10, 0);  // Default end time

            // Try to get actual schedule times from classroom schedules
            if (!classroom.getSchedules().isEmpty()) {
                Schedule schedule = classroom.getSchedules().get(0); // Get first schedule
                if (schedule.getStartTime() != null) {
                    startTime = schedule.getStartTime();
                }
                if (schedule.getEndTime() != null) {
                    endTime = schedule.getEndTime();
                }
            }

            LocalDateTime startDateTime = lecture.getLectureDate().atTime(startTime);
            LocalDateTime endDateTime = lecture.getLectureDate().atTime(endTime);

            // Check for existing TimetableEvent to avoid duplicates
            List<TimetableEvent> existingEvents = timetableEventRepository.findByClassroomIdAndStartDatetime(
                classroom.getId(), startDateTime);

            if (!existingEvents.isEmpty()) {
                logger.debug("⏭️ TimetableEvent already exists for lecture '{}' on {}", lecture.getTitle(), lecture.getLectureDate());
                return;
            }

            // Create new TimetableEvent
            TimetableEvent event = new TimetableEvent();
            event.setTitle(lecture.getTitle());
            event.setDescription(lecture.getContent());
            event.setStartDatetime(startDateTime);
            event.setEndDatetime(endDateTime);
            event.setEventType(TimetableEvent.EventType.CLASS);
            event.setClassroomId(classroom.getId());
            event.setLectureId(lecture.getId()); // Link to lecture for attendance navigation
            event.setCreatedBy(classroom.getTeacher() != null ? classroom.getTeacher().getId() : 1L);
            event.setLocation(classroom.getName());
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());

            timetableEventRepository.save(event);
            logger.debug("📅 Created TimetableEvent '{}' for lecture on {}", event.getTitle(), lecture.getLectureDate());

        } catch (Exception e) {
            logger.error("❌ Error creating TimetableEvent for lecture '{}': {}", lecture.getTitle(), e.getMessage());
        }
    }

    /**
     * 🔄 SYNC: Convert all existing Lectures to TimetableEvents
     */
    public void syncAllLecturesToTimetableEvents() {
        logger.info("🔄 Starting sync of all existing Lectures to TimetableEvents...");

        try {
            List<Lecture> allLectures = lectureRepository.findAll();
            int successCount = 0;
            int skippedCount = 0;
            int errorCount = 0;

            for (Lecture lecture : allLectures) {
                try {
                    Classroom classroom = lecture.getClassroom();
                    if (classroom == null) {
                        logger.warn("⚠️ Skipping lecture '{}' - no associated classroom", lecture.getTitle());
                        skippedCount++;
                        continue;
                    }

                    syncLectureToTimetableEvent(lecture, classroom);
                    successCount++;

                } catch (Exception e) {
                    logger.error("❌ Error syncing lecture '{}' to TimetableEvent: {}", lecture.getTitle(), e.getMessage());
                    errorCount++;
                }
            }

            logger.info("✅ Completed sync: {} success, {} skipped, {} errors out of {} total lectures",
                successCount, skippedCount, errorCount, allLectures.size());

        } catch (Exception e) {
            logger.error("❌ Error during bulk sync of Lectures to TimetableEvents: {}", e.getMessage());
        }
    }

    /**
     * Đồng bộ Schedules từ schedule JSON của ClassEntity
     */
    private void syncSchedulesFromClassEntity(ClassEntity classEntity, Classroom classroom) {
        try {
            // Xóa schedules cũ của classroom này
            List<Schedule> existingSchedules = scheduleRepository.findByClassroomId(classroom.getId());
            if (!existingSchedules.isEmpty()) {
                scheduleRepository.deleteAll(existingSchedules);
                logger.debug("🗑️ Đã xóa {} schedules cũ của classroom '{}'", existingSchedules.size(), classroom.getName());
            }

            // Parse schedule JSON
            String scheduleJson = classEntity.getScheduleJson();
            logger.info("🔍 DEBUG: Schedule JSON for class '{}': {}", classEntity.getClassName(), scheduleJson);

            if (scheduleJson == null || scheduleJson.trim().isEmpty()) {
                logger.warn("⚠️ Không có schedule JSON để đồng bộ cho lớp '{}', tạo default schedule", classEntity.getClassName());
                createDefaultScheduleForClass(classEntity, classroom);
                return;
            }

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode scheduleNode = objectMapper.readTree(scheduleJson);
                logger.info("🔍 DEBUG: Parsed schedule node: {}", scheduleNode);

                JsonNode daysNode = scheduleNode.get("days");
                logger.info("🔍 DEBUG: Days node: {}", daysNode);

                String startTimeStr = scheduleNode.has("startTime") ? scheduleNode.get("startTime").asText() : "07:30";
                String endTimeStr = scheduleNode.has("endTime") ? scheduleNode.get("endTime").asText() : "09:30";
                logger.info("🔍 DEBUG: Time range: {} - {}", startTimeStr, endTimeStr);

                LocalTime startTime = LocalTime.parse(startTimeStr);
                LocalTime endTime = LocalTime.parse(endTimeStr);

                if (daysNode != null && daysNode.isArray()) {
                    logger.info("🔍 DEBUG: Processing {} days from schedule", daysNode.size());
                    for (JsonNode dayNode : daysNode) {
                        String dayName = dayNode.asText().toUpperCase();
                        Integer dayOfWeek = convertDayNameToDayOfWeek(dayName);
                        logger.info("🔍 DEBUG: Day '{}' -> dayOfWeek: {}", dayName, dayOfWeek);

                        if (dayOfWeek != null) {
                            Schedule schedule = new Schedule();
                            schedule.setClassroom(classroom);
                            schedule.setTeacher(classroom.getTeacher());
                            schedule.setDayOfWeek(dayOfWeek);
                            schedule.setStartTime(startTime);
                            schedule.setEndTime(endTime);
                            schedule.setSubject(classroom.getSubject() != null ? classroom.getSubject() : "Học tập");
                            schedule.setRoom(classEntity.getRoom() != null ? classEntity.getRoom().getRoomName() : "");

                            scheduleRepository.save(schedule);
                            logger.info("✅ Tạo schedule cho {} từ {} đến {} của classroom '{}'",
                                dayName, startTime, endTime, classroom.getName());
                        } else {
                            logger.warn("⚠️ Không thể convert day name '{}' thành dayOfWeek", dayName);
                        }
                    }
                } else {
                    logger.warn("⚠️ Days node is null or not an array for class '{}'", classEntity.getClassName());
                }

                logger.info("✅ Đồng bộ schedules cho classroom '{}'", classroom.getName());

            } catch (Exception e) {
                logger.error("❌ Lỗi parse schedule JSON cho lớp '{}': {}", classEntity.getClassName(), e.getMessage());
            }

        } catch (Exception e) {
            logger.error("❌ Lỗi đồng bộ schedules cho classroom '{}': {}", classroom.getName(), e.getMessage());
        }
    }

    /**
     * Convert day name to day of week number (0=Monday, 6=Sunday)
     */
    private Integer convertDayNameToDayOfWeek(String dayName) {
        switch (dayName.toUpperCase()) {
            case "MONDAY": case "MON": case "THU_HAI": return 0;
            case "TUESDAY": case "TUE": case "THU_BA": return 1;
            case "WEDNESDAY": case "WED": case "THU_TU": return 2;
            case "THURSDAY": case "THU": case "THU_NAM": return 3;
            case "FRIDAY": case "FRI": case "THU_SAU": return 4;
            case "SATURDAY": case "SAT": case "THU_BAY": return 5;
            case "SUNDAY": case "SUN": case "CHU_NHAT": return 6;
            default:
                logger.warn("⚠️ Không nhận diện được ngày: {}", dayName);
                return null;
        }
    }

    /**
     * Tạo default schedule cho class khi không có schedule JSON
     */
    private void createDefaultScheduleForClass(ClassEntity classEntity, Classroom classroom) {
        try {
            logger.info("🔧 Tạo default schedule cho class '{}'", classEntity.getClassName());

            // Tạo default schedule: Thứ 2, 4, 6 từ 7:30-9:30
            String[] defaultDays = {"MON", "WED", "FRI"};
            LocalTime defaultStartTime = LocalTime.of(7, 30);
            LocalTime defaultEndTime = LocalTime.of(9, 30);

            for (String dayName : defaultDays) {
                Integer dayOfWeek = convertDayNameToDayOfWeek(dayName);

                if (dayOfWeek != null) {
                    Schedule schedule = new Schedule();
                    schedule.setClassroom(classroom);
                    schedule.setTeacher(classroom.getTeacher());
                    schedule.setDayOfWeek(dayOfWeek);
                    schedule.setStartTime(defaultStartTime);
                    schedule.setEndTime(defaultEndTime);
                    schedule.setSubject(classroom.getSubject() != null ? classroom.getSubject() : "Học tập");
                    schedule.setRoom(classEntity.getRoom() != null ? classEntity.getRoom().getRoomName() : "");

                    scheduleRepository.save(schedule);
                    logger.info("✅ Tạo default schedule cho {} từ {} đến {} của classroom '{}'",
                        dayName, defaultStartTime, defaultEndTime, classroom.getName());
                }
            }

            logger.info("🎯 Hoàn thành tạo {} default schedules cho class '{}'", defaultDays.length, classEntity.getClassName());

        } catch (Exception e) {
            logger.error("❌ Lỗi khi tạo default schedule cho class '{}': {}", classEntity.getClassName(), e.getMessage());
        }
    }

    /**
     * Đồng bộ Student enrollments từ ClassEntity sang Classroom
     * Hiện tại ClassEntity không có direct student enrollments,
     * nhưng có thể có thông qua EnrollmentRequest hoặc currentStudents count
     */
    private void syncStudentEnrollments(ClassEntity classEntity, Classroom classroom) {
        try {
            // Tìm các enrollment requests đã được approve cho course template này
            List<EnrollmentRequest> approvedRequests = enrollmentRequestRepository
                .findByCourseTemplateIdAndStatus(classEntity.getCourseTemplate().getId(),
                    EnrollmentRequest.EnrollmentStatus.APPROVED);

            if (approvedRequests.isEmpty()) {
                logger.debug("📝 Không có enrollment requests được approve cho lớp '{}'", classEntity.getClassName());
                return;
            }

            // Xóa enrollments cũ của classroom này
            List<ClassroomEnrollment> existingEnrollments = classroomEnrollmentRepository.findByClassroomId(classroom.getId());
            if (!existingEnrollments.isEmpty()) {
                classroomEnrollmentRepository.deleteAll(existingEnrollments);
                logger.debug("🗑️ Đã xóa {} enrollments cũ của classroom '{}'", existingEnrollments.size(), classroom.getName());
            }

            // Tạo enrollments mới từ approved requests
            int enrolledCount = 0;
            for (EnrollmentRequest request : approvedRequests) {
                try {
                    // Kiểm tra xem student đã enrolled chưa
                    ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroom.getId(), request.getStudent().getId());
                    boolean alreadyExists = classroomEnrollmentRepository.existsById(enrollmentId);

                    if (!alreadyExists) {
                        ClassroomEnrollment enrollment = new ClassroomEnrollment();
                        enrollment.setId(enrollmentId);
                        enrollment.setClassroom(classroom);
                        enrollment.setUser(request.getStudent());
                        enrollment.setEnrollmentDate(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now());

                        classroomEnrollmentRepository.save(enrollment);
                        enrolledCount++;

                        logger.debug("👨‍🎓 Enrolled student '{}' vào classroom '{}'",
                            request.getStudent().getUsername(), classroom.getName());
                    }

                } catch (Exception e) {
                    logger.error("❌ Lỗi enroll student '{}' vào classroom '{}': {}",
                        request.getStudent().getUsername(), classroom.getName(), e.getMessage());
                }
            }

            logger.info("✅ Đồng bộ {} student enrollments cho classroom '{}'", enrolledCount, classroom.getName());

        } catch (Exception e) {
            logger.error("❌ Lỗi đồng bộ student enrollments cho classroom '{}': {}", classroom.getName(), e.getMessage());
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
            // Fix: Use fullName if available, fallback to username, then role description
            String teacherName = classEntity.getTeacher().getFullName();
            if (teacherName == null || teacherName.trim().isEmpty()) {
                teacherName = classEntity.getTeacher().getUsername();
                if (teacherName == null || teacherName.trim().isEmpty()) {
                    teacherName = "Giảng viên " + classEntity.getTeacher().getId();
                }
            }
            dto.setTeacherName(teacherName);
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

        // ✅ FIX: Populate classLessons to ensure data consistency
        try {
            List<com.classroomapp.classroombackend.dto.ClassLessonDto> classLessons = getClassLessons(classEntity.getId());
            dto.setClassLessons(classLessons);
        } catch (Exception e) {
            logger.warn("Could not load class lessons for class {}: {}", classEntity.getId(), e.getMessage());
            dto.setClassLessons(new ArrayList<>());
        }

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