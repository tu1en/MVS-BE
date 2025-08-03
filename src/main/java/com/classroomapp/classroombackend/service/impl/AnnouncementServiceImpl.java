package com.classroomapp.classroombackend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import com.classroomapp.classroombackend.dto.AnnouncementDto;
import com.classroomapp.classroombackend.dto.CreateAnnouncementDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.model.Notification;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;
import com.classroomapp.classroombackend.repository.NotificationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AnnouncementService;

import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AnnouncementDto createAnnouncement(CreateAnnouncementDto createDto, Long createdBy) {
        Announcement announcement = modelMapper.map(createDto, Announcement.class);
        announcement.setCreatedBy(createdBy);

        if (announcement.getStatus() == null) {
            announcement.setStatus(Announcement.AnnouncementStatus.ACTIVE);
        }
        if (announcement.getIsPinned() == null) {
            announcement.setIsPinned(false);
        }
        if (announcement.getPriority() == null) {
            announcement.setPriority(Announcement.Priority.NORMAL);
        }
        if (announcement.getTargetAudience() == null) {
            announcement.setTargetAudience(Announcement.TargetAudience.ALL);
        }

        Announcement savedAnnouncement = announcementRepository.save(announcement);
        
        // Create notifications for the target audience
        createNotificationsForAnnouncement(savedAnnouncement);

        return convertToDto(savedAnnouncement);
    }

    private void createNotificationsForAnnouncement(Announcement announcement) {
        List<User> targetUsers = new ArrayList<>();
        switch (announcement.getTargetAudience()) {
            case ALL:
                targetUsers.addAll(userRepository.findActiveStudents());
                targetUsers.addAll(userRepository.findActiveTeachers());
                break;
            case STUDENTS:
                targetUsers.addAll(userRepository.findActiveStudents());
                break;
            case TEACHERS:
                targetUsers.addAll(userRepository.findActiveTeachers());
                break;
        }

        User sender = userRepository.findById(announcement.getCreatedBy()).orElse(null);

        for (User user : targetUsers) {
            Notification notification = new Notification();
            notification.setRecipientId(user.getId());
            notification.setSender(sender != null ? sender.getFullName() : "System");
            notification.setContent(announcement.getTitle());
            notification.setType("ANNOUNCEMENT");
            notification.setIsRead(false);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public AnnouncementDto updateAnnouncement(Long announcementId, CreateAnnouncementDto updateDto) {
        Announcement existingAnnouncement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));

        // Update fields
        existingAnnouncement.setTitle(updateDto.getTitle());
        existingAnnouncement.setContent(updateDto.getContent());
        // For simplicity, we are not updating all fields like in the old controller yet.
        // This can be expanded later.

        Announcement savedAnnouncement = announcementRepository.save(existingAnnouncement);
        return convertToDto(savedAnnouncement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        if (!announcementRepository.existsById(announcementId)) {
            throw new ResourceNotFoundException("Announcement not found with id: " + announcementId);
        }
        announcementRepository.deleteById(announcementId);
    }
    
    @Override
    public AnnouncementDto getAnnouncementById(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));
        return convertToDto(announcement);
    }
    
    public List<AnnouncementDto> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDto> getAnnouncementsForStudent() {
        List<Announcement> announcements = announcementRepository.findByTargetAudienceInAndStatusOrderByCreatedAtDesc(
                List.of(Announcement.TargetAudience.ALL, Announcement.TargetAudience.STUDENTS),
                Announcement.AnnouncementStatus.ACTIVE
        );
        return announcements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDto> getAnnouncementsForTeacher() {
        List<Announcement> announcements = announcementRepository.findByTargetAudienceInAndStatusOrderByCreatedAtDesc(
                List.of(Announcement.TargetAudience.ALL, Announcement.TargetAudience.TEACHERS),
                Announcement.AnnouncementStatus.ACTIVE
        );
        return announcements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public int getUnreadAnnouncementCountForTeacher() {
        // Return count of active announcements for teachers
        // This is a simplified implementation - in production you would track read status per user
        List<Announcement> announcements = announcementRepository.findByTargetAudienceInAndStatusOrderByCreatedAtDesc(
                List.of(Announcement.TargetAudience.ALL, Announcement.TargetAudience.TEACHERS),
                Announcement.AnnouncementStatus.ACTIVE
        );
        log.info("Found {} unread announcements for teachers", announcements.size());
        return announcements.size();
    }

    @Override
    public int getUnreadAnnouncementCountForStudent() {
        // Return count of active announcements for students
        // This is a simplified implementation - in production you would track read status per user
        List<Announcement> announcements = announcementRepository.findByTargetAudienceInAndStatusOrderByCreatedAtDesc(
                List.of(Announcement.TargetAudience.ALL, Announcement.TargetAudience.STUDENTS),
                Announcement.AnnouncementStatus.ACTIVE
        );
        log.info("Found {} unread announcements for students", announcements.size());
        return announcements.size();
    }

    @Override
    public void markAnnouncementAsRead(Long announcementId) {
        // In a full implementation, you would track read status per user in a separate table
        // For now, this is a placeholder that logs the action
        log.info("Marking announcement {} as read", announcementId);
        // TODO: Implement user-specific read tracking
    }

    @Override
    public List<AnnouncementDto> getRecentUnreadAnnouncementsForTeacher(int limit) {
        List<Announcement> announcements = announcementRepository.findByTargetAudienceInAndStatusOrderByCreatedAtDesc(
                List.of(Announcement.TargetAudience.ALL, Announcement.TargetAudience.TEACHERS),
                Announcement.AnnouncementStatus.ACTIVE
        );
        return announcements.stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnouncementDto> getRecentUnreadAnnouncementsForStudent(int limit) {
        List<Announcement> announcements = announcementRepository.findByTargetAudienceInAndStatusOrderByCreatedAtDesc(
                List.of(Announcement.TargetAudience.ALL, Announcement.TargetAudience.STUDENTS),
                Announcement.AnnouncementStatus.ACTIVE
        );
        return announcements.stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void markAllAnnouncementsAsReadForTeacher() {
        // In a full implementation, you would mark all announcements as read for the current teacher
        log.info("Marking all announcements as read for teacher");
        // TODO: Implement user-specific read tracking
    }

    @Override
    public void markAllAnnouncementsAsReadForStudent() {
        // In a full implementation, you would mark all announcements as read for the current student
        log.info("Marking all announcements as read for student");
        // TODO: Implement user-specific read tracking
    }


    // --- Empty methods to be implemented later ---

    @Override
    public AnnouncementDto createAnnouncementWithAttachments(CreateAnnouncementDto createDto, List<MultipartFile> attachments, Long createdBy) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AnnouncementDto> getActiveAnnouncementsByClassroom(Long classroomId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AnnouncementDto> getGlobalAnnouncements() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AnnouncementDto> getPinnedAnnouncements(Long classroomId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AnnouncementDto> getAnnouncementsByPriority(Long classroomId, String priority) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public AnnouncementDto archiveAnnouncement(Long announcementId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public AnnouncementDto togglePinAnnouncement(Long announcementId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void markAsRead(Long announcementId, Long userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AnnouncementDto> getUnreadAnnouncements(Long classroomId, Long userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public long countUnreadAnnouncements(Long classroomId, Long userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AnnouncementDto> searchAnnouncements(Long classroomId, String searchTerm) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AnnouncementDto> getAnnouncementsByCreator(Long createdBy) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void processScheduledAnnouncements() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void archiveExpiredAnnouncements() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private AnnouncementDto convertToDto(Announcement announcement) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        dto.setClassroomId(announcement.getClassroomId());
        dto.setCreatedBy(announcement.getCreatedBy());
        dto.setTargetAudience(announcement.getTargetAudience() != null ? announcement.getTargetAudience().name() : null);
        dto.setPriority(announcement.getPriority() != null ? announcement.getPriority().name() : null);
        dto.setScheduledDate(announcement.getScheduledDate());
        dto.setExpiryDate(announcement.getExpiryDate());
        dto.setIsPinned(announcement.getIsPinned());
        dto.setAttachmentsCount(announcement.getAttachmentsCount());
        dto.setCreatedAt(announcement.getCreatedAt());
        dto.setUpdatedAt(announcement.getUpdatedAt());
        dto.setStatus(announcement.getStatus() != null ? announcement.getStatus().name() : null);

        // Set related entity names if available
        if (announcement.getClassroom() != null) {
            dto.setClassroomName(announcement.getClassroom().getName());
        }
        if (announcement.getCreator() != null) {
            dto.setCreatorName(announcement.getCreator().getFullName());
        }

        return dto;
    }
}