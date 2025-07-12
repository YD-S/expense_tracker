package com.expensetracker.mapper;


import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.model.Expense;

public class ExpenseMapper {
    public static ExpenseResponse toExpenseResponse(Expense expense) {
        if (expense == null) {
            return null;
        }
        return new ExpenseResponse(expense.getId(),expense.getCategory(), expense.getAmount(), expense.getDescription(), expense.getDate());
    }
}
