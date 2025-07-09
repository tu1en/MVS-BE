package com.classroomapp.classroombackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.StudentMessageDto;
import com.classroomapp.classroombackend.service.StudentMessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentMessageController {
    
    @Autowired
    private StudentMessageService messageService;
    
    // Send a new message
    @PostMapping("/messages")
    public ResponseEntity<StudentMessageDto> sendMessage(@Valid @RequestBody StudentMessageDto messageDto) {
        try {
            StudentMessageDto sentMessage = messageService.sendMessage(messageDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(sentMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Get message by ID
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<StudentMessageDto> getMessageById(@PathVariable Long messageId) {
        try {
            StudentMessageDto message = messageService.getMessageById(messageId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get messages sent by a user
    @GetMapping("/messages/sent/{senderId}")
    public ResponseEntity<List<StudentMessageDto>> getSentMessages(@PathVariable Long senderId) {
        try {
            List<StudentMessageDto> messages = messageService.getSentMessages(senderId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get messages received by a user
    @GetMapping("/messages/received/{recipientId}")
    public ResponseEntity<List<StudentMessageDto>> getReceivedMessages(@PathVariable Long recipientId) {
        try {
            System.out.println("Getting messages for recipient ID: " + recipientId);
            List<StudentMessageDto> messages = messageService.getReceivedMessages(recipientId);
            System.out.println("Found " + messages.size() + " messages for recipient");
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Error getting messages for recipient ID: " + recipientId);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // Get conversation between two users
    @GetMapping("/messages/conversation/{user1Id}/{user2Id}")
    public ResponseEntity<List<StudentMessageDto>> getConversation(
            @PathVariable Long user1Id, 
            @PathVariable Long user2Id) {
        try {
            System.out.println("Getting conversation between user " + user1Id + " and user " + user2Id);
            List<StudentMessageDto> conversation = messageService.getConversation(user1Id, user2Id);
            System.out.println("Found " + conversation.size() + " messages in conversation");
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            System.err.println("Error getting conversation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // Get unread messages for a recipient
    @GetMapping("/messages/unread/{recipientId}")
    public ResponseEntity<List<StudentMessageDto>> getUnreadMessages(@PathVariable Long recipientId) {
        try {
            List<StudentMessageDto> messages = messageService.getUnreadMessages(recipientId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // Get messages by status
    @GetMapping("/messages/status/{status}")
    public ResponseEntity<List<StudentMessageDto>> getMessagesByStatus(@PathVariable String status) {
        List<StudentMessageDto> messages = messageService.getMessagesByStatus(status);
        return ResponseEntity.ok(messages);
    }
    
    // Get messages by type
    @GetMapping("/messages/type/{messageType}")
    public ResponseEntity<List<StudentMessageDto>> getMessagesByType(@PathVariable String messageType) {
        List<StudentMessageDto> messages = messageService.getMessagesByType(messageType);
        return ResponseEntity.ok(messages);
    }
    
    // Get messages by priority
    @GetMapping("/messages/priority/{priority}")
    public ResponseEntity<List<StudentMessageDto>> getMessagesByPriority(@PathVariable String priority) {
        List<StudentMessageDto> messages = messageService.getMessagesByPriority(priority);
        return ResponseEntity.ok(messages);
    }
    
    // Get urgent messages for a recipient
    @GetMapping("/messages/urgent/{recipientId}")
    public ResponseEntity<List<StudentMessageDto>> getUrgentMessages(@PathVariable Long recipientId) {
        try {
            List<StudentMessageDto> messages = messageService.getUrgentMessages(recipientId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // Search messages
    @GetMapping("/messages/search")
    public ResponseEntity<List<StudentMessageDto>> searchMessages(@RequestParam String keyword) {
        List<StudentMessageDto> messages = messageService.searchMessages(keyword);
        return ResponseEntity.ok(messages);
    }
    
    // Get recent messages
    @GetMapping("/messages/recent")
    public ResponseEntity<List<StudentMessageDto>> getRecentMessages() {
        List<StudentMessageDto> messages = messageService.getRecentMessages();
        return ResponseEntity.ok(messages);
    }
    
    // Get pending replies for a recipient
    @GetMapping("/messages/pending-replies/{recipientId}")
    public ResponseEntity<List<StudentMessageDto>> getPendingReplies(@PathVariable Long recipientId) {
        try {
            List<StudentMessageDto> messages = messageService.getPendingReplies(recipientId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // Mark message as read
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<StudentMessageDto> markAsRead(@PathVariable Long messageId) {
        try {
            StudentMessageDto message = messageService.markAsRead(messageId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Reply to a message
    @PostMapping("/messages/{messageId}/reply")
    public ResponseEntity<StudentMessageDto> replyToMessage(
            @PathVariable Long messageId,
            @RequestParam String reply,
            @RequestParam Long replierId) {
        try {
            StudentMessageDto message = messageService.replyToMessage(messageId, reply, replierId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Resolve a message
    @PutMapping("/messages/{messageId}/resolve")
    public ResponseEntity<StudentMessageDto> resolveMessage(@PathVariable Long messageId) {
        try {
            StudentMessageDto message = messageService.resolveMessage(messageId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Archive a message
    @PutMapping("/messages/{messageId}/archive")
    public ResponseEntity<StudentMessageDto> archiveMessage(@PathVariable Long messageId) {
        try {
            StudentMessageDto message = messageService.archiveMessage(messageId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Count unread messages for a recipient
    @GetMapping("/messages/unread-count/{recipientId}")
    public ResponseEntity<Long> countUnreadMessages(@PathVariable Long recipientId) {
        try {
            Long count = messageService.countUnreadMessages(recipientId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0L);
        }
    }
    
    // Update message priority
    @PutMapping("/messages/{messageId}/priority")
    public ResponseEntity<StudentMessageDto> updateMessagePriority(
            @PathVariable Long messageId,
            @RequestParam String priority) {
        try {
            StudentMessageDto message = messageService.updateMessagePriority(messageId, priority);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Delete a message
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        try {
            messageService.deleteMessage(messageId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Get all messages (admin only)
    @GetMapping("/messages/all")
    public ResponseEntity<List<StudentMessageDto>> getAllMessages() {
        List<StudentMessageDto> messages = messageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }
    
    // New endpoint for student dashboard to get unread count in standard response format
    @GetMapping("/messages/dashboard/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadMessageCount() {
        try {
            long unreadCount = messageService.countUnreadMessages(null);
            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve unread message count");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
