package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.ContractDto;
import com.classroomapp.classroombackend.dto.ContractStatsDto;
import com.classroomapp.classroombackend.dto.InterviewScheduleDto;
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ContractService;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.service.InterviewScheduleService;
import com.classroomapp.classroombackend.util.TopCVCalculation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final InterviewScheduleService interviewScheduleService;

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
        
        User user = null;
        
        // Try to find user if userId is provided and looks like a valid database ID
        if (contractDto.getUserId() != null && contractDto.getUserId() < 999999999L) {
            try {
                user = userRepository.findById(contractDto.getUserId()).orElse(null);
                if (user != null) {
                    // Check if user already has a contract
                    if (contractRepository.existsByUserId(contractDto.getUserId())) {
                        throw new IllegalArgumentException("User already has a contract. Each user can only have one contract.");
                    }
                }
            } catch (Exception e) {
                log.warn("Could not find user with id: {}, proceeding with manual contract creation", contractDto.getUserId());
                user = null;
            }
        }
        
        Contract contract = convertToEntity(contractDto);
        
        // Generate unique Contract ID
        String contractId = generateNextContractId();
        contract.setContractId(contractId);
        
        // Use data from contractDto, fallback to user data if available
        if (contractDto.getFullName() != null && !contractDto.getFullName().trim().isEmpty()) {
            contract.setFullName(contractDto.getFullName());
        } else if (user != null) {
            contract.setFullName(user.getFullName());
        } else {
            throw new IllegalArgumentException("Full name is required for contract creation");
        }
        
        if (contractDto.getEmail() != null && !contractDto.getEmail().trim().isEmpty()) {
            contract.setEmail(contractDto.getEmail());
        } else if (user != null) {
            contract.setEmail(user.getEmail());
        } else {
            throw new IllegalArgumentException("Email is required for contract creation");
        }
        
        if (contractDto.getPhoneNumber() != null && !contractDto.getPhoneNumber().trim().isEmpty()) {
            contract.setPhoneNumber(contractDto.getPhoneNumber());
        } else if (user != null) {
            contract.setPhoneNumber(user.getPhoneNumber());
        }
        
        // Ensure required fields are set
        if (contract.getContractType() == null || contract.getContractType().trim().isEmpty()) {
            throw new IllegalArgumentException("Contract type is required");
        }
        
        if (contract.getPosition() == null || contract.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("Position is required");
        }
        
        if (contract.getSalary() == null || contract.getSalary() <= 0) {
            throw new IllegalArgumentException("Valid salary is required");
        }
        
        if (contract.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
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
                                return contract.getEmail().trim().equalsIgnoreCase(applicantEmail.trim());
                            });
                    
                    log.info("Candidate '{}' has contract: {}", applicantEmail, hasContract);
                    return !hasContract; // Chỉ lấy những người chưa có hợp đồng
                })
                .map(interview -> {
                    ContractDto candidate = new ContractDto();
                    
                    // ✅ FIX: Sử dụng interview ID thay vì auto-generated userId
                    candidate.setId(interview.getId()); // Interview ID để API có thể tìm đúng interview
                    candidate.setUserId(interview.getId()); // Cũng set userId để tương thích
                    
                    // Lấy thông tin cơ bản từ interview
                    candidate.setFullName(interview.getApplicantName());
                    candidate.setEmail(interview.getApplicantEmail());
                    candidate.setPhoneNumber(interview.getApplicantPhone() != null ? interview.getApplicantPhone() : "Chưa có");
                    candidate.setPosition(interview.getJobTitle());
                    candidate.setOffer(interview.getOffer()); // Lấy thông tin offer
                    
                    // ✅ FIX: Xác định contract type dựa trên job title thực tế
                    String jobTitle = interview.getJobTitle() != null ? interview.getJobTitle().toLowerCase() : "";
                    if (jobTitle.contains("giáo viên") || jobTitle.contains("teacher")) {
                        candidate.setContractType("TEACHER");
                        log.info("Set contract type TEACHER for position: {}", interview.getJobTitle());
                    } else {
                        candidate.setContractType("STAFF");
                        log.info("Set contract type STAFF for position: {}", interview.getJobTitle());
                    }
                    
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

    @Override
    public ContractDto getCandidateOfferData(Long candidateId) {
        log.info("Fetching real offer data for candidate ID: {}", candidateId);
        try {
            // Tìm interview schedule của candidate để lấy dữ liệu offer thực
            InterviewScheduleDto interview = interviewScheduleService.getById(candidateId);
            if (interview == null) {
                log.warn("Interview not found for candidate ID: {}", candidateId);
                throw new ResourceNotFoundException("Interview not found for candidate ID: " + candidateId);
            }
            
            ContractDto offerData = new ContractDto();
            
            // Lấy đánh giá từ interview evaluation field
            String evaluation = "Chưa có đánh giá";
            if ("APPROVED".equals(interview.getStatus())) {
                evaluation = "Đạt yêu cầu - Được phê duyệt";
                if (interview.getEvaluation() != null && !interview.getEvaluation().trim().isEmpty()) {
                    evaluation = interview.getEvaluation(); // Sử dụng evaluation field từ interview
                }
            }
            offerData.setEvaluation(evaluation);
            
            // Tính toán lương từ offer amount sử dụng TopCVCalculation
            if (interview.getOffer() != null && !interview.getOffer().trim().isEmpty()) {
                try {
                    // Parse offer amount từ string thành BigDecimal
                    String cleanOffer = interview.getOffer().replaceAll("[^0-9]", "");
                    if (!cleanOffer.isEmpty()) {
                        BigDecimal grossSalary = new BigDecimal(cleanOffer);
                        
                        // Sử dụng TopCVCalculation để tính toán chi tiết lương
                        TopCVCalculation.SalaryCalculationResult salaryResult = 
                            TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
                        
                        // Xác định loại vị trí để tính lương phù hợp
                        String jobTitle = interview.getJobTitle() != null ? interview.getJobTitle().toLowerCase() : "";
                        String contractType = interview.getContractType() != null ? interview.getContractType().toLowerCase() : "";
                        
                        log.info("🔍 DEBUG: Salary calculation for candidate ID: {}", candidateId);
                        log.info("🔍 DEBUG: Job title: '{}'", interview.getJobTitle());
                        log.info("🔍 DEBUG: Contract type: '{}'", interview.getContractType());
                        log.info("🔍 DEBUG: Salary range: '{}'", interview.getSalaryRange());
                        log.info("🔍 DEBUG: Offer amount: '{}'", interview.getOffer());
                        log.info("🔍 DEBUG: Gross salary calculated: {}", salaryResult.getGrossSalary());
                        
                        boolean isTeacher = jobTitle.contains("giáo viên") || jobTitle.contains("teacher") || 
                                           contractType.contains("teacher") || contractType.contains("giáo viên");
                        boolean isManagerOrAccountant = jobTitle.contains("quản lý") || jobTitle.contains("manager") || 
                                                       jobTitle.contains("kế toán") || jobTitle.contains("accountant");
                        
                        log.info("🔍 DEBUG: isTeacher: {}, isManagerOrAccountant: {}", isTeacher, isManagerOrAccountant);
                        
                        if (isTeacher) {
                            // Giáo viên: Chỉ hiển thị lương theo giờ
                            log.info("Processing TEACHER position for candidate ID: {} - jobTitle: {}", candidateId, jobTitle);
                            offerData.setGrossSalary(null); // Để trống
                            offerData.setNetSalary(null);   // Để trống
                            
                            // Tính lương theo giờ (giả sử 8 giờ/ngày, 22 ngày/tháng)
                            BigDecimal hourlyRate = salaryResult.getGrossSalary()
                                .divide(new BigDecimal("176"), 0, RoundingMode.HALF_UP); // 22 ngày * 8 giờ = 176 giờ/tháng
                            log.info("🔍 DEBUG: Calculated hourly rate for teacher: {} from gross salary: {}", hourlyRate, salaryResult.getGrossSalary());
                            offerData.setHourlySalary(hourlyRate.longValue());
                            
                        } else if (isManagerOrAccountant) {
                            // Manager & Kế toán: Chỉ hiển thị lương GROSS và NET
                            log.info("Processing MANAGER/ACCOUNTANT position for candidate ID: {} - jobTitle: {}", candidateId, jobTitle);
                            offerData.setGrossSalary(salaryResult.getGrossSalary().longValue());
                            offerData.setNetSalary(salaryResult.getNetSalary().longValue());
                            offerData.setHourlySalary(null); // Để trống
                            
                        } else {
                            // Mặc định: Hiển thị tất cả (cho các vị trí khác)
                            log.info("Processing OTHER position for candidate ID: {} - jobTitle: {}", candidateId, jobTitle);
                            offerData.setGrossSalary(salaryResult.getGrossSalary().longValue());
                            offerData.setNetSalary(salaryResult.getNetSalary().longValue());
                            
                            BigDecimal hourlyRate = salaryResult.getGrossSalary()
                                .divide(new BigDecimal("176"), 0, RoundingMode.HALF_UP);
                            offerData.setHourlySalary(hourlyRate.longValue());
                        }
                        
                        log.info("🔍 DEBUG: Final salary details for candidate ID: {} - Gross: {}, Net: {}, Hourly: {}", 
                                candidateId, offerData.getGrossSalary(), offerData.getNetSalary(), offerData.getHourlySalary());
                    } else {
                        log.warn("Empty offer amount for candidate ID: {}", candidateId);
                        setDefaultOfferData(offerData);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid offer amount format for candidate ID: {} - offer: {}", candidateId, interview.getOffer());
                    setDefaultOfferData(offerData);
                }
            } else {
                log.warn("No offer amount found for candidate ID: {}", candidateId);
                setDefaultOfferData(offerData);
            }
            
            log.info("Successfully fetched real offer data for candidate ID: {}", candidateId);
            return offerData;
        } catch (ResourceNotFoundException e) {
            throw e; // Re-throw ResourceNotFoundException
        } catch (Exception e) {
            log.error("Error fetching offer data for candidate ID {}: ", candidateId, e);
            throw new RuntimeException("Failed to fetch offer data for candidate ID: " + candidateId, e);
        }
    }
    
    private void setDefaultOfferData(ContractDto offerData) {
        offerData.setGrossSalary(0L);
        offerData.setNetSalary(0L);
        offerData.setHourlySalary(0L);
    }
    
    private String generateNextContractId() {
        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();
        
        // Tạo start và end của ngày để đếm hợp đồng trong ngày
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        // Đếm số hợp đồng đã tạo trong ngày hôm nay
        Long contractsToday = contractRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        
        // Tạo sequence number (bắt đầu từ 01)
        String sequence = String.format("%02d", contractsToday + 1);
        
        // Tạo phần ngày tháng năm (MMYY)
        String dateFormat = String.format("%02d%02d", today.getMonthValue(), today.getYear() % 100);
        
        // Kết hợp thành Contract ID: sequence + MMYY
        String contractId = sequence + dateFormat;
        
        log.info("Generated Contract ID: {} for date: {}", contractId, today);
        return contractId;
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

    private Contract convertToEntity(ContractDto contractDto) {
        Contract contract = new Contract();
        contract.setId(contractDto.getId());
        contract.setUserId(contractDto.getUserId());
        contract.setContractId(contractDto.getContractId());
        contract.setFullName(contractDto.getFullName());
        contract.setEmail(contractDto.getEmail());
        contract.setPhoneNumber(contractDto.getPhoneNumber());
        contract.setContractType(contractDto.getContractType());
        contract.setPosition(contractDto.getPosition());
        contract.setDepartment(contractDto.getDepartment());
        contract.setSalary(contractDto.getSalary());
        contract.setWorkingHours(contractDto.getWorkingHours());
        contract.setStartDate(contractDto.getStartDate());
        contract.setEndDate(contractDto.getEndDate());
        contract.setStatus(contractDto.getStatus());
        contract.setContractTerms(contractDto.getContractTerms());
        contract.setCreatedBy(contractDto.getCreatedBy());
        contract.setCreatedAt(contractDto.getCreatedAt());
        contract.setUpdatedAt(contractDto.getUpdatedAt());
        contract.setOffer(contractDto.getOffer()); // Nếu có trường offer
        // --- CUSTOM FIELDS ---
        contract.setBirthDate(contractDto.getBirthDate());
        contract.setCitizenId(contractDto.getCitizenId());
        contract.setAddress(contractDto.getAddress());
        contract.setQualification(contractDto.getQualification());
        contract.setSubject(contractDto.getSubject());
        contract.setEducationLevel(contractDto.getEducationLevel());
        return contract;
    }

    private ContractDto convertToDto(Contract contract) {
        ContractDto dto = new ContractDto();
        dto.setId(contract.getId());
        dto.setUserId(contract.getUserId());
        dto.setContractId(contract.getContractId());
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

    @Override
    public void createTestContracts() {
        log.info("Creating test contract data");
        
        // Xóa tất cả hợp đồng test cũ (nếu có)
        contractRepository.deleteAll();
        
        LocalDate today = LocalDate.now();
        
        // 1. Hợp đồng ACTIVE (còn lâu mới hết hạn)
        Contract contract1 = new Contract();
        contract1.setUserId(1001L);
        contract1.setContractId("010825"); // Contract ID mẫu
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
        contract2.setContractId("020825"); // Contract ID mẫu
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
        contract3.setContractId("030825"); // Contract ID mẫu
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
        contract4.setContractId("040825"); // Contract ID mẫu
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
        contract5.setContractId("050825"); // Contract ID mẫu
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
