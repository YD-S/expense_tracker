package com.expensetracker.repository;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.BankConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByBankConnection(BankConnection bankConnection);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.bankConnection WHERE t.bankConnection.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.bankConnection.user.id = :userId ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserIdOrderByTransactionDateDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.bankConnection.user.id = :userId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.bankConnection.user.id = :userId " +
            "AND t.transactionType = :transactionType " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndTransactionType(@Param("userId") Long userId,
                                                     @Param("transactionType") Transaction.TransactionType transactionType);

    List<Transaction> findByAccountIdAndTransactionDateBetween(String accountId, LocalDate startDate, LocalDate now);


    @Query("SELECT t.transactionId FROM Transaction t WHERE t.transactionId IN :transactionIds")
    List<String> findExistingTransactionIds(@Param("transactionIds") List<String> transactionIds);
}