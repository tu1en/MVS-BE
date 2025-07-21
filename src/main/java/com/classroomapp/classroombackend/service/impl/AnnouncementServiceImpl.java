package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.AnnouncementDto;
import com.classroomapp.classroombackend.dto.CreateAnnouncementDto;
import com.classroomapp.classroombackend.dto.UpdateAnnouncementDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.model.AnnouncementRead;
import com.classroomapp.classroombackend.model.Notification;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AnnouncementReadRepository;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;
import com.classroomapp.classroombackend.repository.NotificationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AnnouncementService;
import com.classroomapp.classroombackend.service.scheduler.AnnouncementSchedulerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadRepository announcementReadRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;
    private final AnnouncementSchedulerService schedulerService;

    @Override
    @Transactional
    public AnnouncementDto createAnnouncement(CreateAnnouncementDto createDto, Long createdBy) {
        // Validate dates
        validateAnnouncementDates(createDto.getScheduledDate(), createDto.getExpiryDate());
        
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
            notification.setMessage(announcement.getTitle());
            notification.setType("ANNOUNCEMENT");
            notification.setIsRead(false);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public AnnouncementDto updateAnnouncement(Long announcementId, UpdateAnnouncementDto updateDto) {
        Announcement existingAnnouncement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));

        // Validate dates if provided
        validateAnnouncementDates(updateDto.getScheduledDate(), updateDto.getExpiryDate());

        // Update fields only if they are provided (not null)
        if (updateDto.getTitle() != null) {
            existingAnnouncement.setTitle(updateDto.getTitle());
        }
        if (updateDto.getContent() != null) {
            existingAnnouncement.setContent(updateDto.getContent());
        }
        if (updateDto.getTargetAudience() != null) {
            existingAnnouncement.setTargetAudience(parseEnum(Announcement.TargetAudience.class,
                    updateDto.getTargetAudience(), "targetAudience"));
        }
        if (updateDto.getPriority() != null) {
            existingAnnouncement.setPriority(parseEnum(Announcement.Priority.class,
                    updateDto.getPriority(), "priority"));
        }
        if (updateDto.getScheduledDate() != null) {
            existingAnnouncement.setScheduledDate(updateDto.getScheduledDate());
        }
        if (updateDto.getExpiryDate() != null) {
            existingAnnouncement.setExpiryDate(updateDto.getExpiryDate());
        }
        if (updateDto.getIsPinned() != null) {
            existingAnnouncement.setIsPinned(updateDto.getIsPinned());
        }
        if (updateDto.getStatus() != null) {
            existingAnnouncement.setStatus(parseEnum(Announcement.AnnouncementStatus.class,
                    updateDto.getStatus(), "status"));
        }

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
                List.of(Announcement.TargetAudience.STUDENTS, Announcement.TargetAudience.ALL),
                Announcement.AnnouncementStatus.ACTIVE
        );
        return announcements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnouncementDto> getAnnouncementsForTeacher() {
        List<Announcement> announcements = announcementRepository.findByTargetAudienceInAndStatusOrderByCreatedAtDesc(
                List.of(Announcement.TargetAudience.TEACHERS, Announcement.TargetAudience.ALL),
                Announcement.AnnouncementStatus.ACTIVE
        );
        return announcements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // --- Empty methods to be implemented later ---

    @Override
    public AnnouncementDto createAnnouncementWithAttachments(CreateAnnouncementDto createDto, List<MultipartFile> attachments, Long createdBy) {
        // TODO: Implement file upload and attachment handling
        // For now, create announcement without attachments and log the request
        System.out.println("⚠️ File attachments not implemented yet. Creating announcement without attachments.");
        return createAnnouncement(createDto, createdBy);
    }

    @Override
    public List<AnnouncementDto> getActiveAnnouncementsByClassroom(Long classroomId) {
        List<Announcement> announcements = announcementRepository.findActiveByClassroom(classroomId, LocalDateTime.now());
        return announcements.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<AnnouncementDto> getGlobalAnnouncements() {
        List<Announcement> announcements = announcementRepository.findGlobalAnnouncements(LocalDateTime.now());
        return announcements.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<AnnouncementDto> getPinnedAnnouncements(Long classroomId) {
        List<Announcement> announcements = announcementRepository.findByClassroomIdAndIsPinnedTrueAndStatusOrderByCreatedAtDesc(
                classroomId, Announcement.AnnouncementStatus.ACTIVE
        );
        return announcements.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<AnnouncementDto> getAnnouncementsByPriority(Long classroomId, String priority) {
        try {
            Announcement.Priority enumPriority = parseEnum(Announcement.Priority.class, priority, "priority");
            List<Announcement> announcements = announcementRepository.findByClassroomIdAndPriorityAndStatusOrderByCreatedAtDesc(
                    classroomId, enumPriority, Announcement.AnnouncementStatus.ACTIVE
            );
            return announcements.stream().map(this::convertToDto).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority value: " + priority + ". Allowed values: LOW, NORMAL, HIGH");
        }
    }

    @Override
    public AnnouncementDto archiveAnnouncement(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));
        
        // Set status to ARCHIVED instead of deleting
        announcement.setStatus(Announcement.AnnouncementStatus.ARCHIVED);
        announcement.setUpdatedAt(LocalDateTime.now());
        
        Announcement updated = announcementRepository.save(announcement);
        return convertToDto(updated);
    }

    @Override
    public AnnouncementDto togglePinAnnouncement(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));
        
        // Toggle pin status
        announcement.setIsPinned(!announcement.getIsPinned());
        announcement.setUpdatedAt(LocalDateTime.now());
        
        Announcement updated = announcementRepository.save(announcement);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void markAsRead(Long announcementId, Long userId) {
        // Check if user has already read this announcement
        if (announcementReadRepository.existsByAnnouncementIdAndUserId(announcementId, userId)) {
            return; // Already read, no action needed
        }
        
        // Verify announcement exists
        if (!announcementRepository.existsById(announcementId)) {
            throw new ResourceNotFoundException("Announcement not found with ID: " + announcementId);
        }
        
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        // Create read record
        AnnouncementRead readRecord = new AnnouncementRead();
        readRecord.setAnnouncementId(announcementId);
        readRecord.setUserId(userId);
        readRecord.setReadAt(LocalDateTime.now());
        
        announcementReadRepository.save(readRecord);
    }

    @Override
    public List<AnnouncementDto> getUnreadAnnouncements(Long classroomId, Long userId) {
        // Get all active announcements for the classroom (including global announcements)
        List<Announcement> activeAnnouncements;
        if (classroomId != null) {
            activeAnnouncements = announcementRepository.findActiveByClassroom(
                classroomId, LocalDateTime.now()
            );
        } else {
            // Global announcements only
            activeAnnouncements = announcementRepository.findGlobalAnnouncements(
                LocalDateTime.now()
            );
        }
        
        // Filter out announcements that user has already read
        List<Announcement> unreadAnnouncements = activeAnnouncements.stream()
            .filter(announcement -> 
                !announcementReadRepository.existsByAnnouncementIdAndUserId(announcement.getId(), userId)
            )
            .collect(Collectors.toList());
        
        return unreadAnnouncements.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public long countUnreadAnnouncements(Long classroomId, Long userId) {
        if (classroomId != null) {
            // Use repository method to count unread announcements efficiently
            return announcementReadRepository.countUnreadAnnouncements(classroomId, userId);
        } else {
            // Count global unread announcements
            List<Announcement> globalAnnouncements = announcementRepository.findGlobalAnnouncements(
                LocalDateTime.now()
            );
            return globalAnnouncements.stream()
                .filter(announcement -> 
                    !announcementReadRepository.existsByAnnouncementIdAndUserId(announcement.getId(), userId)
                )
                .count();
        }
    }

    @Override
    public List<AnnouncementDto> searchAnnouncements(Long classroomId, String searchTerm) {
        List<Announcement> announcements = announcementRepository.searchAnnouncements(classroomId, searchTerm);
        return announcements.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<AnnouncementDto> getAnnouncementsByCreator(Long createdBy) {
        List<Announcement> announcements = announcementRepository.findByCreatedByOrderByCreatedAtDesc(createdBy);
        return announcements.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public void processScheduledAnnouncements() {
        // Delegate to scheduler service
        schedulerService.processScheduledAnnouncements();
    }

    @Override
    public void archiveExpiredAnnouncements() {
        // Delegate to scheduler service
        schedulerService.archiveExpiredAnnouncements();
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

    /**
     * ✅ **SAFE ENUM PARSING WITH DETAILED ERROR MESSAGES**
     * Validates and parses enum values with informative error messages
     */
    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String fieldName) {
        try {
            return Enum.valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for " + fieldName + ": " + value +
                    ". Allowed values: " + java.util.Arrays.toString(enumType.getEnumConstants()));
        }
    }

    /**
     * ✅ **CUSTOM DATE VALIDATION LOGIC**
     * Validates announcement dates to ensure logical consistency
     */
    private void validateAnnouncementDates(LocalDateTime scheduledDate, LocalDateTime expiryDate) {
        LocalDateTime now = LocalDateTime.now();
        
        if (scheduledDate != null && scheduledDate.isBefore(now)) {
            throw new IllegalArgumentException("Scheduled date cannot be in the past.");
        }
        
        if (scheduledDate != null && expiryDate != null && expiryDate.isBefore(scheduledDate)) {
            throw new IllegalArgumentException("Expiry date cannot be before scheduled date.");
        }
    }
}