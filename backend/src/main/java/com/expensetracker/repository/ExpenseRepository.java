package com.expensetracker.repository;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Boolean existsByNameAndUser(String name, Users user);
    List<Expense> findAllByUserAndCategory(Users user, Category category);
    List<Expense> findAllByUserId(Long userId);
}
