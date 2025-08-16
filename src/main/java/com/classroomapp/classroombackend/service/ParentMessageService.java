package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.ParentMessage;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Parent-Teacher messaging functionality
 * Based on PARENT_ROLE_SPEC.md Phase 2 requirements
 */
public interface ParentMessageService {

    /**
     * Send message from parent to teacher
     */
    ParentMessage sendMessageFromParent(Long parentId, Long teacherId, Long studentId, 
                                       String subject, String messageContent);

    /**
     * Send reply message from parent to teacher
     */
    ParentMessage sendReplyFromParent(Long parentId, Long teacherId, Long studentId, 
                                     String subject, String messageContent, Long replyToId);

    /**
     * Send message from teacher to parent
     */
    ParentMessage sendMessageFromTeacher(Long teacherId, Long parentId, Long studentId, 
                                        String subject, String messageContent);

    /**
     * Send reply message from teacher to parent
     */
    ParentMessage sendReplyFromTeacher(Long teacherId, Long parentId, Long studentId, 
                                      String subject, String messageContent, Long replyToId);

    /**
     * Send message from parent to student
     */
    ParentMessage sendMessageFromParentToStudent(Long parentId, Long studentId, 
                                                String subject, String messageContent);

    /**
     * Send message from student to parent
     */
    ParentMessage sendMessageFromStudentToParent(Long studentId, Long parentId, 
                                                String subject, String messageContent);

    /**
     * Send reply message from parent to student
     */
    ParentMessage sendReplyFromParentToStudent(Long parentId, Long studentId, 
                                             String subject, String messageContent, Long replyToId);

    /**
     * Send reply message from student to parent
     */
    ParentMessage sendReplyFromStudentToParent(Long studentId, Long parentId, 
                                             String subject, String messageContent, Long replyToId);

    /**
     * Get conversation between parent and student
     */
    List<ParentMessage> getParentStudentConversation(Long parentId, Long studentId);

    /**
     * Get conversation between parent and teacher about specific student
     */
    List<ParentMessage> getConversation(Long parentId, Long teacherId, Long studentId);

    /**
     * Get all conversations for parent
     */
    List<ConversationSummary> getParentConversations(Long parentId);

    /**
     * Get all conversations for teacher
     */
    List<ConversationSummary> getTeacherConversations(Long teacherId);

    /**
     * Get message by ID
     */
    Optional<ParentMessage> getMessageById(Long messageId);

    /**
     * Mark message as read
     */
    ParentMessage markMessageAsRead(Long messageId);

    /**
     * Mark conversation as read for user
     */
    void markConversationAsRead(Long parentId, Long teacherId, Long studentId, ParentMessage.SenderType readerType);

    /**
     * Get unread messages for parent
     */
    List<ParentMessage> getUnreadMessagesForParent(Long parentId);

    /**
     * Get unread messages for teacher
     */
    List<ParentMessage> getUnreadMessagesForTeacher(Long teacherId);

    /**
     * Count unread messages for parent
     */
    Long countUnreadMessagesForParent(Long parentId);

    /**
     * Count unread messages for teacher
     */
    Long countUnreadMessagesForTeacher(Long teacherId);

    /**
     * Get conversation threads for parent (summary view)
     */
    List<ParentMessage> getConversationThreadsForParent(Long parentId);

    /**
     * Get conversation threads for teacher (summary view)
     */
    List<ParentMessage> getConversationThreadsForTeacher(Long teacherId);

    /**
     * Search messages
     */
    List<ParentMessage> searchMessages(String searchTerm, Long userId, ParentMessage.SenderType userType);

    /**
     * Get recent messages (for dashboard)
     */
    List<ParentMessage> getRecentMessages(Long userId, ParentMessage.SenderType userType, int days);

    /**
     * Validate parent has access to student
     */
    boolean validateParentStudentAccess(Long parentId, Long studentId);

    /**
     * Get message statistics for parent
     */
    MessageStatistics getMessageStatisticsForParent(Long parentId);

    /**
     * Get message statistics for teacher
     */
    MessageStatistics getMessageStatisticsForTeacher(Long teacherId);

    /**
     * Delete message (soft delete - mark as deleted)
     */
    void deleteMessage(Long messageId, Long userId, ParentMessage.SenderType userType);

    // DTO Classes

    /**
     * Conversation summary for listing conversations
     */
    class ConversationSummary {
        private Long parentId;
        private Long teacherId;
        private Long studentId;
        private String parentName;
        private String teacherName;
        private String studentName;
        private String lastMessage;
        private String lastMessageTime;
        private Long unreadCount;
        private ParentMessage.SenderType lastSenderType;

        public ConversationSummary(Long parentId, Long teacherId, Long studentId, 
                                 String parentName, String teacherName, String studentName,
                                 String lastMessage, String lastMessageTime, Long unreadCount,
                                 ParentMessage.SenderType lastSenderType) {
            this.parentId = parentId;
            this.teacherId = teacherId;
            this.studentId = studentId;
            this.parentName = parentName;
            this.teacherName = teacherName;
            this.studentName = studentName;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
            this.unreadCount = unreadCount;
            this.lastSenderType = lastSenderType;
        }

        // Getters and setters
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }

        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getParentName() { return parentName; }
        public void setParentName(String parentName) { this.parentName = parentName; }

        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

        public String getLastMessageTime() { return lastMessageTime; }
        public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }

        public Long getUnreadCount() { return unreadCount; }
        public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }

        public ParentMessage.SenderType getLastSenderType() { return lastSenderType; }
        public void setLastSenderType(ParentMessage.SenderType lastSenderType) { this.lastSenderType = lastSenderType; }
    }

    /**
     * Message statistics
     */
    class MessageStatistics {
        private Long totalMessages;
        private Long unreadMessages;
        private Long sentMessages;
        private Long receivedMessages;
        private Long conversationCount;

        public MessageStatistics(Long totalMessages, Long unreadMessages, Long sentMessages, 
                               Long receivedMessages, Long conversationCount) {
            this.totalMessages = totalMessages;
            this.unreadMessages = unreadMessages;
            this.sentMessages = sentMessages;
            this.receivedMessages = receivedMessages;
            this.conversationCount = conversationCount;
        }

        // Getters and setters
        public Long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(Long totalMessages) { this.totalMessages = totalMessages; }

        public Long getUnreadMessages() { return unreadMessages; }
        public void setUnreadMessages(Long unreadMessages) { this.unreadMessages = unreadMessages; }

        public Long getSentMessages() { return sentMessages; }
        public void setSentMessages(Long sentMessages) { this.sentMessages = sentMessages; }

        public Long getReceivedMessages() { return receivedMessages; }
        public void setReceivedMessages(Long receivedMessages) { this.receivedMessages = receivedMessages; }

        public Long getConversationCount() { return conversationCount; }
        public void setConversationCount(Long conversationCount) { this.conversationCount = conversationCount; }
    }
}