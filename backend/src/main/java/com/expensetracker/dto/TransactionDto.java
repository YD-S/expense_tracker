package com.expensetracker.dto;

import com.expensetracker.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionDto(
        Long id,
        String transactionId,
        String accountId,
        BigDecimal amount,
        String currency,
        String description,
        LocalDate transactionDate,
        LocalDate bookingDate,
        LocalDate valueDate,
        String creditorName,
        String debtorName,
        String creditorAccount,
        String debtorAccount,
        String transactionCode,
        String proprietaryBankTransactionCode,
        BigDecimal balanceAfterTransaction,
        Transaction.TransactionType transactionType,
        String institutionId,
        String bankConnectionStatus,
        String username,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}