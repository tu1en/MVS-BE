package com.classroomapp.classroombackend.service.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.Shift;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.ShiftRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Shift Management - CRUD operations for work shifts
 * Implements Phase 2: Task #26 - Define Work Shifts
 */
@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    /**
     * Create a new work shift with validation
     */
    @Transactional
    public Shift createShift(Shift shift, User createdBy) {
        validateShift(shift);
        shift.setCreatedBy(createdBy);
        return shiftRepository.save(shift);
    }

    /**
     * Get all active shifts ordered by start time
     */
    public List<Shift> findAllActiveShifts() {
        return shiftRepository.findByIsActiveTrueOrderByStartTimeAsc();
    }

    /**
     * Get shift by ID
     */
    public Shift findById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found with id: " + id));
    }

    /**
     * Update existing shift
     */
    @Transactional
    public Shift updateShift(Long id, Shift updatedShift) {
        Shift existingShift = findById(id);
        validateShift(updatedShift);
        
        existingShift.setName(updatedShift.getName());
        existingShift.setStartTime(updatedShift.getStartTime());
        existingShift.setEndTime(updatedShift.getEndTime());
        existingShift.setDaysOfWeek(updatedShift.getDaysOfWeek());
        existingShift.setActive(updatedShift.getIsActive());
        
        return shiftRepository.save(existingShift);
    }

    /**
     * Soft delete (deactivate) shift
     */
    @Transactional
    public void deactivateShift(Long id) {
        Shift shift = findById(id);
        shift.setActive(false);
        shiftRepository.save(shift);
    }

    /**
     * Find shifts by name search
     */
    public List<Shift> searchShifts(String name) {
        return shiftRepository.findByNameContainingIgnoreCaseOrderByStartTime(name);
    }

    /**
     * Check for duplicate shift names
     */
    public boolean existsByName(String name) {
        return shiftRepository.existsByNameAndIsActiveTrue(name);
    }

    /**
     * Validate shift data
     */
    private void validateShift(Shift shift) {
        if (shift.getStartTime() == null || shift.getEndTime() == null) {
            throw new IllegalArgumentException("Start and end times are required");
        }
        
        if (shift.getStartTime().isAfter(shift.getEndTime()) || shift.getStartTime().equals(shift.getEndTime())) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu");
        }
        
        if (shift.getName() == null || shift.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Cần cung cấp tên ca làm việc");
        }
        
        if (shift.getName().length() > 100) {
            throw new IllegalArgumentException("Shift name cannot exceed 100 characters");
        }
        
        // Check for minimum 1 hour duration
        long minutes = java.time.Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
        if (minutes < 60) {
            throw new IllegalArgumentException("Thời lượng ca làm việc phải tối thiểu 1 giờ");
        }
        
        // Check for maximum 12 hour duration
        if (minutes > 12 * 60) {
            throw new IllegalArgumentException("Shift duration cannot exceed 12 hours");
        }
    }

    /**
     * Get shift statistics
     */
    public java.util.Map<String, Object> getShiftStatistics() {
        List<Shift> shifts = shiftRepository.findAll();
        
        return java.util.Map.of(
            "totalShifts", shifts.size(),
            "activeShifts", shifts.stream().filter(s -> s.getIsActive()).count(),
            "inactiveShifts", shifts.stream().filter(s -> !s.getIsActive()).count()
        );
    }
}