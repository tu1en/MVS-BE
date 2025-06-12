package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.StudentMessageDto;

public interface StudentMessageService {
    
    // Send a new message
    StudentMessageDto sendMessage(StudentMessageDto messageDto);
    
    // Get message by ID
    StudentMessageDto getMessageById(Long messageId);
    
    // Get messages sent by a user
    List<StudentMessageDto> getSentMessages(Long senderId);
    
    // Get messages received by a user
    List<StudentMessageDto> getReceivedMessages(Long recipientId);
    
    // Get conversation between two users
    List<StudentMessageDto> getConversation(Long user1Id, Long user2Id);
    
    // Get unread messages for a recipient
    List<StudentMessageDto> getUnreadMessages(Long recipientId);
    
    // Get messages by status
    List<StudentMessageDto> getMessagesByStatus(String status);
    
    // Get messages by type
    List<StudentMessageDto> getMessagesByType(String messageType);
    
    // Get messages by priority
    List<StudentMessageDto> getMessagesByPriority(String priority);
    
    // Get urgent messages for a recipient
    List<StudentMessageDto> getUrgentMessages(Long recipientId);
    
    // Search messages
    List<StudentMessageDto> searchMessages(String keyword);
    
    // Get recent messages
    List<StudentMessageDto> getRecentMessages();
    
    // Get pending replies for a recipient
    List<StudentMessageDto> getPendingReplies(Long recipientId);
    
    // Mark message as read
    StudentMessageDto markAsRead(Long messageId);
    
    // Reply to a message
    StudentMessageDto replyToMessage(Long messageId, String reply, Long replierId);
    
    // Resolve a message
    StudentMessageDto resolveMessage(Long messageId);
    
    // Archive a message
    StudentMessageDto archiveMessage(Long messageId);
    
    // Count unread messages for a recipient
    Long countUnreadMessages(Long recipientId);
    
    // Update message priority
    StudentMessageDto updateMessagePriority(Long messageId, String priority);
    
    // Delete a message
    void deleteMessage(Long messageId);
    
    // Get all messages (admin only)
    List<StudentMessageDto> getAllMessages();
}
