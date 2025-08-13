package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.model.ParentMessage;
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.ParentMessageService;
import com.classroomapp.classroombackend.service.UserService;
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
 * Teacher Message Controller - API endpoints for teacher-parent messaging
 * Based on PARENT_ROLE_SPEC.md Phase 2 requirements
 */
@RestController
@RequestMapping("/api/teacher/messages")
@Slf4j
@PreAuthorize("hasRole('TEACHER') or hasRole('TEACHING_ASSISTANT')")
public class TeacherMessageController {

    private final ParentMessageService messageService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public TeacherMessageController(ParentMessageService messageService,
                                   UserService userService,
                                   JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Send message to parent
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long teacherId = getTeacherIdFromToken(httpRequest);

            // Validate parent has access to student
            if (!messageService.validateParentStudentAccess(request.getParentId(), request.getStudentId())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Parent does not have access to this student");
                return ResponseEntity.badRequest().body(error);
            }

            ParentMessage message = messageService.sendMessageFromTeacher(
                teacherId, request.getParentId(), request.getStudentId(),
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
     * Send reply to parent message
     */
    @PostMapping("/reply")
    public ResponseEntity<Map<String, Object>> sendReply(
            @Valid @RequestBody SendReplyRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long teacherId = getTeacherIdFromToken(httpRequest);

            // Validate parent has access to student
            if (!messageService.validateParentStudentAccess(request.getParentId(), request.getStudentId())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Parent does not have access to this student");
                return ResponseEntity.badRequest().body(error);
            }

            ParentMessage message = messageService.sendReplyFromTeacher(
                teacherId, request.getParentId(), request.getStudentId(),
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
     * Get conversations list for teacher
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ParentMessageService.ConversationSummary>> getConversations(
            HttpServletRequest request) {
        try {
            Long teacherId = getTeacherIdFromToken(request);
            List<ParentMessageService.ConversationSummary> conversations = 
                messageService.getTeacherConversations(teacherId);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error getting conversations for teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get specific conversation messages
     */
    @GetMapping("/conversations/{parentId}/students/{studentId}")
    public ResponseEntity<List<ParentMessage>> getConversation(
            @PathVariable Long parentId,
            @PathVariable Long studentId,
            HttpServletRequest httpRequest) {
        try {
            Long teacherId = getTeacherIdFromToken(httpRequest);

            // Validate parent has access to student
            if (!messageService.validateParentStudentAccess(parentId, studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<ParentMessage> messages = messageService.getConversation(parentId, teacherId, studentId);
            
            // Mark conversation as read
            messageService.markConversationAsRead(parentId, teacherId, studentId, ParentMessage.SenderType.TEACHER);
            
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
            Long teacherId = getTeacherIdFromToken(request);
            
            // Validate message belongs to teacher conversation
            Optional<ParentMessage> messageOpt = messageService.getMessageById(messageId);
            if (messageOpt.isEmpty() || !messageOpt.get().getTeacherId().equals(teacherId)) {
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
            Long teacherId = getTeacherIdFromToken(request);
            List<ParentMessage> unreadMessages = messageService.getUnreadMessagesForTeacher(teacherId);
            return ResponseEntity.ok(unreadMessages);
        } catch (Exception e) {
            log.error("Error getting unread messages for teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread messages count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        try {
            Long teacherId = getTeacherIdFromToken(request);
            Long unreadCount = messageService.countUnreadMessagesForTeacher(teacherId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", unreadCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting unread count for teacher", e);
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
            Long teacherId = getTeacherIdFromToken(request);
            List<ParentMessage> messages = messageService.searchMessages(
                query, teacherId, ParentMessage.SenderType.TEACHER);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error searching messages for teacher", e);
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
            Long teacherId = getTeacherIdFromToken(request);
            ParentMessageService.MessageStatistics stats = 
                messageService.getMessageStatisticsForTeacher(teacherId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting message statistics for teacher", e);
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
            Long teacherId = getTeacherIdFromToken(request);
            messageService.deleteMessage(messageId, teacherId, ParentMessage.SenderType.TEACHER);
            
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

    private Long getTeacherIdFromToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        String email = jwtUtil.getSubjectFromToken(token);
        
        // Get user ID from email using UserService
        // This would require a method in UserService to get user by email
        // For now, we'll use a mock implementation
        
        try {
            // Mock implementation - in real system, get from UserService
            return 1L; // Replace with actual teacher ID lookup
        } catch (Exception e) {
            throw new IllegalArgumentException("Teacher not found for token");
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
        private Long parentId;
        private Long studentId;
        private String subject;
        private String messageContent;

        // Getters and setters
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getMessageContent() { return messageContent; }
        public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    }

    public static class SendReplyRequest {
        private Long parentId;
        private Long studentId;
        private String subject;
        private String messageContent;
        private Long replyToId;

        // Getters and setters
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }

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