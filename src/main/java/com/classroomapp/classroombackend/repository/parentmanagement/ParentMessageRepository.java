package com.classroomapp.classroombackend.repository.parentmanagement;

import com.classroomapp.classroombackend.model.ParentMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ParentMessage entity
 * Based on PARENT_ROLE_SPEC.md requirements for parent-teacher communication
 */
@Repository
public interface ParentMessageRepository extends JpaRepository<ParentMessage, Long> {

    /**
     * Find messages in conversation between parent and teacher about specific student
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.parentId = :parentId AND pm.teacherId = :teacherId AND pm.studentId = :studentId ORDER BY pm.createdAt ASC")
    List<ParentMessage> findConversation(@Param("parentId") Long parentId, 
                                        @Param("teacherId") Long teacherId, 
                                        @Param("studentId") Long studentId);

    /**
     * Find messages for parent (all conversations)
     */
    List<ParentMessage> findByParentIdOrderByCreatedAtDesc(Long parentId);

    /**
     * Find messages for teacher (all conversations)
     */
    List<ParentMessage> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    /**
     * Find messages about specific student
     */
    List<ParentMessage> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    /**
     * Find unread messages for parent
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.parentId = :parentId AND pm.isRead = false AND pm.senderType = 'TEACHER' ORDER BY pm.createdAt DESC")
    List<ParentMessage> findUnreadMessagesForParent(@Param("parentId") Long parentId);

    /**
     * Find unread messages for teacher
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.teacherId = :teacherId AND pm.isRead = false AND pm.senderType = 'PARENT' ORDER BY pm.createdAt DESC")
    List<ParentMessage> findUnreadMessagesForTeacher(@Param("teacherId") Long teacherId);

    /**
     * Count unread messages for parent
     */
    @Query("SELECT COUNT(pm) FROM ParentMessage pm WHERE pm.parentId = :parentId AND pm.isRead = false AND pm.senderType = 'TEACHER'")
    Long countUnreadMessagesForParent(@Param("parentId") Long parentId);

    /**
     * Count unread messages for teacher
     */
    @Query("SELECT COUNT(pm) FROM ParentMessage pm WHERE pm.teacherId = :teacherId AND pm.isRead = false AND pm.senderType = 'PARENT'")
    Long countUnreadMessagesForTeacher(@Param("teacherId") Long teacherId);

    /**
     * Find messages by sender type
     */
    List<ParentMessage> findBySenderTypeOrderByCreatedAtDesc(ParentMessage.SenderType senderType);

    /**
     * Find reply messages to a specific message
     */
    List<ParentMessage> findByReplyToIdOrderByCreatedAtAsc(Long replyToId);

    /**
     * Find recent messages (within specified timeframe)
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.createdAt >= :since ORDER BY pm.createdAt DESC")
    List<ParentMessage> findRecentMessages(@Param("since") LocalDateTime since);

    /**
     * Find conversation threads (top-level messages only)
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.replyToId IS NULL ORDER BY pm.createdAt DESC")
    List<ParentMessage> findConversationThreads();

    /**
     * Find conversation threads for parent
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.parentId = :parentId AND pm.replyToId IS NULL ORDER BY pm.createdAt DESC")
    List<ParentMessage> findConversationThreadsForParent(@Param("parentId") Long parentId);

    /**
     * Find conversation threads for teacher
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.teacherId = :teacherId AND pm.replyToId IS NULL ORDER BY pm.createdAt DESC")
    List<ParentMessage> findConversationThreadsForTeacher(@Param("teacherId") Long teacherId);

    /**
     * Find unique conversations for parent (distinct parent-teacher-student combinations)
     */
    @Query("SELECT DISTINCT pm.parentId, pm.teacherId, pm.studentId FROM ParentMessage pm WHERE pm.parentId = :parentId")
    List<Object[]> findUniqueConversationsForParent(@Param("parentId") Long parentId);

    /**
     * Find unique conversations for teacher (distinct parent-teacher-student combinations)
     */
    @Query("SELECT DISTINCT pm.parentId, pm.teacherId, pm.studentId FROM ParentMessage pm WHERE pm.teacherId = :teacherId")
    List<Object[]> findUniqueConversationsForTeacher(@Param("teacherId") Long teacherId);

    /**
     * Find latest message in each conversation for parent
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.parentId = :parentId AND pm.createdAt = " +
           "(SELECT MAX(pm2.createdAt) FROM ParentMessage pm2 WHERE pm2.parentId = pm.parentId AND pm2.teacherId = pm.teacherId AND pm2.studentId = pm.studentId)")
    List<ParentMessage> findLatestMessageInEachConversationForParent(@Param("parentId") Long parentId);

    /**
     * Find latest message in each conversation for teacher
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.teacherId = :teacherId AND pm.createdAt = " +
           "(SELECT MAX(pm2.createdAt) FROM ParentMessage pm2 WHERE pm2.parentId = pm.parentId AND pm2.teacherId = pm.teacherId AND pm2.studentId = pm.studentId)")
    List<ParentMessage> findLatestMessageInEachConversationForTeacher(@Param("teacherId") Long teacherId);

    /**
     * Search messages by content
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE LOWER(pm.messageContent) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(pm.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY pm.createdAt DESC")
    List<ParentMessage> searchMessages(@Param("searchTerm") String searchTerm);

    /**
     * Search messages for parent
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.parentId = :parentId AND (LOWER(pm.messageContent) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(pm.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY pm.createdAt DESC")
    List<ParentMessage> searchMessagesForParent(@Param("parentId") Long parentId, @Param("searchTerm") String searchTerm);

    /**
     * Search messages for teacher
     */
    @Query("SELECT pm FROM ParentMessage pm WHERE pm.teacherId = :teacherId AND (LOWER(pm.messageContent) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(pm.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY pm.createdAt DESC")
    List<ParentMessage> searchMessagesForTeacher(@Param("teacherId") Long teacherId, @Param("searchTerm") String searchTerm);
}