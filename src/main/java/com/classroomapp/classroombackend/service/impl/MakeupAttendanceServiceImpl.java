package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.attendancemanagement.CreateMakeupAttendanceRequestDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.MakeupAttendanceRequestDto;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.attendancemanagement.MakeupAttendanceRequest;
import com.classroomapp.classroombackend.model.attendancemanagement.MakeupAttendanceRequest.RequestStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.MakeupAttendanceRequestRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.MakeupAttendanceService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of MakeupAttendanceService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MakeupAttendanceServiceImpl implements MakeupAttendanceService {
    
    private final MakeupAttendanceRequestRepository makeupRequestRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final ClassroomRepository classroomRepository;
    
    @Override
    public MakeupAttendanceRequestDto createRequest(CreateMakeupAttendanceRequestDto dto, String teacherEmail) {
        log.info("Creating makeup attendance request for teacher: {} and lecture: {}", teacherEmail, dto.getLectureId());

        // Validate and trim reason
        String trimmedReason = dto.getReason() != null ? dto.getReason().trim() : "";
        if (trimmedReason.isEmpty()) {
            throw new BusinessLogicException("Lý do điểm danh bù không được để trống");
        }
        if (trimmedReason.length() < 10) {
            throw new BusinessLogicException("Lý do điểm danh bù phải có ít nhất 10 ký tự (không tính khoảng trắng)");
        }
        if (trimmedReason.length() > 2000) {
            throw new BusinessLogicException("Lý do điểm danh bù không được vượt quá 2000 ký tự");
        }

        // Find teacher
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));
        
        // Find lecture
        Lecture lecture = lectureRepository.findById(dto.getLectureId())
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found with ID: " + dto.getLectureId()));
        
        // Find classroom
        Classroom classroom = classroomRepository.findById(dto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + dto.getClassroomId()));
        
        // Validate that teacher is assigned to this classroom
        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            throw new BusinessLogicException("Teacher is not assigned to this classroom");
        }
        
        // Check if there's already a pending or acknowledged request for this lecture
        Optional<MakeupAttendanceRequest> existingRequest = makeupRequestRepository
                .findExistingRequestForLecture(teacher, dto.getLectureId());

        if (existingRequest.isPresent()) {
            MakeupAttendanceRequest existing = existingRequest.get();
            String statusText = existing.getStatus() == RequestStatus.PENDING ? "chờ xác nhận" :
                               existing.getStatus() == RequestStatus.ACKNOWLEDGED ? "đã được xác nhận" : "đã hoàn thành";
            throw new BusinessLogicException("Đã có yêu cầu điểm danh bù " + statusText + " cho buổi học này");
        }
        
        // Create new request with trimmed reason
        MakeupAttendanceRequest request = MakeupAttendanceRequest.builder()
                .teacher(teacher)
                .lecture(lecture)
                .classroom(classroom)
                .reason(trimmedReason)
                .status(RequestStatus.PENDING)
                .build();
        
        MakeupAttendanceRequest savedRequest = makeupRequestRepository.save(request);
        log.info("Created makeup attendance request with ID: {}", savedRequest.getId());
        
        return convertToDto(savedRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MakeupAttendanceRequestDto> getMyRequests(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));
        
        List<MakeupAttendanceRequest> requests = makeupRequestRepository.findByTeacherOrderByRequestedAtDesc(teacher);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MakeupAttendanceRequestDto> getMyRequests(String teacherEmail, Pageable pageable) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));
        
        Page<MakeupAttendanceRequest> requests = makeupRequestRepository.findByTeacherOrderByRequestedAtDesc(teacher, pageable);
        return requests.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MakeupAttendanceRequestDto> getPendingRequests() {
        List<MakeupAttendanceRequest> requests = makeupRequestRepository.findByStatusOrderByRequestedAtAsc(RequestStatus.PENDING);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MakeupAttendanceRequestDto> getPendingRequests(Pageable pageable) {
        Page<MakeupAttendanceRequest> requests = makeupRequestRepository.findByStatusOrderByRequestedAtDesc(RequestStatus.PENDING, pageable);
        return requests.map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MakeupAttendanceRequestDto> getRequestsByStatus(RequestStatus status) {
        List<MakeupAttendanceRequest> requests = makeupRequestRepository.findByStatusOrderByRequestedAtDesc(status);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public MakeupAttendanceRequestDto acknowledgeRequest(Long requestId, String managerEmail, String managerNotes) {
        log.info("Acknowledging makeup attendance request {} by manager: {}", requestId, managerEmail);

        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with email: " + managerEmail));

        MakeupAttendanceRequest request = makeupRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Makeup attendance request not found with ID: " + requestId));

        if (!request.canBeAcknowledged()) {
            throw new BusinessLogicException("Request cannot be acknowledged. Current status: " + request.getStatus());
        }

        if (managerNotes != null && !managerNotes.trim().isEmpty()) {
            request.addManagerNotes(manager, managerNotes);
        }
        request.acknowledge(manager);
        MakeupAttendanceRequest savedRequest = makeupRequestRepository.save(request);

        log.info("Acknowledged makeup attendance request {} by manager: {}", requestId, managerEmail);
        return convertToDto(savedRequest);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MakeupAttendanceRequestDto getRequestById(Long requestId) {
        MakeupAttendanceRequest request = makeupRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Makeup attendance request not found with ID: " + requestId));
        
        return convertToDto(request);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canCreateMakeupRequest(Long lectureId, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));
        
        // Check if there's already a pending or acknowledged request
        Optional<MakeupAttendanceRequest> existingRequest = makeupRequestRepository
                .findExistingRequestForLecture(teacher, lectureId);

        return existingRequest.isEmpty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MakeupAttendanceRequestDto> getAcknowledgedRequestsForTeacher(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));

        List<MakeupAttendanceRequest> requests = makeupRequestRepository.findByTeacherAndStatusOrderByRequestedAtDesc(teacher, RequestStatus.ACKNOWLEDGED);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void markRequestAsCompleted(Long requestId) {
        log.info("Marking makeup attendance request {} as completed", requestId);
        
        MakeupAttendanceRequest request = makeupRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Makeup attendance request not found with ID: " + requestId));
        
        if (!request.canTakeMakeupAttendance()) {
            throw new BusinessLogicException("Cannot mark request as completed. Current status: " + request.getStatus());
        }
        
        request.markAsCompleted();
        makeupRequestRepository.save(request);
        
        log.info("Marked makeup attendance request {} as completed", requestId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTeacherStatistics(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));
        
        Object[] stats = makeupRequestRepository.getTeacherStatistics(teacher);
        Map<String, Long> result = new HashMap<>();
        
        if (stats != null && stats.length >= 4) {
            result.put("pending", stats[0] != null ? ((Number) stats[0]).longValue() : 0L);
            result.put("acknowledged", stats[1] != null ? ((Number) stats[1]).longValue() : 0L);
            result.put("completed", stats[2] != null ? ((Number) stats[2]).longValue() : 0L);
        } else {
            result.put("pending", 0L);
            result.put("acknowledged", 0L);
            result.put("completed", 0L);
        }
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getOverallStatistics() {
        Object[] stats = makeupRequestRepository.getOverallStatistics();
        Map<String, Long> result = new HashMap<>();
        
        if (stats != null && stats.length >= 3) {
            result.put("pending", stats[0] != null ? ((Number) stats[0]).longValue() : 0L);
            result.put("acknowledged", stats[1] != null ? ((Number) stats[1]).longValue() : 0L);
            result.put("completed", stats[2] != null ? ((Number) stats[2]).longValue() : 0L);
        } else {
            result.put("pending", 0L);
            result.put("acknowledged", 0L);
            result.put("completed", 0L);
        }
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MakeupAttendanceRequestDto> getRecentRequests() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<MakeupAttendanceRequest> requests = makeupRequestRepository.findRecentRequests(thirtyDaysAgo);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public MakeupAttendanceRequest getAcknowledgedRequestForLecture(Long lectureId, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + teacherEmail));

        Optional<MakeupAttendanceRequest> request = makeupRequestRepository
                .findExistingRequestForLecture(teacher, lectureId);

        if (request.isPresent() && request.get().getStatus() == RequestStatus.ACKNOWLEDGED) {
            return request.get();
        }

        return null;
    }
    
    /**
     * Convert entity to DTO
     */
    private MakeupAttendanceRequestDto convertToDto(MakeupAttendanceRequest request) {
        return MakeupAttendanceRequestDto.builder()
                .id(request.getId())
                .teacherId(request.getTeacher().getId())
                .teacherName(request.getTeacher().getFullName())
                .teacherEmail(request.getTeacher().getEmail())
                .lectureId(request.getLecture().getId())
                .lectureTitle(request.getLecture().getTitle())
                .lectureDate(request.getLecture().getLectureDate() != null
                    ? request.getLecture().getLectureDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : "N/A")
                .lectureTime(request.getLecture().getSchedule() != null && request.getLecture().getSchedule().getStartTime() != null
                    ? request.getLecture().getSchedule().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : "N/A")
                .classroomId(request.getClassroom().getId())
                .classroomName(request.getClassroom().getName())
                .reason(request.getReason())
                .status(request.getStatus())
                .requestedAt(request.getRequestedAt())
                .approvedById(request.getApprovedBy() != null ? request.getApprovedBy().getId() : null)
                .approvedByName(request.getApprovedBy() != null ? request.getApprovedBy().getFullName() : null)
                .approvedAt(request.getApprovedAt())
                .rejectionReason(request.getRejectionReason())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
