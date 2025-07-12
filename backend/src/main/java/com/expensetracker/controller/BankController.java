package com.expensetracker.controller;

import com.expensetracker.dto.BankDTO;
import com.expensetracker.model.BankConnection;
import com.expensetracker.model.Users;
import com.expensetracker.repository.BankConnectionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.BankInstitutionService;
import com.expensetracker.service.RequisitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banking")
@RequiredArgsConstructor
public class BankController {
    private final BankInstitutionService bankInstitutionService;
    private final UserRepository userRepository;
    private final RequisitionService requisitionService;
    private final BankConnectionRepository bankConnectionRepo;

    @GetMapping("/institutions")
    public ResponseEntity<List<BankDTO>> getInstitutions(@RequestParam String country) {
        return ResponseEntity.ok(bankInstitutionService.getSupportedBanks(country));
    }

    @GetMapping("/institutions/{bankId}")
    public ResponseEntity<BankDTO> getInstitutionDetails(@PathVariable String bankId) {
        return ResponseEntity.ok(bankInstitutionService.getBankDetails(bankId));
    }

    @PostMapping("/institutions/{bankId}/connect")
    public ResponseEntity<?> connectBankAccount(
            @PathVariable String bankId,
            @AuthenticationPrincipal Object principal) {

        try {
            // Resolve user safely
            String username = principal instanceof UserDetails
                    ? ((UserDetails) principal).getUsername()
                    : principal.toString();

            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            // Create connection record first
            BankConnection connection = BankConnection.builder()
                    .user(user)
                    .institutionId(bankId)
                    .requisitionId(generateRequisitionId(user))
                    .status("CREATED")
                    .build();

            bankConnectionRepo.save(connection);

            // Initiate requisition
            Map<String, String> connectionInfo = requisitionService.createRequisition(
                    bankId,
                    connection.getRequisitionId()
            );

            // Update with requisition ID
            connection.setRequisitionId(connectionInfo.get("requisitionId"));
            bankConnectionRepo.save(connection);

            return ResponseEntity.ok(Map.of(
                    "authorizationUrl", connectionInfo.get("link"),
                    "requisitionId", connection.getRequisitionId()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Connection failed: " + e.getMessage()));
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam String ref,
            @RequestParam(required = false) String error) {

        try {
            BankConnection connection = bankConnectionRepo.findByRequisitionId(ref);

            if (error != null) {
                connection.setStatus("ERROR");
                bankConnectionRepo.save(connection);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bank connection failed: " + error));
            }

            connection.setStatus("LINKED");
            bankConnectionRepo.save(connection);

            return ResponseEntity.ok(Map.of(
                    "message", "Bank account connected successfully",
                    "requisitionId", ref
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Callback processing failed"));
        }
    }

    private String generateRequisitionId(Users user) {
        return "requisition-" + user.getId() + "-" + System.currentTimeMillis();
    }
}
