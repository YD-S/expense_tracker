package com.expensetracker.auth;

import com.expensetracker.model.Users;
import com.expensetracker.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull  HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("JwtAuthenticationFilter called for URL: {}", request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No valid Authorization header, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (SignatureException e) {
            log.debug("JWT signature validation failed: {}", e.getMessage());
            request.setAttribute("jwtError", "INVALID_SIGNATURE");
            request.setAttribute("jwtErrorMessage", "JWT signature is invalid");
            filterChain.doFilter(request, response);
            return;
        } catch (MalformedJwtException e) {
            log.debug("JWT is malformed: {}", e.getMessage());
            request.setAttribute("jwtError", "MALFORMED_JWT");
            request.setAttribute("jwtErrorMessage", "JWT token is malformed");
            filterChain.doFilter(request, response);
            return;
        } catch (ExpiredJwtException e) {
            log.debug("JWT is expired: {}", e.getMessage());
            request.setAttribute("jwtError", "EXPIRED_JWT");
            request.setAttribute("jwtErrorMessage", "JWT token has expired");
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            log.debug("JWT parsing failed: {}", e.getMessage());
            request.setAttribute("jwtError", "INVALID_JWT");
            request.setAttribute("jwtErrorMessage", "JWT token is invalid");
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                Users user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username));

                if (jwtService.isTokenValid(jwt, user)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                log.debug("Authentication failed: {}", e.getMessage());
                request.setAttribute("jwtError", "AUTHENTICATION_FAILED");
                request.setAttribute("jwtErrorMessage", "Authentication failed");
                filterChain.doFilter(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}