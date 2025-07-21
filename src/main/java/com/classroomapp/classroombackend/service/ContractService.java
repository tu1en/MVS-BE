package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.ContractDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ContractService {
    
    // Create a new contract
    ContractDto createContract(ContractDto contractDto, String createdBy);
    
    // Get contract by ID
    ContractDto getContractById(Long id);
    
    // Get contracts by user ID
    List<ContractDto> getContractsByUserId(Long userId);
    
    // Get active contract by user ID
    ContractDto getActiveContractByUserId(Long userId);
    
    // Get all contracts with pagination
    Page<ContractDto> getAllContracts(Pageable pageable);
    
    // Get contracts by status
    Page<ContractDto> getContractsByStatus(String status, Pageable pageable);
    
    // Search contracts with filters
    Page<ContractDto> searchContracts(String status, String contractType, String department, 
                                     LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    // Update contract
    ContractDto updateContract(Long id, ContractDto contractDto, String updatedBy);
    
    // Terminate contract
    ContractDto terminateContract(Long id, String terminationReason, String terminationDate, 
                                String whoApproved, String settlementInfo, String terminatedBy);
    
    // Terminate contract by user ID
    ContractDto terminateContractByUserId(Long userId, String terminationReason, String terminationDate,
                                         String whoApproved, String settlementInfo, String terminatedBy);
    
    // Delete contract
    void deleteContract(Long id);
    
    // Get contracts expiring soon
    List<ContractDto> getContractsExpiringSoon(int days);
    
    // Get contract statistics
    ContractStatistics getContractStatistics();
    
    // Inner class for statistics
    class ContractStatistics {
        private long totalContracts;
        private long activeContracts;
        private long terminatedContracts;
        private long expiredContracts;
        private long contractsExpiringSoon;
        
        public ContractStatistics() {}
        
        public ContractStatistics(long totalContracts, long activeContracts, 
                                 long terminatedContracts, long expiredContracts, 
                                 long contractsExpiringSoon) {
            this.totalContracts = totalContracts;
            this.activeContracts = activeContracts;
            this.terminatedContracts = terminatedContracts;
            this.expiredContracts = expiredContracts;
            this.contractsExpiringSoon = contractsExpiringSoon;
        }
        
        // Getters and Setters
        public long getTotalContracts() { return totalContracts; }
        public void setTotalContracts(long totalContracts) { this.totalContracts = totalContracts; }
        
        public long getActiveContracts() { return activeContracts; }
        public void setActiveContracts(long activeContracts) { this.activeContracts = activeContracts; }
        
        public long getTerminatedContracts() { return terminatedContracts; }
        public void setTerminatedContracts(long terminatedContracts) { this.terminatedContracts = terminatedContracts; }
        
        public long getExpiredContracts() { return expiredContracts; }
        public void setExpiredContracts(long expiredContracts) { this.expiredContracts = expiredContracts; }
        
        public long getContractsExpiringSoon() { return contractsExpiringSoon; }
        public void setContractsExpiringSoon(long contractsExpiringSoon) { this.contractsExpiringSoon = contractsExpiringSoon; }
    }
}
