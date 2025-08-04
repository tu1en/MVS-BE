// package com.classroomapp.classroombackend.config;

// import java.time.DayOfWeek;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Random;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.format.annotation.DateTimeFormat;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.classroomapp.classroombackend.model.usermanagement.User;
// import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
// import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
// import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
// import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
// import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
// import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;

// @RestController
// public class AttendanceDataLoader {

//     @Autowired
//     private AttendanceRepository attendanceRepository;

//     @Autowired
//     private AttendanceSessionRepository sessionRepository;

//     @Autowired
//     private AttendanceLogRepository attendanceLogRepository;

//     @Autowired
//     private UserRepository userRepository;

//     private Random random = new Random();

//     @GetMapping("/test/attendance/generate-staff")
//     public String generateFakeStaffAttendance(
//             @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
//             @RequestParam(name = "role", defaultValue = "TEACHER") String role) {
//         List<User> staff = userRepository.findByRole(role);
//         List<AttendanceSession> sessions = sessionRepository.findAll();
//         for (User s : staff) {
//             for (AttendanceSession session : sessions) {
//                 Attendance attendance = new Attendance();
//                 attendance.setUser(s);
//                 attendance.setSession(session);
//                 attendance.setDate(date);
//                 attendance.setCheckInTime(randomTime(session.getStartTime().minusMinutes(5), session.getStartTime().plusMinutes(10)));
//                 attendance.setCheckOutTime(randomTime(session.getEndTime().minusMinutes(10), session.getEndTime().plusMinutes(5)));
//                 attendanceRepository.save(attendance);
//             }
//         }
//         return "Done!";
//     }

//     private LocalDateTime randomTime(LocalTime start, LocalTime end) {
//         int startSecond = start.toSecondOfDay();
//         int endSecond = end.toSecondOfDay();
//         int randomSecond = startSecond + random.nextInt(endSecond - startSecond + 1);
//         return LocalDateTime.of(LocalDate.now(), LocalTime.ofSecondOfDay(randomSecond));
//     }

//     @DeleteMapping("/test/attendance/clear")
//     public String clearFakeStaffAttendance() {
//         attendanceRepository.deleteAll();
//         return "Deleted all fake attendance.";
//     }

//     @GetMapping("/test/attendance/sessions-by-day")
//     public Map<DayOfWeek, List<AttendanceSession>> getSessionsByDay() {
//         List<AttendanceSession> sessions = sessionRepository.findAll();
//         return sessions.stream().collect(Collectors.groupingBy(AttendanceSession::getDayOfWeek));
//     }
// }
