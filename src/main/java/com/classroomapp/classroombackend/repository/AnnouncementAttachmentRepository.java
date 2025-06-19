package com.classroomapp.classroombackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.AnnouncementAttachment;

@Repository
public interface AnnouncementAttachmentRepository extends JpaRepository<AnnouncementAttachment, Long> {
    // Add custom query methods here if needed
} 