package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.dto.AnnouncementDto;
import com.classroomapp.classroombackend.dto.CreateAnnouncementDto;
import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.dto.absencemanagement.AbsenceDTO;
import com.classroomapp.classroombackend.dto.absencemanagement.CreateAbsenceDTO;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AbsenceService;
import com.classroomapp.classroombackend.service.AnnouncementService;
import com.classroomapp.classroombackend.service.ScheduleService;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {
    private final AnnouncementService announcementService;

    private final UserRepository userRepository;
    private final ClassroomService classroomService;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final ScheduleService scheduleService;
    private final AbsenceService absenceService;
    private final ContractRepository contractRepository;

    // ================================
    // SCHEDULE ENDPOINTS - RENAMED TO AVOID CONFLICT
    // ================================
    @GetMapping("/my-schedules")
    public ResponseEntity<?> getMySchedules(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.withDayOfMonth(1);
            LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

            List<TimetableEventDto> schedules = scheduleService.getTimetableForUser(currentUser.getId(), startDate, endDate);

            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/schedule/day/{dayOfWeek}")
    public ResponseEntity<?> getTeacherScheduleByDay(Authentication authentication, @PathVariable Integer dayOfWeek) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

            if (dayOfWeek < 0 || dayOfWeek > 6) {
                return ResponseEntity.badRequest().body("Day of week must be between 0 and 6");
            }

            List<ScheduleDto> schedules = scheduleService.getSchedulesByTeacherAndDay(currentUser.getId(), dayOfWeek);

            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> createSchedule(Authentication authentication, @RequestBody ScheduleDto scheduleDto) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

            scheduleDto.setTeacherId(currentUser.getId());

            ScheduleDto createdSchedule = scheduleService.createScheduleEntry(scheduleDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // ================================
    // COURSE ENDPOINTS
    // ================================
    @GetMapping("/courses")
    public ResponseEntity<List<ClassroomDto>> getTeacherCourses(Authentication authentication) {
        try {
            List<ClassroomDto> courses = classroomService.getClassroomsByCurrentTeacher();
            return ResponseEntity.ok(courses);

        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ================================
    // DASHBOARD STATS ENDPOINT - CONSOLIDATED
    // ================================
    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getTeacherDashboardStats(Authentication authentication) {
        log.info("Teacher requesting dashboard stats");
        
        try {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.findByUsername(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email)));

            List<ClassroomDto> classrooms = classroomService.getClassroomsByCurrentTeacher();
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

            // ✅ Consolidated stats with both real data and mock data for missing features
            Map<String, Object> stats = new HashMap<>();
            stats.put("classStats", Map.of(
                "totalClasses", classrooms.size(), 
                "activeClasses", classrooms.size(), 
                "totalStudents", totalStudents
            ));
            stats.put("assignmentStats", Map.of(
                "totalAssignments", totalAssignments, 
                "pendingGrading", pendingGrading, 
                "graded", graded
            ));
            stats.put("attendanceStats", Map.of(
                "totalSessions", totalAttendanceSessions, 
                "averageAttendance", averageAttendance != null ? Math.round(averageAttendance * 10.0) / 10.0 : 0.0
            ));
            
            // Mock data for features not yet implemented
            stats.put("weeklyShifts", 8);
            stats.put("monthlyHours", 120);
            stats.put("attendanceScore", 95);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error getting teacher dashboard stats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unable to fetch dashboard stats"));
        }
    }

    // ================================
    // LEAVE REQUEST ENDPOINTS - FIXED PATHS
    // ================================
    @GetMapping("/leave-requests")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AbsenceDTO>> getMyLeaveRequests(Authentication authentication) {
        log.info("Teacher requesting leave requests");
        
        try {
            String principal = authentication.getName();
            User currentUser = userRepository.findByEmail(principal)
                    .orElseGet(() -> userRepository.findByUsername(principal)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal)));
            
            List<AbsenceDTO> leaveRequests = absenceService.getMyAbsenceRequests(currentUser.getId());
            return ResponseEntity.ok(leaveRequests);
            
        } catch (Exception e) {
            log.error("Error getting teacher leave requests: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @PostMapping("/leave-requests")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AbsenceDTO> submitLeaveRequest(@Valid @RequestBody CreateAbsenceDTO createDto, Authentication authentication) {
        log.info("Teacher submitting leave request");
        
        try {
            String principal = authentication.getName();
            User currentUser = userRepository.findByEmail(principal)
                    .orElseGet(() -> userRepository.findByUsername(principal)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal)));
            
            AbsenceDTO createdRequest = absenceService.createAbsenceRequest(createDto, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
            
        } catch (Exception e) {
            log.error("Error submitting leave request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/leave-requests/{absenceId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AbsenceDTO> getLeaveRequestById(@PathVariable Long absenceId, Authentication authentication) {
        log.info("Teacher requesting leave request with ID: {}", absenceId);
        
        try {
            String principal = authentication.getName();
            User currentUser = userRepository.findByEmail(principal)
                    .orElseGet(() -> userRepository.findByUsername(principal)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal)));
            
            AbsenceDTO leaveRequest = absenceService.getAbsenceById(absenceId, currentUser.getId());
            return ResponseEntity.ok(leaveRequest);
            
        } catch (Exception e) {
            log.error("Error getting leave request by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // ================================
    // LEGACY ABSENCE ENDPOINTS - FOR BACKWARD COMPATIBILITY
    // ================================
    @GetMapping("/absences")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AbsenceDTO>> getMyAbsenceRequests(Authentication authentication) {
        // Redirect to the new endpoint
        return getMyLeaveRequests(authentication);
    }

    @PostMapping("/absences")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AbsenceDTO> createAbsenceRequest(@Valid @RequestBody CreateAbsenceDTO createDto, Authentication authentication) {
        // Redirect to the new endpoint
        return submitLeaveRequest(createDto, authentication);
    }

    @GetMapping("/absences/{absenceId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AbsenceDTO> getAbsenceById(@PathVariable Long absenceId, Authentication authentication) {
        // Redirect to the new endpoint
        return getLeaveRequestById(absenceId, authentication);
    }

    // ================================
    // CONTRACT ENDPOINTS
    // ================================
    @GetMapping("/official-contract-status")
    public ResponseEntity<?> getOfficialContractStatus(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getRoleId() != RoleConstants.TEACHER) {
            return ResponseEntity.ok(Map.of("hasOfficialContract", false));
        }
        Optional<Contract> contract = contractRepository.findByUserIdAndContractTypeAndStatus(user.getId(), "OFFICIAL", "ACTIVE");
        return ResponseEntity.ok(Map.of("hasOfficialContract", contract.isPresent()));
    }

// ================================
// ANNOUNCEMENT ENDPOINTS - THÊM VÀO TeacherController
// ================================

// 2. Thêm các methods sau:

/**
 * Get all announcements for teacher view
 * This includes announcements they created and global announcements
 */
@GetMapping("/announcements")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<List<AnnouncementDto>> getTeacherAnnouncements(Authentication authentication) {
    log.info("Getting announcements for teacher: {}", authentication.getName());
    
    try {
        String principal = authentication.getName();
        User currentUser = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal)));
        
        // Get announcements created by this teacher
        List<AnnouncementDto> teacherAnnouncements = announcementService.getAnnouncementsByCreator(currentUser.getId());
        
        log.info("Found {} announcements for teacher {}", teacherAnnouncements.size(), authentication.getName());
        return ResponseEntity.ok(teacherAnnouncements);
        
    } catch (Exception e) {
        log.error("Error fetching teacher announcements for {}: {}", authentication.getName(), e.getMessage(), e);
        return ResponseEntity.ok(new ArrayList<>());
    }
}

/**
 * Create a new announcement as teacher
 */
@PostMapping("/announcements")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<AnnouncementDto> createAnnouncement(
        @Valid @RequestBody CreateAnnouncementDto createDto,
        Authentication authentication) {
    
    log.info("Teacher {} creating announcement: {}", authentication.getName(), createDto.getTitle());
    
    try {
        String principal = authentication.getName();
        User currentUser = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal)));
        
        AnnouncementDto createdAnnouncement = announcementService.createAnnouncement(createDto, currentUser.getId());
        
        log.info("Successfully created announcement with ID: {}", createdAnnouncement.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAnnouncement);
        
    } catch (Exception e) {
        log.error("Error creating announcement for teacher {}: {}", authentication.getName(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

/**
 * Update an announcement (only if created by this teacher)
 */
@PutMapping("/announcements/{announcementId}")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<AnnouncementDto> updateAnnouncement(
        @PathVariable Long announcementId,
        @Valid @RequestBody CreateAnnouncementDto updateDto,
        Authentication authentication) {
    
    log.info("Teacher {} updating announcement ID: {}", authentication.getName(), announcementId);
    
    try {
        String principal = authentication.getName();
        User currentUser = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal)));
        
        // Verify teacher owns this announcement
        AnnouncementDto existingAnnouncement = announcementService.getAnnouncementById(announcementId);
        
        if (!existingAnnouncement.getCreatedBy().equals(currentUser.getId())) {
            log.warn("Teacher {} attempted to update announcement {} they don't own", 
                    authentication.getName(), announcementId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        
        AnnouncementDto updatedAnnouncement = announcementService.updateAnnouncement(announcementId, updateDto);
        log.info("Successfully updated announcement ID: {}", announcementId);
        return ResponseEntity.ok(updatedAnnouncement);
        
    } catch (Exception e) {
        log.error("Error updating announcement {} for teacher {}: {}", 
                announcementId, authentication.getName(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

/**
 * Delete an announcement (only if created by this teacher)
 */
@DeleteMapping("/announcements/{announcementId}")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<Void> deleteAnnouncement(
        @PathVariable Long announcementId,
        Authentication authentication) {
    
    log.info("Teacher {} deleting announcement ID: {}", authentication.getName(), announcementId);
    
    try {
        String principal = authentication.getName();
        User currentUser = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal)));
        
        // Verify teacher owns this announcement
        AnnouncementDto existingAnnouncement = announcementService.getAnnouncementById(announcementId);
        
        if (!existingAnnouncement.getCreatedBy().equals(currentUser.getId())) {
            log.warn("Teacher {} attempted to delete announcement {} they don't own", 
                    authentication.getName(), announcementId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        announcementService.deleteAnnouncement(announcementId);
        log.info("Successfully deleted announcement ID: {}", announcementId);
        return ResponseEntity.noContent().build();
        
    } catch (Exception e) {
        log.error("Error deleting announcement {} for teacher {}: {}", 
                announcementId, authentication.getName(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

/**
 * Get announcements for a specific classroom that this teacher manages
 */
@GetMapping("/classrooms/{classroomId}/announcements")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<List<AnnouncementDto>> getClassroomAnnouncements(
        @PathVariable Long classroomId,
        Authentication authentication) {
    
    log.info("Teacher {} getting announcements for classroom ID: {}", authentication.getName(), classroomId);
    
    try {
        // TODO: Add validation that teacher actually manages this classroom
        List<AnnouncementDto> announcements = announcementService.getActiveAnnouncementsByClassroom(classroomId);
        
        log.info("Found {} announcements for classroom {}", announcements.size(), classroomId);
        return ResponseEntity.ok(announcements);
        
    } catch (Exception e) {
        log.error("Error getting classroom announcements for teacher {}: {}", 
                authentication.getName(), e.getMessage(), e);
        return ResponseEntity.ok(new ArrayList<>());
    }
}
}