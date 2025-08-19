package com.classroomapp.classroombackend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.ContractDto;
import com.classroomapp.classroombackend.dto.ContractStatsDto;
import com.classroomapp.classroombackend.dto.InterviewScheduleDto;
import com.classroomapp.classroombackend.dto.RecruitmentApplicationDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.entity.ClassLesson;
import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.repository.ClassLessonRepository;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.service.ContractService;
import com.classroomapp.classroombackend.service.InterviewScheduleService;
import com.classroomapp.classroombackend.service.RecruitmentApplicationService;
import com.classroomapp.classroombackend.service.UserService;
import com.classroomapp.classroombackend.util.TopCVCalculation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final InterviewScheduleService interviewScheduleService;
    private final RecruitmentApplicationService recruitmentApplicationService;
    private final UserService userService;
    private final ClassLessonRepository classLessonRepository;
    private final ClassRepository classRepository;

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng với ID: " + id));
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
                        throw new IllegalArgumentException("Người dùng đã có hợp đồng. Mỗi người dùng chỉ có một hợp đồng.");
                    }
                }
            } catch (Exception e) {
            log.warn("Không thể tìm thấy người dùng với ID: {}, tiếp tục tạo hợp đồng thủ công", contractDto.getUserId());
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
            throw new IllegalArgumentException("Yêu cầu họ và tên để tạo hợp đồng");
        }
        
        if (contractDto.getEmail() != null && !contractDto.getEmail().trim().isEmpty()) {
            contract.setEmail(contractDto.getEmail());
        } else if (user != null) {
            contract.setEmail(user.getEmail());
        } else {
            throw new IllegalArgumentException("Yêu cầu email để tạo hợp đồng");
        }
        
        if (contractDto.getPhoneNumber() != null && !contractDto.getPhoneNumber().trim().isEmpty()) {
            contract.setPhoneNumber(contractDto.getPhoneNumber());
        } else if (user != null) {
            contract.setPhoneNumber(user.getPhoneNumber());
        }
        
        // Ensure required fields are set
        if (contract.getContractType() == null || contract.getContractType().trim().isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu loại hợp đồng");
        }
        
        if (contract.getPosition() == null || contract.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu vị trí");
        }
        
        // Validate mutually exclusive salary logic
        boolean hasHourly = contract.getHourlySalary() != null && contract.getHourlySalary() > 0;
        boolean hasGross = contract.getGrossSalary() != null && contract.getGrossSalary() > 0;
        boolean hasNet = contract.getNetSalary() != null && contract.getNetSalary() > 0;
        boolean hasGrossNet = hasGross || hasNet;
        
        // Ensure exactly one salary type is provided
        if (!hasHourly && !hasGrossNet) {
            throw new IllegalArgumentException("Phải cung cấp lương theo giờ hoặc lương gross/net");
        }
        
        if (hasHourly && hasGrossNet) {
            throw new IllegalArgumentException("Không thể đồng thầm có lương theo giờ và lương gross/net. Chúng loại trừ nhau");
        }
        
        // If gross/net is provided, both should be provided (they go together)
        if (hasGrossNet && (!hasGross || !hasNet)) {
            throw new IllegalArgumentException("Phải cung cấp cả lương gross và net cùng nhau");
        }
        
        // Set the legacy salary field for backward compatibility
        if (hasHourly) {
            contract.setSalary(contract.getHourlySalary().doubleValue());
        } else if (hasGross) {
            contract.setSalary(contract.getGrossSalary().doubleValue());
        }
        
        // Set contract status to PENDING by default for new contracts
        contract.setStatus("PENDING");

        // Set contract start and end dates
        setContractDates(contract, user);
        
        Contract savedContract = contractRepository.save(contract);
        log.info("Contract created successfully with id: {}", savedContract.getId());
        
        // Automatically unlock user account ONLY. Do NOT assign role here.
        try {
            String email = savedContract.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                User unlockedUser = userRepository.findByEmail(email).orElse(null);
                if (unlockedUser != null) {
                    // Mark account as active/unlocked without changing role
                    unlockedUser.setStatus("active");
                    userRepository.save(unlockedUser);
                    log.info("User account unlocked (no role changes) for email: {}", email);
                }
            }
        } catch (Exception e) {
            log.warn("Mở khóa tài khoản người dùng thất bại cho email: {}. Lỗi: {}", 
                    savedContract.getEmail(), e.getMessage());
            // Continue execution - contract creation should not fail if unlocking fails
        }
        
        return convertToDto(savedContract);
    }

    @Override
    public ContractDto updateContract(Long id, ContractDto contractDto) {
        log.info("Updating contract with id: {}", id);
        
        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng với ID: " + id));
        
        // Date validation removed - start/end dates no longer required for contract updates
        
        // Log which fields are being restricted from update
        log.info("Contract update - Preserving read-only fields: fullName, email, phoneNumber, contractType, position, department, salary, workingHours, offer, evaluation, grossSalary, netSalary (hourlySalary only editable right after renewal)");
        
        // ONLY UPDATE EDITABLE FIELDS (as per frontend restrictions):
        // - birthDate, citizenId, address, qualification, subject, educationLevel
        // - contractTerms, status (date fields removed)
        
        // Update editable personal information fields
        if (contractDto.getBirthDate() != null) {
            existingContract.setBirthDate(contractDto.getBirthDate());
        }
        
        if (contractDto.getCitizenId() != null && !contractDto.getCitizenId().trim().isEmpty()) {
            existingContract.setCitizenId(contractDto.getCitizenId());
        }
        
        if (contractDto.getAddress() != null && !contractDto.getAddress().trim().isEmpty()) {
            existingContract.setAddress(contractDto.getAddress());
        }
        
        // Update editable professional information fields
        if (contractDto.getQualification() != null && !contractDto.getQualification().trim().isEmpty()) {
            existingContract.setQualification(contractDto.getQualification());
        }
        
        if (contractDto.getSubject() != null && !contractDto.getSubject().trim().isEmpty()) {
            existingContract.setSubject(contractDto.getSubject());
        }
        
        if (contractDto.getClassLevel() != null && !contractDto.getClassLevel().trim().isEmpty()) {
            existingContract.setClassLevel(contractDto.getClassLevel());
        }
        
        // Update working schedule fields (editable for teachers)
        if (contractDto.getWorkSchedule() != null) {
            existingContract.setWorkSchedule(contractDto.getWorkSchedule());
        }
        
        if (contractDto.getWorkShifts() != null) {
            existingContract.setWorkShifts(contractDto.getWorkShifts());
        }
        
        if (contractDto.getWorkDays() != null) {
            existingContract.setWorkDays(contractDto.getWorkDays());
        }
        
        // Allow updating hourly salary ONLY right after renewal
        // Rule: status must be ACTIVE AND startDate must equal today (renew set startDate = today)
        if (contractDto.getHourlySalary() != null) {
            LocalDate today = LocalDate.now();
            boolean justRenewed =
                "ACTIVE".equals(existingContract.getStatus()) &&
                existingContract.getStartDate() != null &&
                existingContract.getStartDate().isEqual(today);

            if (!justRenewed) {
                log.warn("Denied hourlySalary update for contract {} - Only allowed immediately after renewal (ACTIVE + startDate = today)", existingContract.getId());
                throw new IllegalStateException("Chỉ được phép cập nhật lương theo giờ ngay sau khi 'Ký lại' (trạng thái ACTIVE và ngày bắt đầu bằng hôm nay). Hãy bấm 'Ký lại' trước.");
            }

            Long newHourly = contractDto.getHourlySalary();
            if (newHourly != null && newHourly > 0) {
                existingContract.setHourlySalary(newHourly);
                // Clear gross/net when hourly is used
                existingContract.setGrossSalary(null);
                existingContract.setNetSalary(null);
                // Sync legacy salary field for backward compatibility
                existingContract.setSalary(newHourly.doubleValue());
                log.info("Updated hourlySalary to {} VND/hour after renewal (cleared gross/net, synced salary)", newHourly);
            } else {
                // If provided 0 or null -> clear hourly salary
                existingContract.setHourlySalary(null);
                log.info("Cleared hourlySalary after renewal due to non-positive value in payload");
            }
        }
        
        // Update comments field (editable)
        if (contractDto.getComments() != null) {
            existingContract.setComments(contractDto.getComments());
        }
        
        // Date fields removed from contract updates
        
        if (contractDto.getStatus() != null && !contractDto.getStatus().trim().isEmpty()) {
            existingContract.setStatus(contractDto.getStatus());
        }
        
        if (contractDto.getContractTerms() != null) {
            existingContract.setContractTerms(contractDto.getContractTerms());
        }
        
        // DO NOT UPDATE THESE READ-ONLY/HIDDEN FIELDS:
        // - fullName, email, phoneNumber (read-only in frontend)
        // - contractType, position, department (removed from frontend)
        // - salary, workingHours (read-only/removed in frontend)
        // - offer, evaluation (read-only in frontend)
        
        Contract updatedContract = contractRepository.save(existingContract);
        log.info("Cập nhật hợp đồng thành công với id: {} - Chỉ cập nhật trường được phép, giữ nguyên trường chỉ đọc", updatedContract.getId());
        
        return convertToDto(updatedContract);
    }

    @Override
    public void deleteContract(Long id) {
        log.info("Deleting contract with id: {}", id);
        
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng với ID: " + id));
        
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
        log.info("🔧 FIXED: Fetching candidates ready for contract creation from recruitment applications with status HIRED");
        
        // 🔧 FIX: Use RecruitmentApplicationService instead of InterviewScheduleService
        // Interview schedules with status "APPROVED" are deleted immediately after approval
        // We need to get recruitment applications with status "HIRED" instead
        List<RecruitmentApplicationDto> hiredCandidates = recruitmentApplicationService.getAllApplications().stream()
                .filter(application -> "HIRED".equals(application.getStatus()))
                .collect(Collectors.toList());
        
        log.info("🔧 FIXED: Found {} hired candidates from recruitment applications", hiredCandidates.size());
        
        // Lấy tất cả hợp đồng hiện tại
        List<Contract> allContracts = contractRepository.findAll();
        log.info("Found {} existing contracts", allContracts.size());
        
        // Lọc những người chưa có hợp đồng
        List<ContractDto> candidates = hiredCandidates.stream()
                .filter(application -> {
                    // Kiểm tra xem ứng viên đã có hợp đồng chưa
                    String applicantEmail = application.getEmail();
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
                .map(application -> {
                    ContractDto candidate = new ContractDto();
                    
                    // 🔧 FIX: Use recruitment application ID instead of interview ID
                    candidate.setId(application.getId()); // Recruitment application ID
                    candidate.setUserId(application.getId()); // Also set userId for compatibility
                    
                    // Lấy thông tin cơ bản từ recruitment application
                    candidate.setFullName(application.getFullName());
                    candidate.setEmail(application.getEmail());
                    candidate.setPhoneNumber(application.getPhoneNumber() != null ? application.getPhoneNumber() : "Chưa có");
                    candidate.setPosition(application.getJobTitle());
                    
                    // Không tự động gán loại hợp đồng hay lương mặc định nữa.
                    // FE sẽ cho phép người dùng chọn loại hợp đồng (TEACHER/STAFF) và lương lấy từ Offer API khi mở modal.
                    candidate.setContractType(null);
                    candidate.setSalary(null);
                    
                    return candidate;
                })
                .collect(Collectors.toList());
        
        log.info("🔧 FIXED: Found {} candidates ready for contract from recruitment applications", candidates.size());
        return candidates;
    }

    @Override
    public ContractDto getCandidateOfferData(Long candidateId) {
        log.info("🔍 DEBUG: Starting getCandidateOfferData for recruitment application ID: {}", candidateId);
        try {
            // 🔧 FIX: candidateId is actually a recruitment application ID, not interview ID
            // First get the recruitment application to get candidate email
            log.info("🔍 DEBUG: Fetching recruitment application with ID: {}", candidateId);
            RecruitmentApplicationDto application = recruitmentApplicationService.getApplication(candidateId);
            if (application == null) {
            log.error("❌ LỖI: Không tìm thấy đơn ứng tuyển với ID: {}", candidateId);
                throw new ResourceNotFoundException("Recruitment application not found for ID: " + candidateId);
            }
            
            log.info("✅ DEBUG: Found recruitment application - ID: {}, Email: {}, Name: {}", 
                    application.getId(), application.getEmail(), application.getFullName());
            
            // Tìm interview bằng applicationId thay vì email và không giới hạn trạng thái
            log.info("🔍 DEBUG: Fetching interviews by applicationId: {}", application.getId());
            List<InterviewScheduleDto> interviewsByApp = interviewScheduleService.getByApplication(application.getId());
            log.info("🔍 DEBUG: Total interviews found for application {}: {}", application.getId(), interviewsByApp.size());

            // Chọn interview mới nhất (ưu tiên id lớn nhất nếu không có sort)
            InterviewScheduleDto interview = interviewsByApp.stream()
                    .max((a, b) -> Long.compare(a.getId(), b.getId()))
                    .orElse(null);
            
            // Chuẩn bị offerData mặc định với mutually exclusive salary fields = null
            ContractDto offerData = new ContractDto();
            offerData.setComments("Chưa có đánh giá");
            offerData.setHourlySalary(null);
            offerData.setGrossSalary(null);
            offerData.setNetSalary(null);

            if (interview == null) {
                // KHÔNG NÊN THROW: Interview APPROVED đã bị xóa sau duyệt. Trả về dữ liệu mặc định để FE mở modal.
                log.warn("⚠️ No approved/hired interview found for email {}. Returning default offer payload with null salaries.", application.getEmail());
                return offerData;
            }

            log.info("✅ DEBUG: Found matching interview - ID: {}, Status: {}, Offer: {}", 
                    interview.getId(), interview.getStatus(), interview.getOffer());

            // Lấy đánh giá từ interview evaluation field (nếu có)
            String evaluation = "Chưa có đánh giá";
            if ("APPROVED".equals(interview.getStatus())) {
                evaluation = "Đạt yêu cầu - Được phê duyệt";
            }
            if (interview.getEvaluation() != null && !interview.getEvaluation().trim().isEmpty()) {
                evaluation = interview.getEvaluation();
            }
            offerData.setComments(evaluation);

             log.info("🔍 PROCESSING: Analyzing offer data for candidate ID: {}", candidateId);

              String jobTitleLower = application.getJobTitle() != null ? application.getJobTitle().toLowerCase() : "";
            boolean isTeacher = jobTitleLower.contains("giáo viên") || jobTitleLower.contains("teacher");

            Long extractedHourly = extractHourlySalaryFromOffer(interview);
            if (extractedHourly != null && extractedHourly > 0) {
                offerData.setHourlySalary(extractedHourly);
                log.info("✅ HOURLY SALARY: Set hourly: {} VND/hour, gross/net remain null", extractedHourly);
            } else if (interview.getOffer() != null && !interview.getOffer().trim().isEmpty()) {
                if (isTeacher) {
                    Long hourlyFromGross = calculateHourlySalaryFromGross(interview);
                    if (hourlyFromGross != null && hourlyFromGross > 0) {
                        offerData.setHourlySalary(hourlyFromGross);
                        log.info("✅ HOURLY FROM GROSS: Set hourly: {} VND/hour for teacher, keep gross/net null", hourlyFromGross);
                    } else {
                        log.info("⚠️ TEACHER: Could not compute hourly from gross; leaving all salaries null");
                    }
                } else {
                     try {
                        String cleanOffer = interview.getOffer().replaceAll("[^0-9]", "");
                        if (!cleanOffer.isEmpty()) {
                            BigDecimal grossSalary = new BigDecimal(cleanOffer);
                            TopCVCalculation.SalaryCalculationResult salaryResult = 
                                TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
                            offerData.setGrossSalary(salaryResult.getGrossSalary().longValue());
                            offerData.setNetSalary(salaryResult.getNetSalary().longValue());
                            log.info("✅ GROSS/NET SALARY: Set gross: {} VND, net: {} VND, hourly remains null", 
                                    offerData.getGrossSalary(), offerData.getNetSalary());
                        } else {
                            log.info("⚠️ NO VALID OFFER: All salary fields remain null");
                        }
                    } catch (NumberFormatException e) {
            log.warn("Định dạng số tiền offer không hợp lệ cho ứng viên ID: {}, để trống tất cả trường lương", candidateId);
                    }
                }
            } else {
            log.info("⚠️ KHÔNG CÓ DỮ LIỆU OFFER: Để trống tất cả trường lương");
            }

            log.info("🔄 REFACTORED: Mutually exclusive salary data for candidate ID: {} - Hourly: {}, Gross: {}, Net: {}", 
                    candidateId, offerData.getHourlySalary(), offerData.getGrossSalary(), offerData.getNetSalary());
            return offerData;
        } catch (ResourceNotFoundException e) {
            throw e; // Re-throw ResourceNotFoundException
        } catch (Exception e) {
            log.error("Lỗi khi lấy dữ liệu offer cho ứng viên ID {}: ", candidateId, e);
            throw new RuntimeException("Lấy dữ liệu offer thất bại cho ứng viên ID: " + candidateId, e);
        }
    }
    
    private void setDefaultOfferData(ContractDto offerData) {
        offerData.setComments("Chưa có đánh giá");
        offerData.setGrossSalary(0L);
        offerData.setNetSalary(0L);
        offerData.setHourlySalary(0L);
    }
    
    private Long extractHourlySalaryFromOffer(InterviewScheduleDto interview) {
        log.info("🔍 EXTRACTING: Hourly salary from Offer Management for interview: {}", interview.getId());
        
        // Ưu tiên dùng trường hourlyRate nếu có trong InterviewScheduleDto
        try {
            if (interview.getHourlyRate() != null && interview.getHourlyRate().longValue() > 0) {
                Long hourly = interview.getHourlyRate().setScale(0, RoundingMode.HALF_UP).longValue();
                log.info("✅ EXTRACTED: Hourly salary from hourlyRate field: {} VND/hour", hourly);
                return hourly;
            }

           if (interview.getOffer() != null && !interview.getOffer().trim().isEmpty()) {
                String offer = interview.getOffer().toLowerCase();
                
                // Tìm kiếm pattern "xxx/giờ" hoặc "xxx/hour" trong offer string
                if (offer.contains("/giờ") || offer.contains("/hour")) {
                    String[] parts = offer.split("/");
                    if (parts.length > 0) {
                        String hourlyPart = parts[0].replaceAll("[^0-9]", "");
                        if (!hourlyPart.isEmpty()) {
                            Long hourlySalary = Long.parseLong(hourlyPart);
                            log.info("✅ EXTRACTED: Direct hourly salary from offer: {} VND/hour", hourlySalary);
                            return hourlySalary;
                        }
                    }
                }
            }
            
            log.info("❌ NO DIRECT: Hourly salary not found in offer string");
            return null;
            
        } catch (Exception e) {
            log.warn("Error extracting hourly salary from offer: {}", e.getMessage());
            return null;
        }
    }
    
     private Long calculateHourlySalaryFromGross(InterviewScheduleDto interview) {
        log.info("📊 CALCULATING: Hourly salary from gross for interview: {}", interview.getId());
        
        try {
            if (interview.getOffer() != null && !interview.getOffer().trim().isEmpty()) {
                String cleanOffer = interview.getOffer().replaceAll("[^0-9]", "");
                if (!cleanOffer.isEmpty()) {
                    BigDecimal grossSalary = new BigDecimal(cleanOffer);
                    
                     BigDecimal hourlyRate = grossSalary.divide(new BigDecimal("176"), 0, RoundingMode.HALF_UP);
                    Long hourlySalary = hourlyRate.longValue();
                    
                    log.info("📊 CALCULATED: Hourly salary from gross {} VND -> {} VND/hour", grossSalary, hourlySalary);
                    return hourlySalary;
                }
            }
            
            // Default hourly salary for teachers
            Long defaultHourly = 100000L; // 100,000 VND/hour
            log.info("⚠️ DEFAULT: Using default hourly salary: {} VND/hour", defaultHourly);
            return defaultHourly;
            
        } catch (Exception e) {
            log.warn("Error calculating hourly salary from gross: {}", e.getMessage());
            return 100000L; // Default fallback
        }
    }
    
     private void setDefaultStaffSalary(ContractDto offerData) {
        log.info("⚠️ SETTING: Default staff salary");
        offerData.setGrossSalary(15000000L); // 15 triệu VND gross
        offerData.setNetSalary(12000000L);   // 12 triệu VND net
        offerData.setHourlySalary(null);     // Không có lương theo giờ cho nhân viên
    }
    
    /**
     * Set contract start and end dates based on contract type and teacher's earliest lesson
     */
    private void setContractDates(Contract contract, User user) {
        log.info("Setting contract dates with fixed 90-day duration (new = default 01/09 current year).");

        // Rule: endDate must be exactly 90 days after startDate
        LocalDate startDate = contract.getStartDate();
        if (startDate == null) {
            // For NEW contracts: default start to September 1st of the current year
            LocalDate today = LocalDate.now();
            startDate = LocalDate.of(today.getYear(), 9, 1);
        }
        LocalDate endDate = startDate.plusDays(90);

        contract.setStartDate(startDate);
        contract.setEndDate(endDate);

        log.info("Contract dates set - Start: {}, End (+90d): {}", startDate, endDate);
    }
    
    /**
     * Find the earliest teaching date for a teacher based on their lessons and classes
     */
    private LocalDate findEarliestTeachingDate(Long teacherId) {
        if (teacherId == null) {
            log.warn("Teacher ID is null, cannot find earliest teaching date");
            return null;
        }
        
        log.info("Finding earliest teaching date for teacher ID: {}", teacherId);
        
        // First, try to find earliest actual lesson date
        List<ClassLesson> lessons = classLessonRepository.findByTeacherIdOrderByActualDateAsc(teacherId);
        if (!lessons.isEmpty()) {
            LocalDate earliestLessonDate = lessons.get(0).getActualDate();
            if (earliestLessonDate != null) {
                log.info("Found earliest lesson date: {} for teacher ID: {}", earliestLessonDate, teacherId);
                return earliestLessonDate;
            }
        }
        
        // Fallback: find earliest class start date for this teacher
        List<ClassEntity> classes = classRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
        LocalDate earliestClassDate = null;
        for (ClassEntity classEntity : classes) {
            if (classEntity.getStartDate() != null) {
                if (earliestClassDate == null || classEntity.getStartDate().isBefore(earliestClassDate)) {
                    earliestClassDate = classEntity.getStartDate();
                }
            }
        }
        
        if (earliestClassDate != null) {
            log.info("Found earliest class start date: {} for teacher ID: {}", earliestClassDate, teacherId);
            return earliestClassDate;
        }
        
        log.info("No teaching dates found for teacher ID: {}", teacherId);
        return null;
    }
    
    /**
     * Automatically update contract status based on end date.
     * EXPIRED: endDate < today
     * NEAR_EXPIRY: 0 <= daysUntilEnd <= 30
     * ACTIVE: daysUntilEnd > 30
     */
    private void updateContractStatusBasedOnEndDate(Contract contract) {
        try {
            if (contract == null) return;
            LocalDate endDate = contract.getEndDate();
            LocalDate startDate = contract.getStartDate();
            if (endDate == null) return;
            LocalDate today = LocalDate.now();
            // Preserve PENDING status for future contracts
            if ("PENDING".equals(contract.getStatus())) {
                if (startDate != null && startDate.isAfter(today)) {
                    // Do not override PENDING if it hasn't started yet
                    return;
                }
            }
            String newStatus;
            if (endDate.isBefore(today)) {
                newStatus = "EXPIRED";
            } else {
                long days = ChronoUnit.DAYS.between(today, endDate);
                newStatus = (days <= 30) ? "NEAR_EXPIRY" : "ACTIVE";
            }
            if (newStatus != null && !newStatus.equals(contract.getStatus())) {
                contract.setStatus(newStatus);
                // Persist the status change so subsequent reads reflect it
                contractRepository.save(contract);
                log.info("Auto-updated contract ID {} status to {} based on end date {}", contract.getId(), newStatus, endDate);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-update contract status for ID {}: {}", contract != null ? contract.getId() : null, e.getMessage());
        }
    }
    
    private String generateNextContractId() {
        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();
        
        // Tạo MM/YYYY format cho ID mới
        String monthYear = String.format("%02d/%04d", today.getMonthValue(), today.getYear());
        
        // Lấy tất cả hợp đồng hiện tại để kiểm tra mã đã dùng
        List<Contract> allContracts = contractRepository.findAll();
        
        // Thu thập các mã 2 chữ số đã dùng cho tháng/năm này
        Set<String> usedCodes = new HashSet<>();
        String contractIdPrefix = "HĐLĐ-CT36-";
        String targetMonthYear = monthYear;
        
        for (Contract contract : allContracts) {
            String contractId = contract.getContractId();
            if (contractId != null && contractId.startsWith(contractIdPrefix)) {
                // Parse format: HĐLĐ-CT36-XXMM/YYYY
                String suffix = contractId.substring(contractIdPrefix.length());
                if (suffix.length() >= 7) { // XX + MM/YYYY = 7 chars minimum
                    String code = suffix.substring(0, 2);
                    String idMonthYear = suffix.substring(2);
                    if (targetMonthYear.equals(idMonthYear)) {
                        usedCodes.add(code);
                    }
                }
            }
        }
        
        // Tìm mã 2 chữ số chưa dùng (10-99)
        String uniqueCode = null;
        for (int i = 10; i <= 99; i++) {
            String code = String.format("%02d", i);
            if (!usedCodes.contains(code)) {
                uniqueCode = code;
                break;
            }
        }
        
        // Nếu hết mã 10-99, dùng 00-09
        if (uniqueCode == null) {
            for (int i = 0; i <= 9; i++) {
                String code = String.format("%02d", i);
                if (!usedCodes.contains(code)) {
                    uniqueCode = code;
                    break;
                }
            }
        }
        
        // Fallback: random nếu hết mã
        if (uniqueCode == null) {
            uniqueCode = String.format("%02d", (int)(Math.random() * 90) + 10);
        }
        
        // Tạo Contract ID theo format mới: HĐLĐ-CT36-XXMM/YYYY
        String contractId = contractIdPrefix + uniqueCode + monthYear;
        
        log.info("Generated Contract ID: {} (code: {}, month/year: {}) for date: {}", 
                contractId, uniqueCode, monthYear, today);
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

    @Override
    public ContractDto renewContract(Long contractId) {
        log.info("Renewing contract with id: {}", contractId);
        
        Contract existingContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng với ID: " + contractId));
        
        // Set new start date to current date (renewal date)
        LocalDate renewalDate = LocalDate.now();
        existingContract.setStartDate(renewalDate);
        
        // Set new end date (90 days from renewal date)
        LocalDate newEndDate = renewalDate.plusDays(90);
        existingContract.setEndDate(newEndDate);
        
        // Reset status to ACTIVE upon renewal
        existingContract.setStatus("ACTIVE");
        
        Contract renewedContract = contractRepository.save(existingContract);
        log.info("Contract renewed successfully - ID: {}, New start date: {}, New end date: {}", 
                contractId, renewalDate, newEndDate);
        
        return convertToDto(renewedContract);
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
        
        // Map separate salary fields from Offer Management
        contract.setGrossSalary(contractDto.getGrossSalary());
        contract.setNetSalary(contractDto.getNetSalary());
        contract.setHourlySalary(contractDto.getHourlySalary());
        
        contract.setWorkingHours(contractDto.getWorkingHours());
        // Contract duration fields
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
        contract.setClassLevel(contractDto.getClassLevel());
        // New working schedule fields
        contract.setComments(contractDto.getComments());
        contract.setWorkSchedule(contractDto.getWorkSchedule());
        contract.setWorkShifts(contractDto.getWorkShifts());
        contract.setWorkDays(contractDto.getWorkDays());
        return contract;
    }

    @Override
    public List<ContractDto> createContractsForActiveTeachers(Long minHourly, Long maxHourly, boolean dryRun) {
        log.info("[BULK-TEACHERS] Start {} for active teachers. Range: {} - {} VND/hour", dryRun ? "DRY-RUN" : "CREATE", minHourly, maxHourly);
        long min = (minHourly == null || minHourly <= 0) ? 300000L : minHourly;
        long max = (maxHourly == null || maxHourly <= 0) ? 600000L : maxHourly;
        if (max < min) { long tmp = max; max = min; min = tmp; }

        List<User> teachers = userRepository.findActiveTeachers();
        List<ContractDto> results = new java.util.ArrayList<>();
        int skippedExisting = 0;

        for (User u : teachers) {
            if (u == null || u.getId() == null) continue;
            // Skip if the user already has any contract
            if (contractRepository.existsByUserId(u.getId())) { skippedExisting++; continue; }

            long hourly = min + (long) (Math.random() * (max - min + 1));
            ContractDto dto = new ContractDto();
            dto.setUserId(u.getId());
            dto.setFullName(u.getFullName());
            dto.setEmail(u.getEmail());
            dto.setPhoneNumber(u.getPhoneNumber());
            dto.setContractType("TEACHER");
            dto.setPosition("Teacher");
            dto.setHourlySalary(hourly);
            dto.setGrossSalary(null);
            dto.setNetSalary(null);
            dto.setStatus("ACTIVE");

            if (dryRun) {
                // Simulate contract creation without persisting
                Contract entity = convertToEntity(dto);
                String previewId = generateNextContractId();
                entity.setContractId(previewId);
                setContractDates(entity, u);

                // Build preview DTO manually to avoid convertToDto (which may persist status changes)
                ContractDto preview = new ContractDto();
                preview.setUserId(entity.getUserId());
                preview.setContractId(entity.getContractId());
                preview.setFullName(entity.getFullName());
                preview.setEmail(entity.getEmail());
                preview.setPhoneNumber(entity.getPhoneNumber());
                preview.setContractType(entity.getContractType());
                preview.setPosition(entity.getPosition());
                preview.setHourlySalary(entity.getHourlySalary());
                preview.setStatus(entity.getStatus());
                preview.setStartDate(entity.getStartDate());
                preview.setEndDate(entity.getEndDate());
                results.add(preview);
            } else {
                // Persist using existing validated flow
                ContractDto created = createContract(dto);
                results.add(created);
            }
        }
        // After creating contracts in this run, assign the required status/date distribution to the first 24 created
        if (!dryRun) {
            List<Contract> createdContracts = results.stream()
                    .filter(r -> r != null && r.getId() != null)
                    .map(r -> contractRepository.findById(r.getId()).orElse(null))
                    .filter(c -> c != null)
                    .collect(Collectors.toList());

            int total = Math.min(24, createdContracts.size());
            if (total > 0) {
                LocalDate today = LocalDate.now();
                for (int i = 0; i < total; i++) {
                    Contract c = createdContracts.get(i);
                    if (i < 10) {
                        // 10 ACTIVE: start 30 days ago, 90-day duration
                        LocalDate s = today.minusDays(30);
                        c.setStartDate(s);
                        c.setEndDate(s.plusDays(90));
                        c.setStatus("ACTIVE");
                    } else if (i < 15) {
                        // 5 EXPIRED: ended 30 days ago, 90-day duration
                        LocalDate s = today.minusDays(120); // end = s+90 = today-30
                        c.setStartDate(s);
                        c.setEndDate(s.plusDays(90));
                        c.setStatus("EXPIRED");
                    } else if (i < 20) {
                        // 5 NEAR_EXPIRY: end in 15 days, 90-day duration
                        LocalDate s = today.minusDays(75); // end = s+90 = today+15
                        c.setStartDate(s);
                        c.setEndDate(s.plusDays(90));
                        c.setStatus("NEAR_EXPIRY");
                    } else if (i < 24) {
                        // 4 PENDING (future start), 90-day duration
                        LocalDate s = today.plusDays(30);
                        c.setStartDate(s);
                        c.setEndDate(s.plusDays(90));
                        c.setStatus("PENDING");
                    }
                    contractRepository.save(c);
                }
                log.info("[BULK-TEACHERS] Applied status/date distribution (each 90-day) to {} newly created contracts (max 24 this run)", total);
            } else {
                log.info("[BULK-TEACHERS] No newly created contracts to apply status/date distribution.");
            }
        }

        log.info("[BULK-TEACHERS] {} complete. Total teachers: {}, created: {}, skipped(existing): {}",
                dryRun ? "DRY-RUN" : "CREATE", teachers.size(), results.size(), skippedExisting);
        return results;
    }

    @Override
    public void reseedContractStatuses() {
        log.info("RESEEDING CONTRACT STATUSES FOR DEMO");
        List<Contract> contracts = contractRepository.findAll();

        if (contracts.size() < 24) {
            log.warn("Cannot reseed, found only {} contracts, need at least 24.", contracts.size());
            return;
        }

        LocalDate today = LocalDate.now();

        // 10 ACTIVE contracts
        for (int i = 0; i < 10; i++) {
            Contract contract = contracts.get(i);
            contract.setStartDate(today.minusMonths(2));
            contract.setEndDate(today.plusYears(1));
            contract.setStatus("ACTIVE");
            contractRepository.save(contract);
        }

        // 5 EXPIRED contracts
        for (int i = 10; i < 15; i++) {
            Contract contract = contracts.get(i);
            contract.setStartDate(today.minusYears(1));
            contract.setEndDate(today.minusMonths(1));
            contract.setStatus("EXPIRED");
            contractRepository.save(contract);
        }

        // 5 NEAR_EXPIRY contracts
        for (int i = 15; i < 20; i++) {
            Contract contract = contracts.get(i);
            contract.setStartDate(today.minusMonths(11));
            contract.setEndDate(today.plusDays(15));
            contract.setStatus("NEAR_EXPIRY");
            contractRepository.save(contract);
        }

        // 4 PENDING contracts
        for (int i = 20; i < 24; i++) {
            Contract contract = contracts.get(i);
            contract.setStartDate(today.plusMonths(1));
            contract.setEndDate(today.plusYears(1).plusMonths(1));
            contract.setStatus("PENDING");
            contractRepository.save(contract);
        }

        log.info("Successfully reseeded statuses for 24 contracts.");
    }

    private ContractDto convertToDto(Contract contract) {
        if (contract == null) return null;
        // Auto-update contract status based on end date before converting
        updateContractStatusBasedOnEndDate(contract);

        ContractDto dto = new ContractDto();
        dto.setId(contract.getId());
        dto.setUserId(contract.getUserId());
        dto.setContractId(contract.getContractId());
        dto.setFullName(contract.getFullName());
        dto.setEmail(contract.getEmail());
        // Phone fallback: if contract phone is missing, try fetching from linked User
        String phone = contract.getPhoneNumber();
        if (phone == null || phone.trim().isEmpty()) {
            try {
                User u = null;
                if (contract.getUserId() != null && contract.getUserId() < 999999999L) {
                    u = userRepository.findById(contract.getUserId()).orElse(null);
                }
                if (u == null && contract.getEmail() != null && !contract.getEmail().trim().isEmpty()) {
                    u = userRepository.findByEmail(contract.getEmail()).orElse(null);
                }
                if (u != null && u.getPhoneNumber() != null && !u.getPhoneNumber().trim().isEmpty()) {
                    phone = u.getPhoneNumber();
                }
            } catch (Exception e) {
                log.warn("Phone fallback failed for contract ID {}: {}", contract.getId(), e.getMessage());
            }
        }
        dto.setPhoneNumber(phone);
        dto.setContractType(contract.getContractType());
        dto.setPosition(contract.getPosition());
        dto.setDepartment(contract.getDepartment());
        // Round legacy salary to whole number for consistent display
        if (contract.getSalary() != null) {
            dto.setSalary((double) Math.round(contract.getSalary()));
        } else {
            dto.setSalary(null);
        }
        
        // Map separate salary fields from Offer Management
        dto.setGrossSalary(contract.getGrossSalary());
        dto.setNetSalary(contract.getNetSalary());
        dto.setHourlySalary(contract.getHourlySalary());
        
        dto.setWorkingHours(contract.getWorkingHours());
        // Date field mapping removed
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
        dto.setClassLevel(contract.getClassLevel());
        // New working schedule fields
        dto.setComments(contract.getComments());
        dto.setWorkSchedule(contract.getWorkSchedule());
        dto.setWorkShifts(contract.getWorkShifts());
        dto.setWorkDays(contract.getWorkDays());
        
        // Contract duration fields
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        return dto;
    }

}
