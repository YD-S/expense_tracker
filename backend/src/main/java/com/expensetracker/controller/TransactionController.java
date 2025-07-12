package com.expensetracker.controller;

import com.expensetracker.dto.TransactionDto;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.Users;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping("/sync")
    public ResponseEntity<?> syncTransactions(@AuthenticationPrincipal Object principal) {
        try {
            String username = principal instanceof UserDetails
                    ? ((UserDetails) principal).getUsername()
                    : principal.toString();

            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            List<Transaction> transactions = transactionService.fetchAndSaveTransactions(user.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Transactions synced successfully",
                    "transactionCount", transactions.size(),
                    "transactions", transactions
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to sync transactions: " + e.getMessage()));
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<TransactionDto>> getUserTransactions(@AuthenticationPrincipal Object principal) {
        try {
            String username = principal instanceof UserDetails
                    ? ((UserDetails) principal).getUsername()
                    : principal.toString();

            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            List<Transaction> transactions = transactionService.getUserTransactions(user.getId());

            List<TransactionDto> transactionDtos = new ArrayList<>();

            for( Transaction tx : transactions) {
                transactionDtos.add(TransactionMapper.toDto(tx));
            }

            return ResponseEntity.ok(transactionDtos);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Transaction>> getUserTransactionsByDateRange(
            @AuthenticationPrincipal Object principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            String username = principal instanceof UserDetails
                    ? ((UserDetails) principal).getUsername()
                    : principal.toString();

            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            List<Transaction> transactions = transactionService.getUserTransactionsByDateRange(
                    user.getId(), startDate, endDate);
            return ResponseEntity.ok(transactions);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/type/{transactionType}")
    public ResponseEntity<List<Transaction>> getUserTransactionsByType(
            @AuthenticationPrincipal Object principal,
            @PathVariable Transaction.TransactionType transactionType) {

        try {
            String username = principal instanceof UserDetails
                    ? ((UserDetails) principal).getUsername()
                    : principal.toString();

            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            List<Transaction> transactions = transactionService.getUserTransactionsByType(
                    user.getId(), transactionType);
            return ResponseEntity.ok(transactions);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}