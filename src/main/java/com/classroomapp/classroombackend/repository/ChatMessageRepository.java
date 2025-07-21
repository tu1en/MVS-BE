package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.ChatMessage;
import com.classroomapp.classroombackend.model.LiveStream;
import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * Repository cho ChatMessage
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Tìm tất cả messages trong live stream, sắp xếp theo thời gian
     */
    List<ChatMessage> findByLiveStreamOrderByCreatedAtAsc(LiveStream liveStream);
    
    /**
     * Tìm messages với phân trang
     */
    List<ChatMessage> findByLiveStreamOrderByCreatedAtDesc(LiveStream liveStream, Pageable pageable);
    
    /**
     * Tìm messages từ thời điểm cụ thể (cho real-time sync)
     */
    List<ChatMessage> findByLiveStreamAndCreatedAtAfterOrderByCreatedAtAsc(
            LiveStream liveStream, LocalDateTime after);
    
    /**
     * Tìm messages theo sender
     */
    List<ChatMessage> findByLiveStreamAndSenderOrderByCreatedAtDesc(
            LiveStream liveStream, User sender, Pageable pageable);
    
    /**
     * Tìm messages theo type
     */
    List<ChatMessage> findByLiveStreamAndMessageTypeOrderByCreatedAtAsc(
            LiveStream liveStream, ChatMessage.MessageType messageType);
    
    /**
     * Tìm pinned messages
     */
    List<ChatMessage> findByLiveStreamAndIsPinnedTrueOrderByCreatedAtAsc(LiveStream liveStream);
    
    /**
     * Tìm private messages giữa 2 users
     */
    @Query("SELECT c FROM ChatMessage c WHERE c.liveStream = :liveStream AND c.isPrivate = true " +
           "AND ((c.sender = :user1 AND c.recipientId = :user2Id) OR " +
           "(c.sender = :user2 AND c.recipientId = :user1Id)) ORDER BY c.createdAt ASC")
    List<ChatMessage> findPrivateMessagesBetweenUsers(
            @Param("liveStream") LiveStream liveStream,
            @Param("user1") User user1,
            @Param("user1Id") Long user1Id,
            @Param("user2") User user2,
            @Param("user2Id") Long user2Id);
    
    /**
     * Đếm unread messages cho user
     */
    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.liveStream = :liveStream " +
           "AND c.createdAt > :lastRead AND c.sender != :user " +
           "AND (c.isPrivate = false OR c.recipientId = :userId)")
    long countUnreadMessages(@Param("liveStream") LiveStream liveStream,
                           @Param("lastRead") LocalDateTime lastRead,
                           @Param("user") User user,
                           @Param("userId") Long userId);
    
    /**
     * Tìm messages có từ khóa (search)
     */
    @Query("SELECT c FROM ChatMessage c WHERE c.liveStream = :liveStream " +
           "AND LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY c.createdAt DESC")
    List<ChatMessage> searchMessages(@Param("liveStream") LiveStream liveStream,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);
    
    /**
     * Tìm raise hand messages chưa được xử lý
     */
    @Query("SELECT c FROM ChatMessage c WHERE c.liveStream = :liveStream " +
           "AND c.messageType = 'RAISE_HAND' " +
           "AND c.messageStatus = 'SENT' ORDER BY c.createdAt ASC")
    List<ChatMessage> findPendingRaiseHandMessages(LiveStream liveStream);
    
    /**
     * Tìm system notifications
     */
    List<ChatMessage> findByLiveStreamAndMessageTypeInOrderByCreatedAtDesc(
            LiveStream liveStream, List<ChatMessage.MessageType> messageTypes);
    
    /**
     * Soft delete message
     */
    @Modifying
    @Query("UPDATE ChatMessage c SET c.messageStatus = 'DELETED', c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.id = :messageId")
    int softDeleteMessage(@Param("messageId") Long messageId);
    
    /**
     * Pin message
     */
    @Modifying
    @Query("UPDATE ChatMessage c SET c.isPinned = :pinned, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.id = :messageId")
    int pinMessage(@Param("messageId") Long messageId, @Param("pinned") boolean pinned);
    
    /**
     * Update message status for read receipts
     */
    @Modifying
    @Query("UPDATE ChatMessage c SET c.messageStatus = :status, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.liveStream = :liveStream AND c.sender = :sender " +
           "AND c.createdAt <= :timestamp AND c.messageStatus = 'DELIVERED'")
    int updateMessageStatus(@Param("liveStream") LiveStream liveStream,
                          @Param("sender") User sender,
                          @Param("timestamp") LocalDateTime timestamp,
                          @Param("status") ChatMessage.MessageStatus status);
    
    /**
     * Lấy chat statistics
     */
    @Query("SELECT c.sender.id as senderId, c.sender.fullName as senderName, COUNT(c) as messageCount " +
           "FROM ChatMessage c WHERE c.liveStream = :liveStream AND c.messageStatus != 'DELETED' " +
           "GROUP BY c.sender.id, c.sender.fullName ORDER BY messageCount DESC")
    List<Object[]> getChatStatistics(@Param("liveStream") LiveStream liveStream);
    
    /**
     * Cleanup old messages
     */
    @Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.liveStream = :liveStream " +
           "AND c.messageStatus = 'DELETED' AND c.updatedAt < :before")
    int cleanupOldMessages(@Param("liveStream") LiveStream liveStream,
                         @Param("before") LocalDateTime before);
    
    /**
     * Tìm messages reply cho message cụ thể
     */
    List<ChatMessage> findByLiveStreamAndReplyToMessageIdOrderByCreatedAtAsc(
            LiveStream liveStream, Long replyToMessageId);
    
    /**
     * Count messages theo type
     */
    long countByLiveStreamAndMessageType(LiveStream liveStream, ChatMessage.MessageType messageType);
    
    /**
     * Tìm recent system messages
     */
    @Query("SELECT c FROM ChatMessage c WHERE c.liveStream = :liveStream " +
           "AND c.messageType IN ('SYSTEM', 'NOTIFICATION') " +
           "AND c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<ChatMessage> findRecentSystemMessages(@Param("liveStream") LiveStream liveStream,
                                             @Param("since") LocalDateTime since);
    
    /**
     * Moderate message
     */
    @Modifying
    @Query("UPDATE ChatMessage c SET c.isModerated = true, c.moderatedBy = :moderatorId, " +
           "c.moderatedAt = CURRENT_TIMESTAMP, c.moderationReason = :reason, " +
           "c.messageStatus = 'MODERATED', c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.id = :messageId")
    int moderateMessage(@Param("messageId") Long messageId,
                       @Param("moderatorId") Long moderatorId,
                       @Param("reason") String reason);
}