package com.expensetracker.service;

import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.mapper.ExpenseMapper;
import com.expensetracker.model.Expense;
import com.expensetracker.model.Users;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public List<ExpenseResponse> getAllExpensesByUser(Users user) {
        List<Expense> expenses = expenseRepository.findAllByUserId(user.getId());

        List<ExpenseResponse> expenseResponses = new ArrayList<>();
        for (Expense expense : expenses) {
            ExpenseResponse expenseResponse = ExpenseMapper.toExpenseResponse(expense);
            expenseResponses.add(expenseResponse);
        }

        if (expenses.isEmpty()) {
            throw new RuntimeException("No expenses found for user: " + user.getUsername());
        }
        return expenseResponses;
    }
}
