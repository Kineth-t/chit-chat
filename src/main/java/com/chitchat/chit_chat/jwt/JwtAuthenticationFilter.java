package com.chitchat.chit_chat.jwt;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.chitchat.chit_chat.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;

public class JwtAuthenticationFilter extends OncePerRequestFilter{
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        Long userId = null;
        String jwtToken = null;

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer")) {
            jwtToken = authHeader.substring(7);
        }

        // If JWT token is null, check the cookie
        if(jwtToken == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie: cookies) {
                    if("JWT".equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if(jwtToken == null) {
            // If still no token, just pass the request down the filter chain (unauthenticated).
            filterChain.doFilter(request, response);
            return;
        }

        // Use the JwtService to decode the JWT and extract the user ID.
        userId = jwtService.extractUserId(jwtToken);

        // If user is not already authenticated and token is valid, fetch the user from the database using the ID.
        if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

            if(jwtService.isValidToken(jwtToken, userDetails)) {
                // Spring Security authentication token object
                // This object represents a successfully authenticated user and can be stored in the security context.
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

                // Attach request-specific details (IP, session, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Mark the request as authenticated in the security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
    
}
