package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.dto.absencemanagement.AbsenceDTO;
import com.classroomapp.classroombackend.dto.absencemanagement.CreateAbsenceDTO;
import com.classroomapp.classroombackend.dto.absencemanagement.TeacherLeaveInfoDTO;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Absence;
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AbsenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbsenceServiceImpl implements AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AbsenceDTO createAbsenceRequest(CreateAbsenceDTO createDto, Long userId) {
        log.info("Creating absence request for user: {}", userId);
        
        // Validate user exists and is a teacher or accountant
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Lấy ngày reset nghỉ phép từ hợp đồng chính thức (teacher) hoặc hireDate (accountant)
        LocalDate leaveResetDate = null;
        if (user.getRoleId() == RoleConstants.TEACHER) {
            // Lấy hợp đồng chính thức còn hiệu lực
            Optional<Contract> officialContract = contractRepository.findByUserIdAndContractTypeAndStatus(userId, "OFFICIAL", "ACTIVE");
            if (officialContract.isPresent()) {
                leaveResetDate = officialContract.get().getStartDate();
            } else {
                throw new BusinessLogicException("Chỉ giáo viên có hợp đồng chính thức mới được tạo đơn nghỉ phép");
            }
        } else if (user.getRoleId() == RoleConstants.ACCOUNTANT) {
            leaveResetDate = user.getHireDate();
        } else {
            throw new BusinessLogicException("Chỉ giáo viên có hợp đồng chính thức hoặc kế toán viên mới được tạo đơn nghỉ phép");
        }
        
        // Validate dates
        if (createDto.getStartDate().isAfter(createDto.getEndDate())) {
            throw new BusinessLogicException("Start date cannot be after end date");
        }
        
        if (createDto.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessLogicException("Cannot request leave for past dates");
        }
        
        // Calculate actual number of days (excluding weekends)
        long actualDays = calculateWorkingDays(createDto.getStartDate(), createDto.getEndDate());
        if (actualDays != createDto.getNumberOfDays()) {
            log.warn("Requested days {} don't match calculated working days {}", 
                createDto.getNumberOfDays(), actualDays);
        }
        
        // Check for overlapping leave requests
        if (absenceRepository.hasOverlappingLeave(userId, createDto.getStartDate(), createDto.getEndDate())) {
            throw new BusinessLogicException("Bạn đã có đơn nghỉ phép trùng với khoảng thời gian này. Vui lòng chọn ngày khác.");
        }
        
        // Check annual leave balance
        LocalDate leaveYearStart = leaveResetDate != null ? 
            leaveResetDate.minusYears(1) : user.getHireDate();
        LocalDate leaveYearEnd = leaveResetDate != null ? 
            leaveResetDate : leaveYearStart.plusYears(1);
            
        Integer usedDays = absenceRepository.calculateUsedLeaveDays(userId, leaveYearStart, leaveYearEnd);
        Integer pendingDays = absenceRepository.calculatePendingLeaveDays(userId);
        
        int totalRequestedDays = (usedDays != null ? usedDays : 0) + 
                               (pendingDays != null ? pendingDays : 0) + 
                               createDto.getNumberOfDays();
        
        boolean isOverLimit = totalRequestedDays > 12;
        
        // Create absence entity
        Absence absence = new Absence();
        absence.setUserId(userId);
        absence.setUserEmail(user.getEmail());
        absence.setUserFullName(user.getFullName());
        absence.setStartDate(createDto.getStartDate());
        absence.setEndDate(createDto.getEndDate());
        absence.setNumberOfDays(createDto.getNumberOfDays());
        absence.setDescription(createDto.getDescription());
        absence.setStatus("PENDING");
        absence.setIsOverLimit(isOverLimit);
        absence.setCreatedAt(LocalDateTime.now());
        
        Absence savedAbsence = absenceRepository.save(absence);
        log.info("Absence request created with id: {}", savedAbsence.getId());
        
        return convertToDto(savedAbsence);
    }

    @Override
    public List<AbsenceDTO> getMyAbsenceRequests(Long userId) {
        List<Absence> absences = absenceRepository.findByUserId(userId);
        return absences.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public AbsenceDTO getAbsenceById(Long absenceId, Long userId) {
        Absence absence = absenceRepository.findById(absenceId)
            .orElseThrow(() -> new ResourceNotFoundException("Absence not found with id: " + absenceId));
        
        // Ensure user can only access their own absence requests
        if (!absence.getUserId().equals(userId)) {
            throw new BusinessLogicException("You can only access your own absence requests");
        }
        
        return convertToDto(absence);
    }

    @Override
    public List<AbsenceDTO> getAllAbsenceRequests() {
        log.info("Fetching all absence requests");
        List<Absence> absences = absenceRepository.findAll();
        log.info("Found {} absence requests in database", absences.size());
        return absences.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<AbsenceDTO> getPendingAbsenceRequests() {
        List<Absence> pendingAbsences = absenceRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        return pendingAbsences.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<TeacherLeaveInfoDTO> getAllTeachersLeaveInfo() {
        // Get all teachers and accountants
        log.info("Fetching all teachers and accountants with role ID: {} and {}", RoleConstants.TEACHER, RoleConstants.ACCOUNTANT);
        List<User> employees = userRepository.findByRoleId(RoleConstants.TEACHER);
        employees.addAll(userRepository.findByRoleId(RoleConstants.ACCOUNTANT));
        log.info("Found {} employees (teachers + accountants)", employees.size());
        
        return employees.stream()
            .map(this::calculateTeacherLeaveInfo)
            .collect(Collectors.toList());
    }

    @Override
    public TeacherLeaveInfoDTO getTeacherLeaveInfo(Long employeeId) {
        User employee = userRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại với id: " + employeeId));
        
        if (employee.getRoleId() != RoleConstants.TEACHER && employee.getRoleId() != RoleConstants.ACCOUNTANT) {
            throw new BusinessLogicException("Người dùng không phải là giáo viên hoặc kế toán viên");
        }
        
        return calculateTeacherLeaveInfo(employee);
    }

    @Override
    @Transactional
    public AbsenceDTO approveAbsence(Long absenceId, Long managerId) {
        log.info("Approving absence request: {} by manager: {}", absenceId, managerId);
        
        Absence absence = absenceRepository.findById(absenceId)
            .orElseThrow(() -> new ResourceNotFoundException("Absence not found with id: " + absenceId));
        
        if (!"PENDING".equals(absence.getStatus())) {
            throw new BusinessLogicException("Only pending absence requests can be approved");
        }
        
        // Update absence status
        absence.setStatus("APPROVED");
        absence.setResultStatus("APPROVED");
        absence.setProcessedAt(LocalDateTime.now());
        absence.setProcessedBy(managerId);
        
        // Update user's leave balance if approved
        log.info("Looking for user with ID: {}", absence.getUserId());
        User user = userRepository.findById(absence.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + absence.getUserId()));
        
        log.info("Found user: {} with current leave balance: {}", user.getFullName(), user.getAnnualLeaveBalance());
        
        if (user.getAnnualLeaveBalance() != null) {
            int newBalance = user.getAnnualLeaveBalance() - absence.getNumberOfDays();
            user.setAnnualLeaveBalance(Math.max(0, newBalance)); // Don't go below 0
            userRepository.save(user);
            log.info("Updated user {} leave balance from {} to {}", user.getFullName(), 
                user.getAnnualLeaveBalance() + absence.getNumberOfDays(), user.getAnnualLeaveBalance());
        } else {
            log.warn("User {} has null annual leave balance", user.getFullName());
        }
        
        Absence savedAbsence = absenceRepository.save(absence);
        log.info("Absence request {} approved successfully", absenceId);
        
        return convertToDto(savedAbsence);
    }

    @Override
    @Transactional
    public AbsenceDTO rejectAbsence(Long absenceId, String reason, Long managerId) {
        log.info("Rejecting absence request: {} by manager: {}", absenceId, managerId);
        
        Absence absence = absenceRepository.findById(absenceId)
            .orElseThrow(() -> new ResourceNotFoundException("Absence not found with id: " + absenceId));
        
        if (!"PENDING".equals(absence.getStatus())) {
            throw new BusinessLogicException("Only pending absence requests can be rejected");
        }
        
        absence.setStatus("REJECTED");
        absence.setResultStatus("REJECTED");
        absence.setRejectReason(reason);
        absence.setProcessedAt(LocalDateTime.now());
        absence.setProcessedBy(managerId);
        
        Absence savedAbsence = absenceRepository.save(absence);
        log.info("Absence request {} rejected successfully", absenceId);
        
        return convertToDto(savedAbsence);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    @Transactional
    public void resetAnnualLeave() {
        log.info("Starting scheduled annual leave reset check");
        List<User> teachers = userRepository.findByRoleId(RoleConstants.TEACHER);
        List<User> accountants = userRepository.findByRoleId(RoleConstants.ACCOUNTANT);
        LocalDate today = LocalDate.now();
        for (User teacher : teachers) {
            Optional<Contract> officialContract = contractRepository.findByUserIdAndContractTypeAndStatus(teacher.getId(), "OFFICIAL", "ACTIVE");
            if (officialContract.isPresent() && officialContract.get().getStartDate().isEqual(today)) {
                resetUserAnnualLeave(teacher.getId());
            }
        }
        for (User accountant : accountants) {
            if (accountant.getHireDate() != null && accountant.getHireDate().isEqual(today)) {
                resetUserAnnualLeave(accountant.getId());
            }
        }
        log.info("Completed annual leave reset check");
    }

    @Override
    @Transactional
    public void resetUserAnnualLeave(Long userId) {
        log.info("Resetting annual leave for user: {}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (user.getRoleId() == RoleConstants.TEACHER) {
            Optional<Contract> officialContract = contractRepository.findByUserIdAndContractTypeAndStatus(userId, "OFFICIAL", "ACTIVE");
            if (officialContract.isPresent()) {
                user.setAnnualLeaveBalance(12);
                user.setLeaveResetDate(officialContract.get().getStartDate().plusYears(1));
                userRepository.save(user);
                log.info("Annual leave reset completed for teacher: {}", userId);
            }
        } else if (user.getRoleId() == RoleConstants.ACCOUNTANT) {
            user.setAnnualLeaveBalance(12);
            user.setLeaveResetDate(user.getHireDate().plusYears(1));
            userRepository.save(user);
            log.info("Annual leave reset completed for accountant: {}", userId);
        }
    }

    private TeacherLeaveInfoDTO calculateTeacherLeaveInfo(User employee) {
        TeacherLeaveInfoDTO info = new TeacherLeaveInfoDTO();
        info.setUserId(employee.getId());
        info.setEmail(employee.getEmail());
        info.setFullName(employee.getFullName());
        info.setPhoneNumber(employee.getPhoneNumber());
        info.setDepartment(employee.getDepartment());
        info.setAnnualLeaveBalance(employee.getAnnualLeaveBalance());
        info.setLeaveResetDate(employee.getLeaveResetDate());
        info.setHireDate(employee.getHireDate());
        
        // Calculate used and pending leave from database
        LocalDate leaveYearStart = employee.getLeaveResetDate() != null ? 
            employee.getLeaveResetDate().minusYears(1) : employee.getHireDate();
        LocalDate leaveYearEnd = employee.getLeaveResetDate() != null ? 
            employee.getLeaveResetDate() : leaveYearStart.plusYears(1);
            
        Integer dbUsedLeave = absenceRepository.calculateUsedLeaveDays(
            employee.getId(), leaveYearStart, leaveYearEnd);
        Integer pendingLeave = absenceRepository.calculatePendingLeaveDays(employee.getId());
        
        // Ensure non-null values
        int dbUsed = dbUsedLeave != null ? dbUsedLeave : 0;
        int pending = pendingLeave != null ? pendingLeave : 0;
        
        // Calculate actual used leave based on balance
        // Logic: usedDays = 12 - annualLeaveBalance (if positive) or 12 + |annualLeaveBalance| (if negative)
        Integer currentBalance = employee.getAnnualLeaveBalance();
        int actualUsedLeave;
        int overLimitDays = 0;
        
        if (currentBalance != null) {
            if (currentBalance >= 0) {
                // Normal case: still have balance
                actualUsedLeave = 12 - currentBalance;
            } else {
                // Over-limit case: negative balance means over 12 days
                actualUsedLeave = 12 + Math.abs(currentBalance);
                overLimitDays = Math.abs(currentBalance);
            }
        } else {
            // Fallback to DB calculation if balance is null
            actualUsedLeave = dbUsed;
            overLimitDays = Math.max(0, dbUsed + pending - 12);
        }
        
        info.setUsedLeave(actualUsedLeave);
        info.setPendingLeave(pending);
        info.setOverLimitDays(overLimitDays);
        
        log.debug("Employee {} - Balance: {}, Used: {}, Pending: {}, OverLimit: {}", 
                employee.getFullName(), currentBalance, actualUsedLeave, pending, overLimitDays);
        
        return info;
    }

    private AbsenceDTO convertToDto(Absence absence) {
        AbsenceDTO dto = modelMapper.map(absence, AbsenceDTO.class);
        return dto;
    }

    private long calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long weekends = 0;
        
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek().getValue() >= 6) { // Saturday = 6, Sunday = 7
                weekends++;
            }
            date = date.plusDays(1);
        }
        
        return totalDays - weekends;
    }
} 