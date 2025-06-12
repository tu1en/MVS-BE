package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Assignment;
import com.classroomapp.classroombackend.model.Attendance;
import com.classroomapp.classroombackend.model.AttendanceSession;
import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.StudentMessage;
import com.classroomapp.classroombackend.model.Submission;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.AssignmentRepository;
import com.classroomapp.classroombackend.repository.AttendanceRepository;
import com.classroomapp.classroombackend.repository.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.ClassroomRepository;
import com.classroomapp.classroombackend.repository.StudentMessageRepository;
import com.classroomapp.classroombackend.repository.SubmissionRepository;
import com.classroomapp.classroombackend.repository.UserRepository;

@Component
public class SampleDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private ClassroomEnrollmentRepository enrollmentRepository;
    
    @Autowired
    private StudentMessageRepository messageRepository;
    
    // Add attendance repositories
    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize data if database is empty
        if (classroomRepository.count() == 0) {
            initializeSampleData();
        }
    }

    private void initializeSampleData() {
        System.out.println("🔄 Initializing sample classroom data...");
        
        // Get existing users
        List<User> teachers = userRepository.findByRoleId(2); // Teachers
        List<User> students = userRepository.findByRoleId(1); // Students
        
        if (teachers.isEmpty() || students.isEmpty()) {
            System.out.println("⚠️  No teachers or students found. Creating additional users...");
            createAdditionalUsers();
            teachers = userRepository.findByRoleId(2);
            students = userRepository.findByRoleId(1);
        }
        
        // Create classrooms
        createClassrooms(teachers, students);
        
        // Create assignments
        createAssignments();
        
        // Create submissions
        createSubmissions(students);
        
        // Create student messages
        createStudentMessages(students, teachers);
        
        // Create attendance sessions
        createAttendanceSessions(teachers, students);
        
        System.out.println("✅ Sample data initialization completed!");
    }
    
    private void createAdditionalUsers() {
        // Check and create teacher user if it doesn't exist
        if (!userRepository.findByUsername("teacher").isPresent()) {
            User teacher1 = new User();
            teacher1.setUsername("teacher");
            teacher1.setPassword(passwordEncoder.encode("teacher123"));
            teacher1.setEmail("teacher@classroom.com");
            teacher1.setFullName("Giáo viên Chính");
            teacher1.setRoleId(2);
            teacher1.setDepartment("Khoa Toán");
            teacher1.setStatus("active");
            teacher1.setCreatedAt(LocalDateTime.now());
            teacher1.setHireDate(LocalDate.of(2020, 9, 1));
            userRepository.save(teacher1);
            System.out.println("Created teacher user: teacher");
        }
        
        // Create additional teacher
        User teacher2 = new User();
        teacher2.setUsername("teacher2");
        teacher2.setPassword(passwordEncoder.encode("teacher123"));
        teacher2.setEmail("teacher2@classroom.com");
        teacher2.setFullName("Cô Nguyễn Thị Mai");
        teacher2.setRoleId(2);
        teacher2.setDepartment("Khoa Toán");
        teacher2.setStatus("active");
        teacher2.setCreatedAt(LocalDateTime.now());
        teacher2.setHireDate(LocalDate.of(2020, 9, 1));
        userRepository.save(teacher2);
        
        // Check and create student user if it doesn't exist
        if (!userRepository.findByUsername("student").isPresent()) {
            User student1 = new User();
            student1.setUsername("student");
            student1.setPassword(passwordEncoder.encode("student123"));
            student1.setEmail("student@classroom.com");
            student1.setFullName("Học sinh A");
            student1.setRoleId(1);
            student1.setStatus("active");
            student1.setCreatedAt(LocalDateTime.now());
            student1.setEnrollmentDate(LocalDate.of(2024, 9, 1));
            userRepository.save(student1);
            System.out.println("Created student user: student");
        }
        
        // Create additional students
        User student2 = new User();
        student2.setUsername("student2");
        student2.setPassword(passwordEncoder.encode("student123"));
        student2.setEmail("student2@classroom.com");
        student2.setFullName("Trần Thị Bình");
        student2.setRoleId(1);
        student2.setStatus("active");
        student2.setCreatedAt(LocalDateTime.now());
        student2.setEnrollmentDate(LocalDate.of(2024, 9, 1));
        userRepository.save(student2);
        
        User student3 = new User();
        student3.setUsername("student3");
        student3.setPassword(passwordEncoder.encode("student123"));
        student3.setEmail("student3@classroom.com");
        student3.setFullName("Lê Văn Cường");
        student3.setRoleId(1);
        student3.setStatus("active");
        student3.setCreatedAt(LocalDateTime.now());
        student3.setEnrollmentDate(LocalDate.of(2024, 9, 1));
        userRepository.save(student3);
    }
    
    private void createClassrooms(List<User> teachers, List<User> students) {
        if (teachers.isEmpty()) return;
        
        // Classroom 1: Math
        Classroom mathClass = new Classroom();
        mathClass.setName("Lớp Học Trực Tuyến");
        mathClass.setSubject("Toán");
        mathClass.setSection("Lớp 12A1");
        mathClass.setDescription("Lớp toán nâng cao dành cho học sinh giỏi");
        mathClass.setTeacher(teachers.get(0));
        mathClass = classroomRepository.save(mathClass);
        
        // Classroom 2: Vietnamese Literature
        Classroom literatureClass = new Classroom();
        literatureClass.setName("Văn Học Việt Nam");
        literatureClass.setSubject("Văn");
        literatureClass.setSection("Lớp 12B2");
        literatureClass.setDescription("Học văn học Việt Nam hiện đại");
        literatureClass.setTeacher(teachers.size() > 1 ? teachers.get(1) : teachers.get(0));
        literatureClass = classroomRepository.save(literatureClass);
        
        // Classroom 3: English
        Classroom englishClass = new Classroom();
        englishClass.setName("English Communication");
        englishClass.setSubject("Anh");
        englishClass.setSection("Lớp 12C3");
        englishClass.setDescription("Giao tiếp tiếng Anh nâng cao");
        englishClass.setTeacher(teachers.get(0));
        englishClass = classroomRepository.save(englishClass);
        
        // Enroll students in classrooms
        enrollStudentsInClassrooms(List.of(mathClass, literatureClass, englishClass), students);
    }
    
    private void enrollStudentsInClassrooms(List<Classroom> classrooms, List<User> students) {
        for (Classroom classroom : classrooms) {
            for (User student : students) {
                // Check if enrollment already exists
                boolean exists = enrollmentRepository.findAll().stream()
                    .anyMatch(e -> e.getClassroom().getId().equals(classroom.getId()) && 
                                  e.getUser().getId().equals(student.getId()));
                
                if (!exists) {
                    ClassroomEnrollment enrollment = new ClassroomEnrollment();
                    enrollment.setClassroom(classroom);
                    enrollment.setUser(student);
                    enrollmentRepository.save(enrollment);
                }
            }
        }
    }
    
    private void createAssignments() {
        List<Classroom> classrooms = classroomRepository.findAll();
        
        for (Classroom classroom : classrooms) {
            // Assignment 1: Homework
            Assignment homework = new Assignment();
            homework.setTitle("Bài 1: Hàm số bậc nhất");
            homework.setDescription("Làm các bài tập từ 1 đến 10 trong sách giáo khoa trang 45-47. Nộp bài trước 23:59 ngày mai.");
            homework.setClassroom(classroom);
            homework.setDueDate(LocalDateTime.now().plusDays(2));
            homework.setPoints(100);
            assignmentRepository.save(homework);
            
            // Assignment 2: Quiz
            Assignment quiz = new Assignment();
            quiz.setTitle("Unit 5 Homework");
            quiz.setDescription("Ôn tập bài học unit 5 và làm bài kiểm tra trắc nghiệm online. Thời gian làm bài: 30 phút.");
            quiz.setClassroom(classroom);
            quiz.setDueDate(LocalDateTime.now().plusDays(5));
            quiz.setPoints(50);
            assignmentRepository.save(quiz);
            
            // Assignment 3: Essay
            Assignment essay = new Assignment();
            essay.setTitle("Điện học");
            essay.setDescription("Viết bài luận về tầm quan trọng của giáo dục trong xã hội hiện đại. Độ dài: 500-800 từ.");
            essay.setClassroom(classroom);
            essay.setDueDate(LocalDateTime.now().plusDays(7));
            essay.setPoints(200);
            assignmentRepository.save(essay);
            
            // Assignment 4: Lab Report  
            Assignment lab = new Assignment();
            lab.setTitle("Bài tập axit bazo");
            lab.setDescription("Thực hiện thí nghiệm và viết báo cáo về phản ứng acid-base. Bao gồm: mục đích, dụng cụ, tiến hành, kết quả và kết luận.");
            lab.setClassroom(classroom);
            lab.setDueDate(LocalDateTime.now().plusDays(10));
            lab.setPoints(150);
            assignmentRepository.save(lab);
        }
    }
    
    private void createSubmissions(List<User> students) {
        List<Assignment> assignments = assignmentRepository.findAll();
        
        for (Assignment assignment : assignments) {
            for (User student : students) {
                // Only create submissions for some assignments (to simulate real scenario)
                if (Math.random() > 0.3) { // 70% chance of submission
                    Submission submission = new Submission();
                    submission.setAssignment(assignment);
                    submission.setStudent(student);
                    submission.setSubmittedAt(LocalDateTime.now().minusHours((long)(Math.random() * 48)));
                    submission.setComment("Bài làm của em ạ. Em đã cố gắng làm hết sức có thể.");
                    
                    // Random score for graded submissions
                    if (Math.random() > 0.5) { // 50% chance of being graded
                        submission.setScore((int)(Math.random() * assignment.getPoints()));
                        submission.setGradedAt(LocalDateTime.now().minusHours((long)(Math.random() * 24)));
                        submission.setFeedback("Bài làm tốt! Cần chú ý thêm về phần lý thuyết.");
                    }
                    
                    submissionRepository.save(submission);
                }
            }
        }
    }
    
    private void createStudentMessages(List<User> students, List<User> teachers) {
        if (students.isEmpty() || teachers.isEmpty()) return;
        
        // Sample messages from students to teachers
        StudentMessage message1 = new StudentMessage();
        message1.setSender(students.get(0));
        message1.setRecipient(teachers.get(0));
        message1.setSubject("Hỏi về bài tập toán");
        message1.setContent("Thưa thầy, em không hiểu bài số 5 trang 47. Thầy có thể giải thích thêm không ạ?");
        message1.setCreatedAt(LocalDateTime.now().minusHours(2));
        message1.setIsRead(false);
        message1.setStatus("PENDING");
        message1.setMessageType("QUESTION");
        message1.setPriority("MEDIUM");
        messageRepository.save(message1);
        
        StudentMessage message2 = new StudentMessage();
        message2.setSender(teachers.get(0));
        message2.setRecipient(students.get(0));
        message2.setSubject("Re: Hỏi về bài tập toán");
        message2.setContent("Chào em! Bài số 5 cần em áp dụng công thức đạo hàm. Em có thể xem lại phần lý thuyết ở trang 42-43 nhé.");
        message2.setCreatedAt(LocalDateTime.now().minusHours(1));
        message2.setIsRead(false);
        message2.setStatus("SENT");
        message2.setMessageType("RESPONSE");
        message2.setPriority("MEDIUM");
        message2.setRepliedAt(LocalDateTime.now().minusHours(1));
        message2.setRepliedBy(teachers.get(0));
        messageRepository.save(message2);
        
        // Add more sample messages for variety
        if (students.size() > 1) {
            StudentMessage message3 = new StudentMessage();
            message3.setSender(students.get(1));
            message3.setRecipient(teachers.get(0));
            message3.setSubject("Xin phép nghỉ học");
            message3.setContent("Thưa thầy, ngày mai em có việc gia đình đột xuất nên không thể đến lớp được. Em xin phép ạ!");
            message3.setCreatedAt(LocalDateTime.now().minusHours(5));
            message3.setIsRead(true);
            message3.setReadAt(LocalDateTime.now().minusHours(4));
            message3.setStatus("APPROVED");
            message3.setMessageType("REQUEST");
            message3.setPriority("HIGH");
            messageRepository.save(message3);
        }
    }
    
    private void createAttendanceSessions(List<User> teachers, List<User> students) {
        List<Classroom> classrooms = classroomRepository.findAll();
        
        for (Classroom classroom : classrooms) {
            // Create a past attendance session (COMPLETED)
            AttendanceSession pastSession = new AttendanceSession();
            pastSession.setClassroom(classroom);
            pastSession.setTeacher(classroom.getTeacher());
            pastSession.setSessionName("Buổi học ngày " + LocalDate.now().minusDays(2).toString());
            pastSession.setSessionDate(LocalDateTime.now().minusDays(2).withHour(0).withMinute(0)); // Set session date
            pastSession.setStartTime(LocalDateTime.now().minusDays(2).withHour(8).withMinute(0));
            pastSession.setEndTime(LocalDateTime.now().minusDays(2).withHour(10).withMinute(0));
            pastSession.setStatus(AttendanceSession.SessionStatus.COMPLETED);
            pastSession.setCreatedAt(LocalDateTime.now().minusDays(2));
            pastSession.setLocationRequired(false);
            attendanceSessionRepository.save(pastSession);
            
            // Create attendance records for past session
            for (User student : students) {
                // Check if student is enrolled in this classroom
                boolean isEnrolled = enrollmentRepository.findAll().stream()
                    .anyMatch(e -> e.getClassroom().getId().equals(classroom.getId()) && 
                                  e.getUser().getId().equals(student.getId()));
                
                if (isEnrolled) {
                    Attendance attendance = new Attendance();
                    attendance.setSession(pastSession);
                    attendance.setStudent(student);
                    attendance.setStatus(Math.random() > 0.1 ? Attendance.AttendanceStatus.PRESENT : Attendance.AttendanceStatus.ABSENT);
                    attendance.setCheckInTime(pastSession.getStartTime().plusMinutes((long)(Math.random() * 30)));
                    attendanceRepository.save(attendance);
                }
            }
            
            // Create an ACTIVE attendance session for today
            AttendanceSession activeSession = new AttendanceSession();
            activeSession.setClassroom(classroom);
            activeSession.setTeacher(classroom.getTeacher());
            activeSession.setSessionName("Buổi học hôm nay - " + classroom.getName());
            activeSession.setSessionDate(LocalDateTime.now().withHour(0).withMinute(0)); // Set session date
            activeSession.setStartTime(LocalDateTime.now().withHour(9).withMinute(0));
            activeSession.setEndTime(LocalDateTime.now().withHour(11).withMinute(0));
            activeSession.setStatus(AttendanceSession.SessionStatus.ACTIVE);
            activeSession.setCreatedAt(LocalDateTime.now());
            activeSession.setLocationRequired(false);
            activeSession.setLocationLatitude(21.0285); // Sample location - Hanoi
            activeSession.setLocationLongitude(105.8542);
            activeSession.setLocationRadiusMeters(100); // 100 meters
            attendanceSessionRepository.save(activeSession);
            
            // Create a SCHEDULED session for tomorrow
            AttendanceSession scheduledSession = new AttendanceSession();
            scheduledSession.setClassroom(classroom);
            scheduledSession.setTeacher(classroom.getTeacher());
            scheduledSession.setSessionName("Buổi học ngày mai - " + classroom.getName());
            scheduledSession.setSessionDate(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0)); // Set session date
            scheduledSession.setStartTime(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
            scheduledSession.setEndTime(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0));
            scheduledSession.setStatus(AttendanceSession.SessionStatus.SCHEDULED);
            scheduledSession.setCreatedAt(LocalDateTime.now());
            scheduledSession.setLocationRequired(true);
            scheduledSession.setLocationLatitude(21.0285);
            scheduledSession.setLocationLongitude(105.8542);
            scheduledSession.setLocationRadiusMeters(50); // Stricter radius for scheduled session
            attendanceSessionRepository.save(scheduledSession);
        }
    }
}
