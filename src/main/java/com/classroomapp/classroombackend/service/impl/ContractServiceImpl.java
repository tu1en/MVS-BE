package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.ContractDto;
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ContractService;
import com.classroomapp.classroombackend.dto.ContractStatsDto;
import com.classroomapp.classroombackend.service.InterviewScheduleService;
import com.classroomapp.classroombackend.dto.InterviewScheduleDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final InterviewScheduleService interviewScheduleService;
    private final ModelMapper modelMapper;

    @Override
    public List<ContractDto> getContractsByType(String contractType) {
        log.info("Fetching contracts by type: {}", contractType);
        List<Contract> contracts = contractRepository.findByContractTypeOrderByCreatedAtDesc(contractType);
        return contracts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContractDto> getAllContracts() {
        log.info("Fetching all contracts");
        List<Contract> contracts = contractRepository.findAll();
        return contracts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContractDto getContractById(Long id) {
        log.info("Fetching contract by id: {}", id);
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        return convertToDto(contract);
    }

    @Override
    public ContractDto createContract(ContractDto contractDto) {
        log.info("Creating new contract for user: {}", contractDto.getFullName());
        
        // Validate user exists
        User user = userRepository.findById(contractDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + contractDto.getUserId()));
        
        Contract contract = convertToEntity(contractDto);
        // Use candidate name from contractDto if provided, otherwise use user's name
        if (contractDto.getFullName() != null && !contractDto.getFullName().trim().isEmpty()) {
            contract.setFullName(contractDto.getFullName());
        } else {
            contract.setFullName(user.getFullName());
        }
        // Use candidate email from contractDto if provided, otherwise use user's email
        if (contractDto.getEmail() != null && !contractDto.getEmail().trim().isEmpty()) {
            contract.setEmail(contractDto.getEmail());
        } else {
            contract.setEmail(user.getEmail());
        }
        // Use candidate phone from contractDto if provided, otherwise use user's phone
        if (contractDto.getPhoneNumber() != null && !contractDto.getPhoneNumber().trim().isEmpty()) {
            contract.setPhoneNumber(contractDto.getPhoneNumber());
        } else {
            contract.setPhoneNumber(user.getPhoneNumber());
        }
        
        Contract savedContract = contractRepository.save(contract);
        log.info("Contract created successfully with id: {}", savedContract.getId());
        
        return convertToDto(savedContract);
    }

    @Override
    public ContractDto updateContract(Long id, ContractDto contractDto) {
        log.info("Updating contract with id: {}", id);
        
        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        
        // Update fields
        existingContract.setContractType(contractDto.getContractType());
        existingContract.setPosition(contractDto.getPosition());
        existingContract.setDepartment(contractDto.getDepartment());
        existingContract.setSalary(contractDto.getSalary());
        existingContract.setWorkingHours(contractDto.getWorkingHours());
        existingContract.setStartDate(contractDto.getStartDate());
        existingContract.setEndDate(contractDto.getEndDate());
        existingContract.setStatus(contractDto.getStatus());
        existingContract.setContractTerms(contractDto.getContractTerms());
        
        Contract updatedContract = contractRepository.save(existingContract);
        log.info("Contract updated successfully with id: {}", updatedContract.getId());
        
        return convertToDto(updatedContract);
    }

    @Override
    public void deleteContract(Long id) {
        log.info("Deleting contract with id: {}", id);
        
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        
        contractRepository.delete(contract);
        log.info("Contract deleted successfully with id: {}", id);
    }

    @Override
    public List<ContractDto> getContractsByUserId(Long userId) {
        log.info("Fetching contracts for user id: {}", userId);
        List<Contract> contracts = contractRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return contracts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContractDto> getCandidatesReadyForContract() {
        log.info("Fetching candidates ready for contract creation");
        
        // Lấy danh sách tất cả ứng viên đã được duyệt ở Quản Lý Offer
        List<InterviewScheduleDto> approvedCandidates = interviewScheduleService.getAll().stream()
                .filter(interview -> "APPROVED".equals(interview.getStatus()))
                .collect(Collectors.toList());
        
        log.info("Found {} approved candidates from Offer Management", approvedCandidates.size());
        
        // Lấy tất cả hợp đồng hiện tại
        List<Contract> allContracts = contractRepository.findAll();
        log.info("Found {} existing contracts", allContracts.size());
        
        // Log tất cả email của hợp đồng hiện tại
        allContracts.forEach(contract -> 
            log.info("Existing contract email: '{}'", contract.getEmail()));
        
        // Lọc những người chưa có hợp đồng
        List<ContractDto> candidates = approvedCandidates.stream()
                .filter(interview -> {
                    // Kiểm tra xem ứng viên đã có hợp đồng chưa
                    String applicantEmail = interview.getApplicantEmail();
                    log.info("Checking candidate email: '{}'", applicantEmail);
                    
                    if (applicantEmail == null || applicantEmail.trim().isEmpty()) {
                        log.info("Candidate email is null or empty, excluding");
                        return false;
                    }
                    
                    // Tìm hợp đồng theo email (không phân biệt hoa thường)
                    boolean hasContract = allContracts.stream()
                            .anyMatch(contract -> {
                                if (contract.getEmail() == null) return false;
                                boolean matches = contract.getEmail().trim().equalsIgnoreCase(applicantEmail.trim());
                                if (matches) {
                                    log.info("Found matching contract for email: '{}' <-> '{}'", 
                                            contract.getEmail(), applicantEmail);
                                }
                                return matches;
                            });
                    
                    log.info("Candidate: {} - Has contract: {} - Will include: {}", 
                            applicantEmail, hasContract, !hasContract);
                    return !hasContract;
                })
                .map(interview -> {
                    ContractDto candidate = new ContractDto();
                    
                    // Tự động tạo User ID theo thứ tự (001, 002, 003...)
                    Long nextUserId = generateNextUserId();
                    candidate.setUserId(nextUserId);
                    
                    // Lấy thông tin cơ bản từ interview
                    candidate.setFullName(interview.getApplicantName());
                    candidate.setEmail(interview.getApplicantEmail());
                    candidate.setPhoneNumber(interview.getApplicantPhone() != null ? interview.getApplicantPhone() : "Chưa có");
                    candidate.setPosition(interview.getJobTitle());
                    candidate.setOffer(interview.getOffer()); // Lấy thông tin offer
                    candidate.setContractType("TEACHER"); // Mặc định là giáo viên
                    
                    // Lấy mức lương từ job position (nếu có)
                    if (interview.getSalaryRange() != null && !interview.getSalaryRange().isEmpty()) {
                        try {
                            // Trích xuất số lương từ salary range (ví dụ: "10,000,000 - 15,000,000 VND")
                            String salaryStr = interview.getSalaryRange().replaceAll("[^0-9]", "");
                            if (!salaryStr.isEmpty()) {
                                Double salary = Double.parseDouble(salaryStr.substring(0, Math.min(salaryStr.length(), 8)));
                                candidate.setSalary(salary);
                            }
                        } catch (Exception e) {
                            log.warn("Could not parse salary from: {}", interview.getSalaryRange());
                            candidate.setSalary(10000000.0); // Mặc định 10 triệu
                        }
                    } else {
                        candidate.setSalary(10000000.0); // Mặc định 10 triệu
                    }
                    
                    return candidate;
                })
                .collect(Collectors.toList());
        
        log.info("Found {} candidates ready for contract", candidates.size());
        return candidates;
    }
    
    private Long generateNextUserId() {
        // Lấy số hợp đồng hiện tại để tạo ID tiếp theo
        Long contractCount = contractRepository.count();
        return contractCount + 1;
    }

    @Override
    public ContractStatsDto getContractStats() {
        log.info("Calculating contract statistics");
        
        Long totalContracts = contractRepository.count();
        Long teacherContracts = contractRepository.countByContractType("TEACHER");
        Long accountantContracts = contractRepository.countByContractType("ACCOUNTANT");
        Long activeContracts = contractRepository.countByStatus("ACTIVE");
        Long expiredContracts = contractRepository.countByStatus("EXPIRED");
        
        return new ContractStatsDto(totalContracts, teacherContracts, accountantContracts, 
                                  activeContracts, expiredContracts);
    }

    private ContractDto convertToDto(Contract contract) {
        ContractDto dto = new ContractDto();
        dto.setId(contract.getId());
        dto.setUserId(contract.getUserId());
        dto.setFullName(contract.getFullName());
        dto.setEmail(contract.getEmail());
        dto.setPhoneNumber(contract.getPhoneNumber());
        dto.setContractType(contract.getContractType());
        dto.setPosition(contract.getPosition());
        dto.setDepartment(contract.getDepartment());
        dto.setSalary(contract.getSalary());
        dto.setWorkingHours(contract.getWorkingHours());
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        dto.setStatus(contract.getStatus());
        dto.setContractTerms(contract.getContractTerms());
        dto.setCreatedBy(contract.getCreatedBy());
        dto.setCreatedAt(contract.getCreatedAt());
        dto.setUpdatedAt(contract.getUpdatedAt());
        dto.setOffer(contract.getOffer()); // Nếu có trường offer
        // --- CUSTOM FIELDS ---
        dto.setBirthDate(contract.getBirthDate());
        dto.setCitizenId(contract.getCitizenId());
        dto.setAddress(contract.getAddress());
        dto.setQualification(contract.getQualification());
        dto.setSubject(contract.getSubject());
        dto.setEducationLevel(contract.getEducationLevel());
        return dto;
    }

    private Contract convertToEntity(ContractDto contractDto) {
        Contract contract = modelMapper.map(contractDto, Contract.class);
        // --- CUSTOM FIELDS ---
        contract.setBirthDate(contractDto.getBirthDate());
        contract.setCitizenId(contractDto.getCitizenId());
        contract.setAddress(contractDto.getAddress());
        contract.setQualification(contractDto.getQualification());
        contract.setSubject(contractDto.getSubject());
        contract.setEducationLevel(contractDto.getEducationLevel());
        contract.setOffer(contractDto.getOffer()); // Nếu có trường offer
        return contract;
    }

    @Override
    public void createTestContracts() {
        log.info("Creating test contract data");
        
        // Xóa tất cả hợp đồng test cũ (nếu có)
        contractRepository.deleteAll();
        
        LocalDate today = LocalDate.now();
        
        // 1. Hợp đồng ACTIVE (còn lâu mới hết hạn)
        Contract contract1 = new Contract();
        contract1.setUserId(1001L);
        contract1.setFullName("Nguyễn Văn An");
        contract1.setEmail("nguyen.van.an@example.com");
        contract1.setPhoneNumber("0987654321");
        contract1.setContractType("TEACHER");
        contract1.setPosition("Giáo viên Toán");
        contract1.setDepartment("Phòng Giáo vụ");
        contract1.setSalary(15000000.0);
        contract1.setWorkingHours("ca sáng (7:30-9:30)");
        contract1.setStartDate(LocalDate.of(2024, 1, 15));
        contract1.setEndDate(LocalDate.of(2026, 1, 15));
        contract1.setStatus("ACTIVE");
        contractRepository.save(contract1);
        
        // 2. Hợp đồng ACTIVE (sắp gần hết hạn - còn 20 ngày)
        Contract contract2 = new Contract();
        contract2.setUserId(1002L);
        contract2.setFullName("Trần Thị Bình");
        contract2.setEmail("tran.thi.binh@example.com");
        contract2.setPhoneNumber("0976543210");
        contract2.setContractType("ACCOUNTANT");
        contract2.setPosition("Nhân viên Kế toán");
        contract2.setDepartment("Phòng Tài chính");
        contract2.setSalary(12000000.0);
        contract2.setWorkingHours("ca chiều (14:30-16:30)");
        contract2.setStartDate(LocalDate.of(2024, 2, 1));
        contract2.setEndDate(today.plusDays(20)); // Còn 20 ngày
        contract2.setStatus("ACTIVE");
        contractRepository.save(contract2);
        
        // 3. Hợp đồng NEAR_EXPIRY (gần hết hạn - còn 10 ngày)
        Contract contract3 = new Contract();
        contract3.setUserId(1003L);
        contract3.setFullName("Lê Minh Cường");
        contract3.setEmail("le.minh.cuong@example.com");
        contract3.setPhoneNumber("0965432109");
        contract3.setContractType("TEACHER");
        contract3.setPosition("Giáo viên Lý");
        contract3.setDepartment("Phòng Giáo vụ");
        contract3.setSalary(16000000.0);
        contract3.setWorkingHours("ca tối (19:20-21:20)");
        contract3.setStartDate(LocalDate.of(2024, 3, 1));
        contract3.setEndDate(today.plusDays(10)); // Còn 10 ngày
        contract3.setStatus("NEAR_EXPIRY");
        contractRepository.save(contract3);
        
        // 4. Hợp đồng NEAR_EXPIRY (gần hết hạn - còn 5 ngày)
        Contract contract4 = new Contract();
        contract4.setUserId(1004L);
        contract4.setFullName("Phạm Thị Dung");
        contract4.setEmail("pham.thi.dung@example.com");
        contract4.setPhoneNumber("0954321098");
        contract4.setContractType("ACCOUNTANT");
        contract4.setPosition("Nhân viên Hành chính");
        contract4.setDepartment("Phòng Hành chính");
        contract4.setSalary(11000000.0);
        contract4.setWorkingHours("ca sáng (7:30-9:30)");
        contract4.setStartDate(LocalDate.of(2024, 4, 1));
        contract4.setEndDate(today.plusDays(5)); // Còn 5 ngày
        contract4.setStatus("NEAR_EXPIRY");
        contractRepository.save(contract4);
        
        // 5. Hợp đồng EXPIRED (đã hết hạn - hết hạn 3 ngày trước)
        Contract contract5 = new Contract();
        contract5.setUserId(1005L);
        contract5.setFullName("Hoàng Văn Em");
        contract5.setEmail("hoang.van.em@example.com");
        contract5.setPhoneNumber("0943210987");
        contract5.setContractType("TEACHER");
        contract5.setPosition("Giáo viên Hóa");
        contract5.setDepartment("Phòng Giáo vụ");
        contract5.setSalary(17000000.0);
        contract5.setWorkingHours("ca chiều (14:30-16:30)");
        contract5.setStartDate(LocalDate.of(2024, 5, 1));
        contract5.setEndDate(today.minusDays(3)); // Hết hạn 3 ngày trước
        contract5.setStatus("EXPIRED");
        contractRepository.save(contract5);
        
        log.info("Created 5 test contracts successfully");
    }
}
