package com.classroomapp.classroombackend.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seed sample staff attendance logs to cover scenarios:
 * - Normal day
 * - Late check-in
 * - Early check-out
 * - Overtime (OT=xxm, type OVERTIME)
 * - Weekend work (type WEEKEND)
 * - Holiday work (type HOLIDAY)
 * - Teacher day without OT flag
 */
@Component
@Order(130)
@RequiredArgsConstructor
@Slf4j
public class StaffAttendanceSampleSeeder implements CommandLineRunner {

    private final StaffAttendanceLogRepository attendanceRepo;
    private final UserRepository userRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            // Only seed if repository is nearly empty to avoid duplication
            if (attendanceRepo.count() > 50) {
                log.info("Skip StaffAttendanceSampleSeeder: existing logs > 50");
                return;
            }

            Optional<User> anyStaffOpt = userRepository.findAll().stream()
                .filter(u -> u.getRoleId() != null && u.getRoleId() != 1 && u.getRoleId() != 2) // not student, not teacher
                .min(Comparator.comparing(User::getId));

            Optional<User> anyTeacherOpt = userRepository.findAll().stream()
                .filter(u -> u.getRoleId() != null && u.getRoleId() == 2)
                .min(Comparator.comparing(User::getId));

            if (anyStaffOpt.isEmpty() && anyTeacherOpt.isEmpty()) {
                log.warn("No suitable users found for seeding attendance.");
                return;
            }

            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            LocalDate lastSunday = getLastSunday(today);

            anyStaffOpt.ifPresent(staff -> seedStaffScenarios(staff, today, yesterday, lastSunday));
            anyTeacherOpt.ifPresent(teacher -> seedTeacherScenarios(teacher, today, lastSunday));

            log.info("StaffAttendanceSampleSeeder completed.");
        } catch (Exception e) {
            log.error("Error seeding staff attendance samples", e);
        }
    }

    private void seedStaffScenarios(User staff, LocalDate today, LocalDate yesterday, LocalDate weekend) {
        // 1) Today: Late check-in + OT 45m
        createOrUpdateLog(staff, today,
            LocalTime.of(8, 45), // late
            LocalTime.of(18, 15), // OT 45m vs 17:30
            StaffAttendanceLog.AttendanceType.OVERTIME,
            "Late check-in; OT=45m");

        // 2) Yesterday: Late + Early
        createOrUpdateLog(staff, yesterday,
            LocalTime.of(8, 40),
            LocalTime.of(17, 10),
            StaffAttendanceLog.AttendanceType.NORMAL,
            "Late check-in; Early check-out");

        // 3) Weekend work (3h)
        createOrUpdateLog(staff, weekend,
            LocalTime.of(9, 0),
            LocalTime.of(12, 0),
            StaffAttendanceLog.AttendanceType.WEEKEND,
            "Weekend work");

        // 4) Holiday work (simulate)
        createOrUpdateLog(staff, today.minusDays(3),
            LocalTime.of(10, 0),
            LocalTime.of(16, 0),
            StaffAttendanceLog.AttendanceType.HOLIDAY,
            "Holiday work");
    }

    private void seedTeacherScenarios(User teacher, LocalDate today, LocalDate weekend) {
        // Build teacher logs based on AttendanceSessions (slots) for accuracy
        seedTeacherDayFromSessions(teacher, today, false);
        seedTeacherDayFromSessions(teacher, weekend, true);
    }

    private void seedTeacherDayFromSessions(User teacher, LocalDate date, boolean isWeekend) {
        // Fetch sessions for this teacher and date
        List<AttendanceSession> sessions = attendanceSessionRepository.findByClassroom_TeacherId(teacher.getId());
        List<AttendanceSession> daySessions = sessions.stream()
                .filter(s -> date.equals(s.getSessionDate()))
                .toList();

        LocalTime inTime;
        LocalTime outTime;
        String note;
        if (!daySessions.isEmpty()) {
            LocalDateTime earliest = daySessions.stream()
                    .map(s -> s.getCreatedAt() != null ? s.getCreatedAt() : LocalDateTime.of(date, LocalTime.of(7, 30)))
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.of(date, LocalTime.of(7, 30)));
            LocalDateTime latest = daySessions.stream()
                    .map(s -> s.getExpiresAt() != null ? s.getExpiresAt() : LocalDateTime.of(date, LocalTime.of(20, 30)))
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.of(date, LocalTime.of(20, 30)));
            inTime = earliest.toLocalTime();
            outTime = latest.toLocalTime();
            note = "Teaching slots: " + daySessions.size();
        } else {
            // Fallback minimal window if no sessions found
            inTime = LocalTime.of(8, 0);
            outTime = LocalTime.of(11, 0);
            note = "Teaching (no session records)";
        }

        createOrUpdateLog(teacher, date,
                inTime,
                outTime,
                isWeekend ? StaffAttendanceLog.AttendanceType.WEEKEND : StaffAttendanceLog.AttendanceType.NORMAL,
                note);
    }

    private void createOrUpdateLog(User user, LocalDate date, LocalTime in, LocalTime out,
                                   StaffAttendanceLog.AttendanceType type, String notes) {
        StaffAttendanceLog log = attendanceRepo.findByUserAndAttendanceDate(user, date)
            .orElseGet(StaffAttendanceLog::new);
        if (log.getId() == null) {
            log.setUser(user);
            log.setAttendanceDate(date);
            log.setCreatedAt(LocalDateTime.now());
        }
        log.setUpdatedAt(LocalDateTime.now());
        log.setCheckInTime(in);
        log.setCheckOutTime(out);
        log.setAttendanceType(type);
        log.setNotes(notes);
        attendanceRepo.save(log);
    }

    private LocalDate getLastSunday(LocalDate ref) {
        LocalDate d = ref;
        while (d.getDayOfWeek() != DayOfWeek.SUNDAY) {
            d = d.minusDays(1);
        }
        return d;
    }
}


