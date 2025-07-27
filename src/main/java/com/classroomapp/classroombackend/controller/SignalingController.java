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
 * Controller xá»­ lÃ½ WebRTC signaling messages qua STOMP WebSocket
 */
@Controller
public class SignalingController {
    
    private static final Logger logger = LoggerFactory.getLogger(SignalingController.class);
    private final SimpMessagingTemplate messagingTemplate;
    
    public SignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Xá»­ lÃ½ signaling messages (offer, answer, ice-candidate) cho room
     * Client gá»­i Ä‘áº¿n: /app/room/{roomId}/signal
     * Server broadcast Ä‘áº¿n: /topic/room-{roomId}
     */
    @MessageMapping("/room/{roomId}/signal")
    public void handleSignaling(@DestinationVariable String roomId, @Payload SignalingMessage message) {
        logger.info("ðŸ“¡ Received signaling message: type={}, roomId={}, senderId={}, targetId={}", 
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
            logger.info("ðŸ“¤ Sent signaling to specific user: {}", destination);
        } else {
            // Broadcast to entire room
            String destination = "/topic/room-" + roomId;
            messagingTemplate.convertAndSend(destination, message);
            logger.info("ðŸ“¤ Broadcast signaling to room: {}", destination);
        }
    }
    
    /**
     * Xá»­ lÃ½ user join room
     * Client gá»­i Ä‘áº¿n: /app/room/{roomId}/join
     * Server broadcast Ä‘áº¿n: /topic/room-{roomId}
     */
    @MessageMapping("/room/{roomId}/join")
    public void handleJoinRoom(@DestinationVariable String roomId, @Payload SignalingMessage message) {
        logger.info("ðŸ‘¥ User joining room: roomId={}, userId={}, userName={}", 
                    roomId, message.getSenderId(), 
                    message.getUser() != null ? message.getUser().getName() : "Unknown");
        
        message.setType("user-joined");
        message.setRoomId(roomId);
        
        // Broadcast join event to room
        String destination = "/topic/room-" + roomId;
        messagingTemplate.convertAndSend(destination, message);
        logger.info("ðŸ“¤ Broadcast user join to room: {}", destination);
    }
    
    /**
     * Xá»­ lÃ½ user leave room
     * Client gá»­i Ä‘áº¿n: /app/room/{roomId}/leave
     * Server broadcast Ä‘áº¿n: /topic/room-{roomId}
     */
    @MessageMapping("/room/{roomId}/leave")
    public void handleLeaveRoom(@DestinationVariable String roomId, @Payload SignalingMessage message) {
        logger.info("ðŸ‘‹ User leaving room: roomId={}, userId={}", roomId, message.getSenderId());
        
        message.setType("user-left");
        message.setRoomId(roomId);
        
        // Broadcast leave event to room
        String destination = "/topic/room-" + roomId;
        messagingTemplate.convertAndSend(destination, message);
        logger.info("ðŸ“¤ Broadcast user leave to room: {}", destination);
    }
    
    /**
     * Health check endpoint cho signaling
     * Client gá»­i Ä‘áº¿n: /app/signaling/ping
     * Server reply Ä‘áº¿n: /topic/signaling/pong
     */
    @MessageMapping("/signaling/ping")
    public void handlePing(@Payload SignalingMessage message) {
        logger.info("ðŸ“ Received ping from: {}", message.getSenderId());
        
        SignalingMessage pong = new SignalingMessage();
        pong.setType("pong");
        pong.setSenderId("server");
        
        messagingTemplate.convertAndSend("/topic/signaling/pong", pong);
    }
}
