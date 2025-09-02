package com.chitchat.chit_chat.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.chitchat.chit_chat.model.User;
import com.chitchat.chit_chat.repository.UserRepository;

// Custom implementation of the Spring Security UserDetailsService interface
// It's part of Spring Boot authentication setup, where user information is loaded from a database for login and security purposes.

public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Converts own User object to a Spring Security User object.
        return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(Collections.emptyList())
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
    }
    
}
