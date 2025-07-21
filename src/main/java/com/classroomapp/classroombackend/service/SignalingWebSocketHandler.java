package com.classroomapp.classroombackend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                case "join-room":
                    handleJoinRoom(session, jsonMessage);
                    break;
                case "offer":
                case "answer":
                case "ice-candidate":
                case "candidate":
                    forwardMessageToRoom(session, jsonMessage, roomId);
                    break;
                case "leave":
                case "leave-room":
                    handleLeaveRoom(session, roomId);
                    break;
                case "screen-share-start":
                case "screen-share-stop":
                case "mute-audio":
                case "unmute-audio":
                case "mute-video":
                case "unmute-video":
                case "chat-message":
                    forwardMessageToRoom(session, jsonMessage, roomId);
                    break;
                case "document-navigation":
                case "document-sync":
                case "whiteboard-draw":
                case "whiteboard-clear":
                    handleDocumentAndWhiteboardSync(session, jsonMessage, roomId);
                    break;
                case "get-room-info":
                    handleGetRoomInfo(session, roomId);
                    break;
                case "ping":
                    handlePing(session);
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

    /**
     * Handle get room info request
     */
    private void handleGetRoomInfo(WebSocketSession session, String roomId) {
        try {
            Map<WebSocketSession, String> roomSessions = rooms.get(roomId);

            Map<String, Object> roomInfo = Map.of(
                "type", "room-info",
                "roomId", roomId,
                "participantCount", roomSessions != null ? roomSessions.size() : 0,
                "participants", roomSessions != null ? roomSessions.values() : List.of()
            );

            String roomInfoMessage = objectMapper.writeValueAsString(roomInfo);
            session.sendMessage(new TextMessage(roomInfoMessage));

        } catch (Exception e) {
            logger.error("Error handling get room info: {}", e.getMessage());
        }
    }

    /**
     * Handle ping request
     */
    private void handlePing(WebSocketSession session) {
        try {
            Map<String, Object> pongMessage = Map.of(
                "type", "pong",
                "timestamp", System.currentTimeMillis()
            );

            String pongJson = objectMapper.writeValueAsString(pongMessage);
            session.sendMessage(new TextMessage(pongJson));

        } catch (Exception e) {
            logger.error("Error handling ping: {}", e.getMessage());
        }
    }

    /**
     * Get room statistics
     */
    public Map<String, Object> getRoomStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", rooms.size());
        stats.put("totalSessions", rooms.values().stream().mapToInt(Map::size).sum());

        Map<String, Integer> roomSizes = new HashMap<>();
        rooms.forEach((roomId, sessions) -> roomSizes.put(roomId, sessions.size()));
        stats.put("roomSizes", roomSizes);

        return stats;
    }

    /**
     * Broadcast message to all rooms
     */
    public void broadcastToAllRooms(String message) {
        rooms.values().forEach(roomSessions -> {
            roomSessions.keySet().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        logger.error("Error broadcasting message to session {}: {}", session.getId(), e.getMessage());
                    }
                }
            });
        });
    }

    /**
     * Get active rooms
     */
    public Set<String> getActiveRooms() {
        return rooms.keySet();
    }

    /**
     * Get participants in a room
     */
    public List<String> getRoomParticipants(String roomId) {
        Map<WebSocketSession, String> roomSessions = rooms.get(roomId);
        return roomSessions != null ? new ArrayList<>(roomSessions.values()) : new ArrayList<>();
    }
    
    /**
     * Handle document navigation and whiteboard sync
     * Enhanced for Phase 2 Document Sharing
     */
    private void handleDocumentAndWhiteboardSync(WebSocketSession sender, JsonNode message, String roomId) {
        try {
            String type = message.get("type").asText();
            
            // Log document/whiteboard activities
            logger.info("Document/Whiteboard sync: {} in room {}", type, roomId);
            
            // Add timestamp to message for sync purposes
            Map<String, Object> enhancedMessage = objectMapper.convertValue(message, Map.class);
            enhancedMessage.put("timestamp", System.currentTimeMillis());
            enhancedMessage.put("serverProcessed", true);
            
            String enhancedMessageJson = objectMapper.writeValueAsString(enhancedMessage);
            
            // Forward to all other participants in the room
            Map<WebSocketSession, String> roomSessions = rooms.get(roomId);
            if (roomSessions != null) {
                for (Map.Entry<WebSocketSession, String> entry : roomSessions.entrySet()) {
                    WebSocketSession session = entry.getKey();
                    if (!session.equals(sender) && session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(enhancedMessageJson));
                        } catch (IOException e) {
                            logger.error("Error forwarding document/whiteboard sync to session {}: {}", 
                                       session.getId(), e.getMessage());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error handling document/whiteboard sync: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send document navigation update to specific room
     * Used by DocumentSharingService for server-initiated navigation
     */
    public void sendDocumentNavigationUpdate(String roomId, Map<String, Object> navigationData) {
        try {
            String navigationJson = objectMapper.writeValueAsString(navigationData);
            Map<WebSocketSession, String> roomSessions = rooms.get(roomId);
            
            if (roomSessions != null) {
                for (WebSocketSession session : roomSessions.keySet()) {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(navigationJson));
                        } catch (IOException e) {
                            logger.error("Error sending navigation update to session {}: {}", 
                                       session.getId(), e.getMessage());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error sending document navigation update: {}", e.getMessage(), e);
        }
    }
}
