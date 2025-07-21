package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.classroomapp.classroombackend.websocket.SignalingHandler;

@Configuration
@EnableWebSocket
public class WebRTCWebSocketConfig implements WebSocketConfigurer {

    private final SignalingHandler signalingHandler;

    public WebRTCWebSocketConfig(SignalingHandler signalingHandler) {
        this.signalingHandler = signalingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalingHandler, "/signaling")
                .setAllowedOriginPatterns(
                    "http://localhost:3000",
                    "http://localhost:5173", 
                    "https://mvsclassroom.com",
                    "*"
                )
                .withSockJS();
    }
}
