package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.model.ChatMessage;

/**
 * Service để quản lý chat trong live sessions
 */
public interface ChatService {
    
    /**
     * Gửi tin nhắn text
     * @param liveStreamId ID của live stream
     * @param senderId ID của người gửi
     * @param content Nội dung tin nhắn
     * @param isPrivate Có phải tin nhắn riêng tư không
     * @param recipientId ID người nhận (nếu là private)
     * @param replyToMessageId ID tin nhắn được reply (optional)
     * @return ChatMessage đã được lưu
     */
    ChatMessage sendTextMessage(Long liveStreamId, Long senderId, String content, 
                               boolean isPrivate, Long recipientId, Long replyToMessageId);
    
    /**
     * Gửi emoji message
     * @param liveStreamId ID của live stream
     * @param senderId ID của người gửi
     * @param emoji Emoji được gửi
     * @return ChatMessage đã được lưu
     */
    ChatMessage sendEmojiMessage(Long liveStreamId, Long senderId, String emoji);
    
    /**
     * Gửi file message
     * @param liveStreamId ID của live stream
     * @param senderId ID của người gửi
     * @param fileUrl URL của file
     * @param fileName Tên file
     * @param fileSize Kích thước file
     * @return ChatMessage đã được lưu
     */
    ChatMessage sendFileMessage(Long liveStreamId, Long senderId, String fileUrl, 
                               String fileName, Long fileSize);
    
    /**
     * Raise hand action
     * @param liveStreamId ID của live stream
     * @param studentId ID của student
     * @return ChatMessage raise hand
     */
    ChatMessage raiseHand(Long liveStreamId, Long studentId);
    
    /**
     * Handle raise hand response từ teacher
     * @param messageId ID của raise hand message
     * @param teacherId ID của teacher
     * @param response Teacher response (approve/deny)
     * @return Updated message
     */
    ChatMessage handleRaiseHandResponse(Long messageId, Long teacherId, String response);
    
    /**
     * Lấy tất cả messages trong live stream
     * @param liveStreamId ID của live stream
     * @return List messages sắp xếp theo thời gian
     */
    List<ChatMessage> getChatMessages(Long liveStreamId);
    
    /**
     * Lấy messages với phân trang
     * @param liveStreamId ID của live stream
     * @param pageable Thông tin phân trang
     * @return List messages
     */
    List<ChatMessage> getChatMessages(Long liveStreamId, Pageable pageable);
    
    /**
     * Lấy messages từ thời điểm cụ thể (cho real-time sync)
     * @param liveStreamId ID của live stream
     * @param since Thời điểm bắt đầu
     * @return List messages mới
     */
    List<ChatMessage> getMessagesSince(Long liveStreamId, LocalDateTime since);
    
    /**
     * Lấy private messages giữa 2 users
     * @param liveStreamId ID của live stream
     * @param user1Id ID user 1
     * @param user2Id ID user 2
     * @return List private messages
     */
    List<ChatMessage> getPrivateMessages(Long liveStreamId, Long user1Id, Long user2Id);
    
    /**
     * Search messages theo từ khóa
     * @param liveStreamId ID của live stream
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Phân trang
     * @return List messages match từ khóa
     */
    List<ChatMessage> searchMessages(Long liveStreamId, String keyword, Pageable pageable);
    
    /**
     * Pin/unpin message
     * @param messageId ID của message
     * @param teacherId ID của teacher (chỉ teacher mới có thể pin)
     * @param pinned true để pin, false để unpin
     * @return true nếu thành công
     */
    boolean pinMessage(Long messageId, Long teacherId, boolean pinned);
    
    /**
     * Delete message
     * @param messageId ID của message
     * @param userId ID của user xóa
     * @param userRole Role của user
     * @return true nếu xóa thành công
     */
    boolean deleteMessage(Long messageId, Long userId, String userRole);
    
    /**
     * Moderate message (chỉ teacher)
     * @param messageId ID của message
     * @param teacherId ID của teacher
     * @param reason Lý do moderate
     * @return true nếu thành công
     */
    boolean moderateMessage(Long messageId, Long teacherId, String reason);
    
    /**
     * Add emoji reaction vào message
     * @param messageId ID của message
     * @param userId ID của user react
     * @param emoji Emoji reaction
     * @return Updated message
     */
    ChatMessage addEmojiReaction(Long messageId, Long userId, String emoji);
    
    /**
     * Get pinned messages trong live stream
     * @param liveStreamId ID của live stream
     * @return List pinned messages
     */
    List<ChatMessage> getPinnedMessages(Long liveStreamId);
    
    /**
     * Get pending raise hand requests
     * @param liveStreamId ID của live stream
     * @return List raise hand messages chưa xử lý
     */
    List<ChatMessage> getPendingRaiseHands(Long liveStreamId);
    
    /**
     * Mark messages as read
     * @param liveStreamId ID của live stream
     * @param userId ID của user
     * @param timestamp Thời điểm read
     * @return Số message đã mark read
     */
    int markMessagesAsRead(Long liveStreamId, Long userId, LocalDateTime timestamp);
    
    /**
     * Count unread messages cho user
     * @param liveStreamId ID của live stream
     * @param userId ID của user
     * @param lastRead Thời điểm read cuối cùng
     * @return Số message chưa đọc
     */
    long countUnreadMessages(Long liveStreamId, Long userId, LocalDateTime lastRead);
    
    /**
     * Get chat statistics
     * @param liveStreamId ID của live stream
     * @return Map với statistics
     */
    Map<String, Object> getChatStatistics(Long liveStreamId);
    
    /**
     * Send system notification
     * @param liveStreamId ID của live stream
     * @param content Nội dung notification
     * @return System message
     */
    ChatMessage sendSystemNotification(Long liveStreamId, String content);
    
    /**
     * Send user join notification
     * @param liveStreamId ID của live stream
     * @param userId ID của user join
     * @return Notification message
     */
    ChatMessage sendUserJoinNotification(Long liveStreamId, Long userId);
    
    /**
     * Send user leave notification
     * @param liveStreamId ID của live stream
     * @param userId ID của user leave
     * @return Notification message
     */
    ChatMessage sendUserLeaveNotification(Long liveStreamId, Long userId);
    
    /**
     * Enable/disable chat cho live stream
     * @param liveStreamId ID của live stream
     * @param teacherId ID của teacher
     * @param enabled true để enable, false để disable
     * @return System message về việc enable/disable
     */
    ChatMessage toggleChatEnabled(Long liveStreamId, Long teacherId, boolean enabled);
    
    /**
     * Clear all chat messages (chỉ teacher)
     * @param liveStreamId ID của live stream
     * @param teacherId ID của teacher
     * @return Số message đã xóa
     */
    int clearAllMessages(Long liveStreamId, Long teacherId);
    
    /**
     * Export chat history
     * @param liveStreamId ID của live stream
     * @param format Export format (JSON, CSV, TXT)
     * @return Export data
     */
    Map<String, Object> exportChatHistory(Long liveStreamId, String format);
    
    /**
     * Cleanup old deleted messages
     * @param olderThan Xóa messages cũ hơn thời điểm này
     * @return Số message đã cleanup
     */
    int cleanupOldMessages(LocalDateTime olderThan);
    
    /**
     * Broadcast message to all participants
     * @param liveStreamId ID của live stream
     * @param message Message data
     */
    void broadcastMessage(Long liveStreamId, ChatMessage message);
    
    /**
     * Send private message notification
     * @param liveStreamId ID của live stream
     * @param senderId ID người gửi
     * @param recipientId ID người nhận
     * @param message Private message
     */
    void sendPrivateMessageNotification(Long liveStreamId, Long senderId, Long recipientId, ChatMessage message);
}