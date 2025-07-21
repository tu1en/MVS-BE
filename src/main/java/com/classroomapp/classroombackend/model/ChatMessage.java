package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity để lưu trữ tin nhắn chat trong live sessions
 */
@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    
    public enum MessageType {
        TEXT,
        EMOJI,
        SYSTEM,
        FILE,
        IMAGE,
        POLL,
        RAISE_HAND,
        NOTIFICATION
    }
    
    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ,
        DELETED,
        MODERATED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "live_stream_id", nullable = false)
    private LiveStream liveStream;
    
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
    
    @Column(name = "content", columnDefinition = "NTEXT")
    private String content;
    
    @Column(name = "reply_to_message_id")
    private Long replyToMessageId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_status", nullable = false)
    @Builder.Default
    private MessageStatus messageStatus = MessageStatus.SENT;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;
    
    @Column(name = "is_private")
    @Builder.Default
    private Boolean isPrivate = false;
    
    @Column(name = "recipient_id")
    private Long recipientId; // For private messages
    
    @Column(name = "emoji_reactions", length = 500)
    private String emojiReactions; // JSON string để lưu reactions
    
    @Column(name = "file_url", length = 500)
    private String fileUrl;
    
    @Column(name = "file_name", length = 255)
    private String fileName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "is_moderated")
    @Builder.Default
    private Boolean isModerated = false;
    
    @Column(name = "moderated_by")
    private Long moderatedBy;
    
    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;
    
    @Column(name = "moderation_reason", length = 255)
    private String moderationReason;
    
    /**
     * Check if message can be edited by user
     */
    public boolean canBeEditedBy(Long userId) {
        if (!sender.getId().equals(userId)) {
            return false;
        }
        
        // Allow editing within 5 minutes
        LocalDateTime editDeadline = createdAt.plusMinutes(5);
        return LocalDateTime.now().isBefore(editDeadline);
    }
    
    /**
     * Check if message can be deleted by user
     */
    public boolean canBeDeletedBy(Long userId, String userRole) {
        // Sender can always delete their own message
        if (sender.getId().equals(userId)) {
            return true;
        }
        
        // Teachers can delete any message in their session
        return "TEACHER".equals(userRole);
    }
    
    /**
     * Create system message
     */
    public static ChatMessage createSystemMessage(LiveStream liveStream, String content) {
        return ChatMessage.builder()
                .liveStream(liveStream)
                .sender(null) // System message has no sender
                .messageType(MessageType.SYSTEM)
                .content(content)
                .messageStatus(MessageStatus.DELIVERED)
                .build();
    }
    
    /**
     * Create notification message
     */
    public static ChatMessage createNotification(LiveStream liveStream, User user, String content) {
        return ChatMessage.builder()
                .liveStream(liveStream)
                .sender(user)
                .messageType(MessageType.NOTIFICATION)
                .content(content)
                .messageStatus(MessageStatus.DELIVERED)
                .build();
    }
    
    /**
     * Create raise hand message
     */
    public static ChatMessage createRaiseHandMessage(LiveStream liveStream, User student) {
        return ChatMessage.builder()
                .liveStream(liveStream)
                .sender(student)
                .messageType(MessageType.RAISE_HAND)
                .content(student.getFullName() + " đã giơ tay")
                .messageStatus(MessageStatus.SENT)
                .build();
    }
    
    /**
     * Convert to JSON for real-time transmission
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(id).append(",");
        json.append("\"messageType\":\"").append(messageType).append("\",");
        json.append("\"content\":\"").append(content != null ? content.replace("\"", "\\\"") : "").append("\",");
        json.append("\"senderId\":").append(sender != null ? sender.getId() : "null").append(",");
        json.append("\"senderName\":\"").append(sender != null ? sender.getFullName() : "System").append("\",");
        json.append("\"timestamp\":\"").append(createdAt).append("\",");
        json.append("\"isPinned\":").append(isPinned).append(",");
        json.append("\"isPrivate\":").append(isPrivate).append(",");
        
        if (replyToMessageId != null) {
            json.append("\"replyToMessageId\":").append(replyToMessageId).append(",");
        }
        
        if (fileUrl != null) {
            json.append("\"fileUrl\":\"").append(fileUrl).append("\",");
            json.append("\"fileName\":\"").append(fileName).append("\",");
            json.append("\"fileSize\":").append(fileSize).append(",");
        }
        
        if (emojiReactions != null) {
            json.append("\"reactions\":").append(emojiReactions).append(",");
        }
        
        json.append("\"status\":\"").append(messageStatus).append("\"");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Moderate message
     */
    public void moderate(Long moderatorId, String reason) {
        this.isModerated = true;
        this.moderatedBy = moderatorId;
        this.moderatedAt = LocalDateTime.now();
        this.moderationReason = reason;
        this.messageStatus = MessageStatus.MODERATED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Add emoji reaction
     */
    public void addEmojiReaction(String emoji, Long userId) {
        // Simple implementation - in production, use proper JSON structure
        if (emojiReactions == null) {
            emojiReactions = "{\"" + emoji + "\":[" + userId + "]}";
        } else {
            // This is simplified - proper implementation would parse and update JSON
            emojiReactions += ",\"" + emoji + "\":[" + userId + "]";
        }
        this.updatedAt = LocalDateTime.now();
    }
}