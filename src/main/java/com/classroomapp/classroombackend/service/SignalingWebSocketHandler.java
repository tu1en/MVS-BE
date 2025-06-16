package com.classroomapp.classroombackend.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WebSocket Handler for Video Conference Signaling
 */
@Component
public class SignalingWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SignalingWebSocketHandler.class);
    
    // Map to store rooms and their sessions
    private final Map<String, Map<WebSocketSession, String>> rooms = new ConcurrentHashMap<>();
    
    // ObjectMapper for JSON parsing
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        logger.info("New WebSocket connection established: {}", session.getId());
    }
    
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        removeSessionFromAllRooms(session);
    }
    
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        try {
            JsonNode jsonMessage = objectMapper.readTree(message.getPayload());
            String type = jsonMessage.get("type").asText();
            String roomId = jsonMessage.has("roomId") ? jsonMessage.get("roomId").asText() : null;
            
            logger.info("Received message: {} in room: {}", type, roomId);
            
            switch (type) {
                case "join":
                    handleJoinRoom(session, jsonMessage);
                    break;
                case "offer":
                case "answer":
                case "ice-candidate":
                    forwardMessageToRoom(session, jsonMessage, roomId);
                    break;
                case "leave":
                    handleLeaveRoom(session, roomId);
                    break;
                default:
                    logger.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        logger.error("Transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
        removeSessionFromAllRooms(session);
    }
    
    /**
     * Handle user joining a room
     */
    private void handleJoinRoom(WebSocketSession session, JsonNode message) throws IOException {
        String roomId = message.get("roomId").asText();
        JsonNode user = message.get("user");
        String userId = user.get("id").asText();
        
        // Create room if it doesn't exist
        rooms.putIfAbsent(roomId, new ConcurrentHashMap<>());
        
        // Add session to room
        rooms.get(roomId).put(session, userId);
        
        logger.info("User {} joined room {}. Room size: {}", userId, roomId, rooms.get(roomId).size());
        
        // Notify other clients in the room
        String joinMessage = objectMapper.writeValueAsString(Map.of(
            "type", "user-joined",
            "roomId", roomId,
            "user", objectMapper.readTree(user.toString())
        ));
        
        // Send notification to all other clients in the room
        for (Map.Entry<WebSocketSession, String> entry : rooms.get(roomId).entrySet()) {
            WebSocketSession otherSession = entry.getKey();
            if (!otherSession.equals(session) && otherSession.isOpen()) {
                try {
                    otherSession.sendMessage(new TextMessage(joinMessage));
                } catch (IOException e) {
                    logger.error("Error sending join message to session {}: {}", otherSession.getId(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Forward message to other clients in the room
     */
    private void forwardMessageToRoom(WebSocketSession sender, JsonNode message, String roomId) {
        Map<WebSocketSession, String> roomSessions = rooms.get(roomId);
        
        if (roomSessions == null || roomSessions.isEmpty()) {
            logger.warn("Room not found or empty: {}", roomId);
            return;
        }
        
        String senderId = message.has("userId") ? message.get("userId").asText() : "";
        String messageText = message.toString();
        
        // Forward message to all other clients in the room
        for (Map.Entry<WebSocketSession, String> entry : roomSessions.entrySet()) {
            WebSocketSession session = entry.getKey();
            String sessionUserId = entry.getValue();
            
            // Don't send back to sender
            if (!session.equals(sender) && !sessionUserId.equals(senderId) && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(messageText));
                } catch (IOException e) {
                    logger.error("Error forwarding message to session {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handle user leaving a room
     */
    private void handleLeaveRoom(WebSocketSession session, String roomId) throws IOException {
        Map<WebSocketSession, String> roomSessions = rooms.get(roomId);
        
        if (roomSessions == null || roomSessions.isEmpty()) {
            return;
        }
        
        // Get user ID before removing from room
        String userId = roomSessions.get(session);
        
        // Remove session from room
        roomSessions.remove(session);
        
        logger.info("User {} left room {}", userId, roomId);
        
        // If room is empty, remove it
        if (roomSessions.isEmpty()) {
            rooms.remove(roomId);
            logger.info("Room {} removed because it's empty", roomId);
            return;
        }
        
        // Notify remaining clients in the room
        String leaveMessage = objectMapper.writeValueAsString(Map.of(
            "type", "user-left",
            "roomId", roomId,
            "userId", userId
        ));
        
        // Send notification to all remaining clients in the room
        for (WebSocketSession remainingSession : roomSessions.keySet()) {
            if (remainingSession.isOpen()) {
                try {
                    remainingSession.sendMessage(new TextMessage(leaveMessage));
                } catch (IOException e) {
                    logger.error("Error sending leave message to session {}: {}", remainingSession.getId(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Remove session from all rooms when connection is closed
     */
    private void removeSessionFromAllRooms(WebSocketSession session) {
        for (Map.Entry<String, Map<WebSocketSession, String>> roomEntry : rooms.entrySet()) {
            String roomId = roomEntry.getKey();
            Map<WebSocketSession, String> roomSessions = roomEntry.getValue();
            
            if (roomSessions.containsKey(session)) {
                String userId = roomSessions.get(session);
                roomSessions.remove(session);
                
                logger.info("User {} disconnected from room {}", userId, roomId);
                
                // If room is empty, remove it
                if (roomSessions.isEmpty()) {
                    rooms.remove(roomId);
                    logger.info("Room {} removed because it's empty", roomId);
                } else {
                    // Notify remaining clients in the room
                    try {
                        String leaveMessage = objectMapper.writeValueAsString(Map.of(
                            "type", "user-left",
                            "roomId", roomId,
                            "userId", userId
                        ));
                        
                        for (WebSocketSession remainingSession : roomSessions.keySet()) {
                            if (remainingSession.isOpen()) {
                                try {
                                    remainingSession.sendMessage(new TextMessage(leaveMessage));
                                } catch (IOException e) {
                                    logger.error("Error sending leave message to session {}: {}", remainingSession.getId(), e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error creating leave message: {}", e.getMessage());
                    }
                }
                
                // Only process the first room found with this session
                break;
            }
        }
    }
}
