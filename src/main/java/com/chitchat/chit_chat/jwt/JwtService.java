package com.chitchat.chit_chat.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import com.chitchat.chit_chat.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Extracts the userId claim from the JWT token and returns it as a Long.
    // It calls the extractClaim method with the provided token and a lambda function to extract the userId claim as a String.
    // If userId exists, it converts the String to Long. If not, it returns null.
    public Long extractUserId(String jwtToken) {
        String userIdString = extractClaim(jwtToken, claims -> claims.get("userId", String.class));

        return userIdString != null ? Long.parseLong(userIdString) : null;
    }

    // A method that extracts any claim from the JWT, using a functional interface (Function).
    // First calls the extractAllClaims method to parse the JWT and get all the claims.
    // Then applies the claimsResolver function to extract a specific claim (e.g., userId, username, etc.).
    private <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwtToken);

        return claimsResolver.apply(claims);
    }

    // Parses the JWT and extracts all claims from it.
    public Claims extractAllClaims(String jwtToken) {
        return Jwts.parser() // Uses the Jwts.parser() to create a JWT parser
                .verifyWith(getSignInKey()) // verifyWith(getSignInKey()): This verifies the JWT using the getSignInKey(), ensuring it was signed correctly.
                .build()
                .parseSignedClaims(jwtToken) // This parses the signed JWT.
                .getPayload(); // Extracts the claims (payload) from the JWT.
    }

    // Generates a SecretKey for signing and verifying the JWT using HMAC SHA algorithms.
    public SecretKey getSignInKey() {
        // Uses the secretKey (loaded from the configuration) to create a SecretKey using the Keys.hmacShaKeyFor() method.
        // This key is used to sign and verify the JWT.
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    // main one used to generate the token.
    public String generateToken(Map<String, Object> extraClaims, User user) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("userId", user.getId());

        return Jwts.builder()
                .claims(claims) // A Map<String, Object> is created, and it can be customized with extra claims. By default, it includes the userId.
                .subject(user.getUsername()) // The subject of the JWT is set to the user's username
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey()) // The token is signed using the SecretKey from getSignInKey().
                .compact(); // This serializes the JWT to a compact, URL-safe string.
    }

    public boolean isValidToken(String jwtToken, User user) {
        final Long userIdFromToken = extractUserId(jwtToken);

        final Long userId = user.getId();

        return (userIdFromToken != null && userIdFromToken.equals(userId) && !isTokenExpired(jwtToken));
    }

    public boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date());
    }

    private Date extractExpiration(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration);
    }
}
