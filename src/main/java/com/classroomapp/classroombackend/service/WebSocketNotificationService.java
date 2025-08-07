package com.classroomapp.classroombackend.service;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.classroomapp.classroombackend.dto.ClassDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WebSocketNotificationService extends TextWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("WebSocket connection closed: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Received WebSocket message: {}", message.getPayload());
    }

    /**
     * Broadcast CLASS_CREATED notification với đầy đủ data
     */
    public void notifyClassCreated(ClassDto classDto) {
        try {
            NotificationMessage notification = new NotificationMessage(
                "CLASS_CREATED", 
                "Lớp học mới được tạo: " + classDto.getClassName(), 
                classDto  // ✅ FIX: Gửi đầy đủ data thay vì null
            );
            
            String message = objectMapper.writeValueAsString(notification);
            broadcast(message);
            logger.info("Broadcasted CLASS_CREATED notification for class: {}", classDto.getClassName());
            
        } catch (Exception e) {
            logger.error("Error sending CLASS_CREATED notification", e);
        }
    }

    /**
     * Broadcast CLASS_UPDATED notification
     */
    public void notifyClassUpdated(ClassDto classDto) {
        try {
            NotificationMessage notification = new NotificationMessage(
                "CLASS_UPDATED", 
                "Lớp học được cập nhật: " + classDto.getClassName(), 
                classDto
            );
            
            String message = objectMapper.writeValueAsString(notification);
            broadcast(message);
            logger.info("Broadcasted CLASS_UPDATED notification for class: {}", classDto.getClassName());
            
        } catch (Exception e) {
            logger.error("Error sending CLASS_UPDATED notification", e);
        }
    }

    /**
     * Broadcast CLASS_DELETED notification
     */
    public void notifyClassDeleted(Long classId, String className) {
        try {
            ClassDeletedData data = new ClassDeletedData(classId, className);
            NotificationMessage notification = new NotificationMessage(
                "CLASS_DELETED", 
                "Lớp học đã bị xóa: " + className, 
                data
            );
            
            String message = objectMapper.writeValueAsString(notification);
            broadcast(message);
            logger.info("Broadcasted CLASS_DELETED notification for class: {}", className);
            
        } catch (Exception e) {
            logger.error("Error sending CLASS_DELETED notification", e);
        }
    }

    /**
     * Send general notification
     */
    public void sendNotification(String type, String message, Object data) {
        try {
            NotificationMessage notification = new NotificationMessage(type, message, data);
            String jsonMessage = objectMapper.writeValueAsString(notification);
            broadcast(jsonMessage);
            logger.info("Broadcasted {} notification: {}", type, message);
            
        } catch (Exception e) {
            logger.error("Error sending {} notification", type, e);
        }
    }

    /**
     * Broadcast message to all connected sessions
     */
    private void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                logger.error("Error sending message to session {}", session.getId(), e);
                sessions.remove(session);
            }
        }
    }

    /**
     * Get active sessions count
     */
    public int getActiveSessionsCount() {
        return sessions.size();
    }

    // DTO classes for notifications
    public static class NotificationMessage {
        public String type;
        public String message;
        public Object data;

        public NotificationMessage() {}

        public NotificationMessage(String type, String message, Object data) {
            this.type = type;
            this.message = message;
            this.data = data;
        }

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    public static class ClassDeletedData {
        public Long classId;
        public String className;

        public ClassDeletedData() {}

        public ClassDeletedData(Long classId, String className) {
            this.classId = classId;
            this.className = className;
        }

        // Getters and setters
        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
    }
}