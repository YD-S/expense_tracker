package com.expensetracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username",nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "email",unique = true , nullable = false, length = 20)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(length = 1000)
    private String refreshToken;
}