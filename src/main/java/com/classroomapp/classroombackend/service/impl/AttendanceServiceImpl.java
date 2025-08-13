package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceRecordDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceResultDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateAttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateOrUpdateAttendanceDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.MyAttendanceHistoryDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.StudentAttendanceDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.TeachingHistoryDto;
import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceService;
import com.classroomapp.classroombackend.service.ClassroomSecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final ClassroomEnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ClassroomSecurityService classroomSecurityService;
    private final LectureRepository lectureRepository; // Inject LectureRepository

    @Override
    @Transactional
    public void createOrUpdateAttendance(CreateOrUpdateAttendanceDto dto) {
       throw new UnsupportedOperationException("Phương thức này đã bị ngừng sử dụng và thuộc luồng điểm danh cũ.");
    }

    @Override
    @Transactional
    public void submitAttendance(AttendanceSubmitDto submitDto) {
        // Validate classroom existence
        Classroom classroom = classroomRepository.findById(submitDto.getClassroomId())
                .orElseThrow(() -> new BusinessLogicException("Classroom not found with ID: " + submitDto.getClassroomId()));
                
        // Validate session existence if provided
        AttendanceSession session = null;
        if (submitDto.getSessionId() != null) {
            session = attendanceSessionRepository.findById(submitDto.getSessionId())
                    .orElseThrow(() -> new BusinessLogicException("Session not found with ID: " + submitDto.getSessionId()));
        }

        // No need to check lecture-classroom relationship as we're not using lectures anymore

        // Create a new session if none exists
        if (session == null) {
            session = new AttendanceSession();
            session.setClassroom(classroom);
            session.setCreatedAt(LocalDateTime.now());
            session.setExpiresAt(LocalDateTime.now().plusHours(1)); // Example: session expires in 1 hour
            session.setIsOpen(true);
            session.setSessionDate(LocalDate.now()); // Set session date to today
            // Set teacher clock-in time when attendance is submitted - this enables teaching history tracking
            session.setTeacherClockInTime(LocalDateTime.now());
            session = attendanceSessionRepository.save(session);
        }

        // Ensure session is open and not expired if a new one wasn't created
        if (!session.getIsOpen() ||
            (session.getExpiresAt() != null && LocalDateTime.now().isAfter(session.getExpiresAt()))) {
             // Optionally reopen or create new session based on business rules
             // For this task, we will just throw an error or handle accordingly
            session.setIsOpen(true); // Reopen for submission
            session.setExpiresAt(LocalDateTime.now().plusHours(1)); // Extend expiration
            attendanceSessionRepository.save(session);
        }

        // Set teacher clock-in time if not already set - this enables teaching history tracking
        if (session.getTeacherClockInTime() == null) {
            session.setTeacherClockInTime(LocalDateTime.now());
            attendanceSessionRepository.save(session);
        }

        // Process single attendance record
        User student = userRepository.findById(submitDto.getStudentId())
                .orElseThrow(() -> new BusinessLogicException("Student not found with ID: " + submitDto.getStudentId()));

        // Find existing record or create new one
        Attendance attendance = attendanceRepository.findBySession_IdAndStudent_Id(session.getId(), student.getId())
                .orElseGet(Attendance::new);

        attendance.setSession(session);
        attendance.setStudent(student);
        attendance.setStatus(AttendanceStatus.valueOf(submitDto.getStatus().toUpperCase()));
        // Lưu ghi chú nếu có
        attendance.setNote(submitDto.getNote());

        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public AttendanceSession createSession(CreateAttendanceSessionDto createDto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!classroomSecurityService.isTeacherOfClassroom(user, createDto.getClassroomId())) {
            throw new BusinessLogicException("Chỉ giáo viên mới có thể tạo phiên điểm danh.");
        }

        Classroom classroom = classroomRepository.findById(createDto.getClassroomId())
                .orElseThrow(() -> new BusinessLogicException("Classroom not found"));

        AttendanceSession session = new AttendanceSession();
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.ofInstant(createDto.getEndTime(), ZoneId.systemDefault()));
        session.setClassroom(classroom);
        session.setIsOpen(true);

        return attendanceSessionRepository.save(session);
    }

    @Override
public AttendanceSessionDto getActiveSession(Long classroomId) {
    Optional<AttendanceSession> sessionOpt = attendanceSessionRepository
            .findByClassroomIdAndIsOpenTrue(classroomId);
    
    if (sessionOpt.isEmpty()) {
        return null;
    }
    
    AttendanceSession session = sessionOpt.get();
    return mapToSessionDto(session);
}

    @Override
@Transactional
public void markAttendance(StudentAttendanceDto dto, UserDetails userDetails) {
    // Get the current user from security context
    User student = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new BusinessLogicException("Không tìm thấy người dùng"));
    
    // Find the attendance session
    AttendanceSession session = attendanceSessionRepository.findById(dto.getSessionId())
            .orElseThrow(() -> new BusinessLogicException("Attendance session not found"));
    
    // Check if session is open
    if (!session.getIsOpen()) {
        throw new BusinessLogicException("Phiên điểm danh đã đóng");
    }
    
    // Check if session has expired
    if (session.getExpiresAt() != null && LocalDateTime.now().isAfter(session.getExpiresAt())) {
        throw new BusinessLogicException("Phiên điểm danh đã hết hạn");
    }
    
    // Check if student is enrolled in the classroom
    if (!classroomSecurityService.isMember(session.getClassroom().getId(), userDetails)) {
        throw new BusinessLogicException("Bạn chưa được ghi danh trong lớp học này");
    }
    
    // Check if attendance already recorded
    if (attendanceRepository.existsByStudentAndSession(student, session)) {
        throw new BusinessLogicException("Bạn đã điểm danh cho phiên này rồi");
    }
    
    // Create attendance record
    Attendance attendance = Attendance.builder()
            .session(session)
            .student(student)
            .status(AttendanceStatus.PRESENT)
            .build();
    
    attendanceRepository.save(attendance);
}

    @Override
public List<AttendanceResultDto> getSessionResults(Long sessionId) {
    // Find the attendance session
    AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessLogicException("Attendance session not found"));
    
    // Security check: only teacher can view session results
    User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!classroomSecurityService.isTeacherOfClassroom(currentUser, session.getClassroom().getId())) {
        throw new BusinessLogicException("Chỉ giáo viên mới có thể xem kết quả phiên");
    }
    
    // Get all students enrolled in the classroom
    List<User> studentsInClass = enrollmentRepository.findById_ClassroomId(session.getClassroom().getId())
            .stream()
            .map(enrollment -> enrollment.getUser())
            .collect(Collectors.toList());
    
    // Get attendance records for this session
    List<Attendance> attendanceRecords = attendanceRepository.findBySession(session);
    Map<Long, AttendanceStatus> attendanceMap = attendanceRecords.stream()
            .collect(Collectors.toMap(
                attendance -> attendance.getStudent().getId(),
                Attendance::getStatus
            ));
    
    // Create result DTOs for all students
    return studentsInClass.stream()
            .map(student -> {
                AttendanceStatus status = attendanceMap.getOrDefault(student.getId(), AttendanceStatus.ABSENT);
                return new AttendanceResultDto(
                    1L, // totalSessions - this is for a single session
                    status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE ? 1L : 0L, // attendedSessions
                    status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE ? 100.0 : 0.0, // attendancePercentage
                    Collections.emptyList() // detailedRecords - not needed for session results
                );
            })
            .collect(Collectors.toList());
}

    @Override
    public List<AttendanceRecordDto> getAttendanceForLecture(Long lectureId, Long classroomId) {
        List<User> studentsInClass = enrollmentRepository.findById_ClassroomId(classroomId)
                .stream()
                .map(enrollment -> enrollment.getUser())
                .collect(Collectors.toList());

        Optional<AttendanceSession> sessionOpt = attendanceSessionRepository.findByLectureId(lectureId);

        if (sessionOpt.isEmpty()) {
            return studentsInClass.stream()
                    .map(student -> new AttendanceRecordDto(student.getId(), student.getFullName(), student.getEmail(), null, null))
                    .collect(Collectors.toList());
        }

        AttendanceSession session = sessionOpt.get();
        List<Attendance> records = session.getRecords();
        Map<Long, AttendanceStatus> statusMap = records.stream()
            .collect(Collectors.toMap(record -> record.getStudent().getId(), Attendance::getStatus));

        return studentsInClass.stream()
                .map(student -> {
                    AttendanceStatus status = statusMap.get(student.getId());
                    // Tìm note nếu có
                    String note = records.stream()
                        .filter(r -> r.getStudent().getId().equals(student.getId()))
                        .map(Attendance::getNote)
                        .findFirst()
                        .orElse(null);
                    return new AttendanceRecordDto(student.getId(), student.getFullName(), student.getEmail(), status, note);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MyAttendanceHistoryDto> getMyAttendanceHistory(Long studentId, Long classroomId) {
        System.out.println("Service: Getting attendance history for student " + studentId + " in classroom " + classroomId);
        List<Attendance> rawAttendance = attendanceRepository.findByStudentIdAndSession_ClassroomIdOrderBySession_SessionDateDesc(studentId, classroomId);
        System.out.println("Service: Found " + rawAttendance.size() + " raw attendance records");

        List<MyAttendanceHistoryDto> dtos = rawAttendance.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        System.out.println("Service: Found " + dtos.size() + " DTO records");
        return dtos;
    }

    private MyAttendanceHistoryDto mapToDto(Attendance attendance) {
        MyAttendanceHistoryDto dto = new MyAttendanceHistoryDto();
        dto.setLectureId(attendance.getSession().getLecture().getId());
        dto.setLectureTitle(attendance.getSession().getLecture().getTitle());
        dto.setSessionDate(attendance.getSession().getSessionDate());
        dto.setStatus(attendance.getStatus());
        return dto;
    }
    
    @Override
    public List<TeachingHistoryDto> getTeachingHistory(Long teacherId) {
        System.out.println("Getting teaching history for teacher ID: " + teacherId);
        
        List<AttendanceSession> sessions = attendanceSessionRepository.findTeachingHistoryByTeacherId(teacherId);
        System.out.println("Found " + sessions.size() + " attendance sessions with teacherClockInTime set");
        
        return sessions.stream().map(session -> {
            Lecture lecture = session.getLecture();
            System.out.println("Processing session ID: " + session.getId() + 
                             ", Lecture: " + lecture.getTitle() + 
                             ", Clock-in time: " + session.getTeacherClockInTime());
                             
            TeachingHistoryDto dto = new TeachingHistoryDto();
            dto.setLectureId(lecture.getId());
            dto.setLectureTitle(lecture.getTitle());
            dto.setClassroomId(session.getClassroom().getId());
            dto.setClassroomName(lecture.getClassroom() != null ? lecture.getClassroom().getName() : "Unknown");
            dto.setLectureDate(lecture.getLectureDate());
            dto.setClockInTime(session.getTeacherClockInTime());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Attendance recordStudentAttendance(Long sessionId, String studentCode) {
        // We get the user from the security context, assuming they are logged in.
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessLogicException("Attendance session not found"));

        if (!session.getIsOpen()) {
            throw new BusinessLogicException("Phiên điểm danh đã đóng");
        }
        if (session.getExpiresAt() != null && LocalDateTime.now().isAfter(session.getExpiresAt())) {
            throw new BusinessLogicException("Phiên điểm danh đã hết hạn");
        }

        if (attendanceRepository.findBySession_IdAndStudent_Id(sessionId, user.getId()).isPresent()) {
            throw new BusinessLogicException("Bạn đã điểm danh cho phiên này rồi");
        }

        Attendance record = new Attendance();
        record.setSession(session);
        record.setStudent(user);
        record.setStatus(AttendanceStatus.PRESENT);

        return attendanceRepository.save(record);
    }

    @Override
    @Transactional
    public AttendanceSession closeSession(Long sessionId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessLogicException("Attendance session not found"));

        if (!classroomSecurityService.isTeacherOfClassroom(user, session.getClassroom().getId())) {
            throw new BusinessLogicException("Chỉ giáo viên mới có thể đóng phiên");
        }

        session.setIsOpen(false);
        return attendanceSessionRepository.save(session);
    }

    @Override
    public List<StudentAttendanceDto> getSessionAttendance(Long sessionId) {
        // Security check: only teacher of the class can view all records for a session
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessLogicException("Attendance session not found"));

        if (!classroomSecurityService.isTeacherOfClassroom(user, session.getClassroom().getId())) {
            throw new BusinessLogicException("You are not authorized to view this attendance session.");
        }

        List<Attendance> records = attendanceRepository.findBySession(session);
        return records.stream()
                .map(this::mapToStudentAttendanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceResultDto getAttendanceResult(Long classroomId, Long studentId) {
        // Security: Teacher of the class or the student themselves can view the result.
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isTeacher = classroomSecurityService.isTeacherOfClassroom(currentUser, classroomId);
        boolean isSelf = currentUser.getId().equals(studentId);

        if (!isTeacher && !isSelf) {
            throw new BusinessLogicException("Không có quyền xem kết quả điểm danh này.");
        }

        List<AttendanceSession> sessions = attendanceSessionRepository.findByClassroomId(classroomId);
        long totalSessions = sessions.size();
        if (totalSessions == 0) {
            return new AttendanceResultDto(0, 0, 0.0, Collections.emptyList());
        }

        List<Attendance> studentRecords = attendanceRepository.findByStudentIdAndSessionClassroomId(studentId, classroomId);
        long attendedSessions = studentRecords.size();
        double attendancePercentage = (double) attendedSessions / totalSessions * 100;

        List<StudentAttendanceDto> detailedRecords = studentRecords.stream()
                .map(this::mapToStudentAttendanceDto)
                .collect(Collectors.toList());

        return new AttendanceResultDto(totalSessions, attendedSessions, attendancePercentage, detailedRecords);
    }

    private StudentAttendanceDto mapToStudentAttendanceDto(Attendance record) {
        StudentAttendanceDto dto = new StudentAttendanceDto();
        dto.setSessionId(record.getSession().getId());
        return dto;
    }

    @Override
    public List<AttendanceDto> findByUserId(Long userId) {
        // First find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id: " + userId));
        
        List<Attendance> attendances = attendanceRepository.findByStudent(user);
        return attendances.stream()
                .map(this::mapToAttendanceDto)
                .collect(Collectors.toList());
    }

    private AttendanceDto mapToAttendanceDto(Attendance attendance) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(attendance.getId());
        dto.setUserId(attendance.getStudent().getId());
        dto.setUserName(attendance.getStudent().getUsername());
        dto.setUserFullName(attendance.getStudent().getFullName());
        dto.setPresent(attendance.getStatus() == AttendanceStatus.PRESENT);
        dto.setAttendanceType(attendance.getStatus().name());
        LocalDate sessionDate = attendance.getSession().getSessionDate();
        dto.setSessionDate(sessionDate != null ? sessionDate.atStartOfDay() : LocalDateTime.now());
        if (attendance.getSession().getClassroom() != null) {
            dto.setClassroomId(attendance.getSession().getClassroom().getId());
            dto.setClassroomName(attendance.getSession().getClassroom().getName());
        }
        return dto;
    }

    
    private AttendanceSessionDto mapToSessionDto(AttendanceSession session) {
        AttendanceSessionDto dto = new AttendanceSessionDto();
        dto.setId(session.getId());
        dto.setClassroomId(session.getClassroom().getId());
        dto.setClassroomName(session.getClassroom().getName());
        dto.setStartTime(session.getCreatedAt());
        dto.setEndTime(session.getExpiresAt());
        dto.setStatus(session.getIsOpen() ? "ACTIVE" : "CLOSED");
        dto.setActive(session.isActive());
        dto.setAutoMarkTeacherAttendance(session.isAutoMarkTeacherAttendance());
        dto.setCreatedAt(session.getCreatedAt());
        
        // Set teacher info if available
        if (session.getClassroom().getTeacher() != null) {
            dto.setTeacherId(session.getClassroom().getTeacher().getId());
            dto.setTeacherName(session.getClassroom().getTeacher().getFullName());
        }
        
        // Set title based on lecture or default
        if (session.getLecture() != null) {
            dto.setTitle(session.getLecture().getTitle());
        } else {
            dto.setTitle("Attendance Session");
        }
        
        return dto;
    }
}
