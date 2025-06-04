package com.mvs.repository;

import com.mvs.entity.Notification;
import com.mvs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n WHERE :user MEMBER OF n.recipients ORDER BY n.createdAt DESC")
    List<Notification> findAllByRecipient(User user);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE :user MEMBER OF n.recipients AND n.read = false")
    long countUnreadByUser(User user);
    
    List<Notification> findBySenderOrderByCreatedAtDesc(User sender);
}
