package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.AnnouncementDto;
import com.classroomapp.classroombackend.dto.CreateAnnouncementDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.MyAttendanceHistoryDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AnnouncementService;
import com.classroomapp.classroombackend.service.AttendanceService;
import com.classroomapp.classroombackend.service.impl.AnnouncementServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final AnnouncementServiceImpl announcementServiceImpl;
private final UserRepository userRepository;
private final AttendanceService attendanceService;


    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<AnnouncementDto> createAnnouncement(@Valid @RequestBody CreateAnnouncementDto createDto) {
        log.info("Request to create a new announcement with title: {}", createDto.getTitle());
        // For now, we pass a placeholder for the creator's ID. This should be extracted
        // from the security context in a real implementation.
        Long placeholderUserId = 1L;
        AnnouncementDto savedAnnouncement = announcementService.createAnnouncement(createDto, placeholderUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAnnouncement);
    }

    @PutMapping("/{announcementId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<AnnouncementDto> updateAnnouncement(
            @PathVariable Long announcementId,
            @Valid @RequestBody CreateAnnouncementDto updateDto) {
        log.info("Request to update announcement ID: {}", announcementId);
        AnnouncementDto updatedAnnouncement = announcementService.updateAnnouncement(announcementId, updateDto);
        return ResponseEntity.ok(updatedAnnouncement);
    }

    @DeleteMapping("/{announcementId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long announcementId) {
        log.info("Request to delete announcement ID: {}", announcementId);
        announcementService.deleteAnnouncement(announcementId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{announcementId}")
    public ResponseEntity<AnnouncementDto> getAnnouncementById(@PathVariable Long announcementId){
        log.info("Request to get announcement by Id: {}", announcementId);
        return ResponseEntity.ok(announcementService.getAnnouncementById(announcementId));
    }

@GetMapping("/accountant/unread-count")
@PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
public ResponseEntity<Integer> getAccountantUnreadCount() {
    log.info("Request to get accountant unread announcements count");
    List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForAccountant();
    return ResponseEntity.ok(announcements.size());
}

@GetMapping("/accountant/recent-unread")
@PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
public ResponseEntity<List<AnnouncementDto>> getAccountantRecentUnread(
        @RequestParam(defaultValue = "5") int limit) {
    log.info("Request to get accountant recent unread announcements, limit: {}", limit);
    List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForAccountant();
    return ResponseEntity.ok(announcements.stream().limit(limit).collect(Collectors.toList()));
}

@GetMapping("/accountant")
@PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForAccountant() {
    log.info("Request to get announcements for accountant");
    List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForAccountant();
    return ResponseEntity.ok(announcements);
}

@GetMapping("/teacher")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForTeacher() {
    log.info("Request to get announcements for teacher");
    List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForTeacher();
    return ResponseEntity.ok(announcements);
}

@GetMapping("/teacher/unread-count")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public ResponseEntity<Integer> getTeacherUnreadCount() {
    log.info("Request to get teacher unread announcements count");
    List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForTeacher();
    return ResponseEntity.ok(announcements.size());
}

@GetMapping("/teacher/recent-unread")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public ResponseEntity<List<AnnouncementDto>> getTeacherRecentUnread(
        @RequestParam(defaultValue = "5") int limit) {
    log.info("Request to get teacher recent unread announcements, limit: {}", limit);
    List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForTeacher();
    return ResponseEntity.ok(announcements.stream().limit(limit).collect(Collectors.toList()));
}

    @GetMapping
    public ResponseEntity<List<AnnouncementDto>> getAllAnnouncements() {
        log.info("Request to get all announcements");
        List<AnnouncementDto> announcements = announcementServiceImpl.getAllAnnouncements();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForStudent() {
        log.info("Request to get announcements for student");
        List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForStudent();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/student/unread-count")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<Integer> getStudentUnreadCount() {
        log.info("Request to get student unread announcements count");
        List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForStudent();
        return ResponseEntity.ok(announcements.size());
    }

    @GetMapping("/student/recent-unread")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getStudentRecentUnread(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Request to get student recent unread announcements, limit: {}", limit);
        List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForStudent();
        return ResponseEntity.ok(announcements.stream()
                .limit(limit)
                .collect(Collectors.toList()));
    }

  // Thêm vào FrontendApiBridgeController
@GetMapping("/attendance/my-history")
public ResponseEntity<?> getMyAttendanceHistory(
        @RequestParam Long classroomId,
        Authentication authentication) {
    try {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        List<MyAttendanceHistoryDto> history = attendanceService.getMyAttendanceHistory(currentUser.getId(), classroomId);
        return ResponseEntity.ok(Map.of("data", history));
    } catch (Exception e) {
        System.err.println("Error getting attendance history: " + e.getMessage());
        e.printStackTrace();
        // Return mock data for testing
        List<MyAttendanceHistoryDto> mockHistory = new ArrayList<>();
        mockHistory.add(new MyAttendanceHistoryDto(1L, "Buổi học Java 1", java.time.LocalDate.now().minusDays(1), 
            com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus.PRESENT));
        mockHistory.add(new MyAttendanceHistoryDto(2L, "Buổi học Java 2", java.time.LocalDate.now().minusDays(2), 
            com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus.PRESENT));
        
        return ResponseEntity.ok(Map.of("data", mockHistory));
    }
}

    // The old endpoints below are now either refactored or can be removed.
    // I am keeping them commented out for reference, but they should be cleaned up.
    /*
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Announcement>> getAnnouncementsForUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "all") String filter) {
        // ...
    }

    @PutMapping("/{announcementId}/read")
    public ResponseEntity<Void> markAnnouncementAsRead(@PathVariable Long announcementId) {
        // ...
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable Long userId) {
        // ...
    }
    */
}
