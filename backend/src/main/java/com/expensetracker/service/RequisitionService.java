package com.expensetracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RequisitionService {
    private final RestTemplate restTemplate;
    private final GoCardlessAuthService authService;

    @Value("${gocardless.redirect.url}")
    private String redirectUrl;

    public Map<String, String> createRequisition(String institutionId, String reference) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> request = Map.of(
                "redirect", redirectUrl,
                "institution_id", institutionId,
                "reference", reference,
                "user_language", "ES"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://bankaccountdata.gocardless.com/api/v2/requisitions/",
                new HttpEntity<>(request, headers),
                Map.class);

        Map<String, String> result = new HashMap<>();
        result.put("link", (String) response.getBody().get("link"));
        result.put("requisitionId", (String) response.getBody().get("id"));

        return result;
    }
}