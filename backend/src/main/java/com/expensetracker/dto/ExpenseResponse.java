package com.expensetracker.dto;

import com.expensetracker.model.Category;
import java.time.LocalDateTime;

public record ExpenseResponse(Long id, Category category, Long Amount, String Description, LocalDateTime date) {}
