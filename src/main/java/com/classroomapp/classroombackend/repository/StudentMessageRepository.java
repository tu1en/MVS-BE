package com.classroomapp.classroombackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.StudentMessage;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface StudentMessageRepository extends JpaRepository<StudentMessage, Long> {
    
    List<StudentMessage> findBySenderOrderByCreatedAtDesc(User sender);
    
    List<StudentMessage> findByRecipientOrderByCreatedAtDesc(User recipient);
    
    List<StudentMessage> findBySenderAndRecipientOrderByCreatedAtDesc(User sender, User recipient);
    
    @Query("SELECT sm FROM StudentMessage sm WHERE (sm.sender = :user1 AND sm.recipient = :user2) OR (sm.sender = :user2 AND sm.recipient = :user1) ORDER BY sm.createdAt ASC")
    List<StudentMessage> findConversation(@Param("user1") User user1, @Param("user2") User user2);
    
    List<StudentMessage> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient);
    
    List<StudentMessage> findByStatusOrderByCreatedAtDesc(String status);
    
    List<StudentMessage> findByMessageTypeOrderByCreatedAtDesc(String messageType);
    
    List<StudentMessage> findByPriorityOrderByCreatedAtDesc(String priority);
    
    List<StudentMessage> findByRecipientAndPriorityOrderByCreatedAtDesc(User recipient, String priority);
    
    @Query("SELECT sm FROM StudentMessage sm WHERE sm.subject LIKE %:keyword% OR sm.content LIKE %:keyword% ORDER BY sm.createdAt DESC")
    List<StudentMessage> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT sm FROM StudentMessage sm WHERE sm.subject LIKE %:keyword% OR sm.content LIKE %:keyword% ORDER BY sm.createdAt DESC")
    List<StudentMessage> searchMessages(@Param("keyword") String keyword);
    
    @Query("SELECT sm FROM StudentMessage sm ORDER BY sm.createdAt DESC")
    List<StudentMessage> findRecentMessages();
    
    List<StudentMessage> findByRecipientAndStatusOrderByCreatedAtDesc(User recipient, String status);
    
    @Query("SELECT sm FROM StudentMessage sm WHERE sm.recipient = :recipient AND sm.priority = 'URGENT' ORDER BY sm.createdAt DESC")
    List<StudentMessage> findUrgentMessages(@Param("recipient") User recipient);
    
    @Query("SELECT sm FROM StudentMessage sm WHERE sm.recipient = :recipient AND sm.status = 'SENT' ORDER BY sm.createdAt DESC")
    List<StudentMessage> findPendingReplies(@Param("recipient") User recipient);
    
    @Query("SELECT COUNT(sm) FROM StudentMessage sm WHERE sm.recipient = :recipient AND sm.isRead = false")
    Long countUnreadMessagesByRecipient(@Param("recipient") User recipient);

    Long countByIsReadFalse();

    /**
     * Find messages between users without proper classroom context
     * This is a simplified check - in a real system, you'd need more complex logic
     * to determine if users should be able to message each other
     * Returns: message_id, sender_id, sender_name, recipient_id, recipient_name, subject
     */
    @Query(value = """
        SELECT sm.id as message_id, sm.sender_id, s.full_name as sender_name,
               sm.recipient_id, r.full_name as recipient_name, sm.subject
        FROM student_messages sm
        JOIN users s ON sm.sender_id = s.id
        JOIN users r ON sm.recipient_id = r.id
        WHERE s.role_id = 1 AND r.role_id = 2  -- Student to Teacher messages
        AND NOT EXISTS (
            SELECT 1 FROM classroom_enrollments ce1
            JOIN classrooms c ON ce1.classroom_id = c.id
            WHERE ce1.user_id = s.id AND c.teacher_id = r.id
        )
        ORDER BY sm.id
        """, nativeQuery = true)
    List<Object[]> findMessagesWithoutClassroomContext();
}
