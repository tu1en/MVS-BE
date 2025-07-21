package com.classroomapp.classroombackend.service.classroommanagement.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSlotDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SlotDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSlotDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.ValidationException;
import com.classroomapp.classroombackend.model.classroommanagement.Session;
import com.classroomapp.classroombackend.model.classroommanagement.Slot;
import com.classroomapp.classroombackend.repository.classroommanagement.SessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.SlotRepository;
import com.classroomapp.classroombackend.service.classroommanagement.SlotService;
import com.classroomapp.classroombackend.service.firebase.FirebaseClassroomService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of SlotService
 * Handles business logic for slot management
 */
@Service
@Transactional
@Slf4j
public class SlotServiceImpl implements SlotService {

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private FirebaseClassroomService firebaseClassroomService;

    @Override
    @Transactional(readOnly = true)
    public Page<SlotDto> getAllSlots(Pageable pageable) {
        log.debug("Getting all slots with pagination: {}", pageable);

        Page<Slot> slots = slotRepository.findAll(pageable);
        List<SlotDto> slotDtos = slots.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        return new PageImpl<>(slotDtos, pageable, slots.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public SlotDto getSlotById(Long id) {
        log.debug("Getting slot by id: {}", id);

        if (id == null) {
            throw new ValidationException("Slot ID cannot be null");
        }

        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy slot với ID: " + id));
        return convertToDto(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotDto> getSlotsBySessionId(Long sessionId) {
        log.debug("Getting slots by session id: {}", sessionId);

        if (sessionId == null) {
            throw new ValidationException("Session ID cannot be null");
        }

        List<Slot> slots = slotRepository.findBySessionIdOrderByStartTimeAsc(sessionId);
        return slots.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotDto> getSlotsBySession(Long sessionId) {
        return getSlotsBySessionId(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SlotDto> getSlotsBySession(Long sessionId, Pageable pageable) {
        log.debug("Getting slots by session id with pagination: {}", sessionId);

        if (sessionId == null) {
            throw new ValidationException("Session ID cannot be null");
        }

        Page<Slot> slots = slotRepository.findBySessionId(sessionId, pageable);
        List<SlotDto> slotDtos = slots.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        return new PageImpl<>(slotDtos, pageable, slots.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotDto> getSlotsByStatus(Slot.SlotStatus status) {
        log.debug("Getting slots by status: {}", status);

        if (status == null) {
            throw new ValidationException("Status cannot be null");
        }

        List<Slot> slots = slotRepository.findByStatusOrderByStartTimeAsc(status);
        return slots.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public SlotDto createSlot(CreateSlotDto createDto) {
        log.debug("Creating new slot: {}", createDto);

        validateCreateSlotDto(createDto);

        // Verify session exists and can have slots added
        Session session = sessionRepository.findById(createDto.getSessionId())
            .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + createDto.getSessionId()));

        if (session.getStatus() == Session.SessionStatus.COMPLETED) {
            throw new ValidationException("Cannot add slots to completed session");
        }

        // Check for time conflicts with existing slots in the same session
        List<Slot> existingSlots = slotRepository.findBySessionIdOrderByStartTimeAsc(createDto.getSessionId());
        for (Slot existingSlot : existingSlots) {
            if (hasTimeConflictCreate(createDto, convertToDto(existingSlot))) {
                throw new ValidationException("Slot time conflicts with existing slot: " + existingSlot.getSlotName());
            }
        }

        Slot slot = new Slot();
        slot.setSlotName(createDto.getSlotName());
        slot.setStartTime(createDto.getStartTime());
        slot.setEndTime(createDto.getEndTime());
        slot.setDescription(createDto.getDescription());
        slot.setSession(session); // Use session object instead of sessionId
        slot.setStatus(createDto.getStatus() != null ? createDto.getStatus() : Slot.SlotStatus.PENDING);
        slot.setCreatedAt(LocalDateTime.now());
        slot.setUpdatedAt(LocalDateTime.now());

        Slot savedSlot = slotRepository.save(slot);
        log.info("Created slot with id: {}", savedSlot.getId());

        SlotDto result = convertToDto(savedSlot);

        // Sync to Firebase asynchronously
        firebaseClassroomService.syncSlot(result);

        return result;
    }

    @Override
    public SlotDto updateSlot(Long id, UpdateSlotDto updateDto) {
        log.debug("Updating slot id: {} with data: {}", id, updateDto);

        if (id == null) {
            throw new ValidationException("Slot ID cannot be null");
        }

        validateUpdateSlotDto(updateDto);

        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));

        // Check if slot can be modified
        if (slot.getStatus() == Slot.SlotStatus.DONE) {
            throw new ValidationException("Cannot modify completed slot");
        }

        // Check for time conflicts if time is being updated
        if (updateDto.isTimeUpdate()) {
            // Create temporary slot data for conflict checking
            LocalTime newStartTime = updateDto.getStartTime() != null ? updateDto.getStartTime() : slot.getStartTime();
            LocalTime newEndTime = updateDto.getEndTime() != null ? updateDto.getEndTime() : slot.getEndTime();

            if (newStartTime != null && newEndTime != null && !newStartTime.isBefore(newEndTime)) {
                throw new ValidationException("Start time must be before end time");
            }

            List<Slot> existingSlots = slotRepository.findBySessionIdOrderByStartTimeAsc(slot.getSession().getId());
            for (Slot existingSlot : existingSlots) {
                if (!existingSlot.getId().equals(id)) {
                    if (updateDto.conflictsWith(existingSlot.getStartTime(), existingSlot.getEndTime())) {
                        throw new ValidationException("Slot time conflicts with existing slot: " + existingSlot.getSlotName());
                    }
                }
            }
        }

        // Update fields
        if (updateDto.getSlotName() != null) {
            slot.setSlotName(updateDto.getSlotName());
        }
        if (updateDto.getStartTime() != null) {
            slot.setStartTime(updateDto.getStartTime());
        }
        if (updateDto.getEndTime() != null) {
            slot.setEndTime(updateDto.getEndTime());
        }
        if (updateDto.getDescription() != null) {
            slot.setDescription(updateDto.getDescription());
        }
        if (updateDto.getStatus() != null) {
            validateStatusTransition(slot.getStatus(), updateDto.getStatus());
            slot.setStatus(updateDto.getStatus());
        }

        slot.setUpdatedAt(LocalDateTime.now());

        Slot savedSlot = slotRepository.save(slot);
        log.info("Updated slot with id: {}", savedSlot.getId());

        SlotDto result = convertToDto(savedSlot);

        // Sync to Firebase asynchronously
        firebaseClassroomService.syncSlot(result);

        return result;
    }

    @Override
    public void deleteSlot(Long id) {
        log.debug("Deleting slot id: {}", id);

        if (id == null) {
            throw new ValidationException("Slot ID cannot be null");
        }

        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));

        // Check if slot can be deleted
        if (slot.getStatus() == Slot.SlotStatus.ACTIVE) {
            throw new ValidationException("Cannot delete active slot");
        }

        if (slot.getStatus() == Slot.SlotStatus.DONE) {
            throw new ValidationException("Cannot delete completed slot");
        }

        slotRepository.delete(slot);
        log.info("Deleted slot with id: {}", id);

        // Remove from Firebase asynchronously
        firebaseClassroomService.removeSlot(id);
    }

    @Override
    public SlotDto updateSlotStatus(Long id, Slot.SlotStatus status) {
        log.debug("Updating slot status for id: {} to status: {}", id, status);

        if (id == null) {
            throw new ValidationException("Slot ID cannot be null");
        }

        if (status == null) {
            throw new ValidationException("Status cannot be null");
        }

        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));

        // Validate status transition
        validateStatusTransition(slot.getStatus(), status);

        slot.setStatus(status);
        slot.setUpdatedAt(LocalDateTime.now());

        Slot savedSlot = slotRepository.save(slot);
        log.info("Updated slot status for id: {} to: {}", id, status);

        return convertToDto(savedSlot);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) return false;
        return slotRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countSlotsBySessionId(Long sessionId) {
        if (sessionId == null) return 0;
        return slotRepository.countBySessionId(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countSlotsByStatus(Slot.SlotStatus status) {
        if (status == null) return 0;
        return slotRepository.countByStatus(status);
    }

    /**
     * Convert Slot entity to SlotDto
     */
    private SlotDto convertToDto(Slot slot) {
        if (slot == null) return null;

        SlotDto dto = new SlotDto();
        dto.setId(slot.getId());
        dto.setSlotName(slot.getSlotName());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setDescription(slot.getDescription());
        dto.setStatus(slot.getStatus());
        dto.setSessionId(slot.getSessionId());

        return dto;
    }

    /**
     * Validate SlotDto
     */
    private void validateSlotDto(SlotDto dto, boolean isCreate) {
        if (dto == null) {
            throw new ValidationException("Slot data cannot be null");
        }

        if (isCreate) {
            if (dto.getSlotName() == null || dto.getSlotName().trim().isEmpty()) {
                throw new ValidationException("Slot name is required");
            }
            if (dto.getSessionId() == null) {
                throw new ValidationException("Session ID is required");
            }
            if (dto.getStartTime() == null) {
                throw new ValidationException("Start time is required");
            }
            if (dto.getEndTime() == null) {
                throw new ValidationException("End time is required");
            }
        }

        if (dto.getSlotName() != null && dto.getSlotName().length() > 255) {
            throw new ValidationException("Slot name cannot exceed 255 characters");
        }

        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (!dto.getStartTime().isBefore(dto.getEndTime())) {
                throw new ValidationException("Start time must be before end time");
            }
        }
    }

    /**
     * Check if two slots have time conflict
     */
    private boolean hasTimeConflict(SlotDto slot1, SlotDto slot2) {
        if (slot1 == null || slot2 == null) return false;
        return slot1.overlapsWith(slot2);
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(Slot.SlotStatus currentStatus, Slot.SlotStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change
        }

        switch (currentStatus) {
            case PENDING:
                if (newStatus != Slot.SlotStatus.ACTIVE) {
                    throw new ValidationException("Pending slot can only be changed to Active");
                }
                break;
            case ACTIVE:
                if (newStatus != Slot.SlotStatus.DONE) {
                    throw new ValidationException("Active slot can only be changed to Done");
                }
                break;
            case DONE:
                throw new ValidationException("Completed slot status cannot be changed");
            default:
                throw new ValidationException("Invalid status transition");
        }
    }

    /**
     * Validate CreateSlotDto
     */
    private void validateCreateSlotDto(CreateSlotDto createDto) {
        if (createDto == null) {
            throw new ValidationException("CreateSlotDto cannot be null");
        }

        if (createDto.getSlotName() == null || createDto.getSlotName().trim().isEmpty()) {
            throw new ValidationException("Slot name cannot be null or empty");
        }

        if (createDto.getStartTime() == null) {
            throw new ValidationException("Start time cannot be null");
        }

        if (createDto.getEndTime() == null) {
            throw new ValidationException("End time cannot be null");
        }

        if (!createDto.isValidTimeRange()) {
            throw new ValidationException("Start time must be before end time");
        }

        if (createDto.getSessionId() == null) {
            throw new ValidationException("Session ID cannot be null");
        }
    }

    /**
     * Check time conflict between CreateSlotDto and existing SlotDto
     */
    private boolean hasTimeConflictCreate(CreateSlotDto createDto, SlotDto existingSlot) {
        return createDto.conflictsWith(existingSlot.getStartTime(), existingSlot.getEndTime());
    }

    /**
     * Validate UpdateSlotDto
     */
    private void validateUpdateSlotDto(UpdateSlotDto updateDto) {
        if (updateDto == null) {
            throw new ValidationException("UpdateSlotDto cannot be null");
        }

        if (!updateDto.hasUpdates()) {
            throw new ValidationException("No fields provided for update");
        }

        if (updateDto.getSlotName() != null && updateDto.getSlotName().trim().isEmpty()) {
            throw new ValidationException("Slot name cannot be empty");
        }

        if (updateDto.isTimeUpdate() && !updateDto.isValidTimeRange()) {
            throw new ValidationException("Start time must be before end time");
        }
    }
}
