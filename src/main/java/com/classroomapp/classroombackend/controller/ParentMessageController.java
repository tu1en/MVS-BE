package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.model.ParentMessage;
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.ParentMessageService;
import com.classroomapp.classroombackend.service.ParentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parent Message Controller - API endpoints for parent-teacher messaging
 * Based on PARENT_ROLE_SPEC.md Phase 2 requirements
 */
@RestController
@RequestMapping("/api/parent/messages")
@Slf4j
@PreAuthorize("hasRole('PARENT')")
public class ParentMessageController {

    private final ParentMessageService messageService;
    private final ParentService parentService;
    private final JwtUtil jwtUtil;

    @Autowired
    public ParentMessageController(ParentMessageService messageService,
                                  ParentService parentService,
                                  JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.parentService = parentService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Send message to teacher
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            String token = getTokenFromRequest(httpRequest);
            
            // Validate parent has access to student
            if (!jwtUtil.validateParentChildAccess(token, request.getStudentId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ParentMessage message = messageService.sendMessageFromParent(
                parentId, request.getTeacherId(), request.getStudentId(),
                request.getSubject(), request.getMessageContent()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", message.getId());
            response.put("status", "sent");
            response.put("message", "Tin nhắn đã được gửi thành công");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid send message request: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send reply to message
     */
    @PostMapping("/reply")
    public ResponseEntity<Map<String, Object>> sendReply(
            @Valid @RequestBody SendReplyRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            String token = getTokenFromRequest(httpRequest);
            
            // Validate parent has access to student
            if (!jwtUtil.validateParentChildAccess(token, request.getStudentId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ParentMessage message = messageService.sendReplyFromParent(
                parentId, request.getTeacherId(), request.getStudentId(),
                request.getSubject(), request.getMessageContent(), request.getReplyToId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", message.getId());
            response.put("status", "sent");
            response.put("message", "Phản hồi đã được gửi thành công");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid send reply request: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error sending reply", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get conversations list for parent
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ParentMessageService.ConversationSummary>> getConversations(
            HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            List<ParentMessageService.ConversationSummary> conversations = 
                messageService.getParentConversations(parentId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error getting conversations for parent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get specific conversation messages
     */
    @GetMapping("/conversations/{teacherId}/students/{studentId}")
    public ResponseEntity<List<ParentMessage>> getConversation(
            @PathVariable Long teacherId,
            @PathVariable Long studentId,
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            String token = getTokenFromRequest(httpRequest);
            
            // Validate parent has access to student
            if (!jwtUtil.validateParentChildAccess(token, studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<ParentMessage> messages = messageService.getConversation(parentId, teacherId, studentId);
            
            // Mark conversation as read
            messageService.markConversationAsRead(parentId, teacherId, studentId, ParentMessage.SenderType.PARENT);
            
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error getting conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark message as read
     */
    @PostMapping("/{messageId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long messageId,
            HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            
            // Validate message belongs to parent
            Optional<ParentMessage> messageOpt = messageService.getMessageById(messageId);
            if (messageOpt.isEmpty() || !messageOpt.get().getParentId().equals(parentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ParentMessage message = messageService.markMessageAsRead(messageId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", message.getId());
            response.put("isRead", message.isRead());
            response.put("readAt", message.getReadAt());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid mark as read request: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error marking message as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread messages
     */
    @GetMapping("/unread")
    public ResponseEntity<List<ParentMessage>> getUnreadMessages(HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            List<ParentMessage> unreadMessages = messageService.getUnreadMessagesForParent(parentId);
            return ResponseEntity.ok(unreadMessages);
        } catch (Exception e) {
            log.error("Error getting unread messages for parent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread messages count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            Long unreadCount = messageService.countUnreadMessagesForParent(parentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", unreadCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting unread count for parent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search messages
     */
    @GetMapping("/search")
    public ResponseEntity<List<ParentMessage>> searchMessages(
            @RequestParam String query,
            HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            List<ParentMessage> messages = messageService.searchMessages(
                query, parentId, ParentMessage.SenderType.PARENT);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error searching messages for parent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get message statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ParentMessageService.MessageStatistics> getMessageStats(
            HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            ParentMessageService.MessageStatistics stats = 
                messageService.getMessageStatisticsForParent(parentId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting message statistics for parent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete message
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable Long messageId,
            HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            messageService.deleteMessage(messageId, parentId, ParentMessage.SenderType.PARENT);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tin nhắn đã được xóa");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid delete message request: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error deleting message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods

    private Long getParentIdFromToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        String email = jwtUtil.getSubjectFromToken(token);
        
        Optional<com.classroomapp.classroombackend.model.Parent> parent = parentService.getParentByEmail(email);
        if (parent.isPresent()) {
            return parent.get().getId();
        } else {
            throw new IllegalArgumentException("Parent not found for token");
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("No valid token found");
    }

    // DTO Classes

    public static class SendMessageRequest {
        private Long teacherId;
        private Long studentId;
        private String subject;
        private String messageContent;

        // Getters and setters
        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getMessageContent() { return messageContent; }
        public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    }

    public static class SendReplyRequest {
        private Long teacherId;
        private Long studentId;
        private String subject;
        private String messageContent;
        private Long replyToId;

        // Getters and setters
        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getMessageContent() { return messageContent; }
        public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

        public Long getReplyToId() { return replyToId; }
        public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }
    }
}