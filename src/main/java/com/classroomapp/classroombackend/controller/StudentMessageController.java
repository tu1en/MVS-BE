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
}
