package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.StudentMessageDto;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.StudentMessageService;

/**
 * Controller for managing student messages
 */
@RestController
@RequestMapping("/api/student-messages")
@CrossOrigin(origins = "*")
public class StudentMessageController {

    @Autowired
    private StudentMessageService messageService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get messages sent by the specified user ID
     * @param senderId
     * @return List of sent messages
     */
    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<StudentMessageDto>> getSentMessages(@PathVariable Long senderId) {
        try {
            System.out.println("=== GET SENT MESSAGES DEBUG ===");
            System.out.println("Sender ID: " + senderId);
            
            List<StudentMessageDto> messages = messageService.getSentMessages(senderId);
            
            System.out.println("Found " + messages.size() + " sent messages");
            System.out.println("=== END GET SENT MESSAGES DEBUG ===");
            
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Error getting sent messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }
    
    /**
     * Get messages sent by the current authenticated user
     * @param authentication
     * @return List of sent messages
     */
    @GetMapping("/sent/current")
    public ResponseEntity<List<StudentMessageDto>> getCurrentUserSentMessages(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new RuntimeException("User not found: " + username)));
            
            List<StudentMessageDto> messages = messageService.getSentMessages(currentUser.getId());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Error getting current user sent messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }
    
    /**
     * Get messages by sender ID (alternative endpoint for compatibility)
     * @param senderId
     * @return List of messages
     */
    @GetMapping("/by-sender/{senderId}")
    public ResponseEntity<List<StudentMessageDto>> getMessagesBySender(@PathVariable Long senderId) {
        return getSentMessages(senderId);
    }

    /**
     * Get conversations for a teacher
     * Frontend calls: /api/student-messages/teacher/{teacherId}/conversations
     * @param teacherId
     * @return List of conversations (students who have messaged this teacher)
     */
    @GetMapping("/teacher/{teacherId}/conversations")
    public ResponseEntity<List<Object>> getTeacherConversations(@PathVariable Long teacherId) {
        try {
            System.out.println("=== GET TEACHER CONVERSATIONS DEBUG ===");
            System.out.println("Teacher ID: " + teacherId);

            // Get all messages where teacher is sender or recipient
            List<StudentMessageDto> sentMessages = messageService.getSentMessages(teacherId);
            List<StudentMessageDto> receivedMessages = messageService.getReceivedMessages(teacherId);

            // Create conversation objects grouped by student
            java.util.Map<Long, Object> conversationMap = new java.util.HashMap<>();

            // Process sent messages (teacher -> student)
            for (StudentMessageDto msg : sentMessages) {
                Long studentId = msg.getRecipientId();
                if (!conversationMap.containsKey(studentId)) {
                    java.util.Map<String, Object> conversation = new java.util.HashMap<>();
                    conversation.put("id", studentId);
                    conversation.put("studentId", studentId);
                    conversation.put("studentName", msg.getRecipientName());
                    conversation.put("lastMessage", msg.getContent());
                    conversation.put("lastMessageAt", msg.getCreatedAt());
                    conversation.put("unreadCount", 0);
                    conversationMap.put(studentId, conversation);
                }
            }

            // Process received messages (student -> teacher)
            for (StudentMessageDto msg : receivedMessages) {
                Long studentId = msg.getSenderId();
                if (!conversationMap.containsKey(studentId)) {
                    java.util.Map<String, Object> conversation = new java.util.HashMap<>();
                    conversation.put("id", studentId);
                    conversation.put("studentId", studentId);
                    conversation.put("studentName", msg.getSenderName());
                    conversation.put("lastMessage", msg.getContent());
                    conversation.put("lastMessageAt", msg.getCreatedAt());
                    conversation.put("unreadCount", msg.getIsRead() ? 0 : 1);
                    conversationMap.put(studentId, conversation);
                } else {
                    // Update if this message is more recent
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> existing = (java.util.Map<String, Object>) conversationMap.get(studentId);
                    if (msg.getCreatedAt().isAfter((java.time.LocalDateTime) existing.get("lastMessageAt"))) {
                        existing.put("lastMessage", msg.getContent());
                        existing.put("lastMessageAt", msg.getCreatedAt());
                    }
                    if (!msg.getIsRead()) {
                        existing.put("unreadCount", (Integer) existing.get("unreadCount") + 1);
                    }
                }
            }

            java.util.List<Object> conversations = new java.util.ArrayList<>(conversationMap.values());

            System.out.println("Found " + conversations.size() + " conversations for teacher " + teacherId);
            System.out.println("=== END GET TEACHER CONVERSATIONS DEBUG ===");

            return ResponseEntity.ok(conversations);

        } catch (Exception e) {
            System.err.println("Error getting teacher conversations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    /**
     * Get all messages for a teacher (both sent and received)
     * Frontend calls: /api/student-messages/teacher/{teacherId}
     * @param teacherId
     * @return List of all messages
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<StudentMessageDto>> getTeacherMessages(@PathVariable Long teacherId) {
        try {
            System.out.println("=== GET TEACHER MESSAGES DEBUG ===");
            System.out.println("Teacher ID: " + teacherId);

            List<StudentMessageDto> sentMessages = messageService.getSentMessages(teacherId);
            List<StudentMessageDto> receivedMessages = messageService.getReceivedMessages(teacherId);

            // Combine and sort by creation date
            java.util.List<StudentMessageDto> allMessages = new java.util.ArrayList<>();
            allMessages.addAll(sentMessages);
            allMessages.addAll(receivedMessages);

            // Sort by creation date (newest first)
            allMessages.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            System.out.println("Found " + allMessages.size() + " total messages for teacher " + teacherId);
            System.out.println("=== END GET TEACHER MESSAGES DEBUG ===");

            return ResponseEntity.ok(allMessages);

        } catch (Exception e) {
            System.err.println("Error getting teacher messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }
}
