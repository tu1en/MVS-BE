package com.classroomapp.classroombackend.service.classroommanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSessionDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.ValidationException;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Session;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.SessionRepository;
import com.classroomapp.classroombackend.service.classroommanagement.impl.SessionServiceImpl;
import com.classroomapp.classroombackend.service.firebase.FirebaseClassroomService;

/**
 * Unit tests cho SessionService
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private FirebaseClassroomService firebaseClassroomService;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private Session testSession;
    private Classroom testClassroom;
    private CreateSessionDto createDto;
    private UpdateSessionDto updateDto;

    @BeforeEach
    void setUp() {
        testClassroom = new Classroom();
        testClassroom.setId(1L);
        testClassroom.setClassroomName("Test Classroom");

        testSession = new Session();
        testSession.setId(1L);
        testSession.setSessionDate(LocalDate.now().plusDays(1));
        testSession.setDescription("Test Description");
        testSession.setClassroom(testClassroom);
        testSession.setStatus(Session.SessionStatus.UPCOMING);
        testSession.setCreatedAt(LocalDateTime.now());
        testSession.setUpdatedAt(LocalDateTime.now());

        createDto = new CreateSessionDto();
        createDto.setClassroomId(1L);
        createDto.setSessionDate(LocalDate.now().plusDays(1));
        createDto.setDescription("New Session Description");

        updateDto = new UpdateSessionDto();
        updateDto.setSessionDate(LocalDate.now().plusDays(2));
        updateDto.setDescription("Updated Description");
    }

    @Test
    void getAllSessions_ShouldReturnPageOfSessions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Session> sessions = Arrays.asList(testSession);
        Page<Session> sessionPage = new PageImpl<>(sessions, pageable, 1);

        when(sessionRepository.findAll(pageable)).thenReturn(sessionPage);

        // When
        Page<SessionDto> result = sessionService.getAllSessions(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(LocalDate.now().plusDays(1), result.getContent().get(0).getSessionDate());
        verify(sessionRepository).findAll(pageable);
    }

    @Test
    void getSessionById_WhenExists_ShouldReturnSession() {
        // Given
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // When
        SessionDto result = sessionService.getSessionById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(LocalDate.now().plusDays(1), result.getSessionDate());
        verify(sessionRepository).findById(1L);
    }

    @Test
    void getSessionById_WhenNotExists_ShouldThrowException() {
        // Given
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            sessionService.getSessionById(1L);
        });
        verify(sessionRepository).findById(1L);
    }

    @Test
    void getSessionById_WhenIdIsNull_ShouldThrowValidationException() {
        // When & Then
        assertThrows(ValidationException.class, () -> {
            sessionService.getSessionById(null);
        });
        verify(sessionRepository, never()).findById(any());
    }

    @Test
    void createSession_WhenValid_ShouldReturnCreatedSession() {
        // Given
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(testClassroom));
        when(sessionRepository.existsByClassroomIdAndSessionDate(1L, createDto.getSessionDate())).thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // When
        SessionDto result = sessionService.createSession(createDto);

        // Then
        assertNotNull(result);
        assertEquals(LocalDate.now().plusDays(1), result.getSessionDate());
        verify(classroomRepository).findById(1L);
        verify(sessionRepository).existsByClassroomIdAndSessionDate(1L, createDto.getSessionDate());
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void createSession_WhenClassroomNotExists_ShouldThrowException() {
        // Given
        when(classroomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            sessionService.createSession(createDto);
        });
        verify(classroomRepository).findById(1L);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_WhenDateInPast_ShouldThrowValidationException() {
        // Given
        createDto.setSessionDate(LocalDate.now().minusDays(1));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            sessionService.createSession(createDto);
        });
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void createSession_WhenDuplicateDate_ShouldThrowValidationException() {
        // Given
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(testClassroom));
        when(sessionRepository.existsByClassroomIdAndSessionDate(1L, createDto.getSessionDate())).thenReturn(true);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            sessionService.createSession(createDto);
        });
        verify(classroomRepository).findById(1L);
        verify(sessionRepository).existsByClassroomIdAndSessionDate(1L, createDto.getSessionDate());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void updateSession_WhenValid_ShouldReturnUpdatedSession() {
        // Given
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(sessionRepository.existsByClassroomIdAndSessionDate(anyLong(), any(LocalDate.class))).thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // When
        SessionDto result = sessionService.updateSession(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(sessionRepository).findById(1L);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void updateSession_WhenNotExists_ShouldThrowException() {
        // Given
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            sessionService.updateSession(1L, updateDto);
        });
        verify(sessionRepository).findById(1L);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void deleteSession_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        doNothing().when(sessionRepository).delete(testSession);

        // When
        assertDoesNotThrow(() -> {
            sessionService.deleteSession(1L);
        });

        // Then
        verify(sessionRepository).findById(1L);
        verify(sessionRepository).delete(testSession);
    }

    @Test
    void deleteSession_WhenInProgress_ShouldThrowValidationException() {
        // Given
        testSession.setStatus(Session.SessionStatus.IN_PROGRESS);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            sessionService.deleteSession(1L);
        });
        verify(sessionRepository).findById(1L);
        verify(sessionRepository, never()).delete(any(Session.class));
    }

    @Test
    void getSessionsByClassroom_ShouldReturnClassroomSessions() {
        // Given
        List<Session> sessions = Arrays.asList(testSession);
        when(sessionRepository.findByClassroomIdOrderBySessionDateDesc(1L)).thenReturn(sessions);

        // When
        List<SessionDto> result = sessionService.getSessionsByClassroom(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LocalDate.now().plusDays(1), result.get(0).getSessionDate());
        verify(sessionRepository).findByClassroomIdOrderBySessionDateDesc(1L);
    }

    @Test
    void getSessionsByDate_ShouldReturnSessionsForDate() {
        // Given
        LocalDate testDate = LocalDate.now();
        List<Session> sessions = Arrays.asList(testSession);
        when(sessionRepository.findBySessionDateBetweenOrderBySessionDateAsc(testDate, testDate)).thenReturn(sessions);

        // When
        List<SessionDto> result = sessionService.getSessionsByDate(testDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sessionRepository).findBySessionDateBetweenOrderBySessionDateAsc(testDate, testDate);
    }
}
