package com.chitchat.chit_chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chitchat.chit_chat.model.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {
    public boolean existsByUsername(String username);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.online = :isOnline WHERE u.username = :username")
    public void updateUserOnlineStatus(@Param("username")String username, @Param("isOnline") boolean isOnline);
}
