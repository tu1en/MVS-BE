package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForTeacher() {
        log.info("Request to get announcements for teacher");
        List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForTeacher();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/teacher/unread-count")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Integer> getUnreadAnnouncementCountForTeacher() {
        log.info("Request to get unread announcement count for teacher");
        int count = announcementServiceImpl.getUnreadAnnouncementCountForTeacher();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/student/unread-count")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Integer> getUnreadAnnouncementCountForStudent() {
        log.info("Request to get unread announcement count for student");
        int count = announcementServiceImpl.getUnreadAnnouncementCountForStudent();
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{id}/mark-read")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT') or hasRole('ACCOUNTANT')")
    public ResponseEntity<Void> markAnnouncementAsRead(@PathVariable Long id) {
        log.info("Request to mark announcement {} as read", id);
        announcementServiceImpl.markAnnouncementAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/teacher/recent-unread")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AnnouncementDto>> getRecentUnreadAnnouncementsForTeacher(@RequestParam(defaultValue = "5") int limit) {
        log.info("Request to get recent unread announcements for teacher, limit: {}", limit);
        List<AnnouncementDto> announcements = announcementServiceImpl.getRecentUnreadAnnouncementsForTeacher(limit);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/student/recent-unread")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AnnouncementDto>> getRecentUnreadAnnouncementsForStudent(@RequestParam(defaultValue = "5") int limit) {
        log.info("Request to get recent unread announcements for student, limit: {}", limit);
        List<AnnouncementDto> announcements = announcementServiceImpl.getRecentUnreadAnnouncementsForStudent(limit);
        return ResponseEntity.ok(announcements);
    }

    @PostMapping("/teacher/mark-all-read")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> markAllAnnouncementsAsReadForTeacher() {
        log.info("Request to mark all announcements as read for teacher");
        announcementServiceImpl.markAllAnnouncementsAsReadForTeacher();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/student/mark-all-read")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> markAllAnnouncementsAsReadForStudent() {
        log.info("Request to mark all announcements as read for student");
        announcementServiceImpl.markAllAnnouncementsAsReadForStudent();
        return ResponseEntity.ok().build();
    }

    // Accountant endpoints - similar to Teacher endpoints
    @GetMapping("/accountant")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForAccountant() {
        log.info("Request to get announcements for accountant");
        List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForAccountant();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/accountant/unread-count")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<Integer> getUnreadAnnouncementCountForAccountant() {
        log.info("Request to get unread announcement count for accountant");
        int count = announcementServiceImpl.getUnreadAnnouncementCountForAccountant();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/accountant/recent-unread")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<List<AnnouncementDto>> getRecentUnreadAnnouncementsForAccountant(@RequestParam(defaultValue = "5") int limit) {
        log.info("Request to get recent unread announcements for accountant, limit: {}", limit);
        List<AnnouncementDto> announcements = announcementServiceImpl.getRecentUnreadAnnouncementsForAccountant(limit);
        return ResponseEntity.ok(announcements);
    }

    @PostMapping("/accountant/mark-all-read")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<Void> markAllAnnouncementsAsReadForAccountant() {
        log.info("Request to mark all announcements as read for accountant");
        announcementServiceImpl.markAllAnnouncementsAsReadForAccountant();
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
}
