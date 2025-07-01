package com.classroomapp.classroombackend.accountant.controller;

import com.classroomapp.classroombackend.accountant.model.LaborContract;
import com.classroomapp.classroombackend.accountant.service.LaborContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/labor-contracts")
public class LaborContractController {
    @Autowired
    private LaborContractService laborContractService;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTANT')")
    public List<LaborContract> getAllContracts() {
        return laborContractService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTANT')")
    public ResponseEntity<LaborContract> getContractById(@PathVariable Long id) {
        Optional<LaborContract> contract = laborContractService.findById(id);
        return contract.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNTANT')")
    public LaborContract createContract(@RequestBody LaborContract contract) {
        return laborContractService.save(contract);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTANT')")
    public ResponseEntity<LaborContract> updateContract(@PathVariable Long id, @RequestBody LaborContract contract) {
        Optional<LaborContract> existing = laborContractService.findById(id);
        if (existing.isPresent()) {
            contract.setId(id);
            return ResponseEntity.ok(laborContractService.save(contract));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNTANT')")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        laborContractService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
