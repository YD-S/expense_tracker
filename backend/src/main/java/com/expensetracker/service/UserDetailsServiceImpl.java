package com.expensetracker.service;

import com.expensetracker.dto.AuthRequest;
import com.expensetracker.model.Users;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.serializer.UserSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserSerializer userSerializer;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Users.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .build();
        return UserSerializer.mapToUserDetails(user);
    }

    public UserDetails userExists(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return UserSerializer.mapToUserDetails(user);
    }

    public UserDetails saveUser(AuthRequest request) {
        Users user = Users.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .build();

        System.out.println("Saving user: " + user.getUsername() + ", email: " + user.getEmail());

        Users savedUser = userRepository.save(user);
        return UserSerializer.mapToUserDetails(savedUser);
    }
}