package com.chitchat.chit_chat.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import com.chitchat.chit_chat.model.ChatMessage;
import com.chitchat.chit_chat.model.MESSAGE_TYPE;
import com.chitchat.chit_chat.model.User;
import com.chitchat.chit_chat.service.UserService;

@Component
public class WebSocketListener {
    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketListener.class);

    @EventListener
    public void handleWebSocketConnection(SessionConnectedEvent event) {
        // Handle WebSocket connection events
        logger.info("Connected to websocket", event);
    }

    public void handleWebSocketDisconnection(SessionConnectedEvent event) {
        // Handle WebSocket disconnection events
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getSessionAttributes().get("username").toString();
        userService.setUserOnlineStatus(username, false);

        System.out.println("User " + username + " disconnected from session id(" + headerAccessor.getSessionId() + ")");
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(MESSAGE_TYPE.LEAVE);
        chatMessage.setSender(username);
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }

}
