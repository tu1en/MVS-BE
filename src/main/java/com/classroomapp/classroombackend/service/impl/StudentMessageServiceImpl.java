package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.StudentMessageDto;
import com.classroomapp.classroombackend.model.StudentMessage;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.StudentMessageRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.StudentMessageService;

@Service
@Transactional
public class StudentMessageServiceImpl implements StudentMessageService {
    
    @Autowired
    private StudentMessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public StudentMessageDto sendMessage(StudentMessageDto messageDto) {
        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findById(messageDto.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        
        StudentMessage message = new StudentMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setSubject(messageDto.getSubject());
        message.setContent(messageDto.getContent());
        message.setMessageType(messageDto.getMessageType() != null ? messageDto.getMessageType() : "GENERAL");
        message.setPriority(messageDto.getPriority() != null ? messageDto.getPriority() : "MEDIUM");
        message.setStatus("SENT");
        message.setIsRead(false);
        
        StudentMessage savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public StudentMessageDto getMessageById(Long messageId) {
        StudentMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return convertToDto(message);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getSentMessages(Long senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        List<StudentMessage> messages = messageRepository.findBySenderOrderByCreatedAtDesc(sender);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getReceivedMessages(Long recipientId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found with id: " + recipientId));
        List<StudentMessage> messages = messageRepository.findByRecipientOrderByCreatedAtDesc(recipient);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getConversation(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new RuntimeException("User1 not found with id: " + user1Id));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new RuntimeException("User2 not found with id: " + user2Id));

        List<StudentMessage> messages = messageRepository.findConversation(user1, user2);
        
        return messages.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getUnreadMessages(Long recipientId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found with id: " + recipientId));
        List<StudentMessage> messages = messageRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(recipient);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getMessagesByStatus(String status) {
        List<StudentMessage> messages = messageRepository.findByStatusOrderByCreatedAtDesc(status);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getMessagesByType(String messageType) {
        List<StudentMessage> messages = messageRepository.findByMessageTypeOrderByCreatedAtDesc(messageType);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getMessagesByPriority(String priority) {
        List<StudentMessage> messages = messageRepository.findByPriorityOrderByCreatedAtDesc(priority);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getUrgentMessages(Long recipientId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found with id: " + recipientId));
        List<StudentMessage> messages = messageRepository.findUrgentMessages(recipient);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> searchMessages(String keyword) {
        List<StudentMessage> messages = messageRepository.searchMessages(keyword);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getRecentMessages() {
        List<StudentMessage> messages = messageRepository.findRecentMessages();
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getPendingReplies(Long recipientId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found with id: " + recipientId));
        List<StudentMessage> messages = messageRepository.findPendingReplies(recipient);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public StudentMessageDto markAsRead(Long messageId) {
        StudentMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.markAsRead();
        StudentMessage savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }
    
    @Override
    public StudentMessageDto replyToMessage(Long messageId, String reply, Long replierId) {
        StudentMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        User replier = userRepository.findById(replierId)
                .orElseThrow(() -> new RuntimeException("Replier not found"));
        
        message.reply(reply, replier);
        StudentMessage savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }
    
    @Override
    public StudentMessageDto resolveMessage(Long messageId) {
        StudentMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.resolve();
        StudentMessage savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }
    
    @Override
    public StudentMessageDto archiveMessage(Long messageId) {
        StudentMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.archive();
        StudentMessage savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countUnreadMessages(Long recipientId) {
        try {
            // If recipientId is null, count all unread messages
            if (recipientId == null) {
                return messageRepository.countByIsReadFalse();
            }

            // Check if user exists first
            if (!userRepository.existsById(recipientId)) {
                System.out.println("User with ID " + recipientId + " not found in database");
                return 0L; // Return zero instead of throwing exception
            }
            
            User recipient = userRepository.findById(recipientId).get();
            return messageRepository.countUnreadMessagesByRecipient(recipient);
        } catch (Throwable e) {
            System.err.println("Error counting unread messages for user " + recipientId + ": " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }
    
    @Override
    public StudentMessageDto updateMessagePriority(Long messageId, String priority) {
        StudentMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setPriority(priority);
        message.setUpdatedAt(LocalDateTime.now());
        StudentMessage savedMessage = messageRepository.save(message);
        return convertToDto(savedMessage);
    }
    
    @Override
    public void deleteMessage(Long messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new RuntimeException("Message not found");
        }
        messageRepository.deleteById(messageId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentMessageDto> getAllMessages() {
        List<StudentMessage> messages = messageRepository.findAll();
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    private StudentMessageDto convertToDto(StudentMessage message) {
        if (message == null) {
            return null;
        }
        
        StudentMessageDto dto = new StudentMessageDto();
        dto.setId(message.getId());
        
        // Add null checks for sender and recipient
        if (message.getSender() != null) {
            dto.setSenderId(message.getSender().getId());
            dto.setSenderName(message.getSender().getFullName());
        } else {
            dto.setSenderId(null); // Or a placeholder ID
            dto.setSenderName("Unknown Sender");
        }
        
        if (message.getRecipient() != null) {
            dto.setRecipientId(message.getRecipient().getId());
            dto.setRecipientName(message.getRecipient().getFullName());
        } else {
            dto.setRecipientId(null); // Or a placeholder ID
            dto.setRecipientName("Unknown Recipient");
        }
        
        dto.setSubject(message.getSubject());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setPriority(message.getPriority());
        dto.setStatus(message.getStatus());
        dto.setIsRead(message.getIsRead());
        dto.setReadAt(message.getReadAt());
        dto.setReply(message.getReply());
        dto.setRepliedAt(message.getRepliedAt());
        if (message.getRepliedBy() != null) {
            dto.setRepliedById(message.getRepliedBy().getId());
            dto.setRepliedByName(message.getRepliedBy().getFullName());
        }
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        return dto;
    }
}
