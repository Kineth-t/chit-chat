package com.chitchat.chit_chat.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.chitchat.chit_chat.dto.LoginRequestDTO;
import com.chitchat.chit_chat.dto.LoginResponseDTO;
import com.chitchat.chit_chat.dto.RegisterRequestDTO;
import com.chitchat.chit_chat.dto.UserDTO;
import com.chitchat.chit_chat.jwt.JwtAuthenticationFilter;
import com.chitchat.chit_chat.jwt.JwtService;
import com.chitchat.chit_chat.model.User;
import com.chitchat.chit_chat.repository.UserRepository;

public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public UserDTO signup(RegisterRequestDTO registerRequestDTO) {
        if(userRepository.findByUsername(registerRequestDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        User user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setEmail(registerRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));

        User savedUser = userRepository.save(user);
        return convertToUserDTO(savedUser);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByUsername(loginRequestDTO.getUsername())
            .orElseThrow(() -> new RuntimeException("Username not found"));

        // Uses Spring Security's AuthenticationManager to validate the credentials.
        // If the password is incorrect, Spring will throw an AuthenticationException (like BadCredentialsException), which you might want to handle better (e.g. return a 401 response).
        // Behind the scenes:
        // - UsernamePasswordAuthenticationToken is passed to the authentication manager.
        // - The manager uses a configured UserDetailsService (usually hooked to your UserRepository) to load user data and compare passwords (with PasswordEncoder.matches()).
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword()));

        // After successful authentication, it generates a JWT for the user.
        String jwtToken = jwtService.generateToken(user);

        return LoginResponseDTO.builder()
                .token(jwtToken)
                .userDTO(convertToUserDTO(user))
                .build();
    }

    public ResponseEntity<String> logout() {
        ResponseCookie responseCookie = ResponseCookie.from("JWT", "")
                                                        .httpOnly(true)
                                                        .secure(true)
                                                        .path("/")
                                                        .maxAge(0)
                                                        .sameSite("Strict")
                                                        .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body("Logged out succesfully");
    }

    public Map<String, Object> getOnlineUsers() {
        List<User> userList = userRepository.findByIsOnlineTrue();
        Map<String, Object> onlineUsers = userList.stream().collect(Collectors.toMap(User::getUsername, this::convertToUserDTO));

        return onlineUsers;
    }

    public UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());

        return userDTO;
    }
}
