package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.classroomapp.classroombackend.service.SignalingWebSocketHandler;

/**
 * WebSocket Configuration for Video Conference Signaling
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Bean
    public SignalingWebSocketHandler signalingWebSocketHandler() {
        return new SignalingWebSocketHandler();
    }
    
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        // Register signaling handler for video conference
        registry.addHandler(signalingWebSocketHandler(), "/signaling")
                .setAllowedOrigins("*"); // In production, specify exact origins
    }
}
