package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.ContractDto;
import com.classroomapp.classroombackend.dto.ContractStatsDto;
import com.classroomapp.classroombackend.service.ContractService;
import com.classroomapp.classroombackend.service.ContractStatusSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ContractController {

    private final ContractService contractService;
    private final ContractStatusSchedulerService contractStatusSchedulerService;

    // Lấy tất cả hợp đồng
    @GetMapping(produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<ContractDto>> getAllContracts() {
        log.info("GET /api/contracts - Fetching all contracts");
        try {
            List<ContractDto> contracts = contractService.getAllContracts();
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            log.error("Error fetching all contracts: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy hợp đồng theo loại (TEACHER hoặc STAFF)
    @GetMapping(value = "/type/{contractType}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<ContractDto>> getContractsByType(@PathVariable String contractType) {
        log.info("GET /api/contracts/type/{} - Fetching contracts by type", contractType);
        try {
            List<ContractDto> contracts = contractService.getContractsByType(contractType.toUpperCase());
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            log.error("Error fetching contracts by type {}: ", contractType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy hợp đồng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ContractDto> getContractById(@PathVariable Long id) {
        log.info("GET /api/contracts/{} - Fetching contract by id", id);
        try {
            ContractDto contract = contractService.getContractById(id);
            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            log.error("Error fetching contract by id {}: ", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Tạo hợp đồng mới
    @PostMapping
    public ResponseEntity<ContractDto> createContract(@Valid @RequestBody ContractDto contractDto) {
        log.info("POST /api/contracts - Creating new contract for user: {}", contractDto.getFullName());
        try {
            ContractDto createdContract = contractService.createContract(contractDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdContract);
        } catch (Exception e) {
            log.error("Error creating contract: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Cập nhật hợp đồng
    @PutMapping("/{id}")
    public ResponseEntity<ContractDto> updateContract(@PathVariable Long id, @Valid @RequestBody ContractDto contractDto) {
        log.info("PUT /api/contracts/{} - Updating contract", id);
        try {
            ContractDto updatedContract = contractService.updateContract(id, contractDto);
            return ResponseEntity.ok(updatedContract);
        } catch (Exception e) {
            log.error("Error updating contract {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Xóa hợp đồng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        log.info("DELETE /api/contracts/{} - Deleting contract", id);
        try {
            contractService.deleteContract(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting contract {}: ", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Lấy hợp đồng theo user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ContractDto>> getContractsByUserId(@PathVariable Long userId) {
        log.info("GET /api/contracts/user/{} - Fetching contracts by user id", userId);
        try {
            List<ContractDto> contracts = contractService.getContractsByUserId(userId);
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            log.error("Error fetching contracts by user id {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy danh sách ứng viên đã đỗ phỏng vấn (sẵn sàng tạo hợp đồng)
    @GetMapping("/candidates/ready")
    public ResponseEntity<List<ContractDto>> getCandidatesReadyForContract() {
        log.info("GET /api/contracts/candidates/ready - Fetching candidates ready for contract");
        try {
            List<ContractDto> candidates = contractService.getCandidatesReadyForContract();
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            log.error("Error fetching candidates ready for contract: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Lấy thông tin offer của ứng viên để tạo hợp đồng
    @GetMapping("/candidates/{candidateId}/offer")
    public ResponseEntity<ContractDto> getCandidateOfferData(@PathVariable Long candidateId) {
        log.info("GET /api/contracts/candidates/{}/offer - Fetching candidate offer data", candidateId);
        try {
            ContractDto offerData = contractService.getCandidateOfferData(candidateId);
            return ResponseEntity.ok(offerData);
        } catch (Exception e) {
            log.error("Error fetching candidate offer data for id {}: ", candidateId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Lấy thống kê hợp đồng
    @GetMapping("/stats")
    public ResponseEntity<ContractStatsDto> getContractStats() {
        log.info("GET /api/contracts/stats - Fetching contract statistics");
        try {
            ContractStatsDto stats = contractService.getContractStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching contract statistics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Gia hạn hợp đồng
    @PutMapping("/{id}/renew")
    public ResponseEntity<ContractDto> renewContract(@PathVariable Long id) {
        log.info("PUT /api/contracts/{}/renew - Renewing contract", id);
        try {
            ContractDto renewedContract = contractService.renewContract(id);
            return ResponseEntity.ok(renewedContract);
        } catch (Exception e) {
            log.error("Error renewing contract {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Ký hợp đồng (PENDING -> ACTIVE)
    @PutMapping("/{id}/sign")
    public ResponseEntity<ContractDto> signContract(@PathVariable Long id) {
        log.info("PUT /api/contracts/{}/sign - Signing contract", id);
        try {
            ContractDto signed = contractService.signContract(id);
            return ResponseEntity.ok(signed);
        } catch (Exception e) {
            log.error("Error signing contract {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Cập nhật trạng thái hợp đồng thủ công (để test)
    @PostMapping("/update-status")
    public ResponseEntity<String> updateContractStatuses() {
        log.info("POST /api/contracts/update-status - Manual contract status update");
        try {
            contractStatusSchedulerService.updateContractStatuses();
            return ResponseEntity.ok("Contract statuses updated successfully");
        } catch (Exception e) {
            log.error("Error updating contract statuses: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating contract statuses: " + e.getMessage());
        }
    }

    // Temporary endpoint to reseed contract statuses for demo
    @PostMapping("/reseed-statuses")
    public ResponseEntity<String> reseedContractStatuses() {
        log.info("POST /api/contracts/reseed-statuses - Reseeding contract statuses for demo");
        try {
            contractService.reseedContractStatuses();
            return ResponseEntity.ok("Contract statuses reseeded successfully for 24 contracts.");
        } catch (Exception e) {
            log.error("Error reseeding contract statuses: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reseeding contract statuses: " + e.getMessage());
        }
    }

    // Tạo hợp đồng hàng loạt cho giáo viên đang active (không trùng), gán lương theo giờ trong khoảng
    @PostMapping(value = "/teachers/bulk-create", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<ContractDto>> bulkCreateTeacherContracts(
            @RequestParam(value = "minHourly", required = false) Long minHourly,
            @RequestParam(value = "maxHourly", required = false) Long maxHourly,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun) {
        log.info("POST /api/contracts/teachers/bulk-create - minHourly: {}, maxHourly: {}, dryRun: {}",
                minHourly, maxHourly, dryRun);
        try {
            List<ContractDto> created = contractService.createContractsForActiveTeachers(minHourly, maxHourly, dryRun);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error bulk-creating teacher contracts: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
