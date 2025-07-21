package com.classroomapp.classroombackend.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, List<String>> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("✅ WebSocket connected: " + session.getId());
        
        try {
            // Send connection confirmation
            Map<String, Object> response = new HashMap<>();
            response.put("type", "connection-established");
            response.put("sessionId", session.getId());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            System.err.println("❌ Error sending connection confirmation: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        String roomId = sessionToRoom.get(sessionId);
        
        sessions.remove(sessionId);
        sessionToRoom.remove(sessionId);
        
        if (roomId != null) {
            List<String> roomSessions = rooms.get(roomId);
            if (roomSessions != null) {
                roomSessions.remove(sessionId);
                
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
                
                // Clean up empty rooms
                if (roomSessions.isEmpty()) {
                    rooms.remove(roomId);
                }
            }
        }
        
        System.out.println("🔌 WebSocket closed: " + sessionId + " from room: " + roomId);
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
                    handleJoinRoom(sessionId, roomId, payload);
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

    private void handleJoinRoom(String sessionId, String roomId, Map<String, Object> payload) throws Exception {
        // Add session to room
        rooms.computeIfAbsent(roomId, k -> new ArrayList<>()).add(sessionId);
        sessionToRoom.put(sessionId, roomId);
        
        // Get user info from payload
        Map<String, Object> userInfo = (Map<String, Object>) payload.get("user");
        
        // Notify existing users about new user
        Map<String, Object> joinMessage = new HashMap<>();
        joinMessage.put("type", "user-joined");
        joinMessage.put("roomId", roomId);
        joinMessage.put("user", userInfo);
        joinMessage.put("userId", sessionId);
        
        broadcast(roomId, joinMessage, sessionId);
        
        // Send current room info to new user
        List<String> roomSessions = rooms.get(roomId);
        Map<String, Object> roomInfo = new HashMap<>();
        roomInfo.put("type", "room-info");
        roomInfo.put("roomId", roomId);
        roomInfo.put("participants", roomSessions.size());
        roomInfo.put("yourSessionId", sessionId);
        
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(roomInfo)));
        }
        
        System.out.println("👥 User " + sessionId + " joined room " + roomId + ". Total participants: " + roomSessions.size());
    }

    private void broadcast(String roomId, Map<String, Object> message, String senderId) throws Exception {
        if (!rooms.containsKey(roomId)) {
            System.out.println("⚠️ Room " + roomId + " not found for broadcast");
            return;
        }
        
        List<String> roomSessions = rooms.get(roomId);
        int broadcastCount = 0;
        
        for (String sessionId : new ArrayList<>(roomSessions)) {
            if (!sessionId.equals(senderId)) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                        broadcastCount++;
                    } catch (Exception e) {
                        System.err.println("❌ Error sending to session " + sessionId + ": " + e.getMessage());
                        // Remove broken session
                        sessions.remove(sessionId);
                        roomSessions.remove(sessionId);
                        sessionToRoom.remove(sessionId);
                    }
                }
            }
        }
        
        System.out.println("📡 Broadcasted " + message.get("type") + " to " + broadcastCount + " participants in room " + roomId);
    }

    private void relayToTarget(String targetId, Map<String, Object> message) throws Exception {
        WebSocketSession targetSession = sessions.get(targetId);
        if (targetSession != null && targetSession.isOpen()) {
            try {
                targetSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                System.out.println("🎯 Relayed " + message.get("type") + " to target: " + targetId);
            } catch (Exception e) {
                System.err.println("❌ Error relaying to target " + targetId + ": " + e.getMessage());
                // Clean up broken session
                sessions.remove(targetId);
                String roomId = sessionToRoom.get(targetId);
                if (roomId != null) {
                    rooms.getOrDefault(roomId, new ArrayList<>()).remove(targetId);
                }
                sessionToRoom.remove(targetId);
            }
        } else {
            System.out.println("⚠️ Target session " + targetId + " not found or closed");
        }
    }
    
    public int getActiveConnections() {
        return sessions.size();
    }
    
    public int getActiveRooms() {
        return rooms.size();
    }
}
