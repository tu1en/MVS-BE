package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.CheckScheduleRequest;
import com.classroomapp.classroombackend.dto.ClassDto;
import com.classroomapp.classroombackend.dto.CloneClassRequest;
import com.classroomapp.classroombackend.dto.CreateClassRequest;
import com.classroomapp.classroombackend.entity.ScheduleConflict;
import com.classroomapp.classroombackend.service.ClassService;
import com.classroomapp.classroombackend.service.ScheduleConflictService;

import jakarta.validation.Valid;  // Changed from javax.validation

@RestController
@RequestMapping("/api/classes")
@CrossOrigin(origins = "*")
public class ClassController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassController.class);
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private ScheduleConflictService scheduleConflictService;
    
    @Autowired
    private com.classroomapp.classroombackend.service.TeacherAvailabilityService teacherAvailabilityService;
    
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
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassDto>> getClassById(@PathVariable Long id) {
        try {
            ClassDto classDto = classService.getClassById(id);
            return ResponseEntity.ok(ApiResponse.success(classDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                    .body(ApiResponse.error("Không tìm thấy lớp học"));
        } catch (Exception e) {
            logger.error("Error getting class: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Lỗi lấy chi tiết lớp học: " + e.getMessage()));
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
     * Check schedule conflicts
     */
    @PostMapping("/check-schedule-conflicts")
    public ResponseEntity<ApiResponse<List<ScheduleConflict>>> checkScheduleConflicts(@Valid @RequestBody CheckScheduleRequest request) {
        try {
            List<ScheduleConflict> conflicts = scheduleConflictService.checkScheduleConflicts(
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
}