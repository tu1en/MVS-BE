package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
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
import com.classroomapp.classroombackend.service.firebase.FirebaseClassroomService;

import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

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
    private final FirebaseClassroomService firebaseClassroomService;

    @Override
    @Transactional
    public void createOrUpdateAttendance(CreateOrUpdateAttendanceDto dto) {
       throw new UnsupportedOperationException("Phương thức này đã bị ngừng sử dụng và thuộc luồng điểm danh cũ.");
    }

    @Override
    @Transactional
    public void submitAttendance(AttendanceSubmitDto submitDto) {
        System.out.println("=== SUBMIT ATTENDANCE DEBUG ===");
        System.out.println("Received DTO: " + submitDto);
        
        // Validate classroom existence
        Classroom classroom = classroomRepository.findById(submitDto.getClassroomId())
                .orElseThrow(() -> new BusinessLogicException("Classroom not found with ID: " + submitDto.getClassroomId()));
        
        System.out.println("Found classroom: " + classroom.getId());
        
        // Validate attendance time window (24 hours) - MANDATORY
        if (submitDto.getLectureId() == null) {
            throw new BusinessLogicException("Lecture ID là bắt buộc để kiểm tra thời gian điểm danh.");
        }
        validateAttendanceTimeWindow(submitDto.getLectureId());
        
        // Validate records exist
        if (submitDto.getRecords() == null || submitDto.getRecords().isEmpty()) {
            throw new BusinessLogicException("Không có bản ghi điểm danh nào được cung cấp");
        }
        
        System.out.println("Processing " + submitDto.getRecords().size() + " attendance records");

        // Get the lecture for linking to the session
        Lecture lecture = lectureRepository.findById(submitDto.getLectureId())
                .orElseThrow(() -> new BusinessLogicException("Không tìm thấy bài giảng với ID: " + submitDto.getLectureId()));

        // Check if session already exists for this lecture today
        LocalDate today = LocalDate.now();
        Optional<AttendanceSession> existingSession = attendanceSessionRepository
                .findByLectureIdAndSessionDate(submitDto.getLectureId(), today);

        AttendanceSession session;
        if (existingSession.isPresent()) {
            session = existingSession.get();
            System.out.println("Using existing session: " + session.getId());
        } else {
            // Create a new session for this attendance submission
            session = new AttendanceSession();
            session.setClassroom(classroom);
            session.setLecture(lecture); // Link session to lecture
            session.setCreatedAt(LocalDateTime.now());
            session.setExpiresAt(LocalDateTime.now().plusHours(1)); // Session expires in 1 hour
            session.setIsOpen(true);
            session.setSessionDate(today); // Set session date to today
            // Set teacher clock-in time when attendance is submitted - this enables teaching history tracking
            session.setTeacherClockInTime(LocalDateTime.now());
            session = attendanceSessionRepository.save(session);
            System.out.println("Created new session: " + session.getId());
        }

        // Process all attendance records
        for (AttendanceSubmitDto.AttendanceRecord record : submitDto.getRecords()) {
            try {
                System.out.println("Processing record for student: " + record.getStudentId() + ", status: " + record.getStatus());
                
                // Validate student existence
                User student = userRepository.findById(record.getStudentId())
                        .orElseThrow(() -> new BusinessLogicException("Student not found with ID: " + record.getStudentId()));

                // Find existing record or create new one
                Attendance attendance = attendanceRepository.findBySession_IdAndStudent_Id(session.getId(), student.getId())
                        .orElseGet(Attendance::new);

                attendance.setSession(session);
                attendance.setStudent(student);
                attendance.setStatus(AttendanceStatus.valueOf(record.getStatus().toUpperCase()));
                // Lưu ghi chú nếu có
                attendance.setNote(record.getNote());

                attendanceRepository.save(attendance);
                System.out.println("Saved attendance for student: " + student.getId());
                
            } catch (Exception e) {
                System.err.println("Error processing attendance record for student " + record.getStudentId() + ": " + e.getMessage());
                throw new BusinessLogicException("Failed to process attendance for student " + record.getStudentId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("=== SUBMIT ATTENDANCE COMPLETED ===");
    }

    /**
     * Validate that attendance is being submitted within a reasonable time window
     * @param lectureId The lecture ID (required)
     */
    private void validateAttendanceTimeWindow(Long lectureId) {
        LocalDateTime now = LocalDateTime.now();
        
        System.out.println("=== VALIDATING ATTENDANCE TIME WINDOW ===");
        System.out.println("Lecture ID: " + lectureId);
        System.out.println("Current time: " + now);
        
        // Lecture ID is mandatory
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new BusinessLogicException("Không tìm thấy bài giảng với ID: " + lectureId));
        
        System.out.println("Found lecture: " + lecture.getTitle());
        
        // Check if lecture has a specific date
        if (lecture.getLectureDate() == null) {
            // Option 1: Require teacher to set proper lecture date first
            throw new BusinessLogicException("Bài giảng chưa có ngày cụ thể. Vui lòng cập nhật ngày cho bài giảng trước khi điểm danh.");
            
            // Option 2: Only allow if teacher explicitly confirms today's date
            // For now, we use strict validation requiring proper lecture scheduling
        }
        
        LocalDate lectureDate = lecture.getLectureDate();
        System.out.println("Lecture date: " + lectureDate);
        
        LocalDateTime lectureStart = null;
        LocalDateTime lectureEnd = null;
        
        // Try to get time from schedule
        if (lecture.getSchedule() != null && lecture.getSchedule().getStartTime() != null) {
            lectureStart = lectureDate.atTime(lecture.getSchedule().getStartTime());
            if (lecture.getSchedule().getEndTime() != null) {
                lectureEnd = lectureDate.atTime(lecture.getSchedule().getEndTime());
            }
            System.out.println("Schedule found - Start: " + lectureStart + ", End: " + lectureEnd);
        } else {
            // If no schedule time, use middle of day (12:00) as default
            lectureStart = lectureDate.atTime(12, 0);
            System.out.println("No schedule found, using default time: " + lectureStart);
        }
        
        // Enhanced validation logic to support all test cases
        LocalDate today = LocalDate.now();

        // Test Case 2: Future dates (tomorrow and beyond) - Block with specific message
        if (lectureDate.isAfter(today)) {
            String errorMsg = "⏰ CHƯA ĐẾN THỜI GIAN ĐIỂM DANH!\n" +
                "Ngày hôm nay: " + today.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                "Ngày buổi học: " + lectureDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                "Chỉ có thể điểm danh từ ngày " + lectureDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " trở đi.";
            System.out.println("VALIDATION FAILED: Future date - " + errorMsg);
            throw new BusinessLogicException(errorMsg);
        }

        // Test Case 3: Past dates - Require makeup request (will be handled by frontend)
        if (lectureDate.isBefore(today)) {
            String errorMsg = "📝 CẦN TẠO YÊU CẦU ĐIỂM DANH BÙ!\n" +
                "Ngày hôm nay: " + today.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                "Ngày buổi học: " + lectureDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                "Đã quá thời hạn điểm danh thông thường. Vui lòng tạo yêu cầu điểm danh bù để được manager phê duyệt.";
            System.out.println("VALIDATION FAILED: Past date - requires makeup request - " + errorMsg);
            throw new BusinessLogicException(errorMsg);
        }

        // Test Case 1: Today - Continue with 24-hour window validation
        
        // Original 24-hour window validation (kept for future use)
        LocalDateTime maxAllowedTime = lectureStart.plusHours(24);
        LocalDateTime minAllowedTime = lectureStart.minusHours(24);
        
        System.out.println("Allowed time window: " + minAllowedTime + " to " + maxAllowedTime);
        
        if (now.isBefore(minAllowedTime)) {
            String errorMsg = "⏰ Không thể điểm danh quá sớm!\n" +
                "Chỉ có thể điểm danh trong vòng 24 giờ trước buổi học.\n" +
                "Buổi học: " + lectureDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                (lecture.getSchedule() != null && lecture.getSchedule().getStartTime() != null ? 
                    " lúc " + lecture.getSchedule().getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "") +
                "\nCó thể điểm danh từ: " + minAllowedTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            System.out.println("VALIDATION FAILED: Too early - " + errorMsg);
            throw new BusinessLogicException(errorMsg);
        }
        
        if (now.isAfter(maxAllowedTime)) {
            String errorMsg = "⏰ Không thể điểm danh quá muộn!\n" +
                "Chỉ có thể điểm danh trong vòng 24 giờ sau buổi học.\n" +
                "Buổi học: " + lectureDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                (lectureEnd != null ? 
                    " kết thúc lúc " + lecture.getSchedule().getEndTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) :
                    (lecture.getSchedule() != null && lecture.getSchedule().getStartTime() != null ? 
                        " bắt đầu lúc " + lecture.getSchedule().getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "")) +
                "\nĐã hết hạn điểm danh từ: " + maxAllowedTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            System.out.println("VALIDATION FAILED: Too late - " + errorMsg);
            throw new BusinessLogicException(errorMsg);
        }
        
        System.out.println("✅ VALIDATION PASSED: Attendance within allowed time window");
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

        Optional<AttendanceSession> sessionOpt = attendanceSessionRepository.findTopByLectureIdOrderByCreatedAtDesc(lectureId);

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
    public Map<String, Object> getAttendanceStatusForLecture(Long lectureId, Long classroomId) {
        Map<String, Object> status = new HashMap<>();

        try {
            // Get lecture to check date and time validation
            Lecture lecture = lectureRepository.findById(lectureId)
                    .orElseThrow(() -> new BusinessLogicException("Không tìm thấy bài giảng với ID: " + lectureId));

            LocalDate lectureDate = lecture.getLectureDate();
            LocalDate today = LocalDate.now();

            // Get all students in the classroom
            List<User> studentsInClass = enrollmentRepository.findById_ClassroomId(classroomId)
                    .stream()
                    .map(enrollment -> enrollment.getUser())
                    .collect(Collectors.toList());

            int totalStudents = studentsInClass.size();

            // Find attendance session for this lecture
            Optional<AttendanceSession> sessionOpt = attendanceSessionRepository.findTopByLectureIdOrderByCreatedAtDesc(lectureId);

            // Check for makeup approval
            boolean makeupApproved = checkMakeupApproval(lectureId, classroomId);

            // Determine overall status based on date and existing attendance
            String overallStatus;

            if (lectureDate.isAfter(today)) {
                // Test Case 2: Future dates
                overallStatus = "TOO_EARLY";
            } else if (lectureDate.isBefore(today)) {
                // Test Case 3: Past dates
                if (sessionOpt.isPresent()) {
                    overallStatus = "ALREADY_TAKEN"; // Already taken, can't retake past attendance
                } else if (makeupApproved) {
                    overallStatus = "MAKEUP_APPROVED"; // Can take makeup attendance
                } else {
                    overallStatus = "NEEDS_MAKEUP_REQUEST"; // Need to request makeup
                }
            } else {
                // Test Case 1: Today
                if (sessionOpt.isPresent()) {
                    overallStatus = "ALREADY_TAKEN"; // Can retake with confirmation
                } else {
                    overallStatus = "CAN_TAKE_NORMAL"; // Normal attendance
                }
            }

            // Calculate attendance counts
            int presentCount = 0;
            int absentCount = totalStudents;
            boolean sessionExists = false;
            Long sessionId = null;
            LocalDate sessionDate = null;
            boolean isOpen = false;

            if (sessionOpt.isPresent()) {
                AttendanceSession session = sessionOpt.get();
                List<Attendance> records = session.getRecords();

                presentCount = (int) records.stream()
                        .filter(record -> record.getStatus() == AttendanceStatus.PRESENT ||
                                        record.getStatus() == AttendanceStatus.LATE)
                        .count();
                absentCount = totalStudents - presentCount;
                sessionExists = true;
                sessionId = session.getId();
                sessionDate = session.getSessionDate();
                isOpen = session.getIsOpen();
            }

            // Build response
            status.put("overallStatus", overallStatus);
            status.put("totalStudents", totalStudents);
            status.put("presentCount", presentCount);
            status.put("absentCount", absentCount);
            status.put("makeupApproved", makeupApproved);
            status.put("sessionExists", sessionExists);
            status.put("existingRecordsCount", sessionExists ? presentCount + absentCount : 0);

            if (sessionExists) {
                status.put("sessionId", sessionId);
                status.put("sessionDate", sessionDate);
                status.put("isOpen", isOpen);
            }

        } catch (Exception e) {
            // Return error status
            status.put("overallStatus", "ERROR");
            status.put("totalStudents", 0);
            status.put("presentCount", 0);
            status.put("absentCount", 0);
            status.put("makeupApproved", false);
            status.put("sessionExists", false);
            status.put("error", e.getMessage());
        }

        return status;
    }

    /**
     * Check if makeup attendance has been approved for this lecture
     */
    private boolean checkMakeupApproval(Long lectureId, Long classroomId) {
        // TODO: Implement actual makeup approval check
        // For now, return false - this should check MakeupAttendanceRequest table
        return false;
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
    @Transactional
    public void deleteSession(Long sessionId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessLogicException("Attendance session not found"));

        if (!classroomSecurityService.isTeacherOfClassroom(user, session.getClassroom().getId())) {
            throw new BusinessLogicException("Chỉ giáo viên mới có thể xóa phiên");
        }

        // Xóa tất cả attendance records trước
        List<Attendance> attendanceRecords = attendanceRepository.findBySession(session);
        if (!attendanceRecords.isEmpty()) {
            attendanceRepository.deleteAll(attendanceRecords);
            attendanceRepository.flush(); // Đảm bảo xóa ngay lập tức
        }

        // Sau đó xóa session
        attendanceSessionRepository.delete(session);

        // Xóa khỏi Firebase nếu có
        firebaseClassroomService.removeSession(sessionId);
    }

    /**
     * Xóa session bằng SQL native (alternative method nếu cascade không hoạt động)
     */
    @Transactional
    public void deleteSessionNative(Long sessionId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessLogicException("Attendance session not found"));

        if (!classroomSecurityService.isTeacherOfClassroom(user, session.getClassroom().getId())) {
            throw new BusinessLogicException("Chỉ giáo viên mới có thể xóa phiên");
        }

        // Sử dụng SQL native để xóa
        attendanceSessionRepository.deleteSessionWithRecords(sessionId);

        // Xóa khỏi Firebase nếu có
        firebaseClassroomService.removeSession(sessionId);
    }

    @Override
    public List<StudentAttendanceDto> getSessionAttendance(Long sessionId) {
        // Security check: only teacher of the class can view all records for a session
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessLogicException("Attendance session not found"));

        if (!classroomSecurityService.isTeacherOfClassroom(user, session.getClassroom().getId())) {
            throw new BusinessLogicException("Bạn không có quyền xem phiên điểm danh này.");
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
