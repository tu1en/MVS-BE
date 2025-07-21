package com.classroomapp.classroombackend.service.classroommanagement.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.classroommanagement.CreateSessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.SessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateSessionDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.exception.ValidationException;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Session;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.SessionRepository;
import com.classroomapp.classroombackend.service.classroommanagement.SessionService;
import com.classroomapp.classroombackend.service.firebase.FirebaseClassroomService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private FirebaseClassroomService firebaseClassroomService;

    @Override
    @Transactional(readOnly = true)
    public Page<SessionDto> getAllSessions(Pageable pageable) {
        Page<Session> sessions = sessionRepository.findAll(pageable);
        List<SessionDto> sessionDtos = sessions.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(sessionDtos, pageable, sessions.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDto getSessionById(Long id) {
        if (id == null) throw new ValidationException("Session ID cannot be null");
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy session với ID: " + id));
        return convertToDto(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getSessionsByClassroomId(Long classroomId) {
        if (classroomId == null) throw new ValidationException("Classroom ID cannot be null");
        List<Session> sessions = sessionRepository.findByClassroomIdOrderBySessionDateDesc(classroomId);
        return sessions.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getSessionsByClassroom(Long classroomId) {
        return getSessionsByClassroomId(classroomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getSessionsByDate(LocalDate date) {
        if (date == null) throw new ValidationException("Date cannot be null");
        List<Session> sessions = sessionRepository.findBySessionDateBetweenOrderBySessionDateAsc(date, date);
        return sessions.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getSessionsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) throw new ValidationException("Start date and end date cannot be null");
        if (startDate.isAfter(endDate)) throw new ValidationException("Start date cannot be after end date");
        List<Session> sessions = sessionRepository.findBySessionDateBetweenOrderBySessionDateAsc(startDate, endDate);
        return sessions.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getSessionsByStatus(Session.SessionStatus status) {
        if (status == null) throw new ValidationException("Status cannot be null");
        List<Session> sessions = sessionRepository.findByStatusOrderBySessionDateDesc(status);
        return sessions.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public SessionDto createSession(CreateSessionDto createDto) {
        validateCreateSessionDto(createDto);

        Classroom classroom = classroomRepository.findById(createDto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with id: " + createDto.getClassroomId()));

        if (sessionRepository.existsByClassroomIdAndSessionDate(createDto.getClassroomId(), createDto.getSessionDate())) {
            throw new ValidationException("Session already exists for this classroom on the specified date");
        }

        Session session = new Session();
        session.setClassroom(classroom);
        session.setSessionDate(createDto.getSessionDate());
        session.setDescription(createDto.getDescription());
        session.setStatus(Session.SessionStatus.UPCOMING);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        Session savedSession = sessionRepository.save(session);
        SessionDto result = convertToDto(savedSession);

        // Sync to Firebase asynchronously
        firebaseClassroomService.syncSession(result);

        return result;
    }

    @Override
    public SessionDto updateSession(Long id, UpdateSessionDto updateDto) {
        if (id == null) throw new ValidationException("Session ID cannot be null");
        validateUpdateSessionDto(updateDto);

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        if (session.getStatus() == Session.SessionStatus.COMPLETED) {
            throw new ValidationException("Cannot modify completed session");
        }

        if (updateDto.getSessionDate() != null) {
            if (!updateDto.getSessionDate().equals(session.getSessionDate())
                    && sessionRepository.existsByClassroomIdAndSessionDate(session.getClassroom().getId(), updateDto.getSessionDate())) {
                throw new ValidationException("Session already exists for this classroom on the specified date");
            }
            session.setSessionDate(updateDto.getSessionDate());
        }

        if (updateDto.getDescription() != null) {
            session.setDescription(updateDto.getDescription());
        }

        session.setUpdatedAt(LocalDateTime.now());
        Session savedSession = sessionRepository.save(session);
        SessionDto result = convertToDto(savedSession);

        // Sync to Firebase asynchronously
        firebaseClassroomService.syncSession(result);

        return result;
    }

    @Override
    public void deleteSession(Long id) {
        if (id == null) throw new ValidationException("Session ID cannot be null");

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        if (session.getStatus() == Session.SessionStatus.IN_PROGRESS || session.getStatus() == Session.SessionStatus.COMPLETED) {
            throw new ValidationException("Cannot delete session in progress or completed");
        }

        sessionRepository.delete(session);

        // Remove from Firebase asynchronously
        firebaseClassroomService.removeSession(id);
    }

    @Override
    public SessionDto updateSessionStatus(Long id, Session.SessionStatus status) {
        if (id == null) throw new ValidationException("Session ID cannot be null");
        if (status == null) throw new ValidationException("Status cannot be null");

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        validateStatusTransition(session.getStatus(), status);

        session.setStatus(status);
        session.setUpdatedAt(LocalDateTime.now());

        Session savedSession = sessionRepository.save(session);
        return convertToDto(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return id != null && sessionRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countSessionsByClassroomId(Long classroomId) {
        return classroomId != null ? sessionRepository.countByClassroom_Id(classroomId) : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countSessionsByStatus(Session.SessionStatus status) {
        return status != null ? sessionRepository.countByStatus(status) : 0;
    }

    private SessionDto convertToDto(Session session) {
        if (session == null) return null;
        String classroomName = session.getClassroom() != null ? session.getClassroom().getClassroomName() : "Unknown";
        return new SessionDto(
                session.getId(),
                session.getClassroom() != null ? session.getClassroom().getId() : null,
                classroomName,
                session.getSessionDate(),
                session.getDescription(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private void validateCreateSessionDto(CreateSessionDto dto) {
        if (dto == null) throw new ValidationException("Create session data cannot be null");
        if (dto.getClassroomId() == null) throw new ValidationException("Classroom ID is required");
        if (dto.getSessionDate() == null) throw new ValidationException("Session date is required");
        if (!dto.isValidDate()) throw new ValidationException("Session date cannot be in the past");
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }
    }

    private void validateUpdateSessionDto(UpdateSessionDto dto) {
        if (dto == null) throw new ValidationException("Update session data cannot be null");
        if (!dto.hasUpdates()) throw new ValidationException("At least one field must be provided for update");
        if (!dto.isValidDate()) throw new ValidationException("Session date cannot be in the past");
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new ValidationException("Description cannot exceed 1000 characters");
        }
    }

    private void validateStatusTransition(Session.SessionStatus currentStatus, Session.SessionStatus newStatus) {
        if (currentStatus == newStatus) return;
        switch (currentStatus) {
            case UPCOMING:
                if (newStatus != Session.SessionStatus.IN_PROGRESS) {
                    throw new ValidationException("Upcoming session can only change to In Progress");
                }
                break;
            case IN_PROGRESS:
                if (newStatus != Session.SessionStatus.COMPLETED) {
                    throw new ValidationException("In Progress session can only change to Completed");
                }
                break;
            case COMPLETED:
                throw new ValidationException("Completed session status cannot be changed");
            default:
                throw new ValidationException("Invalid status transition");
        }
    }
}
