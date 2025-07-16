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
       throw new UnsupportedOperationException("This method is deprecated and part of the old attendance flow.");
    }

    @Override
    @Transactional
    public void submitAttendance(AttendanceSubmitDto submitDto) {
        // Validate lecture and classroom existence
        Lecture lecture = lectureRepository.findById(submitDto.getLectureId())
                .orElseThrow(() -> new BusinessLogicException("Lecture not found with ID: " + submitDto.getLectureId()));

        Classroom classroom = classroomRepository.findById(submitDto.getClassroomId())
                .orElseThrow(() -> new BusinessLogicException("Classroom not found with ID: " + submitDto.getClassroomId()));

        // Ensure the lecture belongs to the classroom
        if (!lecture.getClassroom().getId().equals(classroom.getId())) {
            throw new BusinessLogicException("Lecture does not belong to the specified classroom.");
        }

        // Find or create an AttendanceSession for this lecture
        // For simplicity, we'll assume one session per lecture. In a real scenario,
        // you might have multiple sessions per lecture (e.g., re-takes)
        AttendanceSession session = attendanceSessionRepository.findByLectureId(lecture.getId())
                .orElseGet(() -> {
                    AttendanceSession newSession = new AttendanceSession();
                    newSession.setClassroom(classroom);
                    newSession.setLecture(lecture);
                    newSession.setCreatedAt(LocalDateTime.now());
                    newSession.setExpiresAt(LocalDateTime.now().plusHours(1)); // Example: session expires in 1 hour
                    newSession.setIsOpen(true);
                    newSession.setSessionDate(LocalDate.now()); // Set session date to today
                    return attendanceSessionRepository.save(newSession);
                });

        // Ensure session is open and not expired if a new one wasn't created
        if (!session.getIsOpen() ||
            (session.getExpiresAt() != null && LocalDateTime.now().isAfter(session.getExpiresAt()))) {
             // Optionally reopen or create new session based on business rules
             // For this task, we will just throw an error or handle accordingly
            session.setIsOpen(true); // Reopen for submission
            session.setExpiresAt(LocalDateTime.now().plusHours(1)); // Extend expiration
            attendanceSessionRepository.save(session);
        }

        // Process each attendance record
        for (AttendanceSubmitDto.AttendanceRecordUpdateDto recordDto : submitDto.getRecords()) {
            User student = userRepository.findById(recordDto.getStudentId())
                    .orElseThrow(() -> new BusinessLogicException("Student not found with ID: " + recordDto.getStudentId()));

            // Find existing record or create new one
            Attendance attendance = attendanceRepository.findBySession_IdAndStudent_Id(session.getId(), student.getId())
                    .orElseGet(Attendance::new);

            attendance.setSession(session);
            attendance.setStudent(student);
            attendance.setStatus(AttendanceStatus.valueOf(recordDto.getStatus().toUpperCase())); // Convert string to enum

            attendanceRepository.save(attendance);
        }
    }

    @Override
    @Transactional
    public AttendanceSession createSession(CreateAttendanceSessionDto createDto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!classroomSecurityService.isTeacherOfClassroom(user, createDto.getClassroomId())) {
            throw new BusinessLogicException("Only the teacher can create an attendance session.");
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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void markAttendance(StudentAttendanceDto dto, UserDetails userDetails) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<AttendanceResultDto> getSessionResults(Long sessionId) {
        throw new UnsupportedOperationException("Not implemented yet");
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
                    .map(student -> new AttendanceRecordDto(student.getId(), student.getFullName(), student.getEmail(), null))
                    .collect(Collectors.toList());
        }

        AttendanceSession session = sessionOpt.get();
        List<Attendance> records = session.getRecords();
        Map<Long, AttendanceStatus> statusMap = records.stream()
            .collect(Collectors.toMap(record -> record.getStudent().getId(), Attendance::getStatus));

        return studentsInClass.stream()
                .map(student -> {
                    AttendanceStatus status = statusMap.get(student.getId());
                    return new AttendanceRecordDto(student.getId(), student.getFullName(), student.getEmail(), status);
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
        List<AttendanceSession> sessions = attendanceSessionRepository.findTeachingHistoryByTeacherId(teacherId);
        
        return sessions.stream().map(session -> {
            Lecture lecture = session.getLecture();
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
            throw new BusinessLogicException("Attendance session is closed");
        }
        if (session.getExpiresAt() != null && LocalDateTime.now().isAfter(session.getExpiresAt())) {
            throw new BusinessLogicException("Attendance session has expired");
        }

        if (attendanceRepository.findBySession_IdAndStudent_Id(sessionId, user.getId()).isPresent()) {
            throw new BusinessLogicException("Attendance already recorded for this session");
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
            throw new BusinessLogicException("Only the teacher can close the session");
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
            throw new BusinessLogicException("Not authorized to view this attendance result.");
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
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
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


}
