package com.chitchat.chit_chat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chitchat.chit_chat.model.ChatMessage;
import com.chitchat.chit_chat.repository.ChatMessageRepository;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ResponseEntity<List<ChatMessage>> getPrivateMessages(@RequestParam String user1, @RequestParam String user2) {
        List<ChatMessage> messages = chatMessageRepository.findPrivateMessagesBetweenUsers(user1, user2);
        return ResponseEntity.ok(messages);
    }
}
