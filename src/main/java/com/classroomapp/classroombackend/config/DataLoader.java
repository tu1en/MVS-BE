package com.classroomapp.classroombackend.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.dto.StudentRequestFormDTO;
import com.classroomapp.classroombackend.dto.TeacherRequestFormDTO;
import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.model.AnnouncementAttachment;
import com.classroomapp.classroombackend.model.Blog;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.classroommanagement.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Syllabus;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.AnnouncementAttachmentRepository;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;
import com.classroomapp.classroombackend.repository.BlogRepository;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ScheduleRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.SyllabusRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Initialize test data when application starts
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final RequestRepository requestRepository;
    private final AccomplishmentRepository accomplishmentRepository;
    private final ClassroomRepository classroomRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementAttachmentRepository announcementAttachmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final SyllabusRepository syllabusRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public DataLoader(
        UserRepository userRepository,
        BlogRepository blogRepository,
        RequestRepository requestRepository,
        AccomplishmentRepository accomplishmentRepository,
        ClassroomRepository classroomRepository,
        AssignmentRepository assignmentRepository,
        SubmissionRepository submissionRepository,
        AttendanceSessionRepository attendanceSessionRepository,
        AttendanceRepository attendanceRepository,
        AnnouncementRepository announcementRepository,
        AnnouncementAttachmentRepository announcementAttachmentRepository,
        ScheduleRepository scheduleRepository,
        SyllabusRepository syllabusRepository,
        PasswordEncoder passwordEncoder,
        ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.blogRepository = blogRepository;
        this.requestRepository = requestRepository;
        this.accomplishmentRepository = accomplishmentRepository;
        this.classroomRepository = classroomRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.announcementRepository = announcementRepository;
        this.announcementAttachmentRepository = announcementAttachmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.syllabusRepository = syllabusRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Always clear existing data and reload fresh data
        clearAllData();
        
        // Create sample users
        List<User> users = CreateUsers();
        
        // Create sample blogs
        CreateSampleBlogs(users);
        
        // Create sample accomplishments
        CreateAccomplishments();
        
        // Create sample requests
        CreateRequests();

        // Create sample classrooms and enrollments
        CreateClassrooms(users);

        // Create sample assignments and submissions
        CreateAssignments(users);

        // Create sample attendance records
        CreateAttendance(users);

        // Create sample announcements
        CreateAnnouncements(users);
        
        System.out.println("✅ DataLoader: All data has been reset and reloaded successfully!");
    }
    
    /**
     * Clear all existing data from database
     */
    private void clearAllData() {
        System.out.println("🗑️ DataLoader: Clearing all existing data...");
        
        // Clear data in reverse order of dependencies to avoid foreign key constraints
        announcementAttachmentRepository.deleteAll();
        attendanceRepository.deleteAll();
        attendanceSessionRepository.deleteAll();
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        announcementRepository.deleteAll();
        accomplishmentRepository.deleteAll();
        requestRepository.deleteAll();
        blogRepository.deleteAll();
        classroomRepository.deleteAll();
        userRepository.deleteAll();
        
        System.out.println("✅ DataLoader: All existing data cleared successfully!");
    }
    
    /**
     * Create sample users for testing
     * @return List of created users
     */
    private List<User> CreateUsers() {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@classroomapp.com");
        admin.setFullName("Administrator");
        admin.setRoleId(RoleConstants.ADMIN);
        userRepository.save(admin);        
        // Create manager user
        User manager = new User();
        manager.setUsername("manager");
        manager.setPassword(passwordEncoder.encode("manager123"));
        manager.setEmail("fonkunn@gmail.com");
        manager.setFullName("Manager User");
        manager.setRoleId(RoleConstants.MANAGER);
        userRepository.save(manager);

        // Create teacher user
        User teacher = new User();
        teacher.setUsername("teacher");
        teacher.setPassword(passwordEncoder.encode("teacher123"));  
        teacher.setEmail("teacher@classroomapp.com");
        teacher.setFullName("Teacher User");
        teacher.setRoleId(RoleConstants.TEACHER);
        userRepository.save(teacher);
        
        // Create student user
        User student = new User();
        student.setUsername("student");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setEmail("student@classroomapp.com");
        student.setFullName("Student User");
        student.setRoleId(RoleConstants.STUDENT);
        userRepository.save(student);
        
        return Arrays.asList(admin, manager, teacher, student);
    }
    
    /**
     * Create sample blogs for testing
     * @param users List of users for assigning authors
     */
    private void CreateSampleBlogs(List<User> users) {
        User admin = users.get(0);
        User manager = users.get(1);
        User teacher = users.get(2);
        
        // Blog 1 - Published by Admin
        Blog blog1 = new Blog();
        blog1.setTitle("Chào mừng đến với Nền tảng Lớp học của Chúng tôi");
        blog1.setDescription("Đây là bài viết blog đầu tiên trên nền tảng lớp học của chúng tôi. Chúng tôi rất vui mừng thông báo về việc ra mắt môi trường học tập kỹ thuật số mới được thiết kế để tạo điều kiện giao tiếp tốt hơn giữa giáo viên và học sinh.\n\nNền tảng của chúng tôi bao gồm các tính năng như:\n- Lớp học ảo\n- Nộp bài tập\n- Hệ thống chấm điểm\n- Diễn đàn thảo luận\n\nChúng tôi hy vọng bạn sẽ thích sử dụng nền tảng của chúng tôi!");
        blog1.setImageUrl("https://images.unsplash.com/photo-1503676260728-1c00da094a0b?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
        blog1.setThumbnailUrl("https://images.unsplash.com/photo-1503676260728-1c00da094a0b?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
        blog1.setTags("thông báo, chào mừng, nền tảng");
        blog1.setAuthor(admin);
        blog1.setIsPublished(true);
        blog1.setStatus("published");
        LocalDateTime now = LocalDateTime.now();
        blog1.setPublishedDate(now.minusDays(7));
        blog1.setLastEditedDate(now.minusDays(7));
        blog1.setLastEditedBy(admin);
        blog1.setViewCount(156);
        blogRepository.save(blog1);
        
        // Blog 2 - Published by Teacher
        Blog blog2 = new Blog();
        blog2.setTitle("Mẹo Học Trực Tuyến Hiệu Quả");
        blog2.setDescription("Khi chúng ta chuyển sang học trực tuyến nhiều hơn, đây là một số mẹo để giúp học sinh thành công:\n\n1. **Tạo không gian học tập riêng** - Tìm một nơi yên tĩnh, thoải mái nơi bạn có thể tập trung.\n\n2. **Thiết lập thói quen** - Đặt giờ học cố định và tuân thủ chúng.\n\n3. **Nghỉ giải lao** - Sử dụng các kỹ thuật như phương pháp Pomodoro (25 phút làm việc sau đó nghỉ 5 phút).\n\n4. **Giữ tổ chức** - Sử dụng lịch kỹ thuật số và danh sách việc cần làm để theo dõi bài tập và thời hạn.\n\n5. **Tham gia tích cực** - Tham gia vào các cuộc thảo luận trực tuyến và đặt câu hỏi khi cần giúp đỡ.\n\nBạn thấy chiến lược nào hiệu quả nhất cho việc học trực tuyến? Hãy chia sẻ trong phần bình luận!");
        blog2.setImageUrl("https://images.unsplash.com/photo-1498243691581-b145c3f54a5a?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
        blog2.setThumbnailUrl("https://images.unsplash.com/photo-1498243691581-b145c3f54a5a?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
        blog2.setVideoUrl("https://www.youtube.com/embed/sBJmRD7kNTk");
        blog2.setTags("học trực tuyến, mẹo học tập, giáo dục");
        blog2.setAuthor(teacher);
        blog2.setIsPublished(true);
        blog2.setStatus("published");
        blog2.setPublishedDate(now.minusDays(5));
        blog2.setLastEditedDate(now.minusDays(5));
        blog2.setLastEditedBy(teacher);
        blog2.setViewCount(89);
        blogRepository.save(blog2);
        
        // Blog 3 - Draft by Manager
        Blog blog3 = new Blog();
        blog3.setTitle("Tính Năng Mới Sắp Ra Mắt Cho Học Kỳ Tới");
        blog3.setDescription("Chúng tôi đang làm việc trên một số tính năng thú vị sẽ được phát hành trong học kỳ tới. Những cải tiến này dựa trên phản hồi từ học sinh và giáo viên.\n\n**Sắp Ra Mắt:**\n\n- Hệ thống tin nhắn cải tiến\n- Công cụ cộng tác thời gian thực\n- Ứng dụng di động cho iOS và Android\n- Tích hợp với các công cụ giáo dục phổ biến\n- Phân tích nâng cao cho giáo viên\n\nBài viết này vẫn đang là bản nháp và sẽ được cập nhật với nhiều chi tiết hơn trước khi xuất bản.");
        blog3.setThumbnailUrl("https://i1.sndcdn.com/artworks-000473680527-kz21lf-t1080x1080.jpg");
        blog3.setTags("tính năng, sắp ra mắt, cải tiến");
        blog3.setAuthor(manager);
        blog3.setIsPublished(false);
        blog3.setStatus("draft");
        blog3.setLastEditedDate(now.minusDays(2));
        blog3.setLastEditedBy(manager);
        blogRepository.save(blog3);
        
        // Blog 4 - Published by Manager with image and video
        Blog blog4 = new Blog();
        blog4.setTitle("Chuyến Tham Quan Ảo: Khám Phá Các Bảo Tàng Thế Giới");
        blog4.setDescription("Hôm nay chúng ta sẽ thực hiện một chuyến tham quan ảo đến một số bảo tàng nổi tiếng nhất thế giới cung cấp các tour trực tuyến.\n\nNhiều bảo tàng uy tín cung cấp các tour ảo cho phép bạn khám phá bộ sưu tập của họ từ sự thoải mái của ngôi nhà. Đây là một nguồn tài nguyên giáo dục tuyệt vời cho nghệ thuật, lịch sử và nghiên cứu văn hóa.\n\n**Các bảo tàng được giới thiệu trong video:**\n\n- Bảo tàng Louvre, Paris\n- Bảo tàng Anh, London\n- Bảo tàng Nghệ thuật Metropolitan, New York\n- Bảo tàng Vatican, Rome\n- Bảo tàng Nghệ thuật Hiện đại và Đương đại Quốc gia, Seoul\n\nVideo đính kèm cung cấp một tour có hướng dẫn của các bảo tàng này. Chúng tôi hy vọng chuyến tham quan ảo này sẽ truyền cảm hứng cho học sinh tìm hiểu thêm về nghệ thuật và lịch sử!");
        blog4.setImageUrl("https://images.unsplash.com/photo-1515169273894-7e876dcf13da?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
        blog4.setThumbnailUrl("https://images.unsplash.com/photo-1515169273894-7e876dcf13da?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
        blog4.setVideoUrl("https://www.youtube.com/embed/vQ_sAt-VzRk");
        blog4.setTags("tham quan ảo, bảo tàng, nghệ thuật, lịch sử, giáo dục");
        blog4.setAuthor(manager);
        blog4.setIsPublished(true);
        blog4.setStatus("published");
        blog4.setPublishedDate(now.minusDays(1));
        blog4.setLastEditedDate(now.minusDays(1));
        blog4.setLastEditedBy(manager);
        blog4.setViewCount(42);
        blogRepository.save(blog4);
        
        // Blog 5 - Published by Student (if they can create blogs)
        Blog blog5 = new Blog();
        blog5.setTitle("Hành Trình Học Tập Của Tôi: Từ Người Mới Bắt Đầu Đến Nâng Cao");
        blog5.setDescription("Xin chào mọi người! Tôi muốn chia sẻ hành trình học tập cá nhân của mình và một số hiểu biết có thể giúp ích cho các học sinh khác.\n\n**Trải Nghiệm Của Tôi:**\n\nKhi tôi lần đầu sử dụng nền tảng này, tôi cảm thấy choáng ngợp bởi tất cả các tính năng. Nhưng dần dần, tôi khám phá ra cách mỗi công cụ có thể giúp tôi học tốt hơn.\n\n**Những Bài Học Chính:**\n\n1. **Tính nhất quán là chìa khóa** - Học một chút mỗi ngày tốt hơn là nhồi nhét\n2. **Đặt câu hỏi** - Đừng ngần ngại hỏi giáo viên hoặc bạn học để được giúp đỡ\n3. **Sử dụng tất cả tài nguyên** - Tận dụng bài giảng, bài tập và diễn đàn thảo luận\n4. **Theo dõi tiến độ** - Giám sát kết quả học tập để xác định các lĩnh vực cần cải thiện\n5. **Kết nối** - Tham gia vào cộng đồng thông qua tin nhắn và thông báo\n\n**Lời Khuyên Cho Học Sinh Mới:**\n\n- Bắt đầu với những điều cơ bản và dần dần khám phá các tính năng nâng cao\n- Thiết lập lịch học phù hợp với bạn\n- Đừng sợ mắc lỗi - đó là cách chúng ta học!\n- Kết nối với các học sinh khác có cùng sở thích\n\nTôi hy vọng điều này sẽ giúp ích cho ai đó trong hành trình học tập của họ!");
        blog5.setImageUrl("https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
        blog5.setThumbnailUrl("https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
        blog5.setTags("trải nghiệm học sinh, mẹo học tập, động lực, cộng đồng");
        blog5.setAuthor(users.get(3)); // Student user
        blog5.setIsPublished(true);
        blog5.setStatus("published");
        blog5.setPublishedDate(now.minusHours(12));
        blog5.setLastEditedDate(now.minusHours(12));
        blog5.setLastEditedBy(users.get(3));
        blog5.setViewCount(23);
        blogRepository.save(blog5);
    }
    
    /**
     * Create sample accomplishments for testing
     */
    private void CreateAccomplishments() {
        // Get the student user we created
        User student = userRepository.findByUsername("student")
                .orElseThrow(() -> new RuntimeException("Student user not found"));
        
        // Create sample accomplishments
        Accomplishment math = new Accomplishment();
        math.setUser(student);
        math.setCourseTitle("Advanced Mathematics");
        math.setSubject("Mathematics");
        math.setTeacherName("Dr. John Smith");
        math.setGrade(85.5);
        math.setCompletionDate(LocalDate.now().minusDays(30));
        accomplishmentRepository.save(math);
        
        Accomplishment physics = new Accomplishment();
        physics.setUser(student);
        physics.setCourseTitle("Classical Physics");
        physics.setSubject("Physics");
        physics.setTeacherName("Prof. Jane Doe");
        physics.setGrade(92.0);
        physics.setCompletionDate(LocalDate.now().minusDays(15));
        accomplishmentRepository.save(physics);
        
        Accomplishment programming = new Accomplishment();
        programming.setUser(student);
        programming.setCourseTitle("Java Programming");
        programming.setSubject("Computer Science");
        programming.setTeacherName("Mr. Bob Wilson");
        programming.setGrade(88.5);
        programming.setCompletionDate(LocalDate.now().minusDays(7));
        accomplishmentRepository.save(programming);
    }
    
    /**
     * Create sample role requests for testing
     */
    private void CreateRequests() throws Exception {
        // Create a teacher role request
        TeacherRequestFormDTO teacherForm = new TeacherRequestFormDTO();
        teacherForm.setEmail("nguyenvanA@gmail.com");
        teacherForm.setFullName("Nguyễn Văn A");
        teacherForm.setPhoneNumber("0987654321");
        teacherForm.setCvFileName("nguyen_van_a_cv.pdf");
        teacherForm.setCvFileType("application/pdf");
        teacherForm.setCvFileData("U2FtcGxlIENWIGZpbGUgY29udGVudC4gSW4gcmVhbCBpbXBsZW1lbnRhdGlvbiwgdGhpcyB3b3VsZCBiZSBhIGJhc2U2NCBlbmNvZGVkIHN0cmluZyBvZiBhIFBERiBmaWxlLg=="); // Sample base64 data
        teacherForm.setCvFileUrl("/files/teachers/nguyen_van_a_cv.pdf"); // This would be set by the service after upload
        teacherForm.setAdditionalInfo("Tôi đã có 5 năm kinh nghiệm giảng dạy Toán cấp trung học. Tôi từng làm việc tại trường THPT Chu Văn An và là giáo viên dạy thêm tại nhiều trung tâm luyện thi.");
        
        Request teacherRequest = new Request();
        teacherRequest.setEmail(teacherForm.getEmail());
        teacherRequest.setFullName(teacherForm.getFullName());
        teacherRequest.setPhoneNumber(teacherForm.getPhoneNumber());
        teacherRequest.setRequestedRole("TEACHER");
        teacherRequest.setFormResponses(objectMapper.writeValueAsString(teacherForm));
        teacherRequest.setStatus("PENDING");
        teacherRequest.setCreatedAt(LocalDateTime.now().minusDays(3));
        requestRepository.save(teacherRequest);
        
        // Create a student role request
        StudentRequestFormDTO studentForm = new StudentRequestFormDTO();
        studentForm.setEmail("tranvanB@gmail.com");
        studentForm.setFullName("Trần Văn B");
        studentForm.setPhoneNumber("0987123456");
        studentForm.setGrade("Lớp 11");
        studentForm.setParentContact("Phụ huynh: Trần Thị C, SĐT: 0912345678");
        studentForm.setAdditionalInfo("Em muốn đăng ký học thêm môn Toán và Vật lý để chuẩn bị cho kỳ thi quốc gia.");
        
        Request studentRequest = new Request();
        studentRequest.setEmail(studentForm.getEmail());
        studentRequest.setFullName(studentForm.getFullName());
        studentRequest.setPhoneNumber(studentForm.getPhoneNumber());
        studentRequest.setRequestedRole("STUDENT");
        studentRequest.setFormResponses(objectMapper.writeValueAsString(studentForm));
        studentRequest.setStatus("PENDING");
        studentRequest.setCreatedAt(LocalDateTime.now().minusDays(1));
        requestRepository.save(studentRequest);
    }

    private void CreateClassrooms(List<User> users) {
        User teacher = users.get(2); // Get teacher user
        User student = users.get(3); // Get student user

        // Create a Mathematics classroom
        Classroom mathClass = new Classroom();
        mathClass.setName("Advanced Mathematics 2024");
        mathClass.setDescription("Advanced mathematics course covering calculus, linear algebra, and probability theory");
        mathClass.setSubject("Mathematics");
        mathClass.setTeacher(teacher);
        mathClass.setSection("Section A");
        mathClass.getStudents().add(student);
        classroomRepository.save(mathClass);

        // Create a Physics classroom
        Classroom physicsClass = new Classroom();
        physicsClass.setName("Classical Physics");
        physicsClass.setDescription("Introduction to classical mechanics and thermodynamics");
        physicsClass.setSubject("Physics");
        physicsClass.setTeacher(teacher);
        physicsClass.setSection("Section B");
        physicsClass.getStudents().add(student);
        classroomRepository.save(physicsClass);

        // Create schedules for math class
        Schedule mathSchedule1 = new Schedule();
        mathSchedule1.setClassroom(mathClass);
        mathSchedule1.setDayOfWeek(DayOfWeek.MONDAY);
        mathSchedule1.setStartTime(LocalTime.of(9, 0));
        mathSchedule1.setEndTime(LocalTime.of(11, 0));
        mathSchedule1.setLocation("Room 101");
        scheduleRepository.save(mathSchedule1);

        Schedule mathSchedule2 = new Schedule();
        mathSchedule2.setClassroom(mathClass);
        mathSchedule2.setDayOfWeek(DayOfWeek.WEDNESDAY);
        mathSchedule2.setStartTime(LocalTime.of(9, 0));
        mathSchedule2.setEndTime(LocalTime.of(11, 0));
        mathSchedule2.setLocation("Room 101");
        scheduleRepository.save(mathSchedule2);

        // Create schedules for physics class
        Schedule physicsSchedule1 = new Schedule();
        physicsSchedule1.setClassroom(physicsClass);
        physicsSchedule1.setDayOfWeek(DayOfWeek.TUESDAY);
        physicsSchedule1.setStartTime(LocalTime.of(13, 0));
        physicsSchedule1.setEndTime(LocalTime.of(15, 0));
        physicsSchedule1.setLocation("Room 102");
        scheduleRepository.save(physicsSchedule1);

        Schedule physicsSchedule2 = new Schedule();
        physicsSchedule2.setClassroom(physicsClass);
        physicsSchedule2.setDayOfWeek(DayOfWeek.THURSDAY);
        physicsSchedule2.setStartTime(LocalTime.of(13, 0));
        physicsSchedule2.setEndTime(LocalTime.of(15, 0));
        physicsSchedule2.setLocation("Room 102");
        scheduleRepository.save(physicsSchedule2);

        // Create syllabus for math class
        Syllabus mathSyllabus = new Syllabus();
        mathSyllabus.setClassroom(mathClass);
        mathSyllabus.setTitle("Advanced Mathematics 2024 Syllabus");
        mathSyllabus.setContent("1. Introduction to Calculus\n2. Limits and Continuity\n3. Derivatives\n4. Integration\n5. Linear Algebra Basics\n6. Matrices and Determinants\n7. Vector Spaces\n8. Probability Theory");
        syllabusRepository.save(mathSyllabus);

        // Create syllabus for physics class
        Syllabus physicsSyllabus = new Syllabus();
        physicsSyllabus.setClassroom(physicsClass);
        physicsSyllabus.setTitle("Classical Physics Syllabus");
        physicsSyllabus.setContent("1. Introduction to Classical Mechanics\n2. Newton's Laws\n3. Work and Energy\n4. Momentum\n5. Rotational Motion\n6. Thermodynamics Laws\n7. Heat Transfer\n8. Ideal Gas Laws");
        syllabusRepository.save(physicsSyllabus);
    }

    private void CreateAssignments(List<User> users) {
        User teacher = users.get(2);
        User student = users.get(3);
        
        // Find math classroom
        List<Classroom> mathClasses = classroomRepository.findByTeacher(teacher);
        Classroom mathClass = mathClasses.stream()
                .filter(c -> "Mathematics".equals(c.getSubject()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Math classroom not found"));

        // Create a math assignment
        Assignment mathAssignment = new Assignment();
        mathAssignment.setTitle("Calculus Integration Problems");
        mathAssignment.setDescription("Solve the following integration problems. Show all your work and explain your steps.");
        mathAssignment.setClassroom(mathClass);
        mathAssignment.setDueDate(LocalDateTime.now().plusDays(7));
        mathAssignment.setPoints(100);
        assignmentRepository.save(mathAssignment);

        // Create a submission for the math assignment
        Submission mathSubmission = new Submission();
        mathSubmission.setAssignment(mathAssignment);
        mathSubmission.setStudent(student);
        mathSubmission.setSubmittedAt(LocalDateTime.now().minusDays(1));
        mathSubmission.setComment("Here are my solutions to the integration problems...");
        mathSubmission.setScore(85);
        mathSubmission.setFeedback("Good work! Remember to explain your steps more clearly.");
        mathSubmission.setGradedAt(LocalDateTime.now().minusHours(2));
        mathSubmission.setGradedBy(teacher);
        submissionRepository.save(mathSubmission);

        // Create another assignment
        Assignment mathAssignment2 = new Assignment();
        mathAssignment2.setTitle("Linear Algebra Quiz");
        mathAssignment2.setDescription("Complete the following problems on matrices and vector spaces.");
        mathAssignment2.setClassroom(mathClass);
        mathAssignment2.setDueDate(LocalDateTime.now().plusDays(14));
        mathAssignment2.setPoints(50);
        assignmentRepository.save(mathAssignment2);
    }

    private void CreateAttendance(List<User> users) {
        User teacher = users.get(2);
        User student = users.get(3);
        
        // Find math classroom
        List<Classroom> mathClasses = classroomRepository.findByTeacher(teacher);
        Classroom mathClass = mathClasses.stream()
                .filter(c -> "Mathematics".equals(c.getSubject()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Math classroom not found"));

        // Create attendance session for yesterday's class
        AttendanceSession yesterdaySession = new AttendanceSession();
        yesterdaySession.setClassroom(mathClass);
        yesterdaySession.setTeacher(teacher);
        yesterdaySession.setSessionName("Integration by Parts Lecture");
        yesterdaySession.setTitle("Integration by Parts");
        yesterdaySession.setDescription("Learning the technique of integration by parts and its applications");
        yesterdaySession.setSessionDate(LocalDateTime.now().minusDays(1));
        yesterdaySession.setStartTime(LocalDateTime.now().minusDays(1).withHour(9).withMinute(0));
        yesterdaySession.setEndTime(LocalDateTime.now().minusDays(1).withHour(11).withMinute(0));
        yesterdaySession.setSessionType("OFFLINE");
        yesterdaySession.setStatus(AttendanceSession.SessionStatus.COMPLETED);
        yesterdaySession.setCreatedAt(LocalDateTime.now().minusDays(2));
        attendanceSessionRepository.save(yesterdaySession);

        // Mark student's attendance for yesterday
        Attendance yesterdayAttendance = new Attendance();
        yesterdayAttendance.setSession(yesterdaySession);
        yesterdayAttendance.setStudent(student);
        yesterdayAttendance.setStatus(Attendance.AttendanceStatus.PRESENT);
        yesterdayAttendance.setCheckInTime(LocalDateTime.now().minusDays(1).withHour(9).withMinute(0));
        yesterdayAttendance.setCheckOutTime(LocalDateTime.now().minusDays(1).withHour(11).withMinute(0));
        yesterdayAttendance.setCreatedAt(LocalDateTime.now().minusDays(1));
        attendanceRepository.save(yesterdayAttendance);

        // Create attendance session for last week
        AttendanceSession lastWeekSession = new AttendanceSession();
        lastWeekSession.setClassroom(mathClass);
        lastWeekSession.setTeacher(teacher);
        lastWeekSession.setSessionName("Limits and Continuity Lecture");
        lastWeekSession.setTitle("Limits and Continuity");
        lastWeekSession.setDescription("Understanding limits and continuity in calculus");
        lastWeekSession.setSessionDate(LocalDateTime.now().minusDays(7));
        lastWeekSession.setStartTime(LocalDateTime.now().minusDays(7).withHour(9).withMinute(0));
        lastWeekSession.setEndTime(LocalDateTime.now().minusDays(7).withHour(11).withMinute(0));
        lastWeekSession.setSessionType("OFFLINE");
        lastWeekSession.setStatus(AttendanceSession.SessionStatus.COMPLETED);
        lastWeekSession.setCreatedAt(LocalDateTime.now().minusDays(8));
        attendanceSessionRepository.save(lastWeekSession);

        // Mark student's attendance for last week
        Attendance lastWeekAttendance = new Attendance();
        lastWeekAttendance.setSession(lastWeekSession);
        lastWeekAttendance.setStudent(student);
        lastWeekAttendance.setStatus(Attendance.AttendanceStatus.PRESENT);
        lastWeekAttendance.setCheckInTime(LocalDateTime.now().minusDays(7).withHour(9).withMinute(0));
        lastWeekAttendance.setCheckOutTime(LocalDateTime.now().minusDays(7).withHour(11).withMinute(0));
        lastWeekAttendance.setCreatedAt(LocalDateTime.now().minusDays(7));
        attendanceRepository.save(lastWeekAttendance);
    }

    private void CreateAnnouncements(List<User> users) {
        User teacher = users.get(2);
        
        // Find math classroom
        List<Classroom> mathClasses = classroomRepository.findByTeacher(teacher);
        Classroom mathClass = mathClasses.stream()
                .filter(c -> "Mathematics".equals(c.getSubject()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Math classroom not found"));

        // Create an important announcement
        Announcement announcement1 = new Announcement();
        announcement1.setTitle("Thay đổi lịch học tuần tới");
        announcement1.setContent("Do trùng với lịch thi giữa kỳ, lớp học tuần sau sẽ được dời sang thứ 5 thay vì thứ 4 như thường lệ. Mong các em lưu ý và sắp xếp thời gian phù hợp.");
        announcement1.setClassroomId(mathClass.getId());
        announcement1.setCreatedBy(teacher.getId());
        announcement1.setPriority(Announcement.Priority.HIGH);
        announcement1.setCreatedAt(LocalDateTime.now());
        announcement1.setStatus(Announcement.AnnouncementStatus.ACTIVE);
        announcementRepository.save(announcement1);

        // Create an announcement with attachment
        Announcement announcement2 = new Announcement();
        announcement2.setTitle("Tài liệu ôn tập Giải tích");
        announcement2.setContent("Các em tham khảo tài liệu ôn tập đính kèm để chuẩn bị cho bài kiểm tra tuần sau. Tài liệu bao gồm các dạng bài tập quan trọng và một số ví dụ minh họa.");
        announcement2.setClassroomId(mathClass.getId());
        announcement2.setCreatedBy(teacher.getId());
        announcement2.setPriority(Announcement.Priority.NORMAL);
        announcement2.setCreatedAt(LocalDateTime.now().minusDays(2));
        announcement2.setStatus(Announcement.AnnouncementStatus.ACTIVE);
        announcement2.setAttachmentsCount(1);
        announcementRepository.save(announcement2);

        // Create attachment for announcement2
        AnnouncementAttachment attachment = new AnnouncementAttachment();
        attachment.setAnnouncementId(announcement2.getId());
        attachment.setFileName("on_tap_giai_tich.pdf");
        attachment.setFileType("application/pdf");
        attachment.setFilePath("/files/announcements/on_tap_giai_tich.pdf");
        attachment.setFileSize(1024L); // 1KB sample size
        attachment.setUploadedAt(LocalDateTime.now().minusDays(2));
        announcementAttachmentRepository.save(attachment);
    }
}