package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.SignalingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controller xử lý WebRTC signaling messages qua STOMP WebSocket
 */
@Controller
public class SignalingController {
    
    private static final Logger logger = LoggerFactory.getLogger(SignalingController.class);
    private final SimpMessagingTemplate messagingTemplate;
    
    public SignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Xử lý signaling messages (offer, answer, ice-candidate) cho room
     * Client gửi đến: /app/room/{roomId}/signal
     * Server broadcast đến: /topic/room-{roomId}
     */
    @MessageMapping("/room/{roomId}/signal")
    public void handleSignaling(@DestinationVariable String roomId, @Payload SignalingMessage message) {
        logger.info("📡 Received signaling message: type={}, roomId={}, senderId={}, targetId={}", 
                    message.getType(), roomId, message.getSenderId(), message.getTargetId());
        
        // Set roomId if not present
        if (message.getRoomId() == null) {
            message.setRoomId(roomId);
        }
        
        // Broadcast to specific user or entire room
        if (message.getTargetId() != null && !message.getTargetId().isEmpty()) {
            // Send to specific user
            String destination = "/topic/room-" + roomId + "/user-" + message.getTargetId();
            messagingTemplate.convertAndSend(destination, message);
            logger.info("📤 Sent signaling to specific user: {}", destination);
        } else {
            // Broadcast to entire room
            String destination = "/topic/room-" + roomId;
            messagingTemplate.convertAndSend(destination, message);
            logger.info("📤 Broadcast signaling to room: {}", destination);
        }
    }
    
    /**
     * Xử lý user join room
     * Client gửi đến: /app/room/{roomId}/join
     * Server broadcast đến: /topic/room-{roomId}
     */
    @MessageMapping("/room/{roomId}/join")
    public void handleJoinRoom(@DestinationVariable String roomId, @Payload SignalingMessage message) {
        logger.info("👥 User joining room: roomId={}, userId={}, userName={}", 
                    roomId, message.getSenderId(), 
                    message.getUser() != null ? message.getUser().getName() : "Unknown");
        
        message.setType("user-joined");
        message.setRoomId(roomId);
        
        // Broadcast join event to room
        String destination = "/topic/room-" + roomId;
        messagingTemplate.convertAndSend(destination, message);
        logger.info("📤 Broadcast user join to room: {}", destination);
    }
    
    /**
     * Xử lý user leave room
     * Client gửi đến: /app/room/{roomId}/leave
     * Server broadcast đến: /topic/room-{roomId}
     */
    @MessageMapping("/room/{roomId}/leave")
    public void handleLeaveRoom(@DestinationVariable String roomId, @Payload SignalingMessage message) {
        logger.info("👋 User leaving room: roomId={}, userId={}", roomId, message.getSenderId());
        
        message.setType("user-left");
        message.setRoomId(roomId);
        
        // Broadcast leave event to room
        String destination = "/topic/room-" + roomId;
        messagingTemplate.convertAndSend(destination, message);
        logger.info("📤 Broadcast user leave to room: {}", destination);
    }
    
    /**
     * Health check endpoint cho signaling
     * Client gửi đến: /app/signaling/ping
     * Server reply đến: /topic/signaling/pong
     */
    @MessageMapping("/signaling/ping")
    public void handlePing(@Payload SignalingMessage message) {
        logger.info("🏓 Received ping from: {}", message.getSenderId());
        
        SignalingMessage pong = new SignalingMessage();
        pong.setType("pong");
        pong.setSenderId("server");
        
        messagingTemplate.convertAndSend("/topic/signaling/pong", pong);
    }
}
