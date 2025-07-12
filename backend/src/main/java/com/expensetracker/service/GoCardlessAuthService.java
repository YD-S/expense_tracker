package com.expensetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoCardlessAuthService {
    private final RestTemplate restTemplate;

    @Value("${gocardless.secret.id}")
    private String secretId;

    @Value("${gocardless.secret.key}")
    private String secretKey;

    private String accessToken;
    private LocalDateTime tokenExpiry;

    public String getAccessToken() {
        if (accessToken == null || LocalDateTime.now().isAfter(tokenExpiry)) {
            refreshToken();
        }
        return accessToken;
    }

    private void refreshToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> request = Map.of(
                "secret_id", secretId,
                "secret_key", secretKey
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://bankaccountdata.gocardless.com/api/v2/token/new/",
                new HttpEntity<>(request, headers),
                Map.class);

        this.accessToken = (String) response.getBody().get("access");
        this.tokenExpiry = LocalDateTime.now().plusMinutes(23);
    }
}
