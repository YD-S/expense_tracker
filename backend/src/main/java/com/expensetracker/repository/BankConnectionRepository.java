package com.expensetracker.repository;

import com.expensetracker.model.BankConnection;
import com.expensetracker.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankConnectionRepository
        extends JpaRepository<BankConnection, String> {

    List<BankConnection> findByUser(Users user);
    BankConnection findByRequisitionId(String requisitionId);
}
