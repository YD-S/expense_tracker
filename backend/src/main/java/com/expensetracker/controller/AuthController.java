package com.expensetracker.controller;

import com.expensetracker.auth.JwtService;
import com.expensetracker.dto.AuthRequest;
import com.expensetracker.dto.AuthResponse;
import com.expensetracker.dto.RefreshRequest;
import com.expensetracker.service.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Operation(
            summary = "User login",
            description = "Authenticate user and return JWT tokens",
            responses = {
                    @ApiResponse (responseCode = "200", description = "Successful login", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "object", example = "{\"error\": \"Invalid email or password\"}")
                    )),
                    @ApiResponse(responseCode = "500", description = "Authentication service temporarily unavailable", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "object", example = "{\"error\": \"Authentication service temporarily unavailable\"}")
                    )),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "object", example = "{\"error\": \"User not found\"}")
                    ))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
            JwtService.TokenPair tokens = jwtService.generateTokenPair(userDetails);

            return ResponseEntity.ok(new AuthResponse(
                    tokens.accessToken(),
                    tokens.refreshToken()
            ));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));

        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database error occurred"));

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication service temporarily unavailable"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshRequest request) {
        if (!jwtService.isRefreshTokenValid(request.refreshToken())) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(request.refreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        JwtService.TokenPair tokens = jwtService.generateTokenPair(userDetails);

        return ResponseEntity.ok(new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken()
        ));
    }
}