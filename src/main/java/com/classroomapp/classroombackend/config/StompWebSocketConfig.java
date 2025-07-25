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
        // KÃªnh cho client subscribe (vÃ­ dá»¥: /topic/chat)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix cho client gá»­i message lÃªn server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint client káº¿t ná»‘i WebSocket cho chat
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
