package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.ContractDto;
import com.classroomapp.classroombackend.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "*")
public class ContractController {

    @Autowired
    private ContractService contractService;

    // Create a new contract
    @PostMapping
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<?> createContract(@RequestBody ContractDto contractDto, Authentication authentication) {
        try {
            String createdBy = authentication.getName();
            ContractDto createdContract = contractService.createContract(contractDto, createdBy);
            return ResponseEntity.ok(createdContract);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get contract by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getContractById(@PathVariable Long id) {
        try {
            ContractDto contract = contractService.getContractById(id);
            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get contracts by user ID
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getContractsByUserId(@PathVariable Long userId) {
        try {
            List<ContractDto> contracts = contractService.getContractsByUserId(userId);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get active contract by user ID
    @GetMapping("/user/{userId}/active")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getActiveContractByUserId(@PathVariable Long userId) {
        try {
            ContractDto contract = contractService.getActiveContractByUserId(userId);
            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get all contracts with pagination
    @GetMapping
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ContractDto> contracts = contractService.getAllContracts(pageable);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get contracts by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getContractsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ContractDto> contracts = contractService.getContractsByStatus(status, pageable);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Search contracts with filters
    @GetMapping("/search")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> searchContracts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contractType,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ContractDto> contracts = contractService.searchContracts(
                    status, contractType, department, startDate, endDate, pageable);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Update contract
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<?> updateContract(@PathVariable Long id, @RequestBody ContractDto contractDto, 
                                          Authentication authentication) {
        try {
            String updatedBy = authentication.getName();
            ContractDto updatedContract = contractService.updateContract(id, contractDto, updatedBy);
            return ResponseEntity.ok(updatedContract);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Terminate contract
    @PutMapping("/{id}/terminate")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<?> terminateContract(@PathVariable Long id, 
                                             @RequestBody Map<String, Object> request,
                                             Authentication authentication) {
        try {
            String terminationReason = (String) request.get("terminationReason");
            String terminationDate = (String) request.get("terminationDate");
            String whoApproved = (String) request.get("whoApproved");
            String settlementInfo = (String) request.get("settlementInfo");
            String terminatedBy = authentication.getName();
            
            ContractDto terminatedContract = contractService.terminateContract(
                id, terminationReason, terminationDate, whoApproved, settlementInfo, terminatedBy);
            return ResponseEntity.ok(terminatedContract);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Terminate contract by user ID
    @PutMapping("/user/{userId}/terminate")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public ResponseEntity<?> terminateContractByUserId(@PathVariable Long userId, 
                                                     @RequestBody Map<String, String> request,
                                                     Authentication authentication) {
        try {
            String terminationReason = request.get("terminationReason");
            String terminatedBy = authentication.getName();
            ContractDto terminatedContract = contractService.terminateContractByUserId(userId, terminationReason, terminatedBy);
            return ResponseEntity.ok(terminatedContract);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Delete contract
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteContract(@PathVariable Long id) {
        try {
            contractService.deleteContract(id);
            return ResponseEntity.ok(Map.of("message", "Contract deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get contracts expiring soon
    @GetMapping("/expiring-soon")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getContractsExpiringSoon(@RequestParam(defaultValue = "30") int days) {
        try {
            List<ContractDto> contracts = contractService.getContractsExpiringSoon(days);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get contract statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getContractStatistics() {
        try {
            ContractService.ContractStatistics statistics = contractService.getContractStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
