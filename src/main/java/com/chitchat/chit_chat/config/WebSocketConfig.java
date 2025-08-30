package com.chitchat.chit_chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.NonNull;

@Configuration
@EnableWebSocketMessageBroker // Enables STOMP/WebSocket messaging in Spring.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{ //WebSocketMessageBrokerConfigurer: Provides configuration hooks.
    @Override
    public void configureMessageBroker(@ NonNull MessageBrokerRegistry config) {
        // Enable simple broker for group and private messages
        config.enableSimpleBroker("/topic", "/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5147")
                .withSockJS();
    }
}

// Enables message broker: Acts like a message queue for WebSocket messages.
// Registers endpoints (/ws): Entry point for WebSocket handshake.
// Configures broker prefixes:
    // "/app": For incoming messages mapped to controller methods (@MessageMapping)
    // "/topic": For public broadcasts
    // "/queue" and "/user": For private messaging
    // "/user" also used for routing user-specific destinations
