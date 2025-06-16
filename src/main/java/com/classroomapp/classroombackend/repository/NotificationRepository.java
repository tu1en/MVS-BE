package com.classroomapp.classroombackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);
    
    List<Notification> findByTypeOrderByCreatedAtDesc(String type);
    
    List<Notification> findAllByOrderByCreatedAtDesc();
    
    // Fallback methods for simpler queries
    List<Notification> findByRecipientId(Long recipientId);
    List<Notification> findByTypeAndRecipientId(String type, Long recipientId);
}
