package com.chitchat.chit_chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chitchat.chit_chat.model.ChatMessage;

public class ChatMessageRepository implements JpaRepository<ChatMessage, Long> {
    
}
