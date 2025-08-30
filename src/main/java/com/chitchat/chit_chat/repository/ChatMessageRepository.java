package com.chitchat.chit_chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.chitchat.chit_chat.model.ChatMessage;

public class ChatMessageRepository implements JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.timestamp ASC")
    public List<ChatMessage> findPrivateMessagesBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);
}
