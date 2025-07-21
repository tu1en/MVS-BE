package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.ChatMessage;
import com.classroomapp.classroombackend.model.LiveStream;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ChatMessageRepository;
import com.classroomapp.classroombackend.repository.LiveStreamRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ChatService;

/**
 * Implementation của ChatService
 */
@Service
@Transactional
public class ChatServiceImpl implements ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private LiveStreamRepository liveStreamRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Override
    public ChatMessage sendTextMessage(Long liveStreamId, Long senderId, String content, 
                                     boolean isPrivate, Long recipientId, Long replyToMessageId) {
        try {
            logger.info("Sending text message from user {} in live stream {}", senderId, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User sender = getUserById(senderId);
            
            // Validate content
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("Message content cannot be empty");
            }
            
            // Check if chat is enabled for this session
            if (!liveStream.getChatEnabled()) {
                throw new IllegalStateException("Chat is disabled for this session");
            }
            
            ChatMessage message = ChatMessage.builder()
                    .liveStream(liveStream)
                    .sender(sender)
                    .messageType(ChatMessage.MessageType.TEXT)
                    .content(content.trim())
                    .isPrivate(isPrivate)
                    .recipientId(recipientId)
                    .replyToMessageId(replyToMessageId)
                    .messageStatus(ChatMessage.MessageStatus.SENT)
                    .build();
            
            ChatMessage savedMessage = chatMessageRepository.save(message);
            
            // Broadcast message real-time
            if (isPrivate && recipientId != null) {
                sendPrivateMessageNotification(liveStreamId, senderId, recipientId, savedMessage);
            } else {
                broadcastMessage(liveStreamId, savedMessage);
            }
            
            logger.info("Text message sent successfully: {}", savedMessage.getId());
            return savedMessage;
            
        } catch (Exception e) {
            logger.error("Error sending text message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message", e);
        }
    }
    
    @Override
    public ChatMessage sendEmojiMessage(Long liveStreamId, Long senderId, String emoji) {
        try {
            logger.info("Sending emoji message from user {} in live stream {}", senderId, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User sender = getUserById(senderId);
            
            ChatMessage message = ChatMessage.builder()
                    .liveStream(liveStream)
                    .sender(sender)
                    .messageType(ChatMessage.MessageType.EMOJI)
                    .content(emoji)
                    .messageStatus(ChatMessage.MessageStatus.SENT)
                    .build();
            
            ChatMessage savedMessage = chatMessageRepository.save(message);
            broadcastMessage(liveStreamId, savedMessage);
            
            logger.info("Emoji message sent successfully: {}", savedMessage.getId());
            return savedMessage;
            
        } catch (Exception e) {
            logger.error("Error sending emoji message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send emoji", e);
        }
    }
    
    @Override
    public ChatMessage sendFileMessage(Long liveStreamId, Long senderId, String fileUrl, 
                                     String fileName, Long fileSize) {
        try {
            logger.info("Sending file message from user {} in live stream {}", senderId, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User sender = getUserById(senderId);
            
            ChatMessage message = ChatMessage.builder()
                    .liveStream(liveStream)
                    .sender(sender)
                    .messageType(ChatMessage.MessageType.FILE)
                    .content("Đã chia sẻ file: " + fileName)
                    .fileUrl(fileUrl)
                    .fileName(fileName)
                    .fileSize(fileSize)
                    .messageStatus(ChatMessage.MessageStatus.SENT)
                    .build();
            
            ChatMessage savedMessage = chatMessageRepository.save(message);
            broadcastMessage(liveStreamId, savedMessage);
            
            logger.info("File message sent successfully: {}", savedMessage.getId());
            return savedMessage;
            
        } catch (Exception e) {
            logger.error("Error sending file message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send file", e);
        }
    }
    
    @Override
    public ChatMessage raiseHand(Long liveStreamId, Long studentId) {
        try {
            logger.info("Student {} raising hand in live stream {}", studentId, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User student = getUserById(studentId);
            
            // Check if student already has pending raise hand
            List<ChatMessage> pendingRaiseHands = chatMessageRepository.findPendingRaiseHandMessages(liveStream);
            boolean alreadyRaised = pendingRaiseHands.stream()
                    .anyMatch(msg -> msg.getSender().getId().equals(studentId));
            
            if (alreadyRaised) {
                throw new IllegalStateException("You already have a pending raise hand request");
            }
            
            ChatMessage raiseHandMessage = ChatMessage.createRaiseHandMessage(liveStream, student);
            ChatMessage savedMessage = chatMessageRepository.save(raiseHandMessage);
            
            // Notify teacher about raise hand
            broadcastMessage(liveStreamId, savedMessage);
            
            logger.info("Raise hand message created: {}", savedMessage.getId());
            return savedMessage;
            
        } catch (Exception e) {
            logger.error("Error creating raise hand message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to raise hand", e);
        }
    }
    
    @Override
    public ChatMessage handleRaiseHandResponse(Long messageId, Long teacherId, String response) {
        try {
            logger.info("Teacher {} handling raise hand {} with response: {}", teacherId, messageId, response);
            
            Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
            if (!messageOpt.isPresent()) {
                throw new ResourceNotFoundException("Raise hand message not found: " + messageId);
            }
            
            ChatMessage raiseHandMessage = messageOpt.get();
            
            // Validate that this is a raise hand message
            if (raiseHandMessage.getMessageType() != ChatMessage.MessageType.RAISE_HAND) {
                throw new IllegalArgumentException("Message is not a raise hand request");
            }
            
            // Update message status
            ChatMessage.MessageStatus newStatus = "approve".equalsIgnoreCase(response) ? 
                    ChatMessage.MessageStatus.DELIVERED : ChatMessage.MessageStatus.READ;
            raiseHandMessage.setMessageStatus(newStatus);
            raiseHandMessage.setUpdatedAt(LocalDateTime.now());
            
            ChatMessage updatedMessage = chatMessageRepository.save(raiseHandMessage);
            
            // Send system notification about response
            String notificationContent = "approve".equalsIgnoreCase(response) ?
                    raiseHandMessage.getSender().getFullName() + " được phép phát biểu" :
                    raiseHandMessage.getSender().getFullName() + " không được phép phát biểu lúc này";
            
            sendSystemNotification(raiseHandMessage.getLiveStream().getId(), notificationContent);
            
            logger.info("Raise hand response handled successfully");
            return updatedMessage;
            
        } catch (Exception e) {
            logger.error("Error handling raise hand response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to handle raise hand response", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessages(Long liveStreamId) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            return chatMessageRepository.findByLiveStreamOrderByCreatedAtAsc(liveStream);
        } catch (Exception e) {
            logger.error("Error getting chat messages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get chat messages", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessages(Long liveStreamId, Pageable pageable) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            return chatMessageRepository.findByLiveStreamOrderByCreatedAtDesc(liveStream, pageable);
        } catch (Exception e) {
            logger.error("Error getting chat messages with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get chat messages", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesSince(Long liveStreamId, LocalDateTime since) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            return chatMessageRepository.findByLiveStreamAndCreatedAtAfterOrderByCreatedAtAsc(liveStream, since);
        } catch (Exception e) {
            logger.error("Error getting messages since: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get recent messages", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getPrivateMessages(Long liveStreamId, Long user1Id, Long user2Id) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User user1 = getUserById(user1Id);
            User user2 = getUserById(user2Id);
            
            return chatMessageRepository.findPrivateMessagesBetweenUsers(liveStream, user1, user1Id, user2, user2Id);
        } catch (Exception e) {
            logger.error("Error getting private messages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get private messages", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> searchMessages(Long liveStreamId, String keyword, Pageable pageable) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            return chatMessageRepository.searchMessages(liveStream, keyword, pageable);
        } catch (Exception e) {
            logger.error("Error searching messages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search messages", e);
        }
    }
    
    @Override
    public boolean pinMessage(Long messageId, Long teacherId, boolean pinned) {
        try {
            logger.info("Teacher {} {} message {}", teacherId, pinned ? "pinning" : "unpinning", messageId);
            
            // Validate teacher role (should be done in controller with @PreAuthorize)
            User teacher = getUserById(teacherId);
            
            int updatedRows = chatMessageRepository.pinMessage(messageId, pinned);
            
            if (updatedRows > 0) {
                // Broadcast pin/unpin event
                Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
                if (messageOpt.isPresent()) {
                    ChatMessage message = messageOpt.get();
                    String action = pinned ? "đã ghim" : "đã bỏ ghim";
                    sendSystemNotification(message.getLiveStream().getId(), 
                            teacher.getFullName() + " " + action + " một tin nhắn");
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            logger.error("Error pinning message: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean deleteMessage(Long messageId, Long userId, String userRole) {
        try {
            logger.info("User {} deleting message {}", userId, messageId);
            
            Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
            if (!messageOpt.isPresent()) {
                return false;
            }
            
            ChatMessage message = messageOpt.get();
            
            // Check permission to delete
            if (!message.canBeDeletedBy(userId, userRole)) {
                throw new IllegalStateException("You don't have permission to delete this message");
            }
            
            int updatedRows = chatMessageRepository.softDeleteMessage(messageId);
            
            if (updatedRows > 0) {
                // Broadcast delete event
                Map<String, Object> deleteEvent = new HashMap<>();
                deleteEvent.put("type", "message_deleted");
                deleteEvent.put("messageId", messageId);
                deleteEvent.put("deletedBy", userId);
                
                messagingTemplate.convertAndSend("/topic/chat/" + message.getLiveStream().getId(), deleteEvent);
                return true;
            }
            return false;
            
        } catch (Exception e) {
            logger.error("Error deleting message: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean moderateMessage(Long messageId, Long teacherId, String reason) {
        try {
            logger.info("Teacher {} moderating message {} for reason: {}", teacherId, messageId, reason);
            
            int updatedRows = chatMessageRepository.moderateMessage(messageId, teacherId, reason);
            
            if (updatedRows > 0) {
                Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
                if (messageOpt.isPresent()) {
                    ChatMessage message = messageOpt.get();
                    sendSystemNotification(message.getLiveStream().getId(), 
                            "Một tin nhắn đã bị kiểm duyệt: " + reason);
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            logger.error("Error moderating message: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public ChatMessage addEmojiReaction(Long messageId, Long userId, String emoji) {
        try {
            logger.info("User {} adding emoji reaction {} to message {}", userId, emoji, messageId);
            
            Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
            if (!messageOpt.isPresent()) {
                throw new ResourceNotFoundException("Message not found: " + messageId);
            }
            
            ChatMessage message = messageOpt.get();
            message.addEmojiReaction(emoji, userId);
            
            ChatMessage updatedMessage = chatMessageRepository.save(message);
            
            // Broadcast reaction update
            Map<String, Object> reactionEvent = new HashMap<>();
            reactionEvent.put("type", "emoji_reaction");
            reactionEvent.put("messageId", messageId);
            reactionEvent.put("emoji", emoji);
            reactionEvent.put("userId", userId);
            
            messagingTemplate.convertAndSend("/topic/chat/" + message.getLiveStream().getId(), reactionEvent);
            
            return updatedMessage;
            
        } catch (Exception e) {
            logger.error("Error adding emoji reaction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add reaction", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getPinnedMessages(Long liveStreamId) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            return chatMessageRepository.findByLiveStreamAndIsPinnedTrueOrderByCreatedAtAsc(liveStream);
        } catch (Exception e) {
            logger.error("Error getting pinned messages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get pinned messages", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getPendingRaiseHands(Long liveStreamId) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            return chatMessageRepository.findPendingRaiseHandMessages(liveStream);
        } catch (Exception e) {
            logger.error("Error getting pending raise hands: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get raise hand requests", e);
        }
    }
    
    @Override
    public int markMessagesAsRead(Long liveStreamId, Long userId, LocalDateTime timestamp) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User user = getUserById(userId);
            
            return chatMessageRepository.updateMessageStatus(liveStream, user, timestamp, 
                    ChatMessage.MessageStatus.READ);
        } catch (Exception e) {
            logger.error("Error marking messages as read: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countUnreadMessages(Long liveStreamId, Long userId, LocalDateTime lastRead) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            User user = getUserById(userId);
            
            return chatMessageRepository.countUnreadMessages(liveStream, lastRead, user, userId);
        } catch (Exception e) {
            logger.error("Error counting unread messages: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getChatStatistics(Long liveStreamId) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            
            Map<String, Object> stats = new HashMap<>();
            
            // Get participant statistics
            List<Object[]> participantStats = chatMessageRepository.getChatStatistics(liveStream);
            stats.put("participantStats", participantStats);
            
            // Count by message type
            long textCount = chatMessageRepository.countByLiveStreamAndMessageType(liveStream, ChatMessage.MessageType.TEXT);
            long emojiCount = chatMessageRepository.countByLiveStreamAndMessageType(liveStream, ChatMessage.MessageType.EMOJI);
            long raiseHandCount = chatMessageRepository.countByLiveStreamAndMessageType(liveStream, ChatMessage.MessageType.RAISE_HAND);
            
            stats.put("messageTypeCounts", Map.of(
                    "text", textCount,
                    "emoji", emojiCount,
                    "raiseHand", raiseHandCount
            ));
            
            // Count pinned messages
            List<ChatMessage> pinnedMessages = getPinnedMessages(liveStreamId);
            stats.put("pinnedCount", pinnedMessages.size());
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting chat statistics: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    @Override
    public ChatMessage sendSystemNotification(Long liveStreamId, String content) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            
            ChatMessage systemMessage = ChatMessage.createSystemMessage(liveStream, content);
            ChatMessage savedMessage = chatMessageRepository.save(systemMessage);
            
            broadcastMessage(liveStreamId, savedMessage);
            
            return savedMessage;
            
        } catch (Exception e) {
            logger.error("Error sending system notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send system notification", e);
        }
    }
    
    @Override
    public ChatMessage sendUserJoinNotification(Long liveStreamId, Long userId) {
        try {
            User user = getUserById(userId);
            String content = user.getFullName() + " đã tham gia phiên học";
            return sendSystemNotification(liveStreamId, content);
        } catch (Exception e) {
            logger.error("Error sending user join notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send join notification", e);
        }
    }
    
    @Override
    public ChatMessage sendUserLeaveNotification(Long liveStreamId, Long userId) {
        try {
            User user = getUserById(userId);
            String content = user.getFullName() + " đã rời khỏi phiên học";
            return sendSystemNotification(liveStreamId, content);
        } catch (Exception e) {
            logger.error("Error sending user leave notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send leave notification", e);
        }
    }
    
    @Override
    public ChatMessage toggleChatEnabled(Long liveStreamId, Long teacherId, boolean enabled) {
        try {
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            liveStream.setChatEnabled(enabled);
            liveStreamRepository.save(liveStream);
            
            String content = enabled ? "Chat đã được bật" : "Chat đã được tắt";
            return sendSystemNotification(liveStreamId, content);
        } catch (Exception e) {
            logger.error("Error toggling chat enabled: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to toggle chat", e);
        }
    }
    
    @Override
    public int clearAllMessages(Long liveStreamId, Long teacherId) {
        try {
            logger.info("Teacher {} clearing all messages in live stream {}", teacherId, liveStreamId);
            
            LiveStream liveStream = getLiveStreamById(liveStreamId);
            List<ChatMessage> messages = chatMessageRepository.findByLiveStreamOrderByCreatedAtAsc(liveStream);
            
            int clearedCount = 0;
            for (ChatMessage message : messages) {
                if (message.getMessageStatus() != ChatMessage.MessageStatus.DELETED) {
                    chatMessageRepository.softDeleteMessage(message.getId());
                    clearedCount++;
                }
            }
            
            if (clearedCount > 0) {
                sendSystemNotification(liveStreamId, "Tất cả tin nhắn đã được xóa bởi giáo viên");
            }
            
            return clearedCount;
            
        } catch (Exception e) {
            logger.error("Error clearing all messages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to clear messages", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> exportChatHistory(Long liveStreamId, String format) {
        try {
            List<ChatMessage> messages = getChatMessages(liveStreamId);
            
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("liveStreamId", liveStreamId);
            exportData.put("exportFormat", format);
            exportData.put("exportTime", LocalDateTime.now());
            exportData.put("totalMessages", messages.size());
            
            if ("JSON".equalsIgnoreCase(format)) {
                exportData.put("messages", messages);
            } else if ("CSV".equalsIgnoreCase(format)) {
                // Convert to CSV format
                StringBuilder csv = new StringBuilder();
                csv.append("Timestamp,Sender,Type,Content\n");
                for (ChatMessage msg : messages) {
                    csv.append(msg.getCreatedAt()).append(",")
                       .append(msg.getSender() != null ? msg.getSender().getFullName() : "System").append(",")
                       .append(msg.getMessageType()).append(",")
                       .append("\"").append(msg.getContent().replace("\"", "\"\"")).append("\"")
                       .append("\n");
                }
                exportData.put("csvData", csv.toString());
            }
            
            return exportData;
            
        } catch (Exception e) {
            logger.error("Error exporting chat history: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to export chat history", e);
        }
    }
    
    @Override
    public int cleanupOldMessages(LocalDateTime olderThan) {
        try {
            // This would cleanup messages from all live streams
            // In practice, you might want to cleanup by specific live stream
            List<LiveStream> liveStreams = liveStreamRepository.findAll();
            
            int totalCleaned = 0;
            for (LiveStream liveStream : liveStreams) {
                int cleaned = chatMessageRepository.cleanupOldMessages(liveStream, olderThan);
                totalCleaned += cleaned;
            }
            
            logger.info("Cleaned up {} old chat messages", totalCleaned);
            return totalCleaned;
            
        } catch (Exception e) {
            logger.error("Error cleaning up old messages: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public void broadcastMessage(Long liveStreamId, ChatMessage message) {
        try {
            // Send to WebSocket topic for live stream
            messagingTemplate.convertAndSend("/topic/chat/" + liveStreamId, message.toJson());
            
            // Update message status to delivered
            message.setMessageStatus(ChatMessage.MessageStatus.DELIVERED);
            message.setUpdatedAt(LocalDateTime.now());
            chatMessageRepository.save(message);
            
        } catch (Exception e) {
            logger.error("Error broadcasting message: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void sendPrivateMessageNotification(Long liveStreamId, Long senderId, Long recipientId, ChatMessage message) {
        try {
            // Send to specific user's private channel
            messagingTemplate.convertAndSendToUser(
                    recipientId.toString(), 
                    "/queue/private-chat/" + liveStreamId, 
                    message.toJson()
            );
            
            // Also send to sender for confirmation
            messagingTemplate.convertAndSendToUser(
                    senderId.toString(), 
                    "/queue/private-chat/" + liveStreamId, 
                    message.toJson()
            );
            
        } catch (Exception e) {
            logger.error("Error sending private message notification: {}", e.getMessage(), e);
        }
    }
    
    // Helper methods
    private LiveStream getLiveStreamById(Long liveStreamId) {
        return liveStreamRepository.findById(liveStreamId)
                .orElseThrow(() -> new ResourceNotFoundException("Live stream not found: " + liveStreamId));
    }
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}