package com.expensetracker.controller;

import com.expensetracker.model.Users;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(
        name = "Expense Management",
        description = "Endpoints for managing expenses, including creation, retrieval, and deletion of expense records."
)
public class ExpenseController {
    private  final ExpenseService expenseService;
    private final UserRepository userRepository;

    @Operation(
        summary = "Get All Expenses for User",
        description = "Retrieves all expenses associated with the authenticated user.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved expenses"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request"
            )
        }
    )
    @GetMapping("/")
    public ResponseEntity<?> getAllExpensesForUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = principal instanceof UserDetails ? ((UserDetails) principal).getUsername() : principal.toString();

            Users user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found: " + username));

            return new ResponseEntity<>(expenseService.getAllExpensesByUser(user), HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
