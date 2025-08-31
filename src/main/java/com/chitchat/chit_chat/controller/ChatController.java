package com.chitchat.chit_chat.controller;

import java.time.LocalDateTime;
import java.util.Map;

import javax.swing.Spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.chitchat.chit_chat.model.ChatMessage;
import com.chitchat.chit_chat.model.MESSAGE_TYPE;
import com.chitchat.chit_chat.repository.ChatMessageRepository;
import com.chitchat.chit_chat.service.UserService;

// Client connects to: ws://localhost:5147/ws
// SimpMessageHeaderAccessor is a utility class in Spring WebSocket (under the org.springframework.messaging.simp package) that gives you access to STOMP message headers, including session-related metadata like session ID, user info, and custom attributes.
    // It's commonly used in @MessageMapping methods to manage things like:
    // Session attributes (e.g., storing the username when a user joins)
    // Getting the session ID
    // Working with custom headers


@Controller
public class ChatController {
    @Autowired
    private UserService userService;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("chat.addUser") // Handles messages to: /app/chat.addUser
    @SendTo("/topic/public")        // Broadcasts to: /topic/public
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        if(userService.userExists(chatMessage.getSender())) {
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

            if (sessionAttributes == null) {
                System.err.println("Session attributes are null for session ID: " + headerAccessor.getSessionId());
                return null;
            }

            // Store username in session
            sessionAttributes.put("username", chatMessage.getSender());
            userService.setUserOnlineStatus(chatMessage.getSender(), true);

            System.out.println("User " + chatMessage.getSender() + " connected into session id(" + headerAccessor.getSessionId() + ")");

            chatMessage.setTimestamp(LocalDateTime.now());

            if (chatMessage.getContent() == null) {
                chatMessage.setContent("");
            }
            chatMessageRepository.save(chatMessage);
        }
        return null;
    }

    @MessageMapping("chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        if(userService.userExists(chatMessage.getSender())) {
            if(chatMessage.getTimestamp() == null) {
                chatMessage.setTimestamp(LocalDateTime.now());
            }

            if(chatMessage.getContent() == null) {
                chatMessage.setContent("");
            }

            return chatMessageRepository.save(chatMessage);
        }
        return null;
    }

    @MessageMapping("chat.sendPrivateMessage")
    // No @SendTo, because using SimpMessagingTemplate to send messages manually.
    public void sendPrivateMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        if(userService.userExists(chatMessage.getSender()) && userService.userExists(chatMessage.getReceiver())) {
            if(chatMessage.getTimestamp() == null) {
                chatMessage.setTimestamp(LocalDateTime.now());
            }

            if(chatMessage.getContent() == null) {
                chatMessage.setContent("");
            }

            chatMessage.setType(MESSAGE_TYPE.PRIVATE);

            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            System.out.println("Message saved successfully with id: " + savedMessage.getId());

            try{
                 String recipientDestination = "/user/" + chatMessage.getReceiver() + "/queue/private";
                System.out.println("Sending private message to destination: " + recipientDestination);
                messagingTemplate.convertAndSend(recipientDestination, savedMessage);

                String senderDestination = "/user/" + chatMessage.getSender() + "/queue/private";
                System.out.println("Sending private message to sender's destination: " + senderDestination);
                messagingTemplate.convertAndSend(senderDestination, savedMessage);
            }
            catch(Exception e) {
                System.err.println("Error sending private message: " + e.getMessage());
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Either sender or receiver does not exist. Message not sent.");
        }
    }
}
