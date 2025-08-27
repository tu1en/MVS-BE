package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.absencemanagement.AbsenceDTO;
import com.classroomapp.classroombackend.dto.absencemanagement.CreateAbsenceDTO;
import com.classroomapp.classroombackend.dto.ParentRequestDto;
import com.classroomapp.classroombackend.model.ParentRequest;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.repository.ParentRequestRepository;
import com.classroomapp.classroombackend.service.AbsenceService;
import com.classroomapp.classroombackend.service.ClassroomService;
import com.classroomapp.classroombackend.service.ScheduleService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import com.classroomapp.classroombackend.constants.RoleConstants;

/**
 * Teacher-specific controller for teacher dashboard, schedule, and courses
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final UserRepository userRepository;
    private final ClassroomService classroomService;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final ScheduleService scheduleService;
    private final AbsenceService absenceService;
    private final ParentRequestRepository parentRequestRepository;
    // private final CourseTemplateService courseTemplateService; // COMMENTED OUT - Service not found

    // ==================== TEACHER COURSE SYSTEM INTEGRATION ====================
    
    /**
     * Get all courses accessible to teacher (both teaching and available for management)
     * Frontend calls: /api/teacher/course-templates
     */
    // COMMENTED OUT - CourseTemplateDto and CourseTemplateService not found
    /*
    @GetMapping("/course-templates")
    public ResponseEntity<List<CourseTemplateDto>> getTeacherCourseTemplates(Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.findByUsername(email)
                                     .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email/tên đăng nhập: " + email)));
            
            // Get all course templates that teacher can manage
            List<CourseTemplateDto> courses = courseTemplateService.getAllCourseTemplates();
            return ResponseEntity.ok(courses);
            
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    */
    
    /**
     * Get course template details for teacher management
     * Frontend calls: /api/teacher/course-templates/{id}
     */
    /*
    @GetMapping("/course-templates/{id}")
    public ResponseEntity<CourseTemplateDto> getTeacherCourseTemplate(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.findByUsername(email)
                                     .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email/tên đăng nhập: " + email)));
            
            CourseTemplateDto course = courseTemplateService.getCourseTemplateById(id);
            return ResponseEntity.ok(course);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    */
    
    /*
    @PostMapping("/course-templates")
    public ResponseEntity<CourseTemplateDto> createCourseTemplate(@RequestBody CourseTemplateDto courseDto, Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.findByUsername(email)
                                     .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email/tên đăng nhập: " + email)));
            
            // Set teacher as the creator
            courseDto.setTeacherId(currentUser.getId());
            courseDto.setTeacherName(currentUser.getFullName());
            
            CourseTemplateDto createdCourse = courseTemplateService.createCourseTemplate(courseDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    */
    
    /*
    @PutMapping("/course-templates/{id}")
    public ResponseEntity<CourseTemplateDto> updateCourseTemplate(@PathVariable Long id, @RequestBody CourseTemplateDto courseDto, Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.findByUsername(email)
                                     .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email/tên đăng nhập: " + email)));
            
            courseDto.setId(id);
            courseDto.setTeacherId(currentUser.getId());
            courseDto.setTeacherName(currentUser.getFullName());
            
            CourseTemplateDto updatedCourse = courseTemplateService.updateCourseTemplate(courseDto);
            return ResponseEntity.ok(updatedCourse);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    */
    
    /*
    @GetMapping("/course-enrollments")
    public ResponseEntity<Map<String, Object>> getTeacherCourseEnrollments(Authentication authentication) {
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.findByUsername(email)
                                     .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email/tên đăng nhập: " + email)));
            
            // Get teacher's course templates
            List<CourseTemplateDto> teacherCourses = courseTemplateService.getCourseTemplatesByTeacher(currentUser.getId());
            
            Map<String, Object> enrollmentData = new HashMap<>();
            enrollmentData.put("courses", teacherCourses);
            enrollmentData.put("totalCourses", teacherCourses.size());
            enrollmentData.put("totalEnrollments", 0); // Will be calculated when enrollment system is implemented
            
            return ResponseEntity.ok(enrollmentData);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Không thể lấy danh sách ghi danh: " + e.getMessage()));
        }
    }
    */
    @GetMapping("/schedules")
    public ResponseEntity<?> getTeacherSchedule(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

            // Parse date parameters or use default range
            LocalDate start, end;
            if (startDate != null && endDate != null) {
                start = LocalDate.parse(startDate);
                end = LocalDate.parse(endDate);
            } else {
                // Default to current month if no dates provided
                LocalDate today = LocalDate.now();
                start = today.withDayOfMonth(1);
                end = today.withDayOfMonth(today.lengthOfMonth());
            }

            System.out.println("📅 TeacherController.getTeacherSchedule: Getting schedules for user " + currentUser.getId() + " from " + start + " to " + end);

            // Use the more efficient, date-ranged query
            List<TimetableEventDto> schedules = scheduleService.getTimetableForUser(currentUser.getId(), start, end);

            System.out.println("📅 TeacherController.getTeacherSchedule: Found " + schedules.size() + " schedules");

            return ResponseEntity.ok(schedules);
            
        } catch (Exception e) {
            System.err.println("Lỗi trong getTeacherSchedule: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }
    
    /**
     * Get teacher's schedule by day
     * Frontend calls: /teacher/schedule/day/{dayOfWeek}
     */
    @GetMapping("/schedule/day/{dayOfWeek}")
    public ResponseEntity<?> getTeacherScheduleByDay(
            Authentication authentication,
            @PathVariable Integer dayOfWeek) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Validate day of week
            if (dayOfWeek < 0 || dayOfWeek > 6) {
                return ResponseEntity.badRequest().body("Giá trị ngày trong tuần phải từ 0 đến 6");
            }
            
            // Get schedules for teacher and day
            List<ScheduleDto> schedules = scheduleService.getSchedulesByTeacherAndDay(
                    currentUser.getId(), dayOfWeek);
            
            return ResponseEntity.ok(schedules);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }
    
    /**
     * Create a new schedule entry
     * Frontend calls: POST /teacher/schedule
     */
    @PostMapping("/schedule")
    public ResponseEntity<?> createSchedule(
            Authentication authentication,
            @RequestBody ScheduleDto scheduleDto) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Ensure the teacher ID is set to the current user
            scheduleDto.setTeacherId(currentUser.getId());
            
            // Create schedule
            ScheduleDto createdSchedule = scheduleService.createScheduleEntry(scheduleDto);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Get teacher's courses
     * Frontend calls: /teacher/courses
     */
    @GetMapping("/courses")
    public ResponseEntity<List<ClassroomDto>> getTeacherCourses(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Use existing classroom service
            List<ClassroomDto> courses = classroomService.GetClassroomsByTeacher(currentUser.getId());
            return ResponseEntity.ok(courses);
            
        } catch (Exception e) {
            // Return empty list if error
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * Get teacher dashboard stats
     * Frontend calls: /teacher/dashboard-stats
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getTeacherDashboardStats(Authentication authentication) {
        try {
            String email = authentication.getName();
            
            // First try to find by email since authentication.getName() returns email in this context
            User currentUser = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.findByUsername(email)
                                     .orElseThrow(() -> new ResourceNotFoundException("User not found with email/username: " + email)));
                                     
            long teacherId = currentUser.getId();
            
            // Get teacher's classrooms
            List<ClassroomDto> classrooms = classroomService.GetClassroomsByTeacher(teacherId);
            List<Long> classroomIds = classrooms.stream().map(ClassroomDto::getId).collect(Collectors.toList());
            
            long totalStudents = 0;
            long totalAssignments = 0;
            long pendingGrading = 0;
            long graded = 0;
            long totalAttendanceSessions = 0;
            Double averageAttendance = 0.0;

            if (!classroomIds.isEmpty()) {
                totalStudents = userRepository.countStudentsByClassroomIds(classroomIds);
                totalAssignments = assignmentRepository.countByClassroomIdIn(classroomIds);
                pendingGrading = submissionRepository.countPendingSubmissionsByClassroomIds(classroomIds);
                graded = submissionRepository.countGradedSubmissionsByClassroomIds(classroomIds);
                totalAttendanceSessions = attendanceSessionRepository.countByClassroomIdIn(classroomIds);
                averageAttendance = attendanceRepository.getAverageAttendanceByClassroomIds(classroomIds);
            }

            // Calculate stats
            Map<String, Object> stats = new HashMap<>();
            
            Map<String, Object> classStats = new HashMap<>();
            classStats.put("totalClasses", classrooms.size());
            classStats.put("activeClasses", classrooms.size()); 
            classStats.put("totalStudents", totalStudents);
            
            Map<String, Object> assignmentStats = new HashMap<>();
            assignmentStats.put("totalAssignments", totalAssignments);
            assignmentStats.put("pendingGrading", pendingGrading);
            assignmentStats.put("graded", graded);
            
            Map<String, Object> attendanceStats = new HashMap<>();
            attendanceStats.put("totalSessions", totalAttendanceSessions);
            averageAttendance = (averageAttendance != null) ? Math.round(averageAttendance * 10.0) / 10.0 : 0.0;
            attendanceStats.put("averageAttendance", averageAttendance);
            
            stats.put("classStats", classStats);
            stats.put("assignmentStats", assignmentStats);
            stats.put("attendanceStats", attendanceStats);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Không thể lấy thống kê dashboard: " + e.getMessage()));
        }
    }

    /**
     * Endpoint: GET /api/teacher/absences
     * Lấy danh sách đơn nghỉ phép của giáo viên hiện tại
     */
    @GetMapping("/absences")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AbsenceDTO>> getMyAbsenceRequests(Authentication authentication) {
        String principal = authentication.getName();
        User currentUser = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal).orElse(null));
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy người dùng với thông tin xác thực hiện tại");
        }
        List<AbsenceDTO> absences = absenceService.getMyAbsenceRequests(currentUser.getId());
        return ResponseEntity.ok(absences);
    }

    /**
     * Endpoint: POST /api/teacher/absences
     * Giáo viên tạo đơn nghỉ phép mới
     */
    @PostMapping("/absences")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AbsenceDTO> createAbsenceRequest(@Valid @RequestBody CreateAbsenceDTO createDto, Authentication authentication) {
        String principal = authentication.getName();
        User currentUser = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal).orElse(null));
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy người dùng với thông tin xác thực hiện tại");
        }
        AbsenceDTO createdAbsence = absenceService.createAbsenceRequest(createDto, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAbsence);
    }

    /**
     * Endpoint: GET /api/teacher/absences/{absenceId}
     * Lấy chi tiết đơn nghỉ phép theo ID (chỉ xem được đơn của chính mình)
     */
    @GetMapping("/absences/{absenceId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AbsenceDTO> getAbsenceById(@PathVariable Long absenceId, Authentication authentication) {
        String principal = authentication.getName();
        User currentUser = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal).orElse(null));
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy người dùng với thông tin xác thực hiện tại");
        }
        AbsenceDTO absence = absenceService.getAbsenceById(absenceId, currentUser.getId());
        return ResponseEntity.ok(absence);
    }

    /**
     * Get teacher's official contract status
     * Frontend calls: /api/teacher/official-contract-status
     */
    @GetMapping("/official-contract-status")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getOfficialContractStatus(Authentication authentication) {
        try {
            String principal = authentication.getName();
            User currentUser = userRepository.findByEmail(principal)
                    .orElseGet(() -> userRepository.findByUsername(principal).orElse(null));
            
            if (currentUser == null) {
                throw new RuntimeException("Không tìm thấy người dùng với thông tin xác thực hiện tại");
            }
            
            // For now, return a default status. This can be extended with actual contract logic
            Map<String, Object> status = new HashMap<>();
            status.put("hasOfficialContract", true); // Default to true, can be customized based on business logic
            status.put("userId", currentUser.getId());
            status.put("userName", currentUser.getFullName());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("hasOfficialContract", false);
            errorResponse.put("error", "Không thể lấy trạng thái hợp đồng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get parent requests for teacher's classrooms
     */
    @GetMapping("/parent-requests")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ParentRequestDto>> getParentRequestsForTeacher(Authentication authentication) {
        try {
            String principal = authentication.getName();
            User currentUser = userRepository.findByEmail(principal)
                    .orElseGet(() -> userRepository.findByUsername(principal).orElse(null));
            
            if (currentUser == null) {
                throw new RuntimeException("Không tìm thấy người dùng với thông tin xác thực hiện tại");
            }
            
            // Get teacher's classrooms
            List<ClassroomDto> classrooms = classroomService.GetClassroomsByTeacher(currentUser.getId());
            List<ParentRequestDto> allRequests = new ArrayList<>();
            
            // For each classroom, get parent requests
            for (ClassroomDto classroom : classrooms) {
                // Mock data for now - would fetch from ParentRequestRepository
                ParentRequestDto request = new ParentRequestDto();
                request.setId(1L);
                request.setStudentId(1L);
                request.setStudentName("Trần Văn A");
                request.setStudentCode("SV001");
                request.setClassroomId(classroom.getId());
                request.setClassroomName(classroom.getName());
                request.setParentName("Trần Văn B");
                request.setParentPhone("0971335989");
                request.setRequestType(ParentRequest.RequestType.LATE_ARRIVAL);
                request.setRequestDate(LocalDate.now());
                request.setStartTime("08:30");
                request.setReason("Con bị ốm nhẹ, cần đi khám bác sĩ buổi sáng");
                request.setStatus(ParentRequest.RequestStatus.PENDING);
                request.setCreatedAt(java.time.LocalDateTime.now().minusHours(1));
                request.setTeacherNotified(false);
                request.setAssistantNotified(true);
                
                allRequests.add(request);
            }
            
            return ResponseEntity.ok(allRequests);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }
    
    /**
     * Approve or reject a parent request by teacher
     */
    @PostMapping("/parent-request/{requestId}/respond")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<String> respondToParentRequestAsTeacher(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> responseData,
            Authentication authentication) {
        try {
            String principal = authentication.getName();
            User currentUser = userRepository.findByEmail(principal)
                    .orElseGet(() -> userRepository.findByUsername(principal).orElse(null));
            
            if (currentUser == null) {
                throw new RuntimeException("Không tìm thấy người dùng với thông tin xác thực hiện tại");
            }
            
            String action = (String) responseData.get("action"); // "APPROVE" or "REJECT"
            String response = (String) responseData.get("response");
            
            // This would update the ParentRequest in database
            // For now, just log the action
            System.out.println("Teacher " + currentUser.getId() + " would " + action + " parent request " + requestId + " with response: " + response);
            
            return ResponseEntity.ok("Parent request " + action.toLowerCase() + "d successfully by teacher");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error responding to parent request: " + e.getMessage());
        }
    }
    
    /**
     * Get pending parent requests count for teacher notification badge
     */
    @GetMapping("/parent-requests/pending-count")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getPendingParentRequestsCountForTeacher(Authentication authentication) {
        try {
            String principal = authentication.getName();
            User currentUser = userRepository.findByEmail(principal)
                    .orElseGet(() -> userRepository.findByUsername(principal).orElse(null));
            
            if (currentUser == null) {
                throw new RuntimeException("Không tìm thấy người dùng với thông tin xác thực hiện tại");
            }
            
            // This would count from ParentRequestRepository for teacher's classrooms
            // For now, return mock count
            Map<String, Object> result = new HashMap<>();
            result.put("pendingCount", 3);
            result.put("newCount", 2); // New requests since last check
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to get pending count"));
        }
    }
    
    /**
     * Mark parent requests as notified for teacher
     */
    @PostMapping("/parent-requests/mark-notified")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<String> markParentRequestsAsNotifiedForTeacher(Authentication authentication) {
        try {
            String principal = authentication.getName();
            User currentUser = userRepository.findByEmail(principal)
                    .orElseGet(() -> userRepository.findByUsername(principal).orElse(null));
            
            if (currentUser == null) {
                throw new RuntimeException("Không tìm thấy người dùng với thông tin xác thực hiện tại");
            }
            
            // This would update teacherNotified flag in database for teacher's classrooms
            // For now, just log the action
            System.out.println("Would mark all pending parent requests as notified for teacher " + currentUser.getId());
            
            return ResponseEntity.ok("Parent requests marked as notified for teacher");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error marking requests as notified: " + e.getMessage());
        }
    }
}
