package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.repository.AttendanceExplanationRepository;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;

@Component
public class AttendanceDataLoader implements CommandLineRunner {

    @Autowired
    private AttendanceExplanationRepository explanationRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private LectureRepository lectureRepository;

        @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Backend starting - loading sample data...");
        
        // Skip attendance data loading if you want to be extra safe
        // Just uncomment the next line to disable attendance data loading:
        // System.out.println("Sample attendance data loading is disabled for safety.");
        
        try {
            loadSampleAttendanceData();
        } catch (Exception e) {
            System.err.println("Error loading sample attendance data: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            loadExplanationData();
        } catch (Exception e) {
            System.err.println("Error loading explanation data: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            loadAttendanceLogData();
        } catch (Exception e) {
            System.err.println("Error loading attendance log data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadExplanationData() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found. Skipping explanation data loading.");
            return;
        }

        Random random = new Random();
        String[] reasons = {
            "Đi muộn do kẹt xe", "Vắng mặt do ốm", "Quên chấm công",
            "Sự cố gia đình", "Họp khẩn cấp", "Đi công tác",
            "Khám bệnh định kỳ", "Tham gia đào tạo", "Sự cố giao thông",
            "Lỗi hệ thống chấm công"
        };
        ExplanationStatus[] statuses = {
            ExplanationStatus.PENDING, ExplanationStatus.APPROVED, ExplanationStatus.REJECTED
        };

        System.out.println("Loading 10 sample explanation reports...");

        int count = 0;
        int attempts = 0;
        while (count < 10 && attempts < 50) {
            User user = users.get(random.nextInt(users.size()));
            attempts++;

            if (!isStaff(user)) continue;

            AttendanceExplanation explanation = new AttendanceExplanation();
            explanation.setSubmitterName(user.getFullName());
            explanation.setDepartment(user.getDepartment() != null ? user.getDepartment() : "Phòng " + getRoleString(user.getRoleId()));
            explanation.setReason(reasons[count]);
            explanation.setExplanationText("Giải trình chi tiết: " + reasons[count] + ". Tôi xin lỗi vì sự bất tiện này và cam kết sẽ cải thiện trong tương lai.");
            explanation.setAbsenceDate(LocalDate.now().minusDays(random.nextInt(30)));
            explanation.setStatus(statuses[random.nextInt(statuses.length)]);
            explanation.setSubmittedAt(LocalDateTime.now().minusDays(random.nextInt(7)));

            // Thêm ID mặc định cho violation_id để tránh NULL violations
            explanation.setViolationId((long) (count + 1));

            User attachedUser = userRepository.getReferenceById(user.getId());
            explanation.setStaff(attachedUser);

            if (!explanation.getStatus().equals(ExplanationStatus.PENDING)) {
                explanation.setApproverName("Manager " + (count % 3 + 1));
            }

            explanationRepository.save(explanation);
            count++;
        }

        System.out.println("Successfully loaded 10 explanation reports.");
    }



    
    private void loadAttendanceLogData() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found. Skipping attendance log data loading.");
            return;
        }

        Random random = new Random();
        String[] statuses = {"PRESENT", "ABSENT", "LATE"};
        String[] shifts = {"MORNING", "AFTERNOON", "EVENING"};

        System.out.println("Loading 20 sample attendance logs...");

        for (int i = 0; i < 20; i++) {
            User user = users.get(random.nextInt(users.size()));

            AttendanceLog log = new AttendanceLog();
            log.setUserId(user.getId());
            log.setUserName(user.getFullName());
            log.setRole(getRoleString(user.getRoleId()));
            log.setDepartment(user.getDepartment() != null ? user.getDepartment() : "Phòng " + getRoleString(user.getRoleId()));
            log.setDate(LocalDate.now().minusDays(random.nextInt(7)));
            log.setShift(shifts[random.nextInt(shifts.length)]);
            log.setStatus(statuses[random.nextInt(statuses.length)]);

            LocalTime baseCheckIn = getShiftStartTime(log.getShift());
            LocalTime baseCheckOut = getShiftEndTime(log.getShift());

            if (log.getStatus().equals("PRESENT")) {
                log.setCheckIn(baseCheckIn.plusMinutes(random.nextInt(30) - 15));
                log.setCheckOut(baseCheckOut.plusMinutes(random.nextInt(60) - 30));
            } else if (log.getStatus().equals("LATE")) {
                log.setCheckIn(baseCheckIn.plusMinutes(15 + random.nextInt(45)));
                log.setCheckOut(baseCheckOut.plusMinutes(random.nextInt(60) - 30));
            }

            attendanceLogRepository.save(log);
        }

        System.out.println("Successfully loaded 20 attendance logs.");
    }

    private String getRoleString(Integer roleId) {
        if (roleId == null) return "Unknown";
        return switch (roleId) {
            case 1 -> "Student";
            case 2 -> "Teacher";
            case 3 -> "Manager";
            case 4 -> "Admin";
            case 5 -> "Accountant";
            default -> "Unknown";
        };
    }

    private boolean isStaff(User user) {
        Integer roleId = user.getRoleId();
        return roleId != null && roleId != 1;
    }

    private LocalTime getShiftStartTime(String shift) {
        return switch (shift) {
            case "MORNING" -> LocalTime.of(8, 0);
            case "AFTERNOON" -> LocalTime.of(13, 0);
            case "EVENING" -> LocalTime.of(18, 0);
            default -> LocalTime.of(8, 0);
        };
    }

    private LocalTime getShiftEndTime(String shift) {
        return switch (shift) {
            case "MORNING" -> LocalTime.of(12, 0);
            case "AFTERNOON" -> LocalTime.of(17, 0);
            case "EVENING" -> LocalTime.of(22, 0);
            default -> LocalTime.of(17, 0);
        };
    }
    
    private void loadSampleAttendanceData() {
        List<Classroom> classrooms;
        List<Lecture> lectures;
        List<User> students;
        
        try {
            // Check if we already have attendance data
            if (attendanceSessionRepository.count() > 0) {
                System.out.println("Attendance data already exists, skipping sample data loading.");
                return;
            }
            
            classrooms = classroomRepository.findAll();
            lectures = lectureRepository.findAll();
            students = userRepository.findByRoleId(1); // Students have roleId = 1
            
            if (classrooms.isEmpty()) {
                System.out.println("No classrooms found. Skipping attendance data loading.");
                return;
            }
            
            if (lectures.isEmpty()) {
                System.out.println("No lectures found. Skipping attendance data loading.");
                return;
            }
            
            if (students.isEmpty()) {
                System.out.println("No students found. Skipping attendance data loading.");
                return;
            }
            
            System.out.println("Found " + classrooms.size() + " classrooms, " + lectures.size() + " lectures, and " + students.size() + " students.");
        } catch (Exception e) {
            System.err.println("Error checking existing data: " + e.getMessage());
            return;
        }

        try {
            Random random = new Random();
            System.out.println("Creating sample attendance sessions and records...");
            
            // Create 2-3 sample attendance sessions (reduced to avoid overwhelming the system)
            int sessionCount = Math.min(2 + random.nextInt(2), Math.min(classrooms.size(), lectures.size()));
            
            for (int i = 0; i < sessionCount; i++) {
                try {
                    Classroom classroom = classrooms.get(i % classrooms.size());
                    Lecture lecture = lectures.get(i % lectures.size());
                    
                    // Validate classroom and lecture
                    if (classroom == null || lecture == null) {
                        System.out.println("Skipping session " + (i + 1) + " due to null classroom or lecture");
                        continue;
                    }
                    
                    // Create attendance session
                    AttendanceSession session = new AttendanceSession();
                    session.setClassroom(classroom);
                    session.setLecture(lecture);
                    
                    LocalDateTime baseTime = LocalDateTime.now().minusDays(random.nextInt(7));
                    session.setCreatedAt(baseTime);
                    session.setExpiresAt(baseTime.plusHours(2));
                    session.setSessionDate(baseTime.toLocalDate());
                    session.setIsOpen(random.nextBoolean()); // Random open/closed status
                    session.setTeacherClockInTime(baseTime.plusMinutes(random.nextInt(30)));
                    
                    session = attendanceSessionRepository.save(session);
                    System.out.println("Created attendance session " + (i + 1) + " for classroom: " + classroom.getName());
            
                    // Create attendance records for some students
                    int studentCount = Math.min(students.size(), 5 + random.nextInt(6)); // 5-10 students (reduced)
                    List<User> sessionStudents = students.subList(0, Math.min(studentCount, students.size()));
                    
                    int recordsCreated = 0;
                    for (User student : sessionStudents) {
                        try {
                            // 80% chance of having attendance record (some students might not have attended)
                            if (random.nextDouble() < 0.8 && student != null) {
                                Attendance attendance = new Attendance();
                                attendance.setSession(session);
                                attendance.setStudent(student);
                                
                                // Random attendance status: 70% present, 20% absent, 10% late
                                double statusRandom = random.nextDouble();
                                if (statusRandom < 0.7) {
                                    attendance.setStatus(AttendanceStatus.PRESENT);
                                } else if (statusRandom < 0.9) {
                                    attendance.setStatus(AttendanceStatus.ABSENT);
                                } else {
                                    attendance.setStatus(AttendanceStatus.LATE);
                                }
                                
                                attendanceRepository.save(attendance);
                                recordsCreated++;
                            }
                        } catch (Exception e) {
                            System.err.println("Error creating attendance record for student " + student.getId() + ": " + e.getMessage());
                        }
                    }
                    
                    System.out.println("Created " + recordsCreated + " attendance records for session in classroom: " + classroom.getName());
                    
                } catch (Exception e) {
                    System.err.println("Error creating attendance session " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            System.out.println("Successfully created " + sessionCount + " sample attendance sessions with records.");
            
        } catch (Exception e) {
            System.err.println("Error in loadSampleAttendanceData: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
