package com.classroomapp.classroombackend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.ChatMessage;
import com.classroomapp.classroombackend.service.ChatService;

/**
 * Controller để quản lý chat trong live sessions
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    /**
     * Gửi tin nhắn text
     */
    @PostMapping("/send-text")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<ChatMessage> sendTextMessage(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long senderId = Long.valueOf(request.get("senderId").toString());
            String content = request.get("content").toString();
            boolean isPrivate = Boolean.parseBoolean(request.getOrDefault("isPrivate", "false").toString());
            Long recipientId = request.containsKey("recipientId") && request.get("recipientId") != null ?
                    Long.valueOf(request.get("recipientId").toString()) : null;
            Long replyToMessageId = request.containsKey("replyToMessageId") && request.get("replyToMessageId") != null ?
                    Long.valueOf(request.get("replyToMessageId").toString()) : null;
            
            ChatMessage message = chatService.sendTextMessage(liveStreamId, senderId, content, 
                    isPrivate, recipientId, replyToMessageId);
            
            return ResponseEntity.ok(message);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gửi emoji
     */
    @PostMapping("/send-emoji")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<ChatMessage> sendEmojiMessage(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long senderId = Long.valueOf(request.get("senderId").toString());
            String emoji = request.get("emoji").toString();
            
            ChatMessage message = chatService.sendEmojiMessage(liveStreamId, senderId, emoji);
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gửi file
     */
    @PostMapping("/send-file")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<ChatMessage> sendFileMessage(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long senderId = Long.valueOf(request.get("senderId").toString());
            String fileUrl = request.get("fileUrl").toString();
            String fileName = request.get("fileName").toString();
            Long fileSize = Long.valueOf(request.get("fileSize").toString());
            
            ChatMessage message = chatService.sendFileMessage(liveStreamId, senderId, fileUrl, fileName, fileSize);
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Raise hand
     */
    @PostMapping("/raise-hand")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ChatMessage> raiseHand(@RequestBody Map<String, Object> request) {
        try {
            Long liveStreamId = Long.valueOf(request.get("liveStreamId").toString());
            Long studentId = Long.valueOf(request.get("studentId").toString());
            
            ChatMessage message = chatService.raiseHand(liveStreamId, studentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Teacher response to raise hand
     */
    @PostMapping("/handle-raise-hand")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ChatMessage> handleRaiseHandResponse(@RequestBody Map<String, Object> request) {
        try {
            Long messageId = Long.valueOf(request.get("messageId").toString());
            Long teacherId = Long.valueOf(request.get("teacherId").toString());
            String response = request.get("response").toString(); // "approve" or "deny"
            
            ChatMessage message = chatService.handleRaiseHandResponse(messageId, teacherId, response);
            return ResponseEntity.ok(message);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy tất cả messages trong live stream
     */
    @GetMapping("/{liveStreamId}/messages")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<ChatMessage>> getChatMessages(
            @PathVariable Long liveStreamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            if (page > 0 || size != 50) {
                // Use pagination
                Pageable pageable = PageRequest.of(page, size);
                List<ChatMessage> messages = chatService.getChatMessages(liveStreamId, pageable);
                return ResponseEntity.ok(messages);
            } else {
                // Get all messages
                List<ChatMessage> messages = chatService.getChatMessages(liveStreamId);
                return ResponseEntity.ok(messages);
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy messages từ thời điểm cụ thể (cho real-time sync)
     */
    @GetMapping("/{liveStreamId}/messages/since")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<ChatMessage>> getMessagesSince(
            @PathVariable Long liveStreamId,
            @RequestParam String since) {
        try {
            LocalDateTime sinceTime = LocalDateTime.parse(since);
            List<ChatMessage> messages = chatService.getMessagesSince(liveStreamId, sinceTime);
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Lấy private messages giữa 2 users
     */
    @GetMapping("/{liveStreamId}/private-messages")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<ChatMessage>> getPrivateMessages(
            @PathVariable Long liveStreamId,
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        try {
            List<ChatMessage> messages = chatService.getPrivateMessages(liveStreamId, user1Id, user2Id);
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Search messages
     */
    @GetMapping("/{liveStreamId}/search")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<ChatMessage>> searchMessages(
            @PathVariable Long liveStreamId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<ChatMessage> messages = chatService.searchMessages(liveStreamId, keyword, pageable);
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Pin/unpin message
     */
    @PutMapping("/pin/{messageId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> pinMessage(
            @PathVariable Long messageId,
            @RequestParam Long teacherId,
            @RequestParam boolean pinned) {
        try {
            boolean success = chatService.pinMessage(messageId, teacherId, pinned);
            
            Map<String, Object> response = Map.of(
                    "success", success,
                    "messageId", messageId,
                    "pinned", pinned
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete message
     */
    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam String userRole) {
        try {
            boolean success = chatService.deleteMessage(messageId, userId, userRole);
            
            Map<String, Object> response = Map.of(
                    "success", success,
                    "messageId", messageId
            );
            
            return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Moderate message
     */
    @PostMapping("/moderate/{messageId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> moderateMessage(
            @PathVariable Long messageId,
            @RequestParam Long teacherId,
            @RequestParam String reason) {
        try {
            boolean success = chatService.moderateMessage(messageId, teacherId, reason);
            
            Map<String, Object> response = Map.of(
                    "success", success,
                    "messageId", messageId,
                    "reason", reason
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Add emoji reaction
     */
    @PostMapping("/react/{messageId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<ChatMessage> addEmojiReaction(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam String emoji) {
        try {
            ChatMessage message = chatService.addEmojiReaction(messageId, userId, emoji);
            return ResponseEntity.ok(message);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get pinned messages
     */
    @GetMapping("/{liveStreamId}/pinned")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<ChatMessage>> getPinnedMessages(@PathVariable Long liveStreamId) {
        try {
            List<ChatMessage> messages = chatService.getPinnedMessages(liveStreamId);
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get pending raise hands
     */
    @GetMapping("/{liveStreamId}/raise-hands")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ChatMessage>> getPendingRaiseHands(@PathVariable Long liveStreamId) {
        try {
            List<ChatMessage> raiseHands = chatService.getPendingRaiseHands(liveStreamId);
            return ResponseEntity.ok(raiseHands);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Mark messages as read
     */
    @PostMapping("/{liveStreamId}/mark-read")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> markMessagesAsRead(
            @PathVariable Long liveStreamId,
            @RequestParam Long userId,
            @RequestParam(required = false) String timestamp) {
        try {
            LocalDateTime readTime = timestamp != null ? LocalDateTime.parse(timestamp) : LocalDateTime.now();
            int markedCount = chatService.markMessagesAsRead(liveStreamId, userId, readTime);
            
            Map<String, Object> response = Map.of(
                    "markedCount", markedCount,
                    "timestamp", readTime
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Count unread messages
     */
    @GetMapping("/{liveStreamId}/unread-count")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> countUnreadMessages(
            @PathVariable Long liveStreamId,
            @RequestParam Long userId,
            @RequestParam String lastRead) {
        try {
            LocalDateTime lastReadTime = LocalDateTime.parse(lastRead);
            long unreadCount = chatService.countUnreadMessages(liveStreamId, userId, lastReadTime);
            
            Map<String, Object> response = Map.of(
                    "unreadCount", unreadCount,
                    "userId", userId,
                    "lastRead", lastReadTime
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get chat statistics
     */
    @GetMapping("/{liveStreamId}/statistics")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getChatStatistics(@PathVariable Long liveStreamId) {
        try {
            Map<String, Object> statistics = chatService.getChatStatistics(liveStreamId);
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Toggle chat enabled/disabled
     */
    @PostMapping("/{liveStreamId}/toggle")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ChatMessage> toggleChatEnabled(
            @PathVariable Long liveStreamId,
            @RequestParam Long teacherId,
            @RequestParam boolean enabled) {
        try {
            ChatMessage systemMessage = chatService.toggleChatEnabled(liveStreamId, teacherId, enabled);
            return ResponseEntity.ok(systemMessage);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Clear all messages
     */
    @PostMapping("/{liveStreamId}/clear")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> clearAllMessages(
            @PathVariable Long liveStreamId,
            @RequestParam Long teacherId) {
        try {
            int clearedCount = chatService.clearAllMessages(liveStreamId, teacherId);
            
            Map<String, Object> response = Map.of(
                    "clearedCount", clearedCount,
                    "message", "All messages cleared successfully"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Export chat history
     */
    @GetMapping("/{liveStreamId}/export")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> exportChatHistory(
            @PathVariable Long liveStreamId,
            @RequestParam(defaultValue = "JSON") String format) {
        try {
            Map<String, Object> exportData = chatService.exportChatHistory(liveStreamId, format);
            return ResponseEntity.ok(exportData);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // WebSocket message handlers
    
    /**
     * Handle real-time chat messages via WebSocket
     */
    @MessageMapping("/chat.send")
    @SendToUser("/queue/reply")
    public ChatMessage handleChatMessage(@Payload Map<String, Object> chatMessage) {
        try {
            Long liveStreamId = Long.valueOf(chatMessage.get("liveStreamId").toString());
            Long senderId = Long.valueOf(chatMessage.get("senderId").toString());
            String content = chatMessage.get("content").toString();
            String messageType = chatMessage.getOrDefault("messageType", "TEXT").toString();
            
            switch (messageType.toUpperCase()) {
                case "TEXT":
                    return chatService.sendTextMessage(liveStreamId, senderId, content, false, null, null);
                case "EMOJI":
                    return chatService.sendEmojiMessage(liveStreamId, senderId, content);
                default:
                    throw new IllegalArgumentException("Unsupported message type: " + messageType);
            }
            
        } catch (Exception e) {
            // Return error message
            ChatMessage errorMessage = new ChatMessage();
            errorMessage.setContent("Error: " + e.getMessage());
            errorMessage.setMessageType(ChatMessage.MessageType.SYSTEM);
            return errorMessage;
        }
    }
    
    /**
     * Handle typing indicators via WebSocket
     */
    @MessageMapping("/chat.typing")
    public void handleTypingIndicator(@Payload Map<String, Object> typingData) {
        // Broadcast typing indicator to other users in the live stream
        // This would be handled by the WebSocket signaling system
    }
    
    /**
     * Handle user join chat
     */
    @MessageMapping("/chat.join")
    public ChatMessage handleUserJoinChat(@Payload Map<String, Object> joinData) {
        try {
            Long liveStreamId = Long.valueOf(joinData.get("liveStreamId").toString());
            Long userId = Long.valueOf(joinData.get("userId").toString());
            
            return chatService.sendUserJoinNotification(liveStreamId, userId);
            
        } catch (Exception e) {
            ChatMessage errorMessage = new ChatMessage();
            errorMessage.setContent("Error joining chat: " + e.getMessage());
            errorMessage.setMessageType(ChatMessage.MessageType.SYSTEM);
            return errorMessage;
        }
    }
    
    /**
     * Handle user leave chat
     */
    @MessageMapping("/chat.leave")
    public ChatMessage handleUserLeaveChat(@Payload Map<String, Object> leaveData) {
        try {
            Long liveStreamId = Long.valueOf(leaveData.get("liveStreamId").toString());
            Long userId = Long.valueOf(leaveData.get("userId").toString());
            
            return chatService.sendUserLeaveNotification(liveStreamId, userId);
            
        } catch (Exception e) {
            ChatMessage errorMessage = new ChatMessage();
            errorMessage.setContent("Error leaving chat: " + e.getMessage());
            errorMessage.setMessageType(ChatMessage.MessageType.SYSTEM);
            return errorMessage;
        }
    }
}