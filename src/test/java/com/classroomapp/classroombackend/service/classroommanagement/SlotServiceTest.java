package com.classroomapp.classroombackend.service.classroommanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSlotDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SlotDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSlotDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.ValidationException;
import com.classroomapp.classroombackend.model.classroommanagement.Session;
import com.classroomapp.classroombackend.model.classroommanagement.Slot;
import com.classroomapp.classroombackend.repository.classroommanagement.SessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.SlotRepository;
import com.classroomapp.classroombackend.service.classroommanagement.impl.SlotServiceImpl;

/**
 * Unit tests cho SlotService
 */
@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SlotServiceImpl slotService;

    private Slot testSlot;
    private Session testSession;
    private CreateSlotDto createDto;
    private UpdateSlotDto updateDto;

    @BeforeEach
    void setUp() {
        testSession = new Session();
        testSession.setId(1L);
        testSession.setSessionDate(LocalDate.now().plusDays(1));
        testSession.setStatus(Session.SessionStatus.UPCOMING);

        testSlot = new Slot();
        testSlot.setId(1L);
        testSlot.setSlotName("Test Slot");
        testSlot.setStartTime(LocalTime.of(9, 0));
        testSlot.setEndTime(LocalTime.of(10, 0));
        testSlot.setDescription("Test Description");
        testSlot.setSession(testSession);
        testSlot.setStatus(Slot.SlotStatus.PENDING);
        testSlot.setCreatedAt(LocalDateTime.now());
        testSlot.setUpdatedAt(LocalDateTime.now());

        createDto = new CreateSlotDto();
        createDto.setSlotName("New Slot");
        createDto.setStartTime(LocalTime.of(10, 0));
        createDto.setEndTime(LocalTime.of(11, 0));
        createDto.setDescription("New Description");
        createDto.setSessionId(1L);

        updateDto = new UpdateSlotDto();
        updateDto.setSlotName("Updated Slot");
        updateDto.setStartTime(LocalTime.of(11, 0));
        updateDto.setEndTime(LocalTime.of(12, 0));
        updateDto.setDescription("Updated Description");
    }

    @Test
    void getAllSlots_ShouldReturnPageOfSlots() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Slot> slots = Arrays.asList(testSlot);
        Page<Slot> slotPage = new PageImpl<>(slots, pageable, 1);

        when(slotRepository.findAll(pageable)).thenReturn(slotPage);

        // When
        Page<SlotDto> result = slotService.getAllSlots(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Slot", result.getContent().get(0).getSlotName());
        verify(slotRepository).findAll(pageable);
    }

    @Test
    void getSlotById_WhenExists_ShouldReturnSlot() {
        // Given
        when(slotRepository.findById(1L)).thenReturn(Optional.of(testSlot));

        // When
        SlotDto result = slotService.getSlotById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Slot", result.getSlotName());
        verify(slotRepository).findById(1L);
    }

    @Test
    void getSlotById_WhenNotExists_ShouldThrowException() {
        // Given
        when(slotRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            slotService.getSlotById(1L);
        });
        verify(slotRepository).findById(1L);
    }

    @Test
    void getSlotById_WhenIdIsNull_ShouldThrowValidationException() {
        // When & Then
        assertThrows(ValidationException.class, () -> {
            slotService.getSlotById(null);
        });
        verify(slotRepository, never()).findById(any());
    }

    @Test
    void createSlot_WhenValid_ShouldReturnCreatedSlot() {
        // Given
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(slotRepository.findBySessionIdOrderByStartTimeAsc(1L)).thenReturn(Arrays.asList());
        when(slotRepository.save(any(Slot.class))).thenReturn(testSlot);

        // When
        SlotDto result = slotService.createSlot(createDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Slot", result.getSlotName());
        verify(sessionRepository).findById(1L);
        verify(slotRepository).findBySessionIdOrderByStartTimeAsc(1L);
        verify(slotRepository).save(any(Slot.class));
    }

    @Test
    void createSlot_WhenSessionNotExists_ShouldThrowException() {
        // Given
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            slotService.createSlot(createDto);
        });
        verify(sessionRepository).findById(1L);
        verify(slotRepository, never()).save(any());
    }

    @Test
    void createSlot_WhenInvalidTimeRange_ShouldThrowValidationException() {
        // Given
        createDto.setStartTime(LocalTime.of(11, 0));
        createDto.setEndTime(LocalTime.of(10, 0)); // End before start

        // When & Then
        assertThrows(ValidationException.class, () -> {
            slotService.createSlot(createDto);
        });
        verify(slotRepository, never()).save(any());
    }

    @Test
    void createSlot_WhenSessionCompleted_ShouldThrowValidationException() {
        // Given
        testSession.setStatus(Session.SessionStatus.COMPLETED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            slotService.createSlot(createDto);
        });
        verify(sessionRepository).findById(1L);
        verify(slotRepository, never()).save(any());
    }

    @Test
    void createSlot_WhenTimeConflict_ShouldThrowValidationException() {
        // Given
        Slot existingSlot = new Slot();
        existingSlot.setStartTime(LocalTime.of(10, 30));
        existingSlot.setEndTime(LocalTime.of(11, 30));
        existingSlot.setSlotName("Existing Slot");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(slotRepository.findBySessionIdOrderByStartTimeAsc(1L)).thenReturn(Arrays.asList(existingSlot));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            slotService.createSlot(createDto);
        });
        verify(sessionRepository).findById(1L);
        verify(slotRepository).findBySessionIdOrderByStartTimeAsc(1L);
        verify(slotRepository, never()).save(any());
    }

    @Test
    void updateSlot_WhenValid_ShouldReturnUpdatedSlot() {
        // Given
        when(slotRepository.findById(1L)).thenReturn(Optional.of(testSlot));
        when(slotRepository.findBySessionIdOrderByStartTimeAsc(anyLong())).thenReturn(Arrays.asList());
        when(slotRepository.save(any(Slot.class))).thenReturn(testSlot);

        // When
        SlotDto result = slotService.updateSlot(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(slotRepository).findById(1L);
        verify(slotRepository).save(any(Slot.class));
    }

    @Test
    void updateSlot_WhenNotExists_ShouldThrowException() {
        // Given
        when(slotRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            slotService.updateSlot(1L, updateDto);
        });
        verify(slotRepository).findById(1L);
        verify(slotRepository, never()).save(any());
    }

    @Test
    void updateSlot_WhenSlotDone_ShouldThrowValidationException() {
        // Given
        testSlot.setStatus(Slot.SlotStatus.DONE);
        when(slotRepository.findById(1L)).thenReturn(Optional.of(testSlot));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            slotService.updateSlot(1L, updateDto);
        });
        verify(slotRepository).findById(1L);
        verify(slotRepository, never()).save(any());
    }

    @Test
    void deleteSlot_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(slotRepository.findById(1L)).thenReturn(Optional.of(testSlot));
        doNothing().when(slotRepository).delete(testSlot);

        // When
        assertDoesNotThrow(() -> {
            slotService.deleteSlot(1L);
        });

        // Then
        verify(slotRepository).findById(1L);
        verify(slotRepository).delete(testSlot);
    }

    @Test
    void getSlotsBySession_ShouldReturnSessionSlots() {
        // Given
        List<Slot> slots = Arrays.asList(testSlot);
        when(slotRepository.findBySessionIdOrderByStartTimeAsc(1L)).thenReturn(slots);

        // When
        List<SlotDto> result = slotService.getSlotsBySession(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Slot", result.get(0).getSlotName());
        verify(slotRepository).findBySessionIdOrderByStartTimeAsc(1L);
    }
}
