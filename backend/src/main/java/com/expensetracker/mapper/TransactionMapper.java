package com.expensetracker.mapper;

import com.expensetracker.dto.TransactionDto;
import com.expensetracker.model.BankConnection;
import com.expensetracker.model.Transaction;

public class TransactionMapper {

    public static TransactionDto toDto(Transaction tx) {
        return new TransactionDto(
                tx.getId(),
                tx.getTransactionId(),
                tx.getAccountId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getDescription(),
                tx.getTransactionDate(),
                tx.getBookingDate(),
                tx.getValueDate(),
                tx.getCreditorName(),
                tx.getDebtorName(),
                tx.getCreditorAccount(),
                tx.getDebtorAccount(),
                tx.getTransactionCode(),
                tx.getProprietaryBankTransactionCode(),
                tx.getBalanceAfterTransaction(),
                tx.getTransactionType(),
                tx.getBankConnection().getInstitutionId(),
                tx.getBankConnection().getStatus(),
                tx.getBankConnection().getUser().getUsername(),
                tx.getCreatedAt(),
                tx.getUpdatedAt()
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
