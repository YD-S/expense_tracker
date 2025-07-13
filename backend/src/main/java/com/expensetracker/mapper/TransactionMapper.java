package com.expensetracker.mapper;

import com.expensetracker.dto.TransactionDto;
import com.expensetracker.model.BankConnection;
import com.expensetracker.model.Transaction;

public class TransactionMapper {

    public static TransactionDto toDto(Transaction tx) {
        if (tx == null) {
            return null;
        }

        BankConnection bankConnection = tx.getBankConnection();
        String institutionId = bankConnection != null ? bankConnection.getInstitutionId() : null;
        String bankConnectionStatus = bankConnection != null ? bankConnection.getStatus() : null;

        String username = null;
        if (bankConnection != null && bankConnection.getUser() != null) {
            username = bankConnection.getUser().getUsername();
        }

        return new TransactionDto(
                tx.getId(),
                tx.getTransactionId() != null ? tx.getTransactionId() : null,
                tx.getAccountId() != null ? tx.getAccountId() : null,
                tx.getAmount() != null ? tx.getAmount() : null,
                tx.getCurrency() != null ? tx.getCurrency() : null,
                tx.getDescription() != null ? tx.getDescription() : null,
                tx.getTransactionDate() != null ? tx.getTransactionDate() : null,
                tx.getBookingDate() != null ? tx.getBookingDate() : null,
                tx.getValueDate() != null ? tx.getValueDate() : null,
                tx.getCreditorName() != null ? tx.getCreditorName() : null,
                tx.getDebtorName() != null ? tx.getDebtorName() : null,
                tx.getCreditorAccount() != null ? tx.getCreditorAccount() : null,
                tx.getDebtorAccount() != null ? tx.getDebtorAccount() : null,
                tx.getTransactionCode() != null ? tx.getTransactionCode() : null,
                tx.getProprietaryBankTransactionCode() != null ? tx.getProprietaryBankTransactionCode() : null,
                tx.getBalanceAfterTransaction() != null ? tx.getBalanceAfterTransaction() : null,
                tx.getTransactionType() != null ? tx.getTransactionType() : null,
                institutionId,
                bankConnectionStatus,
                username,
                tx.getCreatedAt() != null ? tx.getCreatedAt() : null,
                tx.getUpdatedAt() != null ? tx.getUpdatedAt() : null
        );
    }

    public static Transaction toEntity(TransactionDto dto, BankConnection bankConnection) {
        return Transaction.builder()
                .id(dto.id())
                .transactionId(dto.transactionId())
                .accountId(dto.accountId())
                .amount(dto.amount())
                .currency(dto.currency())
                .description(dto.description())
                .transactionDate(dto.transactionDate())
                .bookingDate(dto.bookingDate())
                .valueDate(dto.valueDate())
                .creditorName(dto.creditorName())
                .debtorName(dto.debtorName())
                .creditorAccount(dto.creditorAccount())
                .debtorAccount(dto.debtorAccount())
                .transactionCode(dto.transactionCode())
                .proprietaryBankTransactionCode(dto.proprietaryBankTransactionCode())
                .balanceAfterTransaction(dto.balanceAfterTransaction())
                .transactionType(dto.transactionType())
                .bankConnection(bankConnection)
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .build();
    }
}
