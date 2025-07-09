package com.expensetracker.controller;

import com.expensetracker.dto.BankDTO;
import com.expensetracker.service.BankInstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banking")
@RequiredArgsConstructor
public class BankController {
    private final BankInstitutionService bankInstitutionService;

    @GetMapping("/institutions")
    public ResponseEntity<List<BankDTO>> getInstitutions(@RequestParam String country) {
        return ResponseEntity.ok(bankInstitutionService.getSupportedBanks(country));
    }

    @GetMapping("/institutions/{bankId}")
    public ResponseEntity<BankDTO> getInstitutionDetails(@PathVariable String bankId) {
        return ResponseEntity.ok(bankInstitutionService.getBankDetails(bankId));
    }
}
