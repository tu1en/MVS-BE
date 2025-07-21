package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.ContractDto;
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ContractDto createContract(ContractDto contractDto, String createdBy) {
        // Check if user exists
        Optional<User> userOpt = userRepository.findById(contractDto.getUserId());
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found with ID: " + contractDto.getUserId());
        }

        // Check if user already has an active contract
        Optional<Contract> existingContract = contractRepository.findByUserIdAndStatus(contractDto.getUserId(), "ACTIVE");
        if (existingContract.isPresent()) {
            throw new RuntimeException("User already has an active contract");
        }

        Contract contract = new Contract();
        contract.setUserId(contractDto.getUserId());
        contract.setContractType(contractDto.getContractType());
        contract.setPosition(contractDto.getPosition());
        contract.setDepartment(contractDto.getDepartment());
        contract.setSalary(contractDto.getSalary());
        contract.setStartDate(contractDto.getStartDate());
        contract.setEndDate(contractDto.getEndDate());
        contract.setStatus("ACTIVE");
        contract.setCreatedBy(createdBy);
        contract.setNotes(contractDto.getNotes());
        contract.setCreatedAt(LocalDateTime.now());

        Contract savedContract = contractRepository.save(contract);
        return convertToDto(savedContract);
    }

    @Override
    public ContractDto getContractById(Long id) {
        Optional<Contract> contractOpt = contractRepository.findById(id);
        if (!contractOpt.isPresent()) {
            throw new RuntimeException("Contract not found with ID: " + id);
        }
        return convertToDto(contractOpt.get());
    }

    @Override
    public List<ContractDto> getContractsByUserId(Long userId) {
        List<Contract> contracts = contractRepository.findByUserId(userId);
        return contracts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContractDto getActiveContractByUserId(Long userId) {
        Optional<Contract> contractOpt = contractRepository.findByUserIdAndStatus(userId, "ACTIVE");
        if (!contractOpt.isPresent()) {
            throw new RuntimeException("No active contract found for user ID: " + userId);
        }
        return convertToDto(contractOpt.get());
    }

    @Override
    public Page<ContractDto> getAllContracts(Pageable pageable) {
        Page<Contract> contracts = contractRepository.findAll(pageable);
        return contracts.map(this::convertToDto);
    }

    @Override
    public Page<ContractDto> getContractsByStatus(String status, Pageable pageable) {
        Page<Contract> contracts = contractRepository.findByStatus(status, pageable);
        return contracts.map(this::convertToDto);
    }

    @Override
    public Page<ContractDto> searchContracts(String status, String contractType, String department, 
                                           LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Contract> contracts = contractRepository.findContractsWithFilters(
                status, contractType, department, startDate, endDate, pageable);
        return contracts.map(this::convertToDto);
    }

    @Override
    public ContractDto updateContract(Long id, ContractDto contractDto, String updatedBy) {
        Optional<Contract> contractOpt = contractRepository.findById(id);
        if (!contractOpt.isPresent()) {
            throw new RuntimeException("Contract not found with ID: " + id);
        }

        Contract contract = contractOpt.get();
        contract.setContractType(contractDto.getContractType());
        contract.setPosition(contractDto.getPosition());
        contract.setDepartment(contractDto.getDepartment());
        contract.setSalary(contractDto.getSalary());
        contract.setStartDate(contractDto.getStartDate());
        contract.setEndDate(contractDto.getEndDate());
        contract.setNotes(contractDto.getNotes());
        contract.setUpdatedAt(LocalDateTime.now());

        Contract savedContract = contractRepository.save(contract);
        return convertToDto(savedContract);
    }

    @Override
    public ContractDto terminateContract(Long id, String terminationReason, String terminationDate, 
                                       String whoApproved, String settlementInfo, String terminatedBy) {
        Optional<Contract> contractOpt = contractRepository.findById(id);
        if (!contractOpt.isPresent()) {
            throw new RuntimeException("Contract not found with ID: " + id);
        }

        Contract contract = contractOpt.get();
        if (!"ACTIVE".equals(contract.getStatus())) {
            throw new RuntimeException("Contract is not active and cannot be terminated");
        }

        contract.setStatus("TERMINATED");
        contract.setTerminationReason(terminationReason);
        
        // Set termination date - use provided date or current date if not provided
        if (terminationDate != null && !terminationDate.isEmpty()) {
            contract.setTerminationDate(LocalDate.parse(terminationDate));
        } else {
            contract.setTerminationDate(LocalDate.now());
        }
        
        contract.setWhoApproved(whoApproved);
        contract.setSettlementInfo(settlementInfo);
        contract.setUpdatedAt(LocalDateTime.now());

        Contract savedContract = contractRepository.save(contract);
        return convertToDto(savedContract);
    }

    @Override
    public ContractDto terminateContractByUserId(Long userId, String terminationReason, String terminationDate,
                                               String whoApproved, String settlementInfo, String terminatedBy) {
        Optional<Contract> contractOpt = contractRepository.findByUserIdAndStatus(userId, "ACTIVE");
        if (!contractOpt.isPresent()) {
            throw new RuntimeException("No active contract found for user ID: " + userId);
        }

        return terminateContract(contractOpt.get().getId(), terminationReason, terminationDate, 
                               whoApproved, settlementInfo, terminatedBy);
    }

    @Override
    public void deleteContract(Long id) {
        if (!contractRepository.existsById(id)) {
            throw new RuntimeException("Contract not found with ID: " + id);
        }
        contractRepository.deleteById(id);
    }

    @Override
    public List<ContractDto> getContractsExpiringSoon(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(days);
        
        List<Contract> contracts = contractRepository.findContractsExpiringBetween(startDate, endDate);
        return contracts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContractStatistics getContractStatistics() {
        long totalContracts = contractRepository.count();
        long activeContracts = contractRepository.countByStatus("ACTIVE");
        long terminatedContracts = contractRepository.countByStatus("TERMINATED");
        long expiredContracts = contractRepository.countByStatus("EXPIRED");
        
        // Contracts expiring in next 30 days
        List<Contract> expiringSoon = contractRepository.findContractsExpiringBetween(
                LocalDate.now(), LocalDate.now().plusDays(30));
        long contractsExpiringSoon = expiringSoon.size();

        return new ContractStatistics(totalContracts, activeContracts, terminatedContracts, 
                                    expiredContracts, contractsExpiringSoon);
    }

    private ContractDto convertToDto(Contract contract) {
        ContractDto dto = new ContractDto();
        dto.setId(contract.getId());
        dto.setUserId(contract.getUserId());
        dto.setContractType(contract.getContractType());
        dto.setPosition(contract.getPosition());
        dto.setDepartment(contract.getDepartment());
        dto.setSalary(contract.getSalary());
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        dto.setStatus(contract.getStatus());
        dto.setTerminationReason(contract.getTerminationReason());
        dto.setTerminationDate(contract.getTerminationDate());
        dto.setCreatedAt(contract.getCreatedAt());
        dto.setUpdatedAt(contract.getUpdatedAt());
        dto.setCreatedBy(contract.getCreatedBy());
        dto.setNotes(contract.getNotes());

        // Get user information
        Optional<User> userOpt = userRepository.findById(contract.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            dto.setUserName(user.getFullName());
            dto.setUserEmail(user.getEmail());
        }

        return dto;
    }
}
