package com.classroomapp.classroombackend.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.classroomapp.classroombackend.model.WebRTCRoom;
import com.classroomapp.classroombackend.model.WebRTCSession;
import com.classroomapp.classroombackend.service.WebRTCSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebRTCSessionService webRTCSessionService;
    
    // Keep in-memory cache for active WebSocket sessions (not persisted)
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            activeSessions.put(session.getId(), session);
            System.out.println("✅ WebSocket connected: " + session.getId());
            
            // Send connection confirmation
            Map<String, Object> response = new HashMap<>();
            response.put("type", "connection-established");
            response.put("sessionId", session.getId());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            
        } catch (Exception e) {
            System.err.println("❌ Error handling connection establishment: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        
        try {
            // Remove from active sessions
            activeSessions.remove(sessionId);
            
            // Get session info from database before disconnecting
            Optional<WebRTCSession> dbSessionOpt = webRTCSessionService.getSessionById(sessionId);
            String roomId = null;
            
            if (dbSessionOpt.isPresent()) {
                roomId = dbSessionOpt.get().getRoomId();
                
                // Disconnect session in database
                webRTCSessionService.disconnectSession(sessionId);
                
                // Notify other users in room about user leaving
                try {
                    Map<String, Object> leaveMessage = new HashMap<>();
                    leaveMessage.put("type", "user-left");
                    leaveMessage.put("roomId", roomId);
                    leaveMessage.put("userId", sessionId);
                    broadcast(roomId, leaveMessage, sessionId);
                } catch (Exception e) {
                    System.err.println("❌ Error broadcasting user left: " + e.getMessage());
                }
            }
            
            System.out.println("🔌 WebSocket closed: " + sessionId + " from room: " + roomId);
            
        } catch (Exception e) {
            System.err.println("❌ Error handling connection close: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");
            String roomId = (String) payload.get("roomId");
            String sessionId = session.getId();

            System.out.println("📨 Received message: " + type + " from session: " + sessionId + " for room: " + roomId);

            switch (type) {
                case "join-room":
                    handleJoinRoom(sessionId, roomId, payload, session);
                    break;
                    
                case "offer":
                case "answer":
                case "ice-candidate":
                    // Relay signaling messages to specific target
                    String targetId = (String) payload.get("targetId");
                    if (targetId != null) {
                        relayToTarget(targetId, payload);
                    } else {
                        // Broadcast to all in room (fallback)
                        broadcast(roomId, payload, sessionId);
                    }
                    break;
                    
                default:
                    // Broadcast other messages to room
                    broadcast(roomId, payload, sessionId);
                    break;
            }
        } catch (Exception e) {
            System.err.println("❌ Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleJoinRoom(String sessionId, String roomId, Map<String, Object> payload, WebSocketSession session) throws Exception {
        try {
            // Extract user info from payload
            Map<String, Object> userInfo = (Map<String, Object>) payload.get("user");
            String userEmail = userInfo != null ? (String) userInfo.get("email") : "anonymous";
            String roomName = (String) payload.get("roomName");
            
            // Get user agent and IP from session
            String userAgent = session.getHandshakeHeaders().getFirst("User-Agent");
            String ipAddress = extractIpAddress(session);
            
            // Create or get room in database
            WebRTCRoom room = webRTCSessionService.createOrGetRoom(roomId, roomName, userEmail);
            
            // Check room capacity
            if (!room.hasCapacity()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "error");
                errorResponse.put("message", "Room is full");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                return;
            }
            
            // Create session in database
            WebRTCSession dbSession = webRTCSessionService.createSession(sessionId, roomId, userEmail, userAgent, ipAddress);
            
            // Notify existing users about new user
            Map<String, Object> joinMessage = new HashMap<>();
            joinMessage.put("type", "user-joined");
            joinMessage.put("roomId", roomId);
            joinMessage.put("user", userInfo);
            joinMessage.put("userId", sessionId);
            
            broadcast(roomId, joinMessage, sessionId);
            
            // Send current room info to new user
            List<WebRTCSession> activeSessions = webRTCSessionService.getActiveSessionsInRoom(roomId);
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("type", "room-info");
            roomInfo.put("roomId", roomId);
            roomInfo.put("participants", activeSessions.size());
            roomInfo.put("yourSessionId", sessionId);
            
            WebSocketSession wsSession = this.activeSessions.get(sessionId);
            if (wsSession != null && wsSession.isOpen()) {
                wsSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(roomInfo)));
            }
            
            System.out.println("👥 User " + sessionId + " joined room " + roomId + ". Total participants: " + activeSessions.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error handling join room: " + e.getMessage());
            throw e;
        }
    }

    private void broadcast(String roomId, Map<String, Object> message, String senderId) throws Exception {
        try {
            // Get active sessions from database
            List<WebRTCSession> roomSessions = webRTCSessionService.getActiveSessionsInRoom(roomId);
            
            if (roomSessions.isEmpty()) {
                System.out.println("⚠️ No active sessions found for room " + roomId);
                return;
            }
            
            int broadcastCount = 0;
            
            for (WebRTCSession dbSession : roomSessions) {
                String sessionId = dbSession.getSessionId();
                if (!sessionId.equals(senderId)) {
                    WebSocketSession wsSession = activeSessions.get(sessionId);
                    if (wsSession != null && wsSession.isOpen()) {
                        try {
                            wsSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                            broadcastCount++;
                        } catch (Exception e) {
                            System.err.println("❌ Error sending to session " + sessionId + ": " + e.getMessage());
                            // Remove broken session from active sessions and database
                            activeSessions.remove(sessionId);
                            webRTCSessionService.disconnectSession(sessionId);
                        }
                    } else {
                        // WebSocket session not found or closed, disconnect from database
                        webRTCSessionService.disconnectSession(sessionId);
                    }
                }
            }
            
            System.out.println("📡 Broadcasted " + message.get("type") + " to " + broadcastCount + " participants in room " + roomId);
            
        } catch (Exception e) {
            System.err.println("❌ Error broadcasting message: " + e.getMessage());
            throw e;
        }
    }

    private void relayToTarget(String targetId, Map<String, Object> message) throws Exception {
        try {
            WebSocketSession targetSession = activeSessions.get(targetId);
            if (targetSession != null && targetSession.isOpen()) {
                try {
                    targetSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                    System.out.println("🎯 Relayed " + message.get("type") + " to target: " + targetId);
                } catch (Exception e) {
                    System.err.println("❌ Error relaying to target " + targetId + ": " + e.getMessage());
                    // Clean up broken session
                    activeSessions.remove(targetId);
                    webRTCSessionService.disconnectSession(targetId);
                }
            } else {
                System.out.println("⚠️ Target session " + targetId + " not found or closed");
                // Disconnect from database if WebSocket session not found
                webRTCSessionService.disconnectSession(targetId);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in relayToTarget: " + e.getMessage());
            throw e;
        }
    }
    
    public int getActiveConnections() {
        return activeSessions.size();
    }
    
    public long getActiveConnectionsFromDB() {
        return webRTCSessionService.getActiveConnectionCount();
    }
    
    public long getActiveRooms() {
        return webRTCSessionService.getActiveRoomCount();
    }
    
    private String extractUserFromSession(WebSocketSession session) {
        // Try to extract user from session attributes or headers
        Object user = session.getAttributes().get("user");
        if (user != null) {
            return user.toString();
        }
        
        // Try to extract from query parameters (if passed during handshake)
        String query = session.getUri().getQuery();
        if (query != null && query.contains("user=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("user=")) {
                    return param.substring(5);
                }
            }
        }
        
        return "anonymous";
    }
    
    private String extractIpAddress(WebSocketSession session) {
        // Try to get real IP from headers (for proxy/load balancer scenarios)
        String xForwardedFor = session.getHandshakeHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = session.getHandshakeHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fallback to remote address
        if (session.getRemoteAddress() != null) {
            return session.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }
}