package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kênh cho client subscribe (ví dụ: /topic/chat)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix cho client gửi message lên server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint client kết nối WebSocket cho chat
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns(
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "https://mvsclassroom.com"
                )
                .withSockJS();
                
        // Endpoint cho WebRTC signaling (Live Classroom)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "https://mvsclassroom.com"
                )
                .withSockJS();
    }
}
