package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.ParentMessage;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentMessageRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.ParentMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ParentMessageService
 * Based on PARENT_ROLE_SPEC.md Phase 2 - Parent-Teacher messaging
 */
@Service
@Slf4j
@Transactional
public class ParentMessageServiceImpl implements ParentMessageService {

    private final ParentMessageRepository messageRepository;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;
    private final UserRepository userRepository;

    @Autowired
    public ParentMessageServiceImpl(ParentMessageRepository messageRepository,
                                   ParentRepository parentRepository,
                                   StudentParentRepository studentParentRepository,
                                   UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.parentRepository = parentRepository;
        this.studentParentRepository = studentParentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ParentMessage sendMessageFromParent(Long parentId, Long teacherId, Long studentId, 
                                              String subject, String messageContent) {
        log.info("Parent {} sending message to teacher {} about student {}", parentId, teacherId, studentId);
        
        // Validate parent has access to student
        if (!validateParentStudentAccess(parentId, studentId)) {
            throw new IllegalArgumentException("Parent does not have access to this student");
        }
        
        ParentMessage message = new ParentMessage(parentId, teacherId, studentId, 
                                                 ParentMessage.SenderType.PARENT, subject, messageContent);
        
        ParentMessage savedMessage = messageRepository.save(message);
        log.info("Message sent from parent with ID: {}", savedMessage.getId());
        
        return savedMessage;
    }

    @Override
    public ParentMessage sendReplyFromParent(Long parentId, Long teacherId, Long studentId, 
                                            String subject, String messageContent, Long replyToId) {
        log.info("Parent {} sending reply to teacher {} about student {}", parentId, teacherId, studentId);
        
        // Validate parent has access to student
        if (!validateParentStudentAccess(parentId, studentId)) {
            throw new IllegalArgumentException("Parent does not have access to this student");
        }
        
        // Validate reply message exists
        if (!messageRepository.existsById(replyToId)) {
            throw new IllegalArgumentException("Reply target message not found");
        }
        
        ParentMessage message = new ParentMessage(parentId, teacherId, studentId, 
                                                 ParentMessage.SenderType.PARENT, subject, messageContent, replyToId);
        
        ParentMessage savedMessage = messageRepository.save(message);
        log.info("Reply sent from parent with ID: {}", savedMessage.getId());
        
        return savedMessage;
    }

    @Override
    public ParentMessage sendMessageFromTeacher(Long teacherId, Long parentId, Long studentId, 
                                               String subject, String messageContent) {
        log.info("Teacher {} sending message to parent {} about student {}", teacherId, parentId, studentId);
        
        // Validate parent has access to student
        if (!validateParentStudentAccess(parentId, studentId)) {
            throw new IllegalArgumentException("Parent does not have access to this student");
        }
        
        ParentMessage message = new ParentMessage(parentId, teacherId, studentId, 
                                                 ParentMessage.SenderType.TEACHER, subject, messageContent);
        
        ParentMessage savedMessage = messageRepository.save(message);
        log.info("Message sent from teacher with ID: {}", savedMessage.getId());
        
        return savedMessage;
    }

    @Override
    public ParentMessage sendReplyFromTeacher(Long teacherId, Long parentId, Long studentId, 
                                             String subject, String messageContent, Long replyToId) {
        log.info("Teacher {} sending reply to parent {} about student {}", teacherId, parentId, studentId);
        
        // Validate parent has access to student
        if (!validateParentStudentAccess(parentId, studentId)) {
            throw new IllegalArgumentException("Parent does not have access to this student");
        }
        
        // Validate reply message exists
        if (!messageRepository.existsById(replyToId)) {
            throw new IllegalArgumentException("Reply target message not found");
        }
        
        ParentMessage message = new ParentMessage(parentId, teacherId, studentId, 
                                                 ParentMessage.SenderType.TEACHER, subject, messageContent, replyToId);
        
        ParentMessage savedMessage = messageRepository.save(message);
        log.info("Reply sent from teacher with ID: {}", savedMessage.getId());
        
        return savedMessage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentMessage> getConversation(Long parentId, Long teacherId, Long studentId) {
        return messageRepository.findConversation(parentId, teacherId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummary> getParentConversations(Long parentId) {
        List<ConversationSummary> summaries = new ArrayList<>();
        List<Object[]> uniqueConversations = messageRepository.findUniqueConversationsForParent(parentId);
        
        for (Object[] conversation : uniqueConversations) {
            Long convParentId = (Long) conversation[0];
            Long teacherId = (Long) conversation[1];
            Long studentId = (Long) conversation[2];
            
            // Get latest message in this conversation
            List<ParentMessage> messages = messageRepository.findConversation(convParentId, teacherId, studentId);
            if (!messages.isEmpty()) {
                ParentMessage latestMessage = messages.get(messages.size() - 1);
                
                // Get names
                String parentName = getParentName(convParentId);
                String teacherName = getTeacherName(teacherId);
                String studentName = getStudentName(studentId);
                
                // Count unread messages from teacher
                Long unreadCount = messageRepository.countUnreadMessagesForParent(convParentId);
                
                ConversationSummary summary = new ConversationSummary(
                    convParentId, teacherId, studentId,
                    parentName, teacherName, studentName,
                    latestMessage.getMessagePreview(),
                    latestMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    unreadCount,
                    latestMessage.getSenderType()
                );
                
                summaries.add(summary);
            }
        }
        
        return summaries;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummary> getTeacherConversations(Long teacherId) {
        List<ConversationSummary> summaries = new ArrayList<>();
        List<Object[]> uniqueConversations = messageRepository.findUniqueConversationsForTeacher(teacherId);
        
        for (Object[] conversation : uniqueConversations) {
            Long parentId = (Long) conversation[0];
            Long convTeacherId = (Long) conversation[1];
            Long studentId = (Long) conversation[2];
            
            // Get latest message in this conversation
            List<ParentMessage> messages = messageRepository.findConversation(parentId, convTeacherId, studentId);
            if (!messages.isEmpty()) {
                ParentMessage latestMessage = messages.get(messages.size() - 1);
                
                // Get names
                String parentName = getParentName(parentId);
                String teacherName = getTeacherName(convTeacherId);
                String studentName = getStudentName(studentId);
                
                // Count unread messages from parent
                Long unreadCount = messageRepository.countUnreadMessagesForTeacher(convTeacherId);
                
                ConversationSummary summary = new ConversationSummary(
                    parentId, convTeacherId, studentId,
                    parentName, teacherName, studentName,
                    latestMessage.getMessagePreview(),
                    latestMessage.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    unreadCount,
                    latestMessage.getSenderType()
                );
                
                summaries.add(summary);
            }
        }
        
        return summaries;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ParentMessage> getMessageById(Long messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    public ParentMessage markMessageAsRead(Long messageId) {
        ParentMessage message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        message.markAsRead();
        
        ParentMessage savedMessage = messageRepository.save(message);
        log.info("Message {} marked as read", messageId);
        
        return savedMessage;
    }

    @Override
    public void markConversationAsRead(Long parentId, Long teacherId, Long studentId, ParentMessage.SenderType readerType) {
        log.info("Marking conversation as read for {} between parent {} and teacher {} about student {}", 
                readerType, parentId, teacherId, studentId);
        
        List<ParentMessage> conversation = messageRepository.findConversation(parentId, teacherId, studentId);
        
        // Determine sender type to mark as read (opposite of reader type)
        ParentMessage.SenderType senderTypeToMarkRead = readerType == ParentMessage.SenderType.PARENT ? 
            ParentMessage.SenderType.TEACHER : ParentMessage.SenderType.PARENT;
        
        for (ParentMessage message : conversation) {
            if (message.getSenderType() == senderTypeToMarkRead && !message.isRead()) {
                message.markAsRead();
                messageRepository.save(message);
            }
        }
        
        log.info("Marked conversation as read");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentMessage> getUnreadMessagesForParent(Long parentId) {
        return messageRepository.findUnreadMessagesForParent(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentMessage> getUnreadMessagesForTeacher(Long teacherId) {
        return messageRepository.findUnreadMessagesForTeacher(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadMessagesForParent(Long parentId) {
        return messageRepository.countUnreadMessagesForParent(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadMessagesForTeacher(Long teacherId) {
        return messageRepository.countUnreadMessagesForTeacher(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentMessage> getConversationThreadsForParent(Long parentId) {
        return messageRepository.findConversationThreadsForParent(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentMessage> getConversationThreadsForTeacher(Long teacherId) {
        return messageRepository.findConversationThreadsForTeacher(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentMessage> searchMessages(String searchTerm, Long userId, ParentMessage.SenderType userType) {
        if (userType == ParentMessage.SenderType.PARENT) {
            return messageRepository.searchMessagesForParent(userId, searchTerm);
        } else {
            return messageRepository.searchMessagesForTeacher(userId, searchTerm);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentMessage> getRecentMessages(Long userId, ParentMessage.SenderType userType, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<ParentMessage> recentMessages = messageRepository.findRecentMessages(since);
        
        // Filter by user ID and type
        return recentMessages.stream()
            .filter(message -> {
                if (userType == ParentMessage.SenderType.PARENT) {
                    return message.getParentId().equals(userId);
                } else {
                    return message.getTeacherId().equals(userId);
                }
            })
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateParentStudentAccess(Long parentId, Long studentId) {
        return studentParentRepository.existsActiveRelationship(parentId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageStatistics getMessageStatisticsForParent(Long parentId) {
        List<ParentMessage> allMessages = messageRepository.findByParentIdOrderByCreatedAtDesc(parentId);
        
        long totalMessages = allMessages.size();
        long unreadMessages = countUnreadMessagesForParent(parentId);
        long sentMessages = allMessages.stream()
            .filter(m -> m.getSenderType() == ParentMessage.SenderType.PARENT)
            .count();
        long receivedMessages = allMessages.stream()
            .filter(m -> m.getSenderType() == ParentMessage.SenderType.TEACHER)
            .count();
        
        List<Object[]> uniqueConversations = messageRepository.findUniqueConversationsForParent(parentId);
        long conversationCount = uniqueConversations.size();
        
        return new MessageStatistics(totalMessages, unreadMessages, sentMessages, receivedMessages, conversationCount);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageStatistics getMessageStatisticsForTeacher(Long teacherId) {
        List<ParentMessage> allMessages = messageRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
        
        long totalMessages = allMessages.size();
        long unreadMessages = countUnreadMessagesForTeacher(teacherId);
        long sentMessages = allMessages.stream()
            .filter(m -> m.getSenderType() == ParentMessage.SenderType.TEACHER)
            .count();
        long receivedMessages = allMessages.stream()
            .filter(m -> m.getSenderType() == ParentMessage.SenderType.PARENT)
            .count();
        
        List<Object[]> uniqueConversations = messageRepository.findUniqueConversationsForTeacher(teacherId);
        long conversationCount = uniqueConversations.size();
        
        return new MessageStatistics(totalMessages, unreadMessages, sentMessages, receivedMessages, conversationCount);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId, ParentMessage.SenderType userType) {
        ParentMessage message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        // Validate user can delete this message (only sender can delete)
        boolean canDelete = false;
        if (userType == ParentMessage.SenderType.PARENT && message.getParentId().equals(userId) && message.getSenderType() == ParentMessage.SenderType.PARENT) {
            canDelete = true;
        } else if (userType == ParentMessage.SenderType.TEACHER && message.getTeacherId().equals(userId) && message.getSenderType() == ParentMessage.SenderType.TEACHER) {
            canDelete = true;
        }
        
        if (!canDelete) {
            throw new IllegalArgumentException("User does not have permission to delete this message");
        }
        
        // Soft delete - mark message content as deleted
        message.setMessageContent("[Tin nhắn đã được xóa]");
        message.setSubject("[Đã xóa]");
        messageRepository.save(message);
        
        log.info("Message {} soft deleted by {} {}", messageId, userType, userId);
    }

    // Helper methods
    
    private String getParentName(Long parentId) {
        Optional<Parent> parent = parentRepository.findById(parentId);
        return parent.map(Parent::getName).orElse("Phụ huynh");
    }
    
    private String getTeacherName(Long teacherId) {
        Optional<User> teacher = userRepository.findById(teacherId);
        return teacher.map(User::getFullName).orElse("Giáo viên");
    }
    
    private String getStudentName(Long studentId) {
        Optional<User> student = userRepository.findById(studentId);
        return student.map(User::getFullName).orElse("Học sinh");
    }
}