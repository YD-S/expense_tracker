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
import java.util.*;
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

    @SuppressWarnings("unchecked")
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
        Map<String, List<Map<String, Object>>> transactions =
                (Map<String, List<Map<String, Object>>>) responseBody.get("transactions");

        List<Transaction> savedTransactions = new ArrayList<>();
        List<Transaction> newTransactions = new ArrayList<>();

        List<String> allTransactionIds = new ArrayList<>();
        if (transactions.containsKey("booked")) {
            for (Map<String, Object> txnData : transactions.get("booked")) {
                String transactionId = (String) txnData.get("transactionId");
                if (transactionId != null) allTransactionIds.add(transactionId);
            }
        }
        if (transactions.containsKey("pending")) {
            for (Map<String, Object> txnData : transactions.get("pending")) {
                String transactionId = (String) txnData.get("transactionId");
                if (transactionId != null) allTransactionIds.add(transactionId);
            }
        }

        List<String> existingTransactionIds = transactionRepository.findExistingTransactionIds(allTransactionIds);

        if (transactions.containsKey("booked")) {
            for (Map<String, Object> txnData : transactions.get("booked")) {
                String transactionId = (String) txnData.get("transactionId");
                if (transactionId != null && !existingTransactionIds.contains(transactionId)) {
                    Transaction txn = mapToTransactionWithoutBalance(txnData, accountId, connection);
                    if (txn != null) newTransactions.add(txn);
                }
            }
        }
        if (transactions.containsKey("pending")) {
            for (Map<String, Object> txnData : transactions.get("pending")) {
                String transactionId = (String) txnData.get("transactionId");
                if (transactionId != null && !existingTransactionIds.contains(transactionId)) {
                    Transaction txn = mapToTransactionWithoutBalance(txnData, accountId, connection);
                    if (txn != null) newTransactions.add(txn);
                }
            }
        }

        newTransactions.sort(Comparator.comparing(Transaction::getTransactionDate).reversed());

        BigDecimal runningBalance = getCurrentBalanceFromApi(accountId);

        for (Transaction txn : newTransactions) {
            txn.setBalanceAfterTransaction(runningBalance);

            if (txn.getTransactionType() == Transaction.TransactionType.CREDIT) {
                runningBalance = runningBalance.subtract(txn.getAmount());
            } else {
                runningBalance = runningBalance.add(txn.getAmount());
            }

            try {
                savedTransactions.add(transactionRepository.save(txn));
            } catch (Exception e) {
                logger.warning("Failed to save transaction " + txn.getTransactionId() +
                        ": " + e.getMessage() + ". Possibly already exists.");
            }
        }

        return savedTransactions;
    }

    private BigDecimal getCurrentBalanceFromApi(String accountId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authService.getAccessToken());

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://bankaccountdata.gocardless.com/api/v2/accounts/" + accountId + "/balances/",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map responseBody = response.getBody();
            List<Map<String, Object>> balances = (List<Map<String, Object>>) responseBody.get("balances");

            if (balances != null && !balances.isEmpty()) {
                for (Map<String, Object> balance : balances) {
                    Map<String, Object> balanceAmount = (Map<String, Object>) balance.get("balanceAmount");
                    if (balanceAmount != null) {
                        String amountStr = (String) balanceAmount.get("amount");
                        return new BigDecimal(amountStr);
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to fetch current balance for account: " + accountId + ". Error: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    private Transaction mapToTransactionWithoutBalance(Map<String, Object> txnData, String accountId, BankConnection connection) {
        try {
            Map<String, Object> transactionAmount = (Map<String, Object>) txnData.get("transactionAmount");
            String amountStr = transactionAmount != null ? (String) transactionAmount.get("amount") : "0.00";
            String currency = transactionAmount != null ? (String) transactionAmount.get("currency") : "UNKNOWN";

            BigDecimal amount = new BigDecimal(amountStr);
            Transaction.TransactionType type = amount.compareTo(BigDecimal.ZERO) >= 0
                    ? Transaction.TransactionType.CREDIT
                    : Transaction.TransactionType.DEBIT;

            BigDecimal absAmount = amount.abs();

            String bookingDateStr = (String) txnData.get("bookingDate");
            String valueDateStr = (String) txnData.get("valueDate");

            LocalDate bookingDate = bookingDateStr != null
                    ? LocalDate.parse(bookingDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                    : null;
            LocalDate valueDate = valueDateStr != null
                    ? LocalDate.parse(valueDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                    : null;

            LocalDate transactionDate = bookingDate != null ? bookingDate : valueDate;

            Map<String, Object> creditorAccount = (Map<String, Object>) txnData.get("creditorAccount");
            String creditorIban = creditorAccount != null ? (String) creditorAccount.get("iban") : null;

            Map<String, Object> debtorAccount = (Map<String, Object>) txnData.get("debtorAccount");
            String debtorIban = debtorAccount != null ? (String) debtorAccount.get("iban") : null;

            String bankTransactionCode = (String) txnData.get("bankTransactionCode");
            String proprietaryCode = (String) txnData.get("proprietaryBankTransactionCode");

            return Transaction.builder()
                    .transactionId((String) txnData.get("transactionId"))
                    .accountId(accountId)
                    .bankConnection(connection)
                    .amount(absAmount)
                    .currency(currency)
                    .description((String) txnData.get("remittanceInformationUnstructured"))
                    .transactionDate(transactionDate)
                    .bookingDate(bookingDate)
                    .valueDate(valueDate)
                    .creditorName((String) txnData.get("creditorName"))
                    .debtorName((String) txnData.get("debtorName"))
                    .creditorAccount(creditorIban)
                    .debtorAccount(debtorIban)
                    .transactionType(type)
                    .transactionCode(bankTransactionCode)
                    .proprietaryBankTransactionCode(proprietaryCode)
                    .balanceAfterTransaction(null)
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
