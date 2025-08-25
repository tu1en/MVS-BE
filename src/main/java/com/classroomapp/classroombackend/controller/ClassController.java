package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.CheckScheduleRequest;
import com.classroomapp.classroombackend.dto.ClassDto;
import com.classroomapp.classroombackend.dto.CloneClassRequest;
import com.classroomapp.classroombackend.dto.CreateClassRequest;
import com.classroomapp.classroombackend.dto.RescheduleRequest;
import com.classroomapp.classroombackend.entity.ScheduleConflict;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.service.ClassService;
import com.classroomapp.classroombackend.service.ClassStatusSchedulerService;
import com.classroomapp.classroombackend.service.ClassroomService;
import com.classroomapp.classroombackend.service.ScheduleConflictService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;  // Changed from javax.validation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/classes")
@CrossOrigin(origins = "*")
public class ClassController {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ClassController.class);
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private ScheduleConflictService scheduleConflictService;
    
    @Autowired
    private com.classroomapp.classroombackend.service.TeacherAvailabilityService teacherAvailabilityService;

    @Autowired
    private ClassroomService classroomService;
    
    /**
     * Create new class from template
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ClassDto>> createClass(@Valid @RequestBody CreateClassRequest request) {
        try {
            // Validate required fields
            if (request.getClassName() == null || request.getClassName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên lớp học không được để trống"));
            }
            
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Ngày bắt đầu và kết thúc không được để trống"));
            }
            
            ClassDto classDto = classService.createClassFromTemplate(request);
            return ResponseEntity.ok(ApiResponse.success(classDto, "Tạo lớp học thành công"));
            
        } catch (ClassService.ScheduleConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT.value())
                    .body(ApiResponse.error("Có xung đột lịch học", null));
                    
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating class: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi tạo lớp học: " + e.getMessage()));
        }
    }
    
    /**
     * Clone class
     */
    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<ClassDto>> cloneClass(
            @PathVariable Long id,
            @Valid @RequestBody CloneClassRequest request) {
        try {
            if (request.getNewClassName() == null || request.getNewClassName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tên lớp mới không được để trống"));
            }
            
            ClassDto classDto = classService.cloneClass(id, request);
            return ResponseEntity.ok(ApiResponse.success(classDto, "Clone lớp học thành công"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cloning class: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi clone lớp học: " + e.getMessage()));
        }
    }
    
    /**
     * Get all classes
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassDto>>> getAllClasses() {
        try {
            List<ClassDto> classes = classService.getAllClasses();
            return ResponseEntity.ok(ApiResponse.success(classes));
        } catch (Exception e) {
            logger.error("Error getting classes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy danh sách lớp học: " + e.getMessage()));
        }
    }
    
    /**
     * Get class by ID
     * FIXED: Improved error handling to return correct HTTP status codes
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassDto>> getClassById(@PathVariable Long id) {
        try {
            ClassDto classDto = classService.getClassById(id);
            return ResponseEntity.ok(ApiResponse.success(classDto));
        } catch (RuntimeException e) {
            // Check if it's actually a not found error or a database error
            String errorMsg = e.getMessage().toLowerCase();
            if (errorMsg.contains("not found") || errorMsg.contains("không tìm thấy")) {
                logger.warn("Class not found: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                        .body(ApiResponse.error("Không tìm thấy lớp học"));
            } else if (errorMsg.contains("deadlock") || errorMsg.contains("timeout") ||
                      errorMsg.contains("constraint") || errorMsg.contains("database")) {
                logger.error("Database error getting class {}: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .body(ApiResponse.error("Dịch vụ tạm thời không khả dụng, vui lòng thử lại"));
            } else {
                logger.error("Runtime error getting class {}: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("Unexpected error getting class {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy chi tiết lớp học: " + e.getMessage()));
        }
    }
    
    /**
     * Cập nhật thông tin lớp (dùng cho toggle công khai/học phí đơn giản)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassDto>> updateClass(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            logger.info("🔄 PUT /api/classes/{} - Thread: {} - Payload: {}",
                id, Thread.currentThread().getName(), payload);

            // Cho phép cập nhật nhẹ isPublic và tuitionFee (xử lý bên service)
            ClassDto updated = classService.updateClassPartial(id, payload);

            logger.info("✅ PUT /api/classes/{} completed successfully - Thread: {}",
                id, Thread.currentThread().getName());

            return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật lớp thành công"));
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage().toLowerCase();

            if (errorMsg.contains("not found") || errorMsg.contains("không tìm thấy")) {
                logger.warn("❌ PUT /api/classes/{} - Class not found - Thread: {}",
                    id, Thread.currentThread().getName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                        .body(ApiResponse.error("Không tìm thấy lớp học"));
            } else if (errorMsg.contains("deadlock") || errorMsg.contains("timeout") ||
                      errorMsg.contains("constraint") || errorMsg.contains("database")) {
                logger.error("❌ PUT /api/classes/{} - Database concurrency error - Thread: {} - Error: {}",
                    id, Thread.currentThread().getName(), e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .body(ApiResponse.error("Dịch vụ tạm thời không khả dụng do xung đột dữ liệu, vui lòng thử lại"));
            } else {
                logger.error("❌ PUT /api/classes/{} - Runtime error - Thread: {} - Error: {}",
                    id, Thread.currentThread().getName(), e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                        .body(ApiResponse.error("Lỗi cập nhật lớp: " + e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("❌ PUT /api/classes/{} - Unexpected error - Thread: {} - Error: {}",
                id, Thread.currentThread().getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error("Lỗi cập nhật lớp học: " + e.getMessage()));
        }
    }
    
    /**
     * Get classes by course template
     */
    @GetMapping("/template/{templateId}")
    public ResponseEntity<ApiResponse<List<ClassDto>>> getClassesByTemplate(@PathVariable Long templateId) {
        try {
            List<ClassDto> classes = classService.getClassesByCourseTemplate(templateId);
            return ResponseEntity.ok(ApiResponse.success(classes));
        } catch (Exception e) {
            logger.error("Error getting classes by template: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy lớp học theo khóa: " + e.getMessage()));
        }
    }
    
    /**
     * Get classes by teacher
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<ClassDto>>> getClassesByTeacher(@PathVariable Long teacherId) {
        try {
            List<ClassDto> classes = classService.getClassesByTeacher(teacherId);
            return ResponseEntity.ok(ApiResponse.success(classes));
        } catch (Exception e) {
            logger.error("Error getting classes by teacher: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy lớp học theo giáo viên: " + e.getMessage()));
        }
    }
    
    /**
     * Get classes by room
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<ClassDto>>> getClassesByRoom(@PathVariable Long roomId) {
        try {
            List<ClassDto> classes = classService.getClassesByRoom(roomId);
            return ResponseEntity.ok(ApiResponse.success(classes));
        } catch (Exception e) {
            logger.error("Error getting classes by room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy lớp học theo phòng: " + e.getMessage()));
        }
    }
    
    /**
     * Update class status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ClassDto>> updateClassStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Trạng thái không được để trống"));
            }
            
            ClassDto classDto = classService.updateClassStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success(classDto, "Cập nhật trạng thái lớp học thành công"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error("Trạng thái không hợp lệ"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(ApiResponse.error("Không tìm thấy lớp học"));
        } catch (Exception e) {
            logger.error("Error updating class status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi cập nhật trạng thái: " + e.getMessage()));
        }
    }

    /**
     * Trigger thủ công job cập nhật trạng thái lớp (hữu ích khi test hoặc cần đồng bộ ngay)
     */
    @PutMapping("/sync-statuses")
    public ResponseEntity<ApiResponse<String>> syncClassStatuses(
            ClassStatusSchedulerService schedulerService) {
        try {
            schedulerService.updateClassStatuses();
            return ResponseEntity.ok(ApiResponse.success("OK", "Đã chạy đồng bộ trạng thái lớp"));
        } catch (Exception e) {
            logger.error("Error syncing class statuses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi chạy đồng bộ trạng thái: " + e.getMessage()));
        }
    }

    /**
     * Đồng bộ một lớp cụ thể sang Classroom
     */
    @PutMapping("/{classId}/sync-to-classroom")
    public ResponseEntity<ApiResponse<String>> syncClassToClassroom(@PathVariable Long classId) {
        try {
            classService.syncClassToClassroom(classId);
            return ResponseEntity.ok(ApiResponse.success("OK", "Đã đồng bộ lớp sang Classroom thành công"));
        } catch (Exception e) {
            logger.error("Error syncing class {} to classroom: {}", classId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi đồng bộ lớp sang Classroom: " + e.getMessage()));
        }
    }

    /**
     * Đồng bộ tất cả lớp sang Classroom
     */
    @PutMapping("/sync-all-to-classrooms")
    public ResponseEntity<ApiResponse<String>> syncAllClassesToClassrooms() {
        try {
            classService.syncAllClassesToClassrooms();
            return ResponseEntity.ok(ApiResponse.success("OK", "Đã đồng bộ tất cả lớp sang Classroom thành công"));
        } catch (Exception e) {
            logger.error("Error syncing all classes to classrooms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi đồng bộ tất cả lớp sang Classroom: " + e.getMessage()));
        }
    }

    /**
     * Check schedule conflicts
     */
    @PostMapping("/check-schedule-conflicts")
    public ResponseEntity<ApiResponse<List<ScheduleConflict>>> checkScheduleConflicts(@Valid @RequestBody CheckScheduleRequest request) {
        try {
            List<ScheduleConflict> conflicts = (request.getClassId() != null)
                    ? scheduleConflictService.checkScheduleConflicts(
                        request.getClassId(),
                        request.getRoomId(),
                        request.getTeacherId(),
                        request.getSchedule(),
                        request.getStartDate(),
                        request.getEndDate()
                    )
                    : scheduleConflictService.checkScheduleConflicts(
                        request.getRoomId(),
                        request.getTeacherId(),
                        request.getSchedule(),
                        request.getStartDate(),
                        request.getEndDate()
                    );
            
            if (conflicts.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(conflicts, "Không có xung đột nào phát hiện"));
            } else {
                return ResponseEntity.ok(ApiResponse.success(conflicts, "Phát hiện xung đột: " + conflicts.size()));
            }
            
        } catch (Exception e) {
            logger.error("Error checking schedule conflicts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi kiểm tra xung đột lịch: " + e.getMessage()));
        }
    }

    /**
     * Get lessons of a class (class_lessons)
     */
    @GetMapping("/{id}/lessons")
    public ResponseEntity<ApiResponse<List<com.classroomapp.classroombackend.dto.ClassLessonDto>>> getClassLessons(@PathVariable Long id) {
        try {
            List<com.classroomapp.classroombackend.dto.ClassLessonDto> lessons = classService.getClassLessons(id);
            return ResponseEntity.ok(ApiResponse.success(lessons));
        } catch (Exception e) {
            logger.error("Error getting class lessons: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy danh sách buổi học: " + e.getMessage()));
        }
    }

    /**
     * Danh sách phòng trống theo khoảng ngày/giờ/ngày trong tuần
     */
    @GetMapping("/free-rooms")
    public ResponseEntity<ApiResponse<List<com.classroomapp.classroombackend.dto.RoomDto>>> getFreeRooms(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(name = "days") List<String> days) {
        try {
            List<com.classroomapp.classroombackend.dto.RoomDto> rooms = classService.findFreeRooms(
                    java.time.LocalDate.parse(startDate),
                    java.time.LocalDate.parse(endDate),
                    java.time.LocalTime.parse(startTime),
                    java.time.LocalTime.parse(endTime),
                    days
            );
            return ResponseEntity.ok(ApiResponse.success(rooms));
        } catch (Exception e) {
            logger.error("Error finding free rooms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error("Lỗi tìm phòng trống: " + e.getMessage()));
        }
    }

    /**
     * Đổi lịch lớp học, có thể tự động gán phòng trống
     */
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<ClassDto>> rescheduleClass(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequest request) {
        try {
            ClassDto dto = classService.rescheduleClass(id, request);
            return ResponseEntity.ok(ApiResponse.success(dto, "Đổi lịch thành công"));
        } catch (com.classroomapp.classroombackend.service.ClassService.ScheduleConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT.value())
                    .body(ApiResponse.error("Có xung đột lịch học", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error rescheduling class: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi đổi lịch: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách giáo viên khả dụng theo môn và lịch đã chọn
     */
    @PostMapping("/available-teachers")
    public ResponseEntity<ApiResponse<List<com.classroomapp.classroombackend.dto.AvailableTeacherDto>>> getAvailableTeachers(
            @Valid @RequestBody com.classroomapp.classroombackend.dto.AvailableTeachersRequest request) {
        try {
            List<com.classroomapp.classroombackend.dto.AvailableTeacherDto> teachers = teacherAvailabilityService.findAvailableTeachers(request);
            return ResponseEntity.ok(ApiResponse.success(teachers, "Tải danh sách giáo viên khả dụng thành công"));
        } catch (Exception e) {
            logger.error("Error finding available teachers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy giáo viên khả dụng: " + e.getMessage()));
        }
    }
    
    /**
     * Get room availability summary
     */
    @GetMapping("/room-availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomAvailability(
            @RequestParam Long roomId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            Map<String, Object> summary = scheduleConflictService.getRoomAvailabilitySummary(
                    roomId,
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate)
            );
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            logger.error("Error getting room availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi kiểm tra sẵn sàng phòng học: " + e.getMessage()));
        }
    }
    
    /**
     * Get teacher availability summary
     */
    @GetMapping("/teacher-availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTeacherAvailability(
            @RequestParam Long teacherId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            Map<String, Object> summary = scheduleConflictService.getTeacherAvailabilitySummary(
                    teacherId,
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate)
            );
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            logger.error("Error getting teacher availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi kiểm tra sẵn sàng giáo viên: " + e.getMessage()));
        }
    }
    
    /**
     * Find optimal schedule slots
     */
    @PostMapping("/find-optimal-slots")
    public ResponseEntity<ApiResponse<Map<String, Object>>> findOptimalSlots(@RequestBody Map<String, Object> request) {
        try {
            // Extract parameters from request map
            Long roomId = request.containsKey("roomId") ? Long.valueOf(request.get("roomId").toString()) : null;
            Long teacherId = request.containsKey("teacherId") ? Long.valueOf(request.get("teacherId").toString()) : null;
            String startDate = request.get("startDate").toString();
            String endDate = request.get("endDate").toString();
            
            @SuppressWarnings("unchecked")
            List<String> requiredDays = (List<String>) request.get("requiredDays");
            Map<String, Object> optimalSlots = scheduleConflictService.findOptimalSlot(
                    roomId, teacherId, requiredDays,
                    request.get("startTime").toString(), request.get("endTime").toString(),
                    LocalDate.parse(startDate), LocalDate.parse(endDate)
            );
            
            return ResponseEntity.ok(ApiResponse.success(optimalSlots));
        } catch (Exception e) {
            logger.error("Error finding optimal slots: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi tìm lịch học tối ưu: " + e.getMessage()));
        }
    }
    
    /**
     * Check template availability
     */
    @GetMapping("/template-availability/{templateId}")
    public ResponseEntity<ApiResponse<Boolean>> checkTemplateAvailability(@PathVariable Long templateId) {
        try {
            boolean available = classService.checkTemplateAvailability(templateId);
            return ResponseEntity.ok(ApiResponse.success(available));
        } catch (Exception e) {
            logger.error("Error checking template availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi kiểm tra sẵn sàng khóa học: " + e.getMessage()));
        }
    }
    
    /**
     * Get students enrolled in a class
     */
    @GetMapping("/{classId}/students")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getClassStudents(@PathVariable Long classId) {
        try {
            logger.info("Fetching students for classroom ID: {}", classId);

            // Get students from classroom service
            List<User> enrolledStudents = classroomService.getStudentsInClassroom(classId);

            // Convert to Map format for frontend compatibility
            List<Map<String, Object>> students = enrolledStudents.stream()
                .map(student -> {
                    Map<String, Object> studentMap = new HashMap<>();
                    studentMap.put("id", student.getId());
                    studentMap.put("fullName", student.getFullName());
                    studentMap.put("username", student.getUsername());
                    studentMap.put("email", student.getEmail());
                    studentMap.put("phoneNumber", student.getPhoneNumber());
                    studentMap.put("status", student.getStatus());
                    return studentMap;
                })
                .collect(Collectors.toList());

            logger.info("Successfully fetched {} students for classroom {}", students.size(), classId);
            return ResponseEntity.ok(ApiResponse.success(students));
        } catch (Exception e) {
            logger.error("Error fetching students for classroom {}: {}", classId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy danh sách học viên: " + e.getMessage()));
        }
    }
    
    /**
     * Debug endpoint to check enrollment data for Math course
     */
    @GetMapping("/debug/math-enrollments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugMathEnrollments() {
        try {
            logger.info("🔍 DEBUG: Checking Math course enrollment data");

            Map<String, Object> debugInfo = new HashMap<>();

            // Find Math classroom by name
            List<com.classroomapp.classroombackend.model.classroommanagement.Classroom> mathClassrooms =
                classroomService.SearchClassroomsByName("Toán học 12").stream()
                .map(dto -> {
                    // Convert DTO back to entity for debugging
                    com.classroomapp.classroombackend.model.classroommanagement.Classroom classroom =
                        new com.classroomapp.classroombackend.model.classroommanagement.Classroom();
                    classroom.setId(dto.getId());
                    classroom.setName(dto.getName());
                    return classroom;
                })
                .collect(Collectors.toList());

            debugInfo.put("mathClassroomsFound", mathClassrooms.size());
            debugInfo.put("mathClassrooms", mathClassrooms.stream()
                .map(c -> Map.of("id", c.getId(), "name", c.getName()))
                .collect(Collectors.toList()));

            if (!mathClassrooms.isEmpty()) {
                Long mathClassroomId = mathClassrooms.get(0).getId();
                debugInfo.put("selectedMathClassroomId", mathClassroomId);

                // Get enrolled students
                List<User> enrolledStudents = classroomService.getStudentsInClassroom(mathClassroomId);
                debugInfo.put("enrolledStudentsCount", enrolledStudents.size());
                debugInfo.put("enrolledStudents", enrolledStudents.stream()
                    .map(student -> Map.of(
                        "id", student.getId(),
                        "fullName", student.getFullName(),
                        "username", student.getUsername(),
                        "email", student.getEmail(),
                        "roleId", student.getRoleId()
                    ))
                    .collect(Collectors.toList()));
            }

            logger.info("🔍 DEBUG: Math enrollment data: {}", debugInfo);
            return ResponseEntity.ok(ApiResponse.success(debugInfo));
        } catch (Exception e) {
            logger.error("🚨 DEBUG: Error checking Math enrollments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Debug error: " + e.getMessage()));
        }
    }

    /**
     * Enroll a student in a class
     */
    @PostMapping("/{classId}/students/{studentId}/enroll")
    public ResponseEntity<ApiResponse<String>> enrollStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        try {
            // TODO: Implement actual enrollment logic
            // For now, just return success message
            
            logger.info("Student {} enrolled in class {}", studentId, classId);
            return ResponseEntity.ok(ApiResponse.success("Đã thêm học viên vào lớp thành công"));
        } catch (Exception e) {
            logger.error("Error enrolling student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi thêm học viên: " + e.getMessage()));
        }
    }
    
    /**
     * Remove a student from a class
     */
    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<ApiResponse<String>> unenrollStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        try {
            // TODO: Implement actual unenrollment logic
            // For now, just return success message
            
            logger.info("Student {} removed from class {}", studentId, classId);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa học viên khỏi lớp thành công"));
        } catch (Exception e) {
            logger.error("Error removing student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi xóa học viên: " + e.getMessage()));
        }
    }
    
    /**
     * Check schedule conflicts for a student
     */
    @PostMapping("/students/{studentId}/schedule-conflicts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStudentScheduleConflicts(
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> request) {
        try {
            // TODO: Implement actual schedule conflict checking
            // For now, simulate conflict checking
            
            Map<String, Object> response = new HashMap<>();
            
            // Mock conflict detection based on student ID
            boolean hasConflict = (studentId % 3 == 0); // Every 3rd student has conflict
            
            response.put("hasConflict", hasConflict);
            
            if (hasConflict) {
                List<Map<String, String>> conflicts = List.of(
                    Map.of(
                        "day", "monday",
                        "time", "07:30-09:30",
                        "className", "Lớp Toán Nâng cao A1"
                    )
                );
                response.put("conflicts", conflicts);
            } else {
                response.put("conflicts", List.of());
            }
            
            logger.info("Checked schedule conflicts for student {}: hasConflict={}", studentId, hasConflict);
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Error checking schedule conflicts for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi kiểm tra xung đột lịch học: " + e.getMessage()));
        }
    }
}