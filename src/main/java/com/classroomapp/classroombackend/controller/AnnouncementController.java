package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.classroomapp.classroombackend.service.AnnouncementService;
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
        int count = announcementService.getUnreadAnnouncementCountForAccountant();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/accountant/recent-unread")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getAccountantRecentUnread(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Request to get accountant recent unread announcements, limit: {}", limit);
        List<AnnouncementDto> announcements = announcementService.getRecentUnreadAnnouncementsForAccountant(limit);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/accountant")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForAccountant() {
        log.info("Request to get announcements for accountant");
        List<AnnouncementDto> announcements = announcementService.getAnnouncementsForAccountant();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/accountant/mark-all-read")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<Void> markAllAnnouncementsAsReadForAccountant() {
        log.info("Request to mark all announcements as read for accountant");
        announcementService.markAllAnnouncementsAsReadForAccountant();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/teacher/unread-count")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Integer> getTeacherUnreadCount() {
        log.info("Request to get teacher unread announcements count");
        int count = announcementService.getUnreadAnnouncementCountForTeacher();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/teacher/recent-unread")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getTeacherRecentUnread(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Request to get teacher recent unread announcements, limit: {}", limit);
        List<AnnouncementDto> announcements = announcementService.getRecentUnreadAnnouncementsForTeacher(limit);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForTeacher() {
        log.info("Request to get announcements for teacher");
        List<AnnouncementDto> announcements = announcementService.getAnnouncementsForTeacher();
        return ResponseEntity.ok(announcements);
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
        List<AnnouncementDto> announcements = announcementService.getAnnouncementsForStudent();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/student/unread-count")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<Integer> getStudentUnreadCount() {
        log.info("Request to get student unread announcements count");
        int count = announcementService.getUnreadAnnouncementCountForStudent();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/student/recent-unread")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getStudentRecentUnread(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Request to get student recent unread announcements, limit: {}", limit);
        List<AnnouncementDto> announcements = announcementService.getRecentUnreadAnnouncementsForStudent(limit);
        return ResponseEntity.ok(announcements);
    }

    // Parent endpoints
    @GetMapping("/parent")
    @PreAuthorize("hasRole('PARENT') or hasRole('ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForParent() {
        log.info("Request to get announcements for parent");
        List<AnnouncementDto> announcements = announcementService.getAnnouncementsForParent();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/parent/unread-count")
    @PreAuthorize("hasRole('PARENT') or hasRole('ADMIN')")
    public ResponseEntity<Integer> getParentUnreadCount() {
        log.info("Request to get parent unread announcements count");
        int count = announcementService.getUnreadAnnouncementCountForParent();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/parent/recent-unread")
    @PreAuthorize("hasRole('PARENT') or hasRole('ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getParentRecentUnread(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Request to get parent recent unread announcements, limit: {}", limit);
        List<AnnouncementDto> announcements = announcementService.getRecentUnreadAnnouncementsForParent(limit);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/parent/mark-all-read")
    @PreAuthorize("hasRole('PARENT') or hasRole('ADMIN')")
    public ResponseEntity<Void> markAllAnnouncementsAsReadForParent() {
        log.info("Request to mark all announcements as read for parent");
        announcementService.markAllAnnouncementsAsReadForParent();
        return ResponseEntity.ok().build();
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

    // --- Generic endpoints to align with frontend service ---

    @PostMapping("/{announcementId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAnnouncementAsReadGeneric(@PathVariable Long announcementId) {
        log.info("Generic: mark announcement {} as read", announcementId);
        announcementService.markAnnouncementAsRead(announcementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{announcementId}/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAnnouncementAsUnreadGeneric(@PathVariable Long announcementId) {
        log.info("Generic: mark announcement {} as unread", announcementId);
        announcementService.markAnnouncementAsUnread(announcementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAnnouncementsAsReadForCurrentRole() {
        log.info("Generic: mark all announcements as read for current user role");
        if (hasRole("ROLE_STUDENT")) {
            announcementService.markAllAnnouncementsAsReadForStudent();
        } else if (hasRole("ROLE_TEACHER")) {
            announcementService.markAllAnnouncementsAsReadForTeacher();
        } else if (hasRole("ROLE_ACCOUNTANT")) {
            announcementService.markAllAnnouncementsAsReadForAccountant();
        } else if (hasRole("ROLE_PARENT")) {
            announcementService.markAllAnnouncementsAsReadForParent();
        } else {
            log.info("No applicable role found for mark-all-read; skipping");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> getUnreadCountForCurrentRole() {
        log.info("Generic: get unread announcements count for current user role");
        int count = 0;
        if (hasRole("ROLE_STUDENT")) {
            count = announcementService.getUnreadAnnouncementCountForStudent();
        } else if (hasRole("ROLE_TEACHER")) {
            count = announcementService.getUnreadAnnouncementCountForTeacher();
        } else if (hasRole("ROLE_ACCOUNTANT")) {
            count = announcementService.getUnreadAnnouncementCountForAccountant();
        } else if (hasRole("ROLE_PARENT")) {
            count = announcementService.getUnreadAnnouncementCountForParent();
        }
        return ResponseEntity.ok(count);
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
