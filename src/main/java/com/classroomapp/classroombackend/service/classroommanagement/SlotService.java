package com.classroomapp.classroombackend.service.classroommanagement;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSlotDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SlotDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSlotDto;
import com.classroomapp.classroombackend.model.classroommanagement.Slot;

/**
 * Service interface for Slot management
 */
public interface SlotService {

    /**
     * Get all slots with pagination
     */
    Page<SlotDto> getAllSlots(Pageable pageable);

    /**
     * Get slot by ID (throws exception if not found)
     */
    SlotDto getSlotById(Long id);

    /**
     * Get slots by session ID
     */
    List<SlotDto> getSlotsBySessionId(Long sessionId);

    /**
     * Get slots by session (alias for controller)
     */
    List<SlotDto> getSlotsBySession(Long sessionId);

    /**
     * Get slots by session with pagination
     */
    Page<SlotDto> getSlotsBySession(Long sessionId, Pageable pageable);

    /**
     * Get slots by status
     */
    List<SlotDto> getSlotsByStatus(Slot.SlotStatus status);

    /**
     * Create new slot
     */
    SlotDto createSlot(CreateSlotDto createDto);

    /**
     * Update existing slot
     */
    SlotDto updateSlot(Long id, UpdateSlotDto updateDto);

    /**
     * Delete slot
     */
    void deleteSlot(Long id);

    /**
     * Update slot status
     */
    SlotDto updateSlotStatus(Long id, Slot.SlotStatus status);

    /**
     * Check if slot exists by ID
     */
    boolean existsById(Long id);

    /**
     * Count slots by session ID
     */
    long countSlotsBySessionId(Long sessionId);

    /**
     * Count slots by status
     */
    long countSlotsByStatus(Slot.SlotStatus status);
}
