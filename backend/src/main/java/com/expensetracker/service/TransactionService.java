package com.expensetracker.service;

import com.expensetracker.dto.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final RestTemplate restTemplate;
    private final GoCardlessAuthService authService;

    public List getTransactions(String accountId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://bankaccountdata.gocardless.com/api/v2/accounts/" + accountId + "/transactions/",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        Map body = response.getBody();
        return parseTransactions(body);
    }

    private List<TransactionDTO> parseTransactions(Map<String, List<Map<String, Object>>> data) {
        List<TransactionDTO> transactions = new ArrayList<>();

        Stream.concat(
                data.get("booked").stream(),
                data.get("pending").stream()
        ).forEach(tx -> {
            TransactionDTO transaction = new TransactionDTO();
            transaction.setTransactionId((String) tx.get("transactionId"));
            transaction.setAmountInCents(Long.parseLong((String) ((Map) tx.get("transactionAmount")).get("amount")));
            transaction.setDate(LocalDate.parse((String) tx.get("bookingDate")));
            transaction.setDescription((String) tx.get("remittanceInformationUnstructured"));
            transactions.add(transaction);
        });

        return transactions;
    }
}