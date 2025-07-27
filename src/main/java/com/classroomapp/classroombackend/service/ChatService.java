package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.model.ChatMessage;

/**
 * Service Ä‘á»ƒ quáº£n lÃ½ chat trong live sessions
 */
public interface ChatService {
    
    /**
     * Gá»­i tin nháº¯n text
     * @param liveStreamId ID cá»§a live stream
     * @param senderId ID cá»§a ngÆ°á»i gá»­i
     * @param content Ná»™i dung tin nháº¯n
     * @param isPrivate CÃ³ pháº£i tin nháº¯n riÃªng tÆ° khÃ´ng
     * @param recipientId ID ngÆ°á»i nháº­n (náº¿u lÃ  private)
     * @param replyToMessageId ID tin nháº¯n Ä‘Æ°á»£c reply (optional)
     * @return ChatMessage Ä‘Ã£ Ä‘Æ°á»£c lÆ°u
     */
    ChatMessage sendTextMessage(Long liveStreamId, Long senderId, String content, 
                               boolean isPrivate, Long recipientId, Long replyToMessageId);
    
    /**
     * Gá»­i emoji message
     * @param liveStreamId ID cá»§a live stream
     * @param senderId ID cá»§a ngÆ°á»i gá»­i
     * @param emoji Emoji Ä‘Æ°á»£c gá»­i
     * @return ChatMessage Ä‘Ã£ Ä‘Æ°á»£c lÆ°u
     */
    ChatMessage sendEmojiMessage(Long liveStreamId, Long senderId, String emoji);
    
    /**
     * Gá»­i file message
     * @param liveStreamId ID cá»§a live stream
     * @param senderId ID cá»§a ngÆ°á»i gá»­i
     * @param fileUrl URL cá»§a file
     * @param fileName TÃªn file
     * @param fileSize KÃ­ch thÆ°á»›c file
     * @return ChatMessage Ä‘Ã£ Ä‘Æ°á»£c lÆ°u
     */
    ChatMessage sendFileMessage(Long liveStreamId, Long senderId, String fileUrl, 
                               String fileName, Long fileSize);
    
    /**
     * Raise hand action
     * @param liveStreamId ID cá»§a live stream
     * @param studentId ID cá»§a student
     * @return ChatMessage raise hand
     */
    ChatMessage raiseHand(Long liveStreamId, Long studentId);
    
    /**
     * Handle raise hand response tá»« teacher
     * @param messageId ID cá»§a raise hand message
     * @param teacherId ID cá»§a teacher
     * @param response Teacher response (approve/deny)
     * @return Updated message
     */
    ChatMessage handleRaiseHandResponse(Long messageId, Long teacherId, String response);
    
    /**
     * Láº¥y táº¥t cáº£ messages trong live stream
     * @param liveStreamId ID cá»§a live stream
     * @return List messages sáº¯p xáº¿p theo thá»i gian
     */
    List<ChatMessage> getChatMessages(Long liveStreamId);
    
    /**
     * Láº¥y messages vá»›i phÃ¢n trang
     * @param liveStreamId ID cá»§a live stream
     * @param pageable ThÃ´ng tin phÃ¢n trang
     * @return List messages
     */
    List<ChatMessage> getChatMessages(Long liveStreamId, Pageable pageable);
    
    /**
     * Láº¥y messages tá»« thá»i Ä‘iá»ƒm cá»¥ thá»ƒ (cho real-time sync)
     * @param liveStreamId ID cá»§a live stream
     * @param since Thá»i Ä‘iá»ƒm báº¯t Ä‘áº§u
     * @return List messages má»›i
     */
    List<ChatMessage> getMessagesSince(Long liveStreamId, LocalDateTime since);
    
    /**
     * Láº¥y private messages giá»¯a 2 users
     * @param liveStreamId ID cá»§a live stream
     * @param user1Id ID user 1
     * @param user2Id ID user 2
     * @return List private messages
     */
    List<ChatMessage> getPrivateMessages(Long liveStreamId, Long user1Id, Long user2Id);
    
    /**
     * Search messages theo tá»« khÃ³a
     * @param liveStreamId ID cá»§a live stream
     * @param keyword Tá»« khÃ³a tÃ¬m kiáº¿m
     * @param pageable PhÃ¢n trang
     * @return List messages match tá»« khÃ³a
     */
    List<ChatMessage> searchMessages(Long liveStreamId, String keyword, Pageable pageable);
    
    /**
     * Pin/unpin message
     * @param messageId ID cá»§a message
     * @param teacherId ID cá»§a teacher (chá»‰ teacher má»›i cÃ³ thá»ƒ pin)
     * @param pinned true Ä‘á»ƒ pin, false Ä‘á»ƒ unpin
     * @return true náº¿u thÃ nh cÃ´ng
     */
    boolean pinMessage(Long messageId, Long teacherId, boolean pinned);
    
    /**
     * Delete message
     * @param messageId ID cá»§a message
     * @param userId ID cá»§a user xÃ³a
     * @param userRole Role cá»§a user
     * @return true náº¿u xÃ³a thÃ nh cÃ´ng
     */
    boolean deleteMessage(Long messageId, Long userId, String userRole);
    
    /**
     * Moderate message (chá»‰ teacher)
     * @param messageId ID cá»§a message
     * @param teacherId ID cá»§a teacher
     * @param reason LÃ½ do moderate
     * @return true náº¿u thÃ nh cÃ´ng
     */
    boolean moderateMessage(Long messageId, Long teacherId, String reason);
    
    /**
     * Add emoji reaction vÃ o message
     * @param messageId ID cá»§a message
     * @param userId ID cá»§a user react
     * @param emoji Emoji reaction
     * @return Updated message
     */
    ChatMessage addEmojiReaction(Long messageId, Long userId, String emoji);
    
    /**
     * Get pinned messages trong live stream
     * @param liveStreamId ID cá»§a live stream
     * @return List pinned messages
     */
    List<ChatMessage> getPinnedMessages(Long liveStreamId);
    
    /**
     * Get pending raise hand requests
     * @param liveStreamId ID cá»§a live stream
     * @return List raise hand messages chÆ°a xá»­ lÃ½
     */
    List<ChatMessage> getPendingRaiseHands(Long liveStreamId);
    
    /**
     * Mark messages as read
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user
     * @param timestamp Thá»i Ä‘iá»ƒm read
     * @return Sá»‘ message Ä‘Ã£ mark read
     */
    int markMessagesAsRead(Long liveStreamId, Long userId, LocalDateTime timestamp);
    
    /**
     * Count unread messages cho user
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user
     * @param lastRead Thá»i Ä‘iá»ƒm read cuá»‘i cÃ¹ng
     * @return Sá»‘ message chÆ°a Ä‘á»c
     */
    long countUnreadMessages(Long liveStreamId, Long userId, LocalDateTime lastRead);
    
    /**
     * Get chat statistics
     * @param liveStreamId ID cá»§a live stream
     * @return Map vá»›i statistics
     */
    Map<String, Object> getChatStatistics(Long liveStreamId);
    
    /**
     * Send system notification
     * @param liveStreamId ID cá»§a live stream
     * @param content Ná»™i dung notification
     * @return System message
     */
    ChatMessage sendSystemNotification(Long liveStreamId, String content);
    
    /**
     * Send user join notification
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user join
     * @return Notification message
     */
    ChatMessage sendUserJoinNotification(Long liveStreamId, Long userId);
    
    /**
     * Send user leave notification
     * @param liveStreamId ID cá»§a live stream
     * @param userId ID cá»§a user leave
     * @return Notification message
     */
    ChatMessage sendUserLeaveNotification(Long liveStreamId, Long userId);
    
    /**
     * Enable/disable chat cho live stream
     * @param liveStreamId ID cá»§a live stream
     * @param teacherId ID cá»§a teacher
     * @param enabled true Ä‘á»ƒ enable, false Ä‘á»ƒ disable
     * @return System message vá» viá»‡c enable/disable
     */
    ChatMessage toggleChatEnabled(Long liveStreamId, Long teacherId, boolean enabled);
    
    /**
     * Clear all chat messages (chá»‰ teacher)
     * @param liveStreamId ID cá»§a live stream
     * @param teacherId ID cá»§a teacher
     * @return Sá»‘ message Ä‘Ã£ xÃ³a
     */
    int clearAllMessages(Long liveStreamId, Long teacherId);
    
    /**
     * Export chat history
     * @param liveStreamId ID cá»§a live stream
     * @param format Export format (JSON, CSV, TXT)
     * @return Export data
     */
    Map<String, Object> exportChatHistory(Long liveStreamId, String format);
    
    /**
     * Cleanup old deleted messages
     * @param olderThan XÃ³a messages cÅ© hÆ¡n thá»i Ä‘iá»ƒm nÃ y
     * @return Sá»‘ message Ä‘Ã£ cleanup
     */
    int cleanupOldMessages(LocalDateTime olderThan);
    
    /**
     * Broadcast message to all participants
     * @param liveStreamId ID cá»§a live stream
     * @param message Message data
     */
    void broadcastMessage(Long liveStreamId, ChatMessage message);
    
    /**
     * Send private message notification
     * @param liveStreamId ID cá»§a live stream
     * @param senderId ID ngÆ°á»i gá»­i
     * @param recipientId ID ngÆ°á»i nháº­n
     * @param message Private message
     */
    void sendPrivateMessageNotification(Long liveStreamId, Long senderId, Long recipientId, ChatMessage message);
}
