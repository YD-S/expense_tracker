package com.expensetracker.service;

import com.expensetracker.model.BankConnection;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.Users;
import com.expensetracker.repository.BankConnectionRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final BankConnectionRepository bankConnectionRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final GoCardlessAuthService authService;

    private static final Logger logger = Logger.getLogger(TransactionService.class.getName());

    public List<Transaction> fetchAndSaveTransactions(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BankConnection> connections = bankConnectionRepository.findByUserAndStatus(user, "LINKED");
        List<Transaction> allTransactions = new ArrayList<>();

        for (BankConnection connection : connections) {
            try {
                List<Transaction> transactions = fetchTransactionsForConnection(connection);
                allTransactions.addAll(transactions);
            } catch (Exception e) {
                logger.warning("Failed to fetch transactions for connection: " + connection.getRequisitionId() +
                        ". Error: " + e.getMessage());
            }
        }

        return allTransactions;
    }

    private List<Transaction> fetchTransactionsForConnection(BankConnection connection) {
        List<String> accountIds = getAccountsForRequisition(connection.getRequisitionId());
        List<Transaction> allTransactions = new ArrayList<>();

        for (String accountId : accountIds) {
            List<Transaction> transactions = fetchTransactionsForAccount(accountId, connection);
            allTransactions.addAll(transactions);
        }

        return allTransactions;
    }

    private List<String> getAccountsForRequisition(String requisitionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://bankaccountdata.gocardless.com/api/v2/requisitions/" + requisitionId + "/",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        return (List<String>) response.getBody().get("accounts");
    }

    private List<Transaction> fetchTransactionsForAccount(String accountId, BankConnection connection) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://bankaccountdata.gocardless.com/api/v2/accounts/" + accountId + "/transactions/",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        Map<String, List<Map<String, Object>>> transactions = (Map<String, List<Map<String, Object>>>) responseBody.get("transactions");

        List<Transaction> savedTransactions = new ArrayList<>();

        if (transactions.containsKey("booked")) {
            for (Map<String, Object> txnData : transactions.get("booked")) {
                Transaction transaction = mapToTransaction(txnData, accountId, connection);
                if (transaction != null) {
                    if (transactionRepository.findByTransactionId(transaction.getTransactionId()).isEmpty()) {
                        savedTransactions.add(transactionRepository.save(transaction));
                    }
                }
            }
        }

        if (transactions.containsKey("pending")) {
            for (Map<String, Object> txnData : transactions.get("pending")) {
                Transaction transaction = mapToTransaction(txnData, accountId, connection);
                if (transaction != null) {
                    if (transactionRepository.findByTransactionId(transaction.getTransactionId()).isEmpty()) {
                        savedTransactions.add(transactionRepository.save(transaction));
                    }
                }
            }
        }

        return savedTransactions;
    }

    private Transaction mapToTransaction(Map<String, Object> txnData, String accountId, BankConnection connection) {
        try {
            Map<String, Object> transactionAmount = (Map<String, Object>) txnData.get("transactionAmount");
            String amountStr = (String) transactionAmount.get("amount");
            String currency = (String) transactionAmount.get("currency");

            BigDecimal amount = new BigDecimal(amountStr);
            Transaction.TransactionType type = amount.compareTo(BigDecimal.ZERO) >= 0 ?
                    Transaction.TransactionType.CREDIT : Transaction.TransactionType.DEBIT;

            String creditorAccount = null;
            Map<String, Object> creditorAccountData = (Map<String, Object>) txnData.get("creditorAccount");
            if (creditorAccountData != null) {
                creditorAccount = (String) creditorAccountData.get("iban");
                if (creditorAccount == null) {
                    creditorAccount = (String) creditorAccountData.get("bban");
                }
            }

            String debtorAccount = null;
            Map<String, Object> debtorAccountData = (Map<String, Object>) txnData.get("debtorAccount");
            if (debtorAccountData != null) {
                debtorAccount = (String) debtorAccountData.get("iban");
                if (debtorAccount == null) {
                    debtorAccount = (String) debtorAccountData.get("bban");
                }
            }

            BigDecimal balanceAfterTransaction = null;
            Map<String, Object> balanceData = (Map<String, Object>) txnData.get("balanceAfterTransaction");
            if (balanceData != null) {
                String balanceStr = (String) balanceData.get("amount");
                if (balanceStr != null) {
                    try {
                        balanceAfterTransaction = new BigDecimal(balanceStr);
                    } catch (NumberFormatException e) {
                        logger.warning("Invalid balance amount format: " + balanceStr);
                        balanceAfterTransaction = null; // or handle with a default value if needed
                    }
                }
            }

            String bankTransactionCode = (String) txnData.get("bankTransactionCode");

            String proprietaryBankTransactionCode = (String) txnData.get("proprietaryBankTransactionCode");

            return Transaction.builder()
                    .transactionId((String) txnData.get("transactionId"))
                    .accountId(accountId)
                    .bankConnection(connection)
                    .amount(amount.abs())
                    .currency(currency)
                    .description((String) txnData.get("remittanceInformationUnstructured"))
                    .transactionDate(LocalDate.parse((String) txnData.get("bookingDate"), DateTimeFormatter.ISO_LOCAL_DATE))
                    .bookingDate(txnData.get("bookingDate") != null ?
                            LocalDate.parse((String) txnData.get("bookingDate"), DateTimeFormatter.ISO_LOCAL_DATE) : null)
                    .valueDate(txnData.get("valueDate") != null ?
                            LocalDate.parse((String) txnData.get("valueDate"), DateTimeFormatter.ISO_LOCAL_DATE) : null)
                    .creditorName((String) txnData.get("creditorName"))
                    .debtorName((String) txnData.get("debtorName"))
                    .creditorAccount(creditorAccount)
                    .debtorAccount(debtorAccount)
                    .transactionCode(bankTransactionCode)
                    .proprietaryBankTransactionCode(proprietaryBankTransactionCode)
                    .balanceAfterTransaction(balanceAfterTransaction)
                    .transactionType(type)
                    .build();
        } catch (Exception e) {
            logger.warning("Failed to map transaction data: " + e.getMessage());
            return null;
        }
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getUserTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    public List<Transaction> getUserTransactionsByType(Long userId, Transaction.TransactionType type) {
        return transactionRepository.findByUserIdAndTransactionType(userId, type);
    }
}