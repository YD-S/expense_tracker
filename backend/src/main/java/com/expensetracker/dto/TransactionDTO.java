package com.expensetracker.dto;

import com.expensetracker.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private String transactionId;
    private String bankId;
    private String description;
    private long amountInCents;
    private LocalDate date;
    private Category category;
}