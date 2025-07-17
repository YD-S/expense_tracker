package com.expensetracker.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        log.info("JwtAuthEntryPoint called for URL: {}", request.getRequestURI());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errorCode;
        String errorMessage;

        String jwtError = (String) request.getAttribute("jwtError");
        String jwtErrorMessage = (String) request.getAttribute("jwtErrorMessage");

        if (jwtError != null && jwtErrorMessage != null) {
            errorCode = jwtError;
            errorMessage = jwtErrorMessage;
        } else {
            errorCode = "MISSING_TOKEN";
            errorMessage = "JWT token is missing";
        }

        String jsonResponse = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\"}",
                errorCode,
                errorMessage
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();

    }
}