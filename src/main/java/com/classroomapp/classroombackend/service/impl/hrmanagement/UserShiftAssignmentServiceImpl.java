package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateShiftAssignmentDto;
import com.classroomapp.classroombackend.dto.hrmanagement.UserShiftAssignmentDto;
import com.classroomapp.classroombackend.model.hrmanagement.UserShiftAssignment;
import com.classroomapp.classroombackend.model.hrmanagement.WorkShift;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.UserShiftAssignmentRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.WorkShiftRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.hrmanagement.UserShiftAssignmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of UserShiftAssignmentService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserShiftAssignmentServiceImpl implements UserShiftAssignmentService {
    
    private final UserShiftAssignmentRepository assignmentRepository;
    private final WorkShiftRepository workShiftRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public List<UserShiftAssignmentDto> createShiftAssignments(CreateShiftAssignmentDto createDto, Long createdBy) {
        log.info("Creating shift assignments for {} users", createDto.getUserIds().size());
        
        // Validate input
        if (!validateAssignmentData(createDto)) {
            throw new IllegalArgumentException("Dá»¯ liá»‡u phÃ¢n cÃ´ng ca lÃ m viá»‡c khÃ´ng há»£p lá»‡");
        }
        
        // Get shift
        WorkShift shift = workShiftRepository.findById(createDto.getShiftId())
            .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y ca lÃ m viá»‡c vá»›i ID: " + createDto.getShiftId()));
        
        if (!shift.isCurrentlyActive()) {
            throw new IllegalArgumentException("Ca lÃ m viá»‡c Ä‘Ã£ bá»‹ vÃ´ hiá»‡u hÃ³a");
        }
        
        List<UserShiftAssignment> assignments = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        
        // Process each user
        for (Long userId : createDto.getUserIds()) {
            try {
                // Get user
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng vá»›i ID: " + userId));
                
                // Check if user is eligible for shift assignment
                if (!user.isEligibleForShiftAssignment()) {
                    conflicts.add("NgÆ°á»i dÃ¹ng " + user.getFullName() + " khÃ´ng thá»ƒ Ä‘Æ°á»£c phÃ¢n cÃ´ng ca lÃ m viá»‡c (GiÃ¡o viÃªn)");
                    continue;
                }
                
                // Check for overlapping assignments
                List<UserShiftAssignment> overlapping = assignmentRepository.findOverlappingAssignments(
                    userId, createDto.getStartDate(), createDto.getEndDate());
                
                if (!overlapping.isEmpty()) {
                    conflicts.add("NgÆ°á»i dÃ¹ng " + user.getFullName() + " Ä‘Ã£ cÃ³ phÃ¢n cÃ´ng ca trÃ¹ng láº·p trong khoáº£ng thá»i gian nÃ y");
                    continue;
                }
                
                // Create assignment
                UserShiftAssignment assignment = new UserShiftAssignment();
                assignment.setUser(user);
                assignment.setWorkShift(shift);
                assignment.setStartDate(createDto.getStartDate());
                assignment.setEndDate(createDto.getEndDate());
                assignment.setNotes(createDto.getNotes());
                assignment.setCreatedBy(createdBy);
                assignment.setIsActive(true);
                
                assignments.add(assignment);
                
            } catch (Exception e) {
                log.error("Error processing user ID: {}", userId, e);
                conflicts.add("Lá»—i xá»­ lÃ½ ngÆ°á»i dÃ¹ng ID " + userId + ": " + e.getMessage());
            }
        }
        
        // If there are conflicts, throw exception with details
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("CÃ³ lá»—i trong quÃ¡ trÃ¬nh phÃ¢n cÃ´ng:\n" + String.join("\n", conflicts));
        }
        
        // Save all assignments
        List<UserShiftAssignment> savedAssignments = assignmentRepository.saveAll(assignments);
        log.info("Created {} shift assignments successfully", savedAssignments.size());
        
        return savedAssignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserShiftAssignmentDto updateShiftAssignment(Long id, CreateShiftAssignmentDto updateDto) {
        log.info("Updating shift assignment with ID: {}", id);
        
        UserShiftAssignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y phÃ¢n cÃ´ng ca vá»›i ID: " + id));
        
        // Validate input
        if (!validateAssignmentData(updateDto)) {
            throw new IllegalArgumentException("Dá»¯ liá»‡u phÃ¢n cÃ´ng ca lÃ m viá»‡c khÃ´ng há»£p lá»‡");
        }
        
        // For update, we only allow single user
        if (updateDto.getUserIds().size() != 1) {
            throw new IllegalArgumentException("Chá»‰ cÃ³ thá»ƒ cáº­p nháº­t phÃ¢n cÃ´ng cho má»™t ngÆ°á»i dÃ¹ng");
        }
        
        Long userId = updateDto.getUserIds().get(0);
        
        // Get shift
        WorkShift shift = workShiftRepository.findById(updateDto.getShiftId())
            .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y ca lÃ m viá»‡c vá»›i ID: " + updateDto.getShiftId()));
        
        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng vá»›i ID: " + userId));
        
        // Check for overlapping assignments (excluding current assignment)
        List<UserShiftAssignment> overlapping = assignmentRepository.findOverlappingAssignments(
            userId, updateDto.getStartDate(), updateDto.getEndDate(), id);
        
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("NgÆ°á»i dÃ¹ng Ä‘Ã£ cÃ³ phÃ¢n cÃ´ng ca trÃ¹ng láº·p trong khoáº£ng thá»i gian nÃ y");
        }
        
        // Update assignment
        assignment.setUser(user);
        assignment.setWorkShift(shift);
        assignment.setStartDate(updateDto.getStartDate());
        assignment.setEndDate(updateDto.getEndDate());
        assignment.setNotes(updateDto.getNotes());
        
        UserShiftAssignment updatedAssignment = assignmentRepository.save(assignment);
        log.info("Shift assignment updated successfully");
        
        return convertToDto(updatedAssignment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserShiftAssignmentDto getAssignmentById(Long id) {
        UserShiftAssignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y phÃ¢n cÃ´ng ca vá»›i ID: " + id));
        
        return convertToDto(assignment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserShiftAssignmentDto> getAssignmentsByUser(Long userId, boolean activeOnly) {
        List<UserShiftAssignment> assignments;
        
        if (activeOnly) {
            assignments = assignmentRepository.findByUserIdAndIsActiveTrueOrderByStartDateDesc(userId);
        } else {
            assignments = assignmentRepository.findByUserIdOrderByStartDateDesc(userId);
        }
        
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserShiftAssignmentDto> getAssignmentsByShift(Long shiftId, boolean activeOnly) {
        List<UserShiftAssignment> assignments;
        
        if (activeOnly) {
            assignments = assignmentRepository.findByWorkShiftIdAndIsActiveTrueOrderByStartDateDesc(shiftId);
        } else {
            assignments = assignmentRepository.findByWorkShiftIdOrderByStartDateDesc(shiftId);
        }
        
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserShiftAssignmentDto> getAssignments(Pageable pageable) {
        Page<UserShiftAssignment> assignments = assignmentRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        return assignments.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserShiftAssignmentDto> getAssignmentsInDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<UserShiftAssignment> assignments = assignmentRepository.findAssignmentsInDateRange(startDate, endDate, pageable);
        return assignments.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserShiftAssignmentDto getUserAssignmentForDate(Long userId, LocalDate date) {
        return assignmentRepository.findUserAssignmentForDate(userId, date)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserShiftAssignmentDto> getAssignmentsForDate(LocalDate date) {
        List<UserShiftAssignment> assignments = assignmentRepository.findActiveAssignmentsForDate(date);
        return assignments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteAssignment(Long id) {
        log.info("Deleting shift assignment with ID: {}", id);
        
        UserShiftAssignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y phÃ¢n cÃ´ng ca vá»›i ID: " + id));
        
        // Soft delete
        assignment.setIsActive(false);
        assignmentRepository.save(assignment);
        
        log.info("Shift assignment deleted successfully");
    }
    
    @Override
    public UserShiftAssignmentDto toggleAssignmentStatus(Long id, boolean isActive) {
        log.info("Toggling assignment status for ID: {} to {}", id, isActive);
        
        UserShiftAssignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y phÃ¢n cÃ´ng ca vá»›i ID: " + id));
        
        assignment.setIsActive(isActive);
        UserShiftAssignment updatedAssignment = assignmentRepository.save(assignment);
        
        return convertToDto(updatedAssignment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserShiftAssignmentDto> checkOverlappingAssignments(Long userId, LocalDate startDate, LocalDate endDate, Long excludeId) {
        List<UserShiftAssignment> overlapping;
        
        if (excludeId != null) {
            overlapping = assignmentRepository.findOverlappingAssignments(userId, startDate, endDate, excludeId);
        } else {
            overlapping = assignmentRepository.findOverlappingAssignments(userId, startDate, endDate);
        }
        
        return overlapping.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean validateAssignmentData(CreateShiftAssignmentDto createDto) {
        if (createDto == null) {
            return false;
        }
        
        // Check required fields
        if (createDto.getUserIds() == null || createDto.getUserIds().isEmpty()) {
            return false;
        }
        
        if (createDto.getShiftId() == null) {
            return false;
        }
        
        if (createDto.getStartDate() == null || createDto.getEndDate() == null) {
            return false;
        }
        
        // Check date range
        if (!createDto.isValidDateRange()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object> getEligibleUsersForShiftAssignment() {
        // Get all users except Teachers (roleId = 2)
        List<User> eligibleUsers = userRepository.findAll().stream()
                .filter(user -> user.isEligibleForShiftAssignment() && "active".equals(user.getStatus()))
                .collect(Collectors.toList());
        
        return eligibleUsers.stream()
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("fullName", user.getFullName());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("department", user.getDepartment());
                    userInfo.put("role", user.getRole());
                    return userInfo;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getAssignmentStatistics(Long userId) {
        long totalAssignments = assignmentRepository.countByUserIdAndIsActiveTrue(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("totalActiveAssignments", totalAssignments);
        
        return stats;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getShiftAssignmentStatistics(Long shiftId) {
        long totalAssignments = assignmentRepository.countByWorkShiftIdAndIsActiveTrue(shiftId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("shiftId", shiftId);
        stats.put("totalActiveAssignments", totalAssignments);
        
        return stats;
    }
    
    /**
     * Convert UserShiftAssignment entity to DTO
     */
    private UserShiftAssignmentDto convertToDto(UserShiftAssignment assignment) {
        UserShiftAssignmentDto dto = modelMapper.map(assignment, UserShiftAssignmentDto.class);
        
        // Set additional fields
        if (assignment.getUser() != null) {
            dto.setUserId(assignment.getUser().getId());
            dto.setUserFullName(assignment.getUser().getFullName());
            dto.setUserEmail(assignment.getUser().getEmail());
            dto.setUserDepartment(assignment.getUser().getDepartment());
        }
        
        if (assignment.getWorkShift() != null) {
            dto.setShiftId(assignment.getWorkShift().getId());
            dto.setShiftName(assignment.getWorkShift().getName());
            dto.setShiftTimeRange(assignment.getWorkShift().getStartTime() + " - " + assignment.getWorkShift().getEndTime());
        }
        
        // Set computed fields
        dto.setDurationInDays(assignment.getDurationInDays());
        dto.setIsCurrentlyValid(assignment.isValidForDate(LocalDate.now()));

        return dto;
    }
}
