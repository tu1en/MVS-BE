package com.classroomapp.classroombackend.config.seed;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceSeeder {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final LectureRepository lectureRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @Transactional
    public void seed() {
        // We will now always run this seeder to ensure some teaching history exists.
        System.out.println("🔄 [AttendanceSeeder] Seeding attendance data and ensuring teaching history...");

        List<Classroom> classrooms = classroomRepository.findAll();
        if (classrooms.isEmpty()) {
            System.out.println("⚠️ [AttendanceSeeder] No classrooms found, skipping seed.");
            return;
        }

        for (Classroom classroom : classrooms) {
            seedAttendanceForClassroom(classroom);
        }

        // Seed attendance logs and staff attendance logs
        seedAttendanceLogs();
        seedStaffAttendanceLogs();
        
        System.out.println("✅ [AttendanceSeeder] Finished seeding attendance data with logs.");
    }
    
    private void seedAttendanceLogs() {
        if (attendanceLogRepository.count() > 0) {
            System.out.println("✅ [AttendanceSeeder] Attendance logs already exist. Skipping.");
            return;
        }
        
        List<User> allUsers = userRepository.findAll();
        if (allUsers.isEmpty()) {
            System.out.println("⚠️ [AttendanceSeeder] No users found for attendance logs seeding.");
            return;
        }
        
        // Create logs for the past 30 days for all users
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        for (User user : allUsers) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                // Skip weekends randomly (70% chance to skip)
                if ((date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7) 
                    && random.nextDouble() < 0.7) {
                    continue;
                }
                
                // Random 85% attendance rate
                if (random.nextDouble() < 0.85) {
                    AttendanceLog log = new AttendanceLog();
                    log.setUserId(user.getId());
                    log.setUserName(user.getFullName());
                    String roleName = getRoleName(user.getRoleId());
                    log.setRole(roleName);
                    log.setDepartment(getDepartmentForRole(roleName));
                    log.setDate(date);
                    log.setShift("MORNING");
                    
                    // Random check-in time (8:00-9:30) - Fixed minute calculation
                    LocalTime baseCheckIn = LocalTime.of(8, 0);
                    LocalTime checkIn = baseCheckIn.plusMinutes(random.nextInt(91)); // 0-90 minutes = 8:00-9:30
                    log.setCheckIn(checkIn);

                    // Random check-out time (16:30-18:00) - Fixed minute calculation
                    LocalTime baseCheckOut = LocalTime.of(16, 30);
                    LocalTime checkOut = baseCheckOut.plusMinutes(random.nextInt(91)); // 0-90 minutes = 16:30-18:00
                    log.setCheckOut(checkOut);
                    
                    // Determine status based on check-in time
                    if (checkIn.isBefore(LocalTime.of(8, 30))) {
                        log.setStatus("ON_TIME");
                    } else if (checkIn.isBefore(LocalTime.of(9, 0))) {
                        log.setStatus("LATE");
                    } else {
                        log.setStatus("VERY_LATE");
                    }
                    
                    attendanceLogRepository.save(log);
                }
            }
        }
        
        System.out.println("✅ [AttendanceSeeder] Created attendance logs for " + allUsers.size() + " users over 30 days.");
    }
    
    private void seedStaffAttendanceLogs() {
        // Staff attendance logs are essentially the same as attendance logs for staff
        // But we can create some specific staff-only logs
        List<User> staffUsers = userRepository.findByRoleId(1); // STAFF role
        
        if (staffUsers.isEmpty()) {
            System.out.println("⚠️ [AttendanceSeeder] No staff users found for staff attendance logs.");
            return;
        }
        
        // Create additional detailed logs for staff (overtime, special shifts)
        LocalDate startDate = LocalDate.now().minusDays(15);
        LocalDate endDate = LocalDate.now();
        
        for (User staff : staffUsers) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                // Random 60% chance for overtime/special duties
                if (random.nextDouble() < 0.6) {
                    AttendanceLog overtimeLog = new AttendanceLog();
                    overtimeLog.setUserId(staff.getId());
                    overtimeLog.setUserName(staff.getFullName());
                    overtimeLog.setRole("STAFF");
                    overtimeLog.setDepartment("ADMINISTRATION");
                    overtimeLog.setDate(date);
                    overtimeLog.setShift("EVENING");
                    
                    // Evening shift times
                    LocalTime checkIn = LocalTime.of(18, random.nextInt(60));
                    overtimeLog.setCheckIn(checkIn);
                    
                    LocalTime checkOut = LocalTime.of(20 + random.nextInt(3), random.nextInt(60));
                    overtimeLog.setCheckOut(checkOut);
                    
                    overtimeLog.setStatus("OVERTIME");
                    
                    attendanceLogRepository.save(overtimeLog);
                }
            }
        }
        
        System.out.println("✅ [AttendanceSeeder] Created staff attendance logs for " + staffUsers.size() + " staff members.");
    }
    
    private String getDepartmentForRole(String roleName) {
        switch (roleName) {
            case "ADMIN": return "ADMINISTRATION";
            case "TEACHER": return "EDUCATION";
            case "STUDENT": return "EDUCATION";
            case "STAFF": return "ADMINISTRATION";
            default: return "GENERAL";
        }
    }

    private void seedAttendanceForClassroom(Classroom classroom) {
        List<Lecture> lectures = lectureRepository.findByClassroomId(classroom.getId());
        List<User> students = classroomEnrollmentRepository.findByClassroomId(classroom.getId())
                .stream()
                .map(enrollment -> enrollment.getUser())
                .collect(Collectors.toList());

        if (lectures.isEmpty()) {
            System.out.println("⚠️ [AttendanceSeeder] No lectures for classroom: " + classroom.getName() + ", skipping.");
            return;
        }

        // Seed attendance for lectures that have a date in the past or today
        for (Lecture lecture : lectures) {
            // Check if lecture has a date and if it's in the past or today
            if (lecture.getLectureDate() != null && !lecture.getLectureDate().isAfter(LocalDate.now())) {
                
                // Find existing or create a new session
                AttendanceSession session = attendanceSessionRepository.findByLectureId(lecture.getId())
                    .orElse(new AttendanceSession());

                // Always ensure there is a clock-in time for past lectures
                if (session.getTeacherClockInTime() == null) {
                    session.setTeacherClockInTime(lecture.getLectureDate().atTime(8, 30)); // Set a fixed time for consistency
                }
                
                // If it's a new session, set its properties
                if (session.getId() == null) {
                    session.setLecture(lecture);
                    session.setSessionDate(lecture.getLectureDate());
                    session.setClassroom(classroom);
                    attendanceSessionRepository.save(session);

                    // Create attendance records for each student only for the new session
                    if (!students.isEmpty()) {
                        for (int j = 0; j < students.size(); j++) {
                            User student = students.get(j);
                            Attendance attendance = new Attendance();
                            attendance.setSession(session);
                            attendance.setStudent(student);
                            
                            // Alternate status for variety
                            attendance.setStatus(j % 3 == 0 ? AttendanceStatus.ABSENT : (j % 3 == 1 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT));
                            attendanceRepository.save(attendance);
                        }
                    }
                } else {
                    // If the session already exists, just save the updated clock-in time
                    attendanceSessionRepository.save(session);
                }
            }
        }
        System.out.println("✅ [AttendanceSeeder] Seeded/updated attendance for classroom: " + classroom.getName());
    }

    private String getRoleName(Integer roleId) {
        if (roleId == null) return "UNKNOWN";
        switch (roleId) {
            case 1: return "STUDENT";
            case 2: return "TEACHER";
            case 3: return "MANAGER";
            case 4: return "ADMIN";
            default: return "UNKNOWN";
        }
    }
}