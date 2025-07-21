package com.classroomapp.classroombackend.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.LiveStreamDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.LiveStream;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.LiveStreamRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.LiveSessionAttendanceService;

@Service
@Transactional
public class LiveSessionAttendanceServiceImpl implements LiveSessionAttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(LiveSessionAttendanceServiceImpl.class);

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LiveStreamRepository liveStreamRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Override
    public AttendanceSession createAttendanceSessionForLiveStream(LiveStreamDto liveStreamDto) {
        try {
            logger.info("Creating attendance session for live stream: {}", liveStreamDto.getId());

            Optional<Lecture> lectureOpt = lectureRepository.findById(liveStreamDto.getLectureId());
            if (!lectureOpt.isPresent()) {
                throw new ResourceNotFoundException("Lecture not found: " + liveStreamDto.getLectureId());
            }

            Lecture lecture = lectureOpt.get();
            Classroom classroom = lecture.getClassroom();
            User teacher = lecture.getTeacher();

            AttendanceSession attendanceSession = AttendanceSession.builder()
                    .lecture(lecture)
                    .classroom(classroom)
                    .teacher(teacher)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(3))
                    .isOpen(true)
                    .status(AttendanceSession.SessionStatus.OPEN)
                    .startTime(Instant.now())
                    .sessionDate(LocalDate.now())
                    .autoMarkTeacherAttendance(true)
                    .isActive(true)
                    .teacherClockInTime(LocalDateTime.now())
                    .build();

            String qrCodeData = "LIVE_SESSION_" + liveStreamDto.getId() + "_" + System.currentTimeMillis();
            attendanceSession.setQrCodeData(qrCodeData);

            AttendanceSession savedSession = attendanceSessionRepository.save(attendanceSession);
            logger.info("Created attendance session {} for live stream {}", savedSession.getId(), liveStreamDto.getId());

            return savedSession;

        } catch (Exception e) {
            logger.error("Error creating attendance session for live stream: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create attendance session", e);
        }
    }

    @Override
    public Map<String, Object> markStudentJoinedLiveSession(Long liveStreamId, Long studentId, LocalDateTime joinTime) {
        try {
            logger.info("Marking student {} joined live session: {}", studentId, liveStreamId);

            AttendanceSession attendanceSession = getAttendanceSessionByLiveStreamId(liveStreamId);
            if (attendanceSession == null) {
                throw new ResourceNotFoundException("No attendance session found for live stream: " + liveStreamId);
            }

            Optional<User> studentOpt = userRepository.findById(studentId);
            if (!studentOpt.isPresent()) {
                throw new ResourceNotFoundException("Student not found: " + studentId);
            }

            User student = studentOpt.get();

            Optional<Attendance> existingAttendance = attendanceRepository
                    .findBySessionAndStudent(attendanceSession, student);

            Attendance attendance;
            if (existingAttendance.isPresent()) {
                attendance = existingAttendance.get();
                if (attendance.getJoinTime() == null) {
                    attendance.setJoinTime(joinTime.atZone(ZoneId.systemDefault()).toInstant());
                    attendance.setStatus(AttendanceStatus.PRESENT); // ✅ Sửa
                }
            } else {
                attendance = Attendance.builder()
                        .session(attendanceSession)
                        .student(student)
                        .joinTime(joinTime.atZone(ZoneId.systemDefault()).toInstant())
                        .status(AttendanceStatus.PRESENT) // ✅ Sửa
                        .build();
            }

            Attendance savedAttendance = attendanceRepository.save(attendance);

            Map<String, Object> result = new HashMap<>();
            result.put("attendanceId", savedAttendance.getId());
            result.put("studentId", studentId);
            result.put("liveStreamId", liveStreamId);
            result.put("joinTime", joinTime);
            result.put("status", "JOINED");

            logger.info("Marked student {} as joined live session {}", studentId, liveStreamId);
            return result;

        } catch (Exception e) {
            logger.error("Error marking student joined live session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark student attendance", e);
        }
    }

    @Override
    public Map<String, Object> markStudentLeftLiveSession(Long liveStreamId, Long studentId, LocalDateTime leaveTime) {
        try {
            logger.info("Marking student {} left live session: {}", studentId, liveStreamId);

            AttendanceSession attendanceSession = getAttendanceSessionByLiveStreamId(liveStreamId);
            if (attendanceSession == null) {
                throw new ResourceNotFoundException("No attendance session found for live stream: " + liveStreamId);
            }

            Optional<User> studentOpt = userRepository.findById(studentId);
            if (!studentOpt.isPresent()) {
                throw new ResourceNotFoundException("Student not found: " + studentId);
            }

            User student = studentOpt.get();

            Optional<Attendance> attendanceOpt = attendanceRepository
                    .findBySessionAndStudent(attendanceSession, student);

            if (attendanceOpt.isPresent()) {
                Attendance attendance = attendanceOpt.get();
                attendance.setLeaveTime(leaveTime.atZone(ZoneId.systemDefault()).toInstant());

                if (attendance.getJoinTime() != null) {
                    long durationMinutes = java.time.Duration.between(
                            attendance.getJoinTime(),
                            attendance.getLeaveTime()
                    ).toMinutes();
                    attendance.setDurationMinutes((int) durationMinutes);
                }

                attendanceRepository.save(attendance);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("studentId", studentId);
            result.put("liveStreamId", liveStreamId);
            result.put("leaveTime", leaveTime);
            result.put("status", "LEFT");

            logger.info("Marked student {} as left live session {}", studentId, liveStreamId);
            return result;

        } catch (Exception e) {
            logger.error("Error marking student left live session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark student leave", e);
        }
    }

    @Override
    public AttendanceSession endAttendanceSessionForLiveStream(Long liveStreamId) {
        try {
            logger.info("Ending attendance session for live stream: {}", liveStreamId);

            AttendanceSession attendanceSession = getAttendanceSessionByLiveStreamId(liveStreamId);
            if (attendanceSession != null) {
                attendanceSession.setStatus(AttendanceSession.SessionStatus.CLOSED);
                attendanceSession.setEndTime(Instant.now());
                attendanceSession.setIsOpen(false);

                AttendanceSession savedSession = attendanceSessionRepository.save(attendanceSession);
                logger.info("Ended attendance session {} for live stream {}", savedSession.getId(), liveStreamId);
                return savedSession;
            }

            return null;

        } catch (Exception e) {
            logger.error("Error ending attendance session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to end attendance session", e);
        }
    }

    @Override
    public List<Map<String, Object>> getAttendanceForLiveSession(Long liveStreamId) {
        try {
            AttendanceSession attendanceSession = getAttendanceSessionByLiveStreamId(liveStreamId);
            if (attendanceSession == null) {
                return new ArrayList<>();
            }

            List<Attendance> attendanceRecords = attendanceRepository.findBySession(attendanceSession);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Attendance attendance : attendanceRecords) {
                Map<String, Object> record = new HashMap<>();
                record.put("attendanceId", attendance.getId());
                record.put("studentId", attendance.getStudent().getId());
                record.put("studentName", attendance.getStudent().getFullName());
                record.put("joinTime", attendance.getJoinTime());
                record.put("leaveTime", attendance.getLeaveTime());
                record.put("durationMinutes", attendance.getDurationMinutes());
                record.put("status", attendance.getStatus().toString()); // ✅ Sửa
                result.add(record);
            }

            return result;

        } catch (Exception e) {
            logger.error("Error getting attendance for live session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get attendance records", e);
        }
    }

    @Override
    public Map<String, Object> calculateAttendanceRateForLiveSession(Long liveStreamId) {
        try {
            AttendanceSession attendanceSession = getAttendanceSessionByLiveStreamId(liveStreamId);
            if (attendanceSession == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("attendanceRate", 0.0);
                result.put("totalStudents", 0);
                result.put("presentStudents", 0);
                return result;
            }

            List<Attendance> attendanceRecords = attendanceRepository.findBySession(attendanceSession);
            long presentCount = attendanceRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT) // ✅ Sửa
                    .count();

            int totalStudents = attendanceRecords.size();
            double attendanceRate = totalStudents > 0 ? (double) presentCount / totalStudents * 100 : 0.0;

            Map<String, Object> result = new HashMap<>();
            result.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            result.put("totalStudents", totalStudents);
            result.put("presentStudents", presentCount);
            result.put("liveStreamId", liveStreamId);
            result.put("sessionId", attendanceSession.getId());

            return result;

        } catch (Exception e) {
            logger.error("Error calculating attendance rate: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate attendance rate", e);
        }
    }

    @Override
    public AttendanceSession getAttendanceSessionByLiveStreamId(Long liveStreamId) {
        try {
            Optional<LiveStream> liveStreamOpt = liveStreamRepository.findById(liveStreamId);
            if (!liveStreamOpt.isPresent()) {
                return null;
            }

            LiveStream liveStream = liveStreamOpt.get();

            Optional<AttendanceSession> sessionOpt = attendanceSessionRepository
                    .findByLectureAndIsActiveOrderByCreatedAtDesc(liveStream.getLecture(), true)
                    .stream()
                    .findFirst();

            return sessionOpt.orElse(null);

        } catch (Exception e) {
            logger.error("Error finding attendance session for live stream: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public AttendanceSession updateAttendanceSessionFromLiveStream(Long liveStreamId, Map<String, Object> attendanceData) {
        try {
            AttendanceSession attendanceSession = getAttendanceSessionByLiveStreamId(liveStreamId);
            if (attendanceSession == null) {
                throw new ResourceNotFoundException("No attendance session found for live stream: " + liveStreamId);
            }

            if (attendanceData.containsKey("viewerCount")) {
                logger.info("Live stream {} has {} viewers", liveStreamId, attendanceData.get("viewerCount"));
            }

            if (attendanceData.containsKey("status") && "ENDED".equals(attendanceData.get("status"))) {
                attendanceSession.setStatus(AttendanceSession.SessionStatus.CLOSED);
                attendanceSession.setEndTime(Instant.now());
                attendanceSession.setIsOpen(false);
            }

            return attendanceSessionRepository.save(attendanceSession);

        } catch (Exception e) {
            logger.error("Error updating attendance session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update attendance session", e);
        }
    }
}
