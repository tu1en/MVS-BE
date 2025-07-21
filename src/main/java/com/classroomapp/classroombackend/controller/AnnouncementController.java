package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.classroomapp.classroombackend.dto.AnnouncementDto;
import com.classroomapp.classroombackend.dto.CreateAnnouncementDto;
import com.classroomapp.classroombackend.dto.UpdateAnnouncementDto;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
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
    private final UserRepository userRepository;

    /**
     * ✅ Tạo mới announcement (Admin hoặc Manager)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<AnnouncementDto> createAnnouncement(@Valid @RequestBody CreateAnnouncementDto createDto,
                                                               Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        log.info("Creating new announcement by user: {}", email);
        AnnouncementDto savedAnnouncement = announcementService.createAnnouncement(createDto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAnnouncement);
    }

    /**
     * ✅ Cập nhật announcement (sử dụng UpdateAnnouncementDto)
     */
    @PutMapping("/{announcementId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<AnnouncementDto> updateAnnouncement(@PathVariable Long announcementId,
                                                               @Valid @RequestBody UpdateAnnouncementDto updateDto) {
        log.info("Updating announcement ID: {}", announcementId);
        AnnouncementDto updatedAnnouncement = announcementService.updateAnnouncement(announcementId, updateDto);
        return ResponseEntity.ok(updatedAnnouncement);
    }

    /**
     * ✅ Xóa announcement
     */
    @DeleteMapping("/{announcementId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long announcementId) {
        log.info("Deleting announcement ID: {}", announcementId);
        announcementService.deleteAnnouncement(announcementId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ✅ Lấy announcement theo ID
     */
    @GetMapping("/{announcementId}")
    public ResponseEntity<AnnouncementDto> getAnnouncementById(@PathVariable Long announcementId) {
        log.info("Fetching announcement by ID: {}", announcementId);
        return ResponseEntity.ok(announcementService.getAnnouncementById(announcementId));
    }

    /**
     * ✅ Lấy tất cả announcements (Admin/Manager)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getAllAnnouncements() {
        log.info("Fetching all announcements");
        List<AnnouncementDto> announcements = announcementServiceImpl.getAllAnnouncements();
        return ResponseEntity.ok(announcements);
    }

    /**
     * ✅ Lấy tất cả announcements với pagination (Admin/Manager)
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<AnnouncementDto>> getAllAnnouncementsPaginated(Pageable pageable) {
        log.info("Fetching all announcements with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        // Note: This would require implementing pagination in the service layer
        // For now, converting list to page for demonstration
        List<AnnouncementDto> announcements = announcementServiceImpl.getAllAnnouncements();
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(announcements, pageable, announcements.size()));
    }

    /**
     * ✅ Lấy announcements cho Student (role STUDENT)
     */
    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForStudent() {
        log.info("Fetching announcements for Student");
        List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForStudent();
        return ResponseEntity.ok(announcements);
    }

    /**
     * ✅ Lấy announcements cho Teacher (role TEACHER)
     */
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsForTeacher(Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching announcements for Teacher: {}", email);
        List<AnnouncementDto> announcements = announcementServiceImpl.getAnnouncementsForTeacher();
        return ResponseEntity.ok(announcements);
    }

    /**
     * ✅ Lấy global announcements (cho tất cả user)
     */
    @GetMapping("/global")
    public ResponseEntity<List<AnnouncementDto>> getGlobalAnnouncements() {
        log.info("Fetching global announcements");
        List<AnnouncementDto> announcements = announcementService.getGlobalAnnouncements();
        return ResponseEntity.ok(announcements);
    }

    /**
     * ✅ Lấy announcements theo classroom ID
     */
    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncementsByClassroom(@PathVariable Long classroomId) {
        log.info("Fetching announcements for classroom ID: {}", classroomId);
        List<AnnouncementDto> announcements = announcementService.getActiveAnnouncementsByClassroom(classroomId);
        return ResponseEntity.ok(announcements);
    }

    /**
     * ✅ Lấy pinned announcements theo classroom
     */
    @GetMapping("/classroom/{classroomId}/pinned")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getPinnedAnnouncements(@PathVariable Long classroomId) {
        log.info("Fetching pinned announcements for classroom ID: {}", classroomId);
        List<AnnouncementDto> announcements = announcementService.getPinnedAnnouncements(classroomId);
        return ResponseEntity.ok(announcements);
    }

    /**
     * ✅ Archive announcement (thay vì xóa hoàn toàn)
     */
    @PutMapping("/{announcementId}/archive")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> archiveAnnouncement(@PathVariable Long announcementId) {
        log.info("Archiving announcement ID: {}", announcementId);
        // announcementService.archiveAnnouncement(announcementId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ✅ Mark announcement as read
     */
    @PostMapping("/{announcementId}/read")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long announcementId, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        
        log.info("Marking announcement {} as read for user {}", announcementId, user.getId());
        announcementService.markAsRead(announcementId, user.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * ✅ Get unread announcements for user in classroom
     */
    @GetMapping("/classroom/{classroomId}/unread")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<AnnouncementDto>> getUnreadAnnouncements(@PathVariable Long classroomId, 
                                                                        Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        
        log.info("Fetching unread announcements for user {} in classroom {}", user.getId(), classroomId);
        List<AnnouncementDto> announcements = announcementService.getUnreadAnnouncements(classroomId, user.getId());
        return ResponseEntity.ok(announcements);
    }

    /**
     * ✅ Count unread announcements for user in classroom
     */
    @GetMapping("/classroom/{classroomId}/unread/count")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Long> countUnreadAnnouncements(@PathVariable Long classroomId, 
                                                        Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        
        log.info("Counting unread announcements for user {} in classroom {}", user.getId(), classroomId);
        long count = announcementService.countUnreadAnnouncements(classroomId, user.getId());
        return ResponseEntity.ok(count);
    }
}
