package com.expensetracker.service;

import com.expensetracker.dto.BankDTO;
import com.expensetracker.model.BankConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BankInstitutionService {
    private final RestTemplate restTemplate;
    private final GoCardlessAuthService authService;

    public List<BankDTO> getSupportedBanks(String countryCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());

        ResponseEntity<BankDTO[]> response = restTemplate.exchange(
                "https://bankaccountdata.gocardless.com/api/v2/institutions/?country=" + countryCode,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                BankDTO[].class);

        return Arrays.asList(response.getBody());
    }

    public BankDTO getBankDetails(String bankId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authService.getAccessToken());

            ResponseEntity<BankDTO> response = restTemplate.exchange(
                    "https://bankaccountdata.gocardless.com/api/v2/institutions/" + bankId + "/",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    BankDTO.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to fetch bank details: " + e.getMessage());
        }
    }

    public Map<String, Object> getTransactions(List<BankConnection> connections){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://bankaccountdata.gocardless.com/api/v2/transactions/",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        return response.getBody();
    }
}