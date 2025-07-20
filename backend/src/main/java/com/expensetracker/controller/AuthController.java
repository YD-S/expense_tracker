package com.expensetracker.controller;

import com.expensetracker.auth.JwtService;
import com.expensetracker.dto.AuthRequest;
import com.expensetracker.dto.AuthResponse;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.RefreshRequest;
import com.expensetracker.model.Users;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Endpoints for user authentication, registration and token management"
)
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthService authService;

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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));

        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database error occurred"));

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication service temporarily unavailable"));
        }
    }

    @Operation(
            summary = "User registration",
            description = "Register a new user and return JWT tokens",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successful registration", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class)
                    )),
                    @ApiResponse(responseCode = "409", description = "User already exists", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "object", example = "{\"error\": \"User already exists\"}")
                    )),
                    @ApiResponse(responseCode = "500", description = "Registration service temporarily unavailable", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "object", example = "{\"error\": \"Registration service temporarily unavailable\"}")
                    ))
            }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            authService.register(request);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Username or Email already exists"));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Username, email, and password must not be null"));

        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration service temporarily unavailable"));
        }
    }

    @Operation(
            summary = "Refresh JWT token",
            description = "Refresh access token using a valid refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class)
                    )),
                    @ApiResponse(responseCode = "401", description = "Invalid refresh token", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "object", example = "{\"error\": \"Invalid refresh token\"}")
                    )),
                    @ApiResponse(responseCode = "500", description = "Token refresh service temporarily unavailable", content = @Content(
                            mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "object", example = "{\"error\": \"Token refresh service temporarily unavailable\"}")
                    ))
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshRequest request) {
        try {
                if (!jwtService.isRefreshTokenValid(request.refreshToken())) {
                    throw new BadCredentialsException("Invalid refresh token");
                }

                String username = jwtService.extractUsername(request.refreshToken());
                Users user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new BadCredentialsException("User not found"));
                JwtService.TokenPair tokens = jwtService.generateTokenPair(user);

                return ResponseEntity.ok(new AuthResponse(
                        tokens.accessToken(),
                        tokens.refreshToken()
                ));
            } catch ( Exception e) {
                System.err.println("Token refresh error: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, null));
            }
        }
}