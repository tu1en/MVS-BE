package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

/**
 * Data seeder để tạo dữ liệu mẫu cho classroom "Tôn"
 * Bao gồm:
 * - 5 bài tập cần chấm điểm (có submission nhưng chưa có grade)
 * - 5 bài tập sắp hết hạn (due_date trong 1-3 ngày tới)
 * - 5 bài tập đã hết hạn (due_date đã qua 1-7 ngày)
 * - 5 học sinh mới với enrollment vào classroom "Tôn"
 */
@Component
public class TonClassroomDataSeeder {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ClassroomEnrollmentRepository enrollmentRepository;

    @Transactional
    public void seedTonClassroomData() {
        System.out.println("🔄 [TonClassroomDataSeeder] Bắt đầu tạo dữ liệu mẫu cho classroom Tôn...");

        // Tìm classroom "Tôn" (ID = 1 - "Toán cao cấp A1")
        Optional<Classroom> tonClassroomOpt = classroomRepository.findById(1L);
        if (!tonClassroomOpt.isPresent()) {
            System.out.println("❌ [TonClassroomDataSeeder] Không tìm thấy classroom Tôn (ID=1)");
            return;
        }

        Classroom tonClassroom = tonClassroomOpt.get();
        System.out.println("✅ [TonClassroomDataSeeder] Tìm thấy classroom: " + tonClassroom.getName());

        // Tạo 5 học sinh mới
        List<User> newStudents = createNewStudents();
        System.out.println("✅ [TonClassroomDataSeeder] Đã tạo " + newStudents.size() + " học sinh mới");

        // Đăng ký học sinh vào classroom
        enrollStudentsToClassroom(tonClassroom, newStudents);
        System.out.println("✅ [TonClassroomDataSeeder] Đã đăng ký học sinh vào classroom");

        // Lấy danh sách tất cả học sinh trong classroom (bao gồm cả học sinh cũ)
        List<User> allStudents = getAllStudentsInClassroom(tonClassroom);
        System.out.println("✅ [TonClassroomDataSeeder] Tổng số học sinh trong classroom: " + allStudents.size());

        // Tạo 5 bài tập cần chấm điểm
        List<Assignment> needGradingAssignments = createNeedGradingAssignments(tonClassroom);
        createSubmissionsForAssignments(needGradingAssignments, allStudents, tonClassroom, false); // false = chưa chấm điểm
        System.out.println("✅ [TonClassroomDataSeeder] Đã tạo " + needGradingAssignments.size() + " bài tập cần chấm điểm");

        // Tạo 5 bài tập sắp hết hạn
        List<Assignment> upcomingAssignments = createUpcomingAssignments(tonClassroom);
        System.out.println("✅ [TonClassroomDataSeeder] Đã tạo " + upcomingAssignments.size() + " bài tập sắp hết hạn");

        // Tạo 5 bài tập đã hết hạn
        List<Assignment> overdueAssignments = createOverdueAssignments(tonClassroom);
        createSubmissionsForAssignments(overdueAssignments, allStudents, tonClassroom, true); // true = có thể có hoặc không có submission
        System.out.println("✅ [TonClassroomDataSeeder] Đã tạo " + overdueAssignments.size() + " bài tập đã hết hạn");

        System.out.println("🎉 [TonClassroomDataSeeder] Hoàn thành tạo dữ liệu mẫu cho classroom Tôn!");
    }

    /**
     * Tạo 5 học sinh mới
     */
    private List<User> createNewStudents() {
        List<User> students = new ArrayList<>();
        String[] studentNames = {
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Hoàng Cường", 
            "Phạm Thị Dung", "Hoàng Văn Em"
        };
        String[] studentEmails = {
            "nguyenvanan.ton@student.edu.vn", "tranthibinh.ton@student.edu.vn", 
            "lehoangcuong.ton@student.edu.vn", "phamthidung.ton@student.edu.vn", 
            "hoangvanem.ton@student.edu.vn"
        };

        for (int i = 0; i < 5; i++) {
            // Kiểm tra xem email đã tồn tại chưa
            if (userRepository.findByEmail(studentEmails[i]).isPresent()) {
                System.out.println("⚠️ [TonClassroomDataSeeder] Email đã tồn tại: " + studentEmails[i]);
                continue;
            }

            User student = new User();
            student.setUsername("student_ton_" + (i + 1));
            student.setEmail(studentEmails[i]);
            student.setFullName(studentNames[i]);
            student.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6"); // password: 123456
            student.setRoleId(1); // STUDENT role
            student.setStatus("active");
            student.setCreatedAt(LocalDateTime.now());
            student.setUpdatedAt(LocalDateTime.now());

            User savedStudent = userRepository.save(student);
            students.add(savedStudent);
            System.out.println("✅ [TonClassroomDataSeeder] Tạo học sinh: " + savedStudent.getFullName() + " (ID=" + savedStudent.getId() + ")");
        }

        return students;
    }

    /**
     * Đăng ký học sinh vào classroom
     */
    private void enrollStudentsToClassroom(Classroom classroom, List<User> students) {
        for (User student : students) {
            // Kiểm tra xem đã đăng ký chưa
            Optional<ClassroomEnrollment> existingEnrollment = 
                enrollmentRepository.findByClassroomIdAndUserId(classroom.getId(), student.getId());
            
            if (existingEnrollment.isPresent()) {
                System.out.println("⚠️ [TonClassroomDataSeeder] Học sinh đã đăng ký: " + student.getFullName());
                continue;
            }

            ClassroomEnrollment enrollment = new ClassroomEnrollment();
            ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroom.getId(), student.getId());
            enrollment.setId(enrollmentId);
            enrollment.setClassroom(classroom);
            enrollment.setUser(student);

            enrollmentRepository.save(enrollment);
            System.out.println("✅ [TonClassroomDataSeeder] Đăng ký học sinh: " + student.getFullName() + " vào classroom: " + classroom.getName());
        }
    }

    /**
     * Lấy tất cả học sinh trong classroom
     */
    private List<User> getAllStudentsInClassroom(Classroom classroom) {
        List<ClassroomEnrollment> enrollments = enrollmentRepository.findByClassroomId(classroom.getId());
        List<User> students = new ArrayList<>();
        
        for (ClassroomEnrollment enrollment : enrollments) {
            User user = enrollment.getUser();
            if (user.getRoleId() == 1) { // STUDENT role
                students.add(user);
            }
        }
        
        return students;
    }

    /**
     * Tạo 5 bài tập cần chấm điểm (đã có submission nhưng chưa chấm)
     */
    private List<Assignment> createNeedGradingAssignments(Classroom classroom) {
        List<Assignment> assignments = new ArrayList<>();
        String[] titles = {
            "Bài tập Đạo hàm và Tích phân - Cần chấm điểm",
            "Thực hành Giải phương trình vi phân - Cần chấm điểm", 
            "Bài tập Ma trận và Định thức nâng cao - Cần chấm điểm",
            "Ứng dụng Toán học trong Kinh tế - Cần chấm điểm",
            "Bài tập tổng hợp Giải tích - Cần chấm điểm"
        };

        LocalDateTime baseDueDate = LocalDateTime.now().plusDays(10); // Hạn nộp sau 10 ngày

        for (int i = 0; i < 5; i++) {
            Assignment assignment = new Assignment();
            assignment.setTitle(titles[i]);
            assignment.setDescription("Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. " +
                "Cần giáo viên xem xét và đánh giá.");
            assignment.setDueDate(baseDueDate.plusDays(i)); // Mỗi bài cách nhau 1 ngày
            assignment.setPoints(100);
            assignment.setClassroom(classroom);

            Assignment savedAssignment = assignmentRepository.save(assignment);
            assignments.add(savedAssignment);
            System.out.println("✅ [TonClassroomDataSeeder] Tạo assignment cần chấm: " + savedAssignment.getTitle() + " (ID=" + savedAssignment.getId() + ")");
        }

        return assignments;
    }

    /**
     * Tạo 5 bài tập sắp hết hạn (due_date trong 1-3 ngày tới)
     */
    private List<Assignment> createUpcomingAssignments(Classroom classroom) {
        List<Assignment> assignments = new ArrayList<>();
        String[] titles = {
            "Bài kiểm tra Giới hạn và Liên tục - Sắp hết hạn",
            "Thực hành Tính tích phân bằng phương pháp thế - Sắp hết hạn",
            "Bài tập Chuỗi số và Chuỗi hàm - Sắp hết hạn",
            "Ứng dụng Đạo hàm trong Hình học - Sắp hết hạn",
            "Bài tập Phương trình tham số - Sắp hết hạn"
        };

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            Assignment assignment = new Assignment();
            assignment.setTitle(titles[i]);
            assignment.setDescription("Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong " +
                (i + 1) + " ngày tới để không bị trễ hạn.");
            assignment.setDueDate(now.plusDays(i + 1)); // Hết hạn trong 1-5 ngày tới
            assignment.setPoints(80 + (i * 5)); // Điểm từ 80-100
            assignment.setClassroom(classroom);

            Assignment savedAssignment = assignmentRepository.save(assignment);
            assignments.add(savedAssignment);
            System.out.println("✅ [TonClassroomDataSeeder] Tạo assignment sắp hết hạn: " + savedAssignment.getTitle() +
                " (ID=" + savedAssignment.getId() + ", Due: " + savedAssignment.getDueDate() + ")");
        }

        return assignments;
    }

    /**
     * Tạo 5 bài tập đã hết hạn (due_date đã qua 1-7 ngày)
     */
    private List<Assignment> createOverdueAssignments(Classroom classroom) {
        List<Assignment> assignments = new ArrayList<>();
        String[] titles = {
            "Bài tập Hàm số một biến - Đã hết hạn",
            "Thực hành Tính đạo hàm cấp cao - Đã hết hạn",
            "Bài kiểm tra Tích phân bội - Đã hết hạn",
            "Ứng dụng Toán học trong Vật lý - Đã hết hạn",
            "Bài tập tổng hợp Đại số tuyến tính - Đã hết hạn"
        };

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            Assignment assignment = new Assignment();
            assignment.setTitle(titles[i]);
            assignment.setDescription("Bài tập này đã hết hạn nộp từ " + (i + 1) + " ngày trước. " +
                "Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.");
            assignment.setDueDate(now.minusDays(i + 1)); // Đã hết hạn từ 1-5 ngày trước
            assignment.setPoints(90 + (i * 2)); // Điểm từ 90-98
            assignment.setClassroom(classroom);

            Assignment savedAssignment = assignmentRepository.save(assignment);
            assignments.add(savedAssignment);
            System.out.println("✅ [TonClassroomDataSeeder] Tạo assignment đã hết hạn: " + savedAssignment.getTitle() +
                " (ID=" + savedAssignment.getId() + ", Due: " + savedAssignment.getDueDate() + ")");
        }

        return assignments;
    }

    /**
     * Tạo submissions cho assignments
     * @param assignments Danh sách assignments
     * @param students Danh sách học sinh
     * @param classroom Classroom để lấy teacher
     * @param isGraded true nếu đã chấm điểm, false nếu chưa chấm
     */
    private void createSubmissionsForAssignments(List<Assignment> assignments, List<User> students, Classroom classroom, boolean isGraded) {
        for (Assignment assignment : assignments) {
            // Chỉ một số học sinh nộp bài (60-80% học sinh)
            int numSubmissions = (int) (students.size() * (0.6 + Math.random() * 0.2));

            for (int i = 0; i < numSubmissions && i < students.size(); i++) {
                User student = students.get(i);

                // Kiểm tra xem đã có submission chưa
                if (submissionRepository.findByAssignmentAndStudent(assignment, student).isPresent()) {
                    continue;
                }

                Submission submission = new Submission();
                submission.setAssignment(assignment);
                submission.setStudent(student);
                submission.setComment("Bài làm của " + student.getFullName() + " cho assignment: " + assignment.getTitle());

                // Thời gian nộp bài (trước hoặc sau due date tùy thuộc vào loại assignment)
                LocalDateTime submittedTime;
                if (assignment.getDueDate().isBefore(LocalDateTime.now())) {
                    // Assignment đã hết hạn - nộp bài trước due date
                    submittedTime = assignment.getDueDate().minusHours(1 + (int)(Math.random() * 24));
                } else {
                    // Assignment chưa hết hạn - nộp bài gần đây
                    submittedTime = LocalDateTime.now().minusHours((int)(Math.random() * 48));
                }

                // Set submitted time
                submission.setSubmittedAt(submittedTime);

                if (isGraded && Math.random() > 0.3) { // 70% được chấm điểm
                    submission.setScore(70 + (int)(Math.random() * 30)); // Điểm từ 70-100
                    submission.setFeedback("Bài làm tốt. Cần cải thiện một số điểm nhỏ.");
                    submission.setGradedAt(LocalDateTime.now().minusDays(1));

                    // Set graded by teacher (lấy teacher của classroom)
                    submission.setGradedBy(classroom.getTeacher());
                }

                Submission savedSubmission = submissionRepository.save(submission);
                System.out.println("✅ [TonClassroomDataSeeder] Tạo submission: Assignment=" + assignment.getId() +
                    ", Student=" + student.getFullName() + ", Score=" + savedSubmission.getScore());
            }
        }
    }
}
