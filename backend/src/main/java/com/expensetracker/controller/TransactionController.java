package com.expensetracker.controller;

import com.expensetracker.dto.TransactionDto;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.Users;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Endpoints for managing user transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Sync Transactions",
            description = "Fetch and save transactions for the authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transactions synced successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"message\":\"Transactions synced successfully\",\"transactionCount\":10,\"transactions\":[{...}]}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Failed to sync transactions",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"error\":\"Failed to sync transactions\"}")
                            )
                    )
            }
    )
    @PostMapping("/sync")
    public ResponseEntity<?> syncTransactions(@AuthenticationPrincipal Object principal) {
        try {
            String username = principal instanceof UserDetails
                    ? ((UserDetails) principal).getUsername()
                    : principal.toString();

            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            List<Transaction> transactions = transactionService.fetchAndSaveTransactions(user.getId());

            if (transactions.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No new transactions found"));
            }

            List<TransactionDto> transactionsDto = new ArrayList<>();
            for (Transaction tx : transactions) {
                transactionsDto.add(TransactionMapper.toDto(tx));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Transactions synced successfully",
                    "transactionCount", transactionsDto.size(),
                    "transactions", transactionsDto
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to sync transactions: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Get User Transactions",
            description = "Retrieve all transactions for the authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved transactions",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TransactionDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Failed to retrieve transactions",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = "{\"error\": \"Failed to retrieve transactions\"}")
                            )
                    )
            }
    )
    @GetMapping("/")
    public ResponseEntity<List<TransactionDto>> getUserTransactions(@AuthenticationPrincipal Object principal) {
        try {
            String username = principal instanceof UserDetails
                    ? ((UserDetails) principal).getUsername()
                    : principal.toString();

            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            List<Transaction> transactions = transactionService.getUserTransactions(user.getId());

            List<TransactionDto> transactionDtos = transactions.stream()
                    .sorted(Comparator.comparing(Transaction::getId))
                    .map(TransactionMapper::toDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(transactionDtos);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to retrieve transactions"));
        }
    }

    @Operation(
            summary = "Get User Transactions by Date Range",
            description = "Retrieve transactions for the authenticated user within a specified date range",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved transactions",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TransactionDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Failed to retrieve transactions",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = "{\"error\": \"Failed to retrieve transactions\"}")
                            )
                    )
            }
    )
    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionDto>> getUserTransactionsByDateRange(
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

            List<TransactionDto> transactionDtos = new ArrayList<>();
            for (Transaction tx : transactions) {
                transactionDtos.add(TransactionMapper.toDto(tx));
            }

            return ResponseEntity.ok(transactionDtos);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Get User Transactions by Type",
            description = "Retrieve transactions for the authenticated user filtered by transaction type",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved transactions",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TransactionDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Failed to retrieve transactions",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", example = "{\"error\": \"Failed to retrieve transactions\"}")
                            )
                    )
            }
    )
    @GetMapping("/type/{transactionType}")
    public ResponseEntity<List<TransactionDto>> getUserTransactionsByType(
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

            List<TransactionDto> transactionDtos = new ArrayList<>();
            for (Transaction tx : transactions) {
                transactionDtos.add(TransactionMapper.toDto(tx));
            }

            return ResponseEntity.ok(transactionDtos);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}