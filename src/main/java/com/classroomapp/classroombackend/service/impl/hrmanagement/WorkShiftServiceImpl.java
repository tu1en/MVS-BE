package com.classroomapp.classroombackend.service.impl.hrmanagement;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateWorkShiftDto;
import com.classroomapp.classroombackend.dto.hrmanagement.WorkShiftDto;
import com.classroomapp.classroombackend.model.hrmanagement.WorkShift;
import com.classroomapp.classroombackend.repository.hrmanagement.WorkShiftRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.UserShiftAssignmentRepository;
import com.classroomapp.classroombackend.service.hrmanagement.WorkShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of WorkShiftService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkShiftServiceImpl implements WorkShiftService {
    
    private final WorkShiftRepository workShiftRepository;
    private final UserShiftAssignmentRepository assignmentRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public WorkShiftDto createShift(CreateWorkShiftDto createDto, Long createdBy) {
        log.info("Creating new work shift: {}", createDto.getName());
        
        // Validate input
        if (!validateShiftData(createDto)) {
            throw new IllegalArgumentException("Dữ liệu ca làm việc không hợp lệ");
        }
        
        // Check if name already exists
        if (!isShiftNameAvailable(createDto.getName(), null)) {
            throw new IllegalArgumentException("Tên ca làm việc đã tồn tại: " + createDto.getName());
        }
        
        // Create new shift
        WorkShift shift = new WorkShift();
        shift.setName(createDto.getName().trim());
        shift.setStartTime(createDto.getStartTime());
        shift.setEndTime(createDto.getEndTime());
        shift.setBreakHours(createDto.getBreakHours());
        shift.setDescription(createDto.getDescription());
        shift.setCreatedBy(createdBy);
        shift.setIsActive(true);
        
        WorkShift savedShift = workShiftRepository.save(shift);
        log.info("Work shift created successfully with ID: {}", savedShift.getId());
        
        return convertToDto(savedShift);
    }
    
    @Override
    public WorkShiftDto updateShift(Long id, CreateWorkShiftDto updateDto) {
        log.info("Updating work shift with ID: {}", id);
        
        WorkShift shift = workShiftRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc với ID: " + id));
        
        // Validate input
        if (!validateShiftData(updateDto)) {
            throw new IllegalArgumentException("Dữ liệu ca làm việc không hợp lệ");
        }
        
        // Check if name is available (excluding current shift)
        if (!isShiftNameAvailable(updateDto.getName(), id)) {
            throw new IllegalArgumentException("Tên ca làm việc đã tồn tại: " + updateDto.getName());
        }
        
        // Update shift data
        shift.setName(updateDto.getName().trim());
        shift.setStartTime(updateDto.getStartTime());
        shift.setEndTime(updateDto.getEndTime());
        shift.setBreakHours(updateDto.getBreakHours());
        shift.setDescription(updateDto.getDescription());
        
        WorkShift updatedShift = workShiftRepository.save(shift);
        log.info("Work shift updated successfully: {}", updatedShift.getName());
        
        return convertToDto(updatedShift);
    }
    
    @Override
    @Transactional(readOnly = true)
    public WorkShiftDto getShiftById(Long id) {
        WorkShift shift = workShiftRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc với ID: " + id));
        
        return convertToDto(shift);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WorkShiftDto> getAllActiveShifts() {
        List<WorkShift> shifts = workShiftRepository.findByIsActiveTrueOrderByName();
        return shifts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<WorkShiftDto> getShifts(Pageable pageable) {
        Page<WorkShift> shifts = workShiftRepository.findByIsActiveTrueOrderByName(pageable);
        return shifts.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<WorkShiftDto> searchShiftsByName(String name, Pageable pageable) {
        Page<WorkShift> shifts = workShiftRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name, pageable);
        return shifts.map(this::convertToDto);
    }
    
    @Override
    public void deleteShift(Long id) {
        log.info("Deleting work shift with ID: {}", id);
        
        WorkShift shift = workShiftRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc với ID: " + id));
        
        // Check if shift has active assignments
        long assignmentCount = assignmentRepository.countByWorkShiftIdAndIsActiveTrue(id);
        if (assignmentCount > 0) {
            throw new IllegalStateException("Không thể xóa ca làm việc đang được sử dụng. " +
                    "Có " + assignmentCount + " phân công đang hoạt động.");
        }
        
        // Soft delete
        shift.setIsActive(false);
        workShiftRepository.save(shift);
        
        log.info("Work shift deleted successfully: {}", shift.getName());
    }
    
    @Override
    public WorkShiftDto toggleShiftStatus(Long id, boolean isActive) {
        log.info("Toggling shift status for ID: {} to {}", id, isActive);
        
        WorkShift shift = workShiftRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc với ID: " + id));
        
        shift.setIsActive(isActive);
        WorkShift updatedShift = workShiftRepository.save(shift);
        
        return convertToDto(updatedShift);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isShiftNameAvailable(String name, Long excludeId) {
        if (excludeId == null) {
            return !workShiftRepository.existsByNameIgnoreCase(name.trim());
        } else {
            return !workShiftRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), excludeId);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WorkShiftDto> getShiftsWithAssignmentCounts() {
        List<Object[]> results = workShiftRepository.findActiveShiftsWithAssignmentCount();
        
        return results.stream()
                .map(result -> {
                    WorkShift shift = (WorkShift) result[0];
                    Long assignmentCount = (Long) result[1];
                    
                    WorkShiftDto dto = convertToDto(shift);
                    dto.setAssignmentCount(assignmentCount.intValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WorkShiftDto> getUnusedShifts() {
        List<WorkShift> shifts = workShiftRepository.findUnusedActiveShifts();
        return shifts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean validateShiftData(CreateWorkShiftDto createDto) {
        if (createDto == null) {
            return false;
        }
        
        // Check required fields
        if (createDto.getName() == null || createDto.getName().trim().isEmpty()) {
            return false;
        }
        
        if (createDto.getStartTime() == null || createDto.getEndTime() == null) {
            return false;
        }
        
        // Check time range
        if (!createDto.isValidTimeRange()) {
            return false;
        }
        
        // Check break hours
        if (createDto.getBreakHours() != null && createDto.getBreakHours() < 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Convert WorkShift entity to DTO
     */
    private WorkShiftDto convertToDto(WorkShift shift) {
        WorkShiftDto dto = modelMapper.map(shift, WorkShiftDto.class);
        dto.setWorkingHours(shift.getWorkingHours());
        return dto;
    }
}
