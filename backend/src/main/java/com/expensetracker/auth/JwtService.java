package com.expensetracker.auth;

import com.expensetracker.model.Users;
import com.expensetracker.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserRepository userRepository;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public TokenPair generateTokenPair(Users user) {
        String accessToken = buildToken(user, accessTokenExpiration);
        String refreshToken = buildToken(user, refreshTokenExpiration);

        userRepository.findByUsername(user.getUsername())
                .ifPresent(usr -> {
                    usr.setRefreshToken(refreshToken);
                    userRepository.save(usr);
                });

        return new TokenPair(accessToken, refreshToken);
    }

    private String buildToken(Users user, long expiration) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            String username = extractUsername(refreshToken);
            return userRepository.findByUsername(username)
                    .map(user -> refreshToken.equals(user.getRefreshToken()))
                    .orElse(false);
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    public boolean isTokenValid(String token, Users user) {
        try {
            String username = extractUsername(token);
            return username.equals(user.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}