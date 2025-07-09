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
