package com.expensetracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bank_connection_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transaction_bank_connection")
    )
    private BankConnection bankConnection;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Column(name = "value_date")
    private LocalDate valueDate;

    @Column(name = "creditor_name", length = 255)
    private String creditorName;

    @Column(name = "debtor_name", length = 255)
    private String debtorName;

    @Column(name = "creditor_account", length = 255)
    private String creditorAccount;

    @Column(name = "debtor_account", length = 255)
    private String debtorAccount;

    @Column(name = "transaction_code", length = 50)
    private String transactionCode;

    @Column(name = "proprietary_bank_transaction_code", length = 50)
    private String proprietaryBankTransactionCode;

    @Column(name = "balance_after_transaction", precision = 19, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransactionType {
        DEBIT, CREDIT
    }
}