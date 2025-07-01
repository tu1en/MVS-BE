package com.classroomapp.classroombackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.RequestRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.classroomapp.classroombackend.dto.TeacherRequestFormDTO;
import com.classroomapp.classroombackend.dto.StudentRequestFormDTO;

import java.time.LocalDateTime;

/**
 * Initialize test data when application starts
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public DataLoader(
        UserRepository userRepository, 
        RequestRepository requestRepository,
        PasswordEncoder passwordEncoder,
        ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Clear existing data
        userRepository.deleteAll();
        requestRepository.deleteAll();
        
        // Create sample users
        CreateUsers();
        
        // Create sample requests
        // CreateRequests();
    }
    
    /**
     * Create sample users for testing
     */
    private void CreateUsers() {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@classroomapp.com");
        admin.setFullName("Administrator");
        admin.setRoleId(RoleConstants.ADMIN);
        userRepository.save(admin);
        
        // Create teacher user
        User manager = new User();
        manager.setUsername("manager");
        manager.setPassword(passwordEncoder.encode("manager123"));
        manager.setEmail("manager@classroomapp.com");
        manager.setFullName("Nigga Cheese");
        manager.setRoleId(RoleConstants.MANAGER);
        userRepository.save(manager);

        // Create teacher user
        User teacher = new User();
        teacher.setUsername("teacher");
        teacher.setPassword(passwordEncoder.encode("teacher123"));
        teacher.setEmail("teacher@classroomapp.com");
        teacher.setFullName("Butt Slapper");
        teacher.setRoleId(RoleConstants.TEACHER);
        userRepository.save(teacher);
        
<<<<<<< Updated upstream
        // Create student user
        User student = new User();
        student.setUsername("student");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setEmail("student@classroomapp.com");
        student.setFullName("Ass Cracker");
        student.setRoleId(RoleConstants.STUDENT);
        userRepository.save(student);
=======
        User extraStudent2 = new User();
        extraStudent2.setUsername("student3");
        extraStudent2.setPassword(passwordEncoder.encode("student123"));
        extraStudent2.setEmail("student3@classroomapp.com");
        extraStudent2.setFullName("Bob Wilson");
        extraStudent2.setRoleId(RoleConstants.STUDENT);
        userRepository.save(extraStudent2);
        
        User extraStudent3 = new User();
        extraStudent3.setUsername("student4");
        extraStudent3.setPassword(passwordEncoder.encode("student123"));
        extraStudent3.setEmail("student4@classroomapp.com");
        extraStudent3.setFullName("Carol Davis");
        extraStudent3.setRoleId(RoleConstants.STUDENT);
        userRepository.save(extraStudent3);
        
        User extraStudent4 = new User();
        extraStudent4.setUsername("student5");
        extraStudent4.setPassword(passwordEncoder.encode("student123"));
        extraStudent4.setEmail("student5@classroomapp.com");
        extraStudent4.setFullName("David Chen");
        extraStudent4.setRoleId(RoleConstants.STUDENT);
        userRepository.save(extraStudent4);
        
        User extraTeacher1 = new User();
        extraTeacher1.setUsername("teacher2");
        extraTeacher1.setPassword(passwordEncoder.encode("teacher123"));
        extraTeacher1.setEmail("teacher2@classroomapp.com");
        extraTeacher1.setFullName("Dr. Sarah Williams");
        extraTeacher1.setRoleId(RoleConstants.TEACHER);
        userRepository.save(extraTeacher1);
        
        User extraTeacher2 = new User();
        extraTeacher2.setUsername("teacher3");
        extraTeacher2.setPassword(passwordEncoder.encode("teacher123"));
        extraTeacher2.setEmail("teacher3@classroomapp.com");
        extraTeacher2.setFullName("Prof. Michael Brown");
        extraTeacher2.setRoleId(RoleConstants.TEACHER);
        userRepository.save(extraTeacher2);
        
        // Create accountant user
        User accountant = new User();
        accountant.setUsername("accountant");
        accountant.setPassword(passwordEncoder.encode("accountant123"));
        accountant.setEmail("accountant@classroomapp.com");
        accountant.setFullName("Accountant User");
        accountant.setRoleId(RoleConstants.ACCOUNTANT);
        userRepository.save(accountant);

        // Note: Frontend should use actual user IDs instead of hardcoded values
        System.out.println("✅ Created 11 users (4 students, 3 teachers, 1 manager, 1 admin, 1 accountant, 1 extra student) for comprehensive testing");
        return Arrays.asList(student, teacher, manager, admin, extraStudent1, extraStudent2, 
                            extraStudent3, extraStudent4, extraTeacher1, extraTeacher2, accountant);
    }

    /**
     * Create sample blogs for testingassigning authors
     * 
     * @param users List of users for assigning authors
     */
    private void CreateSampleBlogs(List<User> users) {
        User admin = users.get(3);
        User manager = users.get(2);
        User teacher = users.get(1);
        LocalDateTime now = LocalDateTime.now();

        Blog blog1 = new Blog();
        // Blog 1 - Published by Adminvới Nền tảng Lớp học của Chúng tôi
        blog1.setTitle("Chào mừng đến với Nền tảng Lớp học của Chúng tôi");
        blog1.setDescription(
                "Đây là bài viết blog đầu tiên trên nền tảng lớp học của chúng tôi. Chúng tôi rất vui mừng thông báo về việc ra mắt môi trường học tập kỹ thuật số mới được thiết kế để tạo điều kiện giao tiếp tốt hơn giữa giáo viên và học sinh.\n\nNền tảng của chúng tôi bao gồm các tính năng như:\n- Lớp học ảo\n- Nộp bài tập\n- Hệ thống chấm điểm\n- Diễn đàn thảo luận\n\nChúng tôi hy vọng bạn sẽ thích sử dụng nền tảng của chúng tôi!");
        blog1.setImageUrl(
                "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
        blog1.setThumbnailUrl(
                "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
        blog1.setTags("thông báo, chào mừng, nền tảng");
        blog1.setAuthor(admin);
        blog1.setIsPublished(true);
        blog1.setStatus("published");
        blog1.setPublishedDate(now.minusDays(7));
        blog1.setLastEditedDate(now.minusDays(7));
        blog1.setLastEditedBy(admin);
        blog1.setViewCount(156);
        blogRepository.save(blog1);

        Blog blog2 = new Blog();
        // Blog 2 - Published by Teacherến Hiệu Quả
        blog2.setTitle("Mẹo Học Trực Tuyến Hiệu Quả");
        blog2.setDescription(
                "Khi chúng ta chuyển sang học trực tuyến nhiều hơn, đây là một số mẹo để giúp học sinh thành công:\n\n1. **Tạo không gian học tập riêng** - Tìm một nơi yên tĩnh, thoải mái nơi bạn có thể tập trung.\n\n2. **Thiết lập thói quen** - Đặt giờ học cố định và tuân thủ chúng.\n\n3. **Nghỉ giải lao** - Sử dụng các kỹ thuật như phương pháp Pomodoro (25 phút làm việc sau đó nghỉ 5 phút).\n\n4. **Giữ tổ chức** - Sử dụng lịch kỹ thuật số và danh sách việc cần làm để theo dõi bài tập và thời hạn.\n\n5. **Tham gia tích cực** - Tham gia vào các cuộc thảo luận trực tuyến và đặt câu hỏi khi cần giúp đỡ.\n\nBạn thấy chiến lược nào hiệu quả nhất cho việc học trực tuyến? Hãy chia sẻ trong phần bình luận!");
        blog2.setImageUrl(
                "https://images.unsplash.com/photo-1498243691581-b145c3f54a5a?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
        blog2.setThumbnailUrl(
                "https://images.unsplash.com/photo-1498243691581-b145c3f54a5a?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
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

        Blog blog3 = new Blog();
        // Blog 3 - Draft by Manageri Sắp Ra Mắt Cho Học Kỳ Tới
        blog3.setTitle("Tính Năng Mới Sắp Ra Mắt Cho Học Kỳ Tới");
        blog3.setDescription(
                "Chúng tôi đang làm việc trên một số tính năng thú vị sẽ được phát hành trong học kỳ tới. Những cải tiến này dựa trên phản hồi từ học sinh và giáo viên.\n\n**Sắp Ra Mắt:**\n\n- Hệ thống tin nhắn cải tiến\n- Công cụ cộng tác thời gian thực\n- Ứng dụng di động cho iOS và Android\n- Tích hợp với các công cụ giáo dục phổ biến\n- Phân tích nâng cao cho giáo viên\n\nBài viết này vẫn đang là bản nháp và sẽ được cập nhật với nhiều chi tiết hơn trước khi xuất bản.");
        blog3.setThumbnailUrl("https://i1.sndcdn.com/artworks-000473680527-kz21lf-t1080x1080.jpg");
        blog3.setTags("tính năng, sắp ra mắt, cải tiến");
        blog3.setAuthor(manager);
        blog3.setIsPublished(false);
        blog3.setStatus("draft");
        blog3.setLastEditedDate(now.minusDays(2));
        blog3.setLastEditedBy(manager);
        blogRepository.save(blog3);

        Blog blog4 = new Blog();
        // Blog 4 - Published by Manager with image and video Tàng Thế Giới
        blog4.setTitle("Chuyến Tham Quan Ảo: Khám Phá Các Bảo Tàng Thế Giới");
        blog4.setDescription(
                "Hôm nay chúng ta sẽ thực hiện một chuyến tham quan ảo đến một số bảo tàng nổi tiếng nhất thế giới cung cấp các tour trực tuyến.\n\nNhiều bảo tàng uy tín cung cấp các tour ảo cho phép bạn khám phá bộ sưu tập của họ từ sự thoải mái của ngôi nhà. Đây là một nguồn tài nguyên giáo dục tuyệt vời cho nghệ thuật, lịch sử và nghiên cứu văn hóa.\n\n**Các bảo tàng được giới thiệu trong video:**\n\n- Bảo tàng Louvre, Paris\n- Bảo tàng Anh, London\n- Bảo tàng Nghệ thuật Metropolitan, New York\n- Bảo tàng Vatican, Rome\n- Bảo tàng Nghệ thuật Hiện đại và Đương đại Quốc gia, Seoul\n\nVideo đính kèm cung cấp một tour có hướng dẫn của các bảo tàng này. Chúng tôi hy vọng chuyến tham quan ảo này sẽ truyền cảm hứng cho học sinh tìm hiểu thêm về nghệ thuật và lịch sử!");
        blog4.setImageUrl(
                "https://images.unsplash.com/photo-1515169273894-7e876dcf13da?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
        blog4.setThumbnailUrl(
                "https://images.unsplash.com/photo-1515169273894-7e876dcf13da?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
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

        Blog blog5 = new Blog();
        // Blog 5 - Published by Student (if they can create blogs)t Đầu Đến Nâng Cao
        blog5.setTitle("Hành Trình Học Tập Của Tôi: Từ Người Mới Bắt Đầu Đến Nâng Cao");
        blog5.setDescription(
                "Xin chào mọi người! Tôi muốn chia sẻ hành trình học tập cá nhân của mình và một số hiểu biết có thể giúp ích cho các học sinh khác.\n\n"
                        +
                        "**Trải Nghiệm Của Tôi:**\n\n" +
                        "Khi tôi lần đầu sử dụng nền tảng này, tôi cảm thấy choáng ngợp bởi tất cả các tính năng. Nhưng dần dần, tôi khám phá ra cách mỗi công cụ có thể giúp tôi học tốt hơn.\n\n"
                        +
                        "**Những Bài Học Chính:**\n\n" +
                        "1. **Tính nhất quán là chìa khóa** - Học một chút mỗi ngày tốt hơn là nhồi nhét\n" +
                        "2. **Đặt câu hỏi** - Đừng ngần ngại hỏi giáo viên hoặc bạn học để được giúp đỡ\n" +
                        "3. **Sử dụng tất cả tài nguyên** - Tận dụng bài giảng, bài tập và diễn đàn thảo luận\n" +
                        "4. **Theo dõi tiến độ** - Giám sát kết quả học tập để xác định các lĩnh vực cần cải thiện\n" +
                        "5. **Kết nối** - Tham gia vào cộng đồng thông qua tin nhắn và thông báo\n\n" +
                        "**Lời Khuyên Cho Học Sinh Mới:**\n\n" +
                        "- Bắt đầu với những điều cơ bản và dần dần khám phá các tính năng nâng cao\n" +
                        "- Thiết lập lịch học phù hợp với bạn\n" +
                        "- Đừng sợ mắc lỗi - đó là cách chúng ta học!\n" +
                        "- Kết nối với các học sinh khác có cùng sở thích\n\n" +
                        "Tôi hy vọng điều này sẽ giúp ích cho ai đó trong hành trình học tập của họ!");
        blog5.setImageUrl(
                "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80");
        blog5.setThumbnailUrl(
                "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60");
        blog5.setTags("trải nghiệm học sinh, mẹo học tập, động lực, cộng đồng");
        blog5.setAuthor(users.get(0)); // Student user
        blog5.setIsPublished(true);
        blog5.setStatus("published");
        blog5.setPublishedDate(now.minusHours(12));
        blog5.setLastEditedDate(now.minusHours(12));
        blog5.setLastEditedBy(users.get(0));
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

        Accomplishment math = new Accomplishment();
        // Create sample accomplishments
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
        teacherForm.setCvFileData(
                "U2FtcGxlIENWIGZpbGUgY29udGVudC4gSW4gcmVhbCBpbXBsZW1lbnRhdGlvbiwgdGhpcyB3b3VsZCBiZSBhIGJhc2U2NCBlbmNvZGVkIHN0cmluZyBvZiBhIFBERiBmaWxlLg=="); // Sample
                                                                                                                                                             // base64
                                                                                                                                                             // data
        teacherForm.setCvFileUrl("/files/teachers/nguyen_van_a_cv.pdf"); // This would be set by the service after
                                                                         // upload
        teacherForm.setAdditionalInfo(
                "Tôi đã có 5 năm kinh nghiệm giảng dạy Toán cấp trung học. Tôi từng làm việc tại trường THPT Chu Văn An và là giáo viên dạy thêm tại nhiều trung tâm luyện thi.");

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

    /**
     * Create sample classrooms for testing
     * 
     * @param users List of users for assigning teachers
     * @return List of created classrooms
     */
    private List<Classroom> CreateSampleClassrooms(List<User> users) {
        // Get teacher users
        User teacher1 = users.get(1);  // teacher user
        User teacher2 = users.get(8);  // Dr. Sarah Williams
        User teacher3 = users.get(9);  // Prof. Michael Brown
        User admin = users.get(3);     // admin user (can also teach)
        
        // Classroom 1 - Mathematics
        Classroom mathClass = new Classroom();
        mathClass.setName("Advanced Mathematics");
        mathClass.setDescription("Advanced mathematics course covering calculus, algebra, and geometry. This course is designed for students who want to deepen their understanding of mathematical concepts and prepare for higher education.");
        mathClass.setSection("A");
        mathClass.setSubject("Mathematics");
        mathClass.setTeacher(teacher1);
        mathClass.setStudents(new HashSet<>());
        classroomRepository.save(mathClass);
        
        // Classroom 2 - Physics
        Classroom physicsClass = new Classroom();
        physicsClass.setName("Classical Physics");
        physicsClass.setDescription("Introduction to classical physics including mechanics, thermodynamics, and electromagnetism. Students will learn fundamental principles through theory and hands-on experiments.");
        physicsClass.setSection("B");
        physicsClass.setSubject("Physics");
        physicsClass.setTeacher(teacher2);
        physicsClass.setStudents(new HashSet<>());
        classroomRepository.save(physicsClass);
        
        // Classroom 3 - Computer Science
        Classroom csClass = new Classroom();
        csClass.setName("Java Programming");
        csClass.setDescription("Comprehensive Java programming course covering object-oriented programming, data structures, and software development best practices. Perfect for beginners and intermediate students.");
        csClass.setSection("C");
        csClass.setSubject("Computer Science");
        csClass.setTeacher(admin); // Admin teaching this class
        csClass.setStudents(new HashSet<>());
        classroomRepository.save(csClass);
        
        // Classroom 4 - English Literature
        Classroom englishClass = new Classroom();
        englishClass.setName("English Literature");
        englishClass.setDescription("Exploration of classic and contemporary English literature. Students will analyze various literary works, improve their writing skills, and develop critical thinking abilities.");
        englishClass.setSection("D");
        englishClass.setSubject("English");
        englishClass.setTeacher(teacher3);
        englishClass.setStudents(new HashSet<>());
        classroomRepository.save(englishClass);
        
        System.out.println("✅ Created 4 sample classrooms");
        return Arrays.asList(mathClass, physicsClass, csClass, englishClass);
    }

    /**
     * Create classroom enrollments for students
     * 
     * @param users List of users
     * @param classrooms List of classrooms
     */
    private void CreateClassroomEnrollments(List<User> users, List<Classroom> classrooms) {
        // Get all student users (indices 0, 4, 5, 6, 7)
        User student1 = users.get(0);  // student
        User student2 = users.get(4);  // Alice Johnson
        User student3 = users.get(5);  // Bob Wilson  
        User student4 = users.get(6);  // Carol Davis
        User student5 = users.get(7);  // David Chen
        
        List<User> students = Arrays.asList(student1, student2, student3, student4, student5);
        
        // Enroll all students in Math class (everyone needs math)
        for (User student : students) {
            ClassroomEnrollment mathEnrollment = new ClassroomEnrollment();
            mathEnrollment.setClassroom(classrooms.get(0)); // Math class
            mathEnrollment.setUser(student);
            classroomEnrollmentRepository.save(mathEnrollment);
        }
        
        // Enroll most students in Physics (science track)
        for (int i = 0; i < 4; i++) {
            ClassroomEnrollment physicsEnrollment = new ClassroomEnrollment();
            physicsEnrollment.setClassroom(classrooms.get(1)); // Physics class
            physicsEnrollment.setUser(students.get(i));
            classroomEnrollmentRepository.save(physicsEnrollment);
        }
        
        // Enroll some students in Java Programming (CS track)
        for (int i = 0; i < 3; i++) {
            ClassroomEnrollment javaEnrollment = new ClassroomEnrollment();
            javaEnrollment.setClassroom(classrooms.get(2)); // Java class
            javaEnrollment.setUser(students.get(i));
            classroomEnrollmentRepository.save(javaEnrollment);
        }
        
        // Enroll all students in English Literature (required)
        for (User student : students) {
            ClassroomEnrollment englishEnrollment = new ClassroomEnrollment();
            englishEnrollment.setClassroom(classrooms.get(3)); // English class
            englishEnrollment.setUser(student);
            classroomEnrollmentRepository.save(englishEnrollment);
        }
        
        // Enroll some students in Chemistry (if we have more classrooms)
        if (classrooms.size() > 4) {
            for (int i = 1; i < 4; i++) {
                ClassroomEnrollment chemEnrollment = new ClassroomEnrollment();
                chemEnrollment.setClassroom(classrooms.get(4)); // Chemistry class
                chemEnrollment.setUser(students.get(i));
                classroomEnrollmentRepository.save(chemEnrollment);
            }
        }
        
        // Enroll some students in History (if we have more classrooms)
        if (classrooms.size() > 5) {
            for (int i = 2; i < 5; i++) {
                ClassroomEnrollment historyEnrollment = new ClassroomEnrollment();
                historyEnrollment.setClassroom(classrooms.get(5)); // History class
                historyEnrollment.setUser(students.get(i));
                classroomEnrollmentRepository.save(historyEnrollment);
            }
        }
        
        System.out.println("✅ Created comprehensive classroom enrollments for " + students.size() + " students across " + classrooms.size() + " classrooms");
    }

    /**
     * Create sample messages between users
     * 
     * @param users List of users     */    private void CreateSampleMessages(List<User> users) {
        try {
            // Check if we have enough users
            if (users == null || users.size() < 6) {
                System.out.println("⚠️ Not enough users to create sample messages. Need at least 6 users.");
                return;
            }
            
            System.out.println("Creating sample messages with users:");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                String roleName = "Unknown";
                switch(user.getRoleId()) {
                    case 1: roleName = "STUDENT"; break;
                    case 2: roleName = "TEACHER"; break; 
                    case 3: roleName = "MANAGER"; break;
                    case 4: roleName = "ADMIN"; break;
                    case 55: roleName = "ACCOUNTANT"; break;
                }
                System.out.println("User " + i + ": " + user.getUsername() + " (ID: " + user.getId() + ", Role: " + roleName + ")");
            }
            
            User teacher = users.get(1); // teacher user - ID should be 2
            User student1 = users.get(0); // student user - ID should be 1
            User student2 = users.get(4); // student2 - ID should be 5
            User student3 = users.get(5); // student3 - ID should be 6
            
            System.out.println("Selected teacher: " + teacher.getUsername() + " (ID: " + teacher.getId() + ")");
            System.out.println("Selected student1: " + student1.getUsername() + " (ID: " + student1.getId() + ")");
            System.out.println("Selected student2: " + student2.getUsername() + " (ID: " + student2.getId() + ")");
            System.out.println("Selected student3: " + student3.getUsername() + " (ID: " + student3.getId() + ")");
              
            // Create conversations between teacher and each student
            
            // Conversation 1: Teacher and Student1
            // Message 1: Student asks teacher about assignment
            StudentMessage msg1 = new StudentMessage();
            msg1.setSender(student1);
            msg1.setRecipient(teacher);
            msg1.setSubject("Question about Math Assignment");
            msg1.setContent("Hi Teacher, I have a question about the math assignment due next week. Could you please clarify the requirements for problem 3?");
            msg1.setPriority("MEDIUM");
            msg1.setStatus("DELIVERED");
            StudentMessage savedMsg1 = studentMessageRepository.save(msg1);
            System.out.println("Saved message 1: ID=" + savedMsg1.getId() + ", From=" + savedMsg1.getSender().getId() + " (" + savedMsg1.getSender().getFullName() + "), To=" + savedMsg1.getRecipient().getId() + " (" + savedMsg1.getRecipient().getFullName() + ")");
            
            // Message 2: Teacher responds to student
            StudentMessage msg2 = new StudentMessage();
            msg2.setSender(teacher);
            msg2.setRecipient(student1);
            msg2.setSubject("Re: Question about Math Assignment");
            msg2.setContent("Hello! For problem 3, please make sure to show all your work step by step. Focus on the algebraic manipulation we covered in class last Tuesday.");
            msg2.setPriority("MEDIUM");
            msg2.setStatus("DELIVERED");
            StudentMessage savedMsg2 = studentMessageRepository.save(msg2);
            System.out.println("Saved message 2: ID=" + savedMsg2.getId() + ", From=" + savedMsg2.getSender().getId() + " (" + savedMsg2.getSender().getFullName() + "), To=" + savedMsg2.getRecipient().getId() + " (" + savedMsg2.getRecipient().getFullName() + ")");
            
            // Message 3: Student follows up
            StudentMessage msg3 = new StudentMessage();
            msg3.setSender(student1);
            msg3.setRecipient(teacher);
            msg3.setSubject("Re: Question about Math Assignment");
            msg3.setContent("Thank you for clarifying! I'll focus on showing the steps clearly.");
            msg3.setPriority("MEDIUM");
            msg3.setStatus("DELIVERED");
            StudentMessage savedMsg3 = studentMessageRepository.save(msg3);
            System.out.println("Saved message 3: ID=" + savedMsg3.getId() + ", From=" + savedMsg3.getSender().getId() + " (" + savedMsg3.getSender().getFullName() + "), To=" + savedMsg3.getRecipient().getId() + " (" + savedMsg3.getRecipient().getFullName() + ")");
            
            // Conversation 2: Teacher and Student2
            // Message 4: Student asks about schedule
            StudentMessage msg4 = new StudentMessage();
            msg4.setSender(student2);
            msg4.setRecipient(teacher);
            msg4.setSubject("Schedule Change Request");
            msg4.setContent("Dear Teacher, I won't be able to attend the class this Friday due to a medical appointment. Is there any makeup session available?");
            msg4.setPriority("HIGH");
            msg4.setStatus("DELIVERED");
            StudentMessage savedMsg4 = studentMessageRepository.save(msg4);
            System.out.println("Saved message 4: ID=" + savedMsg4.getId() + ", From=" + savedMsg4.getSender().getId() + " (" + savedMsg4.getSender().getFullName() + "), To=" + savedMsg4.getRecipient().getId() + " (" + savedMsg4.getRecipient().getFullName() + ")");
            
            // Message 5: Teacher responds about schedule
            StudentMessage msg5 = new StudentMessage();
            msg5.setSender(teacher);
            msg5.setRecipient(student2);
            msg5.setSubject("Re: Schedule Change Request");
            msg5.setContent("Yes, we can arrange a makeup session on Monday afternoon. Please let me know if that works for you.");
            msg5.setPriority("HIGH");
            msg5.setStatus("DELIVERED");
            StudentMessage savedMsg5 = studentMessageRepository.save(msg5);
            System.out.println("Saved message 5: ID=" + savedMsg5.getId() + ", From=" + savedMsg5.getSender().getId() + " (" + savedMsg5.getSender().getFullName() + "), To=" + savedMsg5.getRecipient().getId() + " (" + savedMsg5.getRecipient().getFullName() + ")");
            
            // Conversation 3: Teacher and Student3
            // Message 6: Teacher sends announcement
            StudentMessage msg6 = new StudentMessage();
            msg6.setSender(teacher);
            msg6.setRecipient(student3);
            msg6.setSubject("Test Reminder");
            msg6.setContent("This is a reminder that we have our midterm test next Monday. Please review chapters 1-5. Good luck!");
            msg6.setPriority("HIGH");
            msg6.setStatus("DELIVERED");
            StudentMessage savedMsg6 = studentMessageRepository.save(msg6);
            System.out.println("Saved message 6: ID=" + savedMsg6.getId() + ", From=" + savedMsg6.getSender().getId() + " (" + savedMsg6.getSender().getFullName() + "), To=" + savedMsg6.getRecipient().getId() + " (" + savedMsg6.getRecipient().getFullName() + ")");
            
            // Message 7: Student acknowledges
            StudentMessage msg7 = new StudentMessage();
            msg7.setSender(student3);
            msg7.setRecipient(teacher);
            msg7.setSubject("Re: Test Reminder");
            msg7.setContent("Thank you for the reminder. I'll make sure to prepare well for the test.");
            msg7.setPriority("MEDIUM");
            msg7.setStatus("DELIVERED");
            StudentMessage savedMsg7 = studentMessageRepository.save(msg7);
            System.out.println("Saved message 7: ID=" + savedMsg7.getId() + ", From=" + savedMsg7.getSender().getId() + " (" + savedMsg7.getSender().getFullName() + "), To=" + savedMsg7.getRecipient().getId() + " (" + savedMsg7.getRecipient().getFullName() + ")");
            
            // Count all messages to verify
            System.out.println("Total message count in database: " + studentMessageRepository.count());
            
            // Verify conversations
            for (User student : Arrays.asList(student1, student2, student3)) {
                List<StudentMessage> conversation = studentMessageRepository.findConversation(teacher, student);
                System.out.println("Conversation between teacher " + teacher.getId() + " and student " + student.getId() + 
                                   ": " + conversation.size() + " messages");
            }
            
            System.out.println("✅ Created sample messages between teacher and students");
        } catch (Exception e) {
            System.err.println("❌ Error creating sample messages: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the entire application startup, just skip message creation
        }
    }
    
    /**
     * Create sample schedules for classrooms
     * 
     * @param users List of users
     * @param classrooms List of classrooms
     */
    private void CreateSampleSchedules(List<User> users, List<Classroom> classrooms) {
        User teacher1 = users.get(1);  // teacher user
        User teacher2 = users.get(8);  // Dr. Sarah Williams
        User teacher3 = users.get(9);  // Prof. Michael Brown
        User admin = users.get(3);     // admin user
        
        // Schedule 1: Mathematics - Monday 8:00-9:30
        Schedule mathSchedule = new Schedule();
        mathSchedule.setTeacher(teacher1);
        mathSchedule.setClassroom(classrooms.get(0)); // Math class
        mathSchedule.setDayOfWeek(0); // Monday
        mathSchedule.setStartTime(LocalTime.of(8, 0));
        mathSchedule.setEndTime(LocalTime.of(9, 30));
        mathSchedule.setRoom("Room 101");
        mathSchedule.setSubject("Advanced Mathematics");
        mathSchedule.setMaterialsUrl("https://drive.google.com/folder/math-materials");
        mathSchedule.setMeetUrl("https://meet.google.com/math-class");
        scheduleRepository.save(mathSchedule);
        
        // Schedule 2: Mathematics - Wednesday 8:00-9:30
        Schedule mathSchedule2 = new Schedule();
        mathSchedule2.setTeacher(teacher1);
        mathSchedule2.setClassroom(classrooms.get(0)); // Math class
        mathSchedule2.setDayOfWeek(2); // Wednesday
        mathSchedule2.setStartTime(LocalTime.of(8, 0));
        mathSchedule2.setEndTime(LocalTime.of(9, 30));
        mathSchedule2.setRoom("Room 101");
        mathSchedule2.setSubject("Advanced Mathematics");
        mathSchedule2.setMaterialsUrl("https://drive.google.com/folder/math-materials");
        mathSchedule2.setMeetUrl("https://meet.google.com/math-class");
        scheduleRepository.save(mathSchedule2);
        
        // Schedule 3: Physics - Tuesday 10:00-11:30
        Schedule physicsSchedule = new Schedule();
        physicsSchedule.setTeacher(teacher2);
        physicsSchedule.setClassroom(classrooms.get(1)); // Physics class
        physicsSchedule.setDayOfWeek(1); // Tuesday
        physicsSchedule.setStartTime(LocalTime.of(10, 0));
        physicsSchedule.setEndTime(LocalTime.of(11, 30));
        physicsSchedule.setRoom("Lab 201");
        physicsSchedule.setSubject("Classical Physics");
        physicsSchedule.setMaterialsUrl("https://drive.google.com/folder/physics-materials");
        physicsSchedule.setMeetUrl("https://meet.google.com/physics-class");
        scheduleRepository.save(physicsSchedule);
        
        // Schedule 4: Physics - Thursday 10:00-11:30
        Schedule physicsSchedule2 = new Schedule();
        physicsSchedule2.setTeacher(teacher2);
        physicsSchedule2.setClassroom(classrooms.get(1)); // Physics class
        physicsSchedule2.setDayOfWeek(3); // Thursday
        physicsSchedule2.setStartTime(LocalTime.of(10, 0));
        physicsSchedule2.setEndTime(LocalTime.of(11, 30));
        physicsSchedule2.setRoom("Lab 201");
        physicsSchedule2.setSubject("Classical Physics");
        physicsSchedule2.setMaterialsUrl("https://drive.google.com/folder/physics-materials");
        physicsSchedule2.setMeetUrl("https://meet.google.com/physics-class");
        scheduleRepository.save(physicsSchedule2);
        
        // Schedule 5: Java Programming - Friday 13:00-14:30
        Schedule csSchedule = new Schedule();
        csSchedule.setTeacher(admin);
        csSchedule.setClassroom(classrooms.get(2)); // CS class
        csSchedule.setDayOfWeek(4); // Friday
        csSchedule.setStartTime(LocalTime.of(13, 0));
        csSchedule.setEndTime(LocalTime.of(14, 30));
        csSchedule.setRoom("Computer Lab 301");
        csSchedule.setSubject("Java Programming");
        csSchedule.setMaterialsUrl("https://drive.google.com/folder/java-materials");
        csSchedule.setMeetUrl("https://meet.google.com/java-class");
        scheduleRepository.save(csSchedule);
        
        // Schedule 6: English Literature - Monday 14:00-15:30
        Schedule englishSchedule = new Schedule();
        englishSchedule.setTeacher(teacher3);
        englishSchedule.setClassroom(classrooms.get(3)); // English class
        englishSchedule.setDayOfWeek(0); // Monday
        englishSchedule.setStartTime(LocalTime.of(14, 0));
        englishSchedule.setEndTime(LocalTime.of(15, 30));
        englishSchedule.setRoom("Room 105");
        englishSchedule.setSubject("English Literature");
        englishSchedule.setMaterialsUrl("https://drive.google.com/folder/english-materials");
        englishSchedule.setMeetUrl("https://meet.google.com/english-class");
        scheduleRepository.save(englishSchedule);
        
        System.out.println("✅ Created 6 sample schedules");
    }
    
    /**
     * Create sample assignments for classrooms
     * 
     * @param classrooms List of classrooms
     */
    private void CreateSampleAssignments(List<Classroom> classrooms) {
        LocalDateTime now = LocalDateTime.now();
        
        // Assignment 1: Math - Due in 1 week
        Assignment mathAssignment1 = new Assignment();
        mathAssignment1.setTitle("Calculus Problem Set 1");
        mathAssignment1.setDescription("Complete the calculus problems from chapter 3. Show all work and include step-by-step solutions. Focus on limits, derivatives, and their applications.\n\nSubmission requirements:\n- Handwritten or typed solutions\n- Include graphs where applicable\n- Explain your reasoning for each problem");
        mathAssignment1.setDueDate(now.plusDays(7));
        mathAssignment1.setPoints(100);
        mathAssignment1.setFileAttachmentUrl("https://drive.google.com/file/calculus-problems-ch3.pdf");
        mathAssignment1.setClassroom(classrooms.get(0));
        assignmentRepository.save(mathAssignment1);
        
        // Assignment 2: Math - Due in 2 weeks
        Assignment mathAssignment2 = new Assignment();
        mathAssignment2.setTitle("Algebraic Equations Quiz");
        mathAssignment2.setDescription("Online quiz covering algebraic equations and systems of equations. You will have 60 minutes to complete 20 questions.\n\nTopics covered:\n- Linear equations\n- Quadratic equations\n- Systems of equations\n- Word problems");
        mathAssignment2.setDueDate(now.plusDays(14));
        mathAssignment2.setPoints(50);
        mathAssignment2.setClassroom(classrooms.get(0));
        assignmentRepository.save(mathAssignment2);
        
        // Assignment 3: Physics - Lab Report
        Assignment physicsAssignment1 = new Assignment();
        physicsAssignment1.setTitle("Pendulum Motion Lab Report");
        physicsAssignment1.setDescription("Write a comprehensive lab report on the pendulum motion experiment we conducted in class. Your report should include:\n\n1. Introduction and hypothesis\n2. Experimental procedure\n3. Data collection and analysis\n4. Graphs and calculations\n5. Conclusion and sources of error\n\nFormat: 3-5 pages, double-spaced, 12pt font");
        physicsAssignment1.setDueDate(now.plusDays(10));
        physicsAssignment1.setPoints(75);
        physicsAssignment1.setFileAttachmentUrl("https://drive.google.com/file/lab-report-template.docx");
        physicsAssignment1.setClassroom(classrooms.get(1));
        assignmentRepository.save(physicsAssignment1);
        
        // Assignment 4: Physics - Problem solving
        Assignment physicsAssignment2 = new Assignment();
        physicsAssignment2.setTitle("Thermodynamics Problem Set");
        physicsAssignment2.setDescription("Solve the thermodynamics problems from chapter 8. Focus on heat transfer, thermal equilibrium, and the laws of thermodynamics.\n\nProblems 8.1 through 8.15\nShow all calculations and include proper units");
        physicsAssignment2.setDueDate(now.plusDays(12));
        physicsAssignment2.setPoints(80);
        physicsAssignment2.setClassroom(classrooms.get(1));
        assignmentRepository.save(physicsAssignment2);
        
        // Assignment 5: Java Programming - Project
        Assignment csAssignment1 = new Assignment();
        csAssignment1.setTitle("Java OOP Project - Library Management System");
        csAssignment1.setDescription("Create a library management system using Java object-oriented programming principles.\n\nRequirements:\n- Create classes for Book, Member, and Library\n- Implement methods for adding/removing books\n- Implement member registration and book borrowing\n- Use inheritance and encapsulation\n- Include proper documentation\n- Submit source code and demo video\n\nDeadline is flexible but recommended completion in 3 weeks.");
        csAssignment1.setDueDate(now.plusDays(21));
        csAssignment1.setPoints(150);
        csAssignment1.setFileAttachmentUrl("https://drive.google.com/file/java-project-requirements.pdf");
        csAssignment1.setClassroom(classrooms.get(2));
        assignmentRepository.save(csAssignment1);
        
        // Assignment 6: Java Programming - Quiz
        Assignment csAssignment2 = new Assignment();
        csAssignment2.setTitle("Java Fundamentals Quiz");
        csAssignment2.setDescription("Online quiz covering Java fundamentals including variables, data types, control structures, and basic OOP concepts.\n\n30 multiple choice questions\n45 minutes time limit\nOpen book allowed");
        csAssignment2.setDueDate(now.plusDays(5));
        csAssignment2.setPoints(60);
        csAssignment2.setClassroom(classrooms.get(2));
        assignmentRepository.save(csAssignment2);
        
        // Assignment 7: English Literature - Essay
        Assignment englishAssignment1 = new Assignment();
        englishAssignment1.setTitle("Character Analysis Essay - Hamlet");
        englishAssignment1.setDescription("Write a 5-page character analysis essay on Hamlet, focusing on his psychological development throughout the play.\n\nRequirements:\n- Thesis statement and clear argument\n- Use of textual evidence and quotes\n- MLA format\n- Minimum 5 scholarly sources\n- Double-spaced, 12pt Times New Roman font\n\nSubmit through the online portal as a PDF");
        englishAssignment1.setDueDate(now.plusDays(14));
        englishAssignment1.setPoints(120);
        englishAssignment1.setFileAttachmentUrl("https://drive.google.com/file/essay-guidelines.pdf");
        englishAssignment1.setClassroom(classrooms.get(3));
        assignmentRepository.save(englishAssignment1);
        
        // Assignment 8: English Literature - Reading Comprehension
        Assignment englishAssignment2 = new Assignment();
        englishAssignment2.setTitle("Poetry Analysis - Romantic Period");
        englishAssignment2.setDescription("Analyze three poems from the Romantic period (Wordsworth, Coleridge, or Byron). Compare themes, literary devices, and historical context.\n\n2-3 pages per poem analysis\nInclude discussion of:\n- Imagery and symbolism\n- Meter and rhyme scheme\n- Historical and biographical context\n- Personal interpretation");
        englishAssignment2.setDueDate(now.plusDays(8));
        englishAssignment2.setPoints(90);
        englishAssignment2.setClassroom(classrooms.get(3));
        assignmentRepository.save(englishAssignment2);
        
        System.out.println("✅ Created 8 sample assignments across all classrooms");
    }
    
    /**
     * Create sample submissions for assignments
     * 
     * @param users List of users
     */
    private void CreateSampleSubmissions(List<User> users) {
        // Get all assignments that were just created
        List<Assignment> assignments = assignmentRepository.findAll();
        
        if (assignments.isEmpty()) {
            System.out.println("⚠️ No assignments found, skipping submission creation");
            return;
        }
        
        // Get student users (first 5 users are: student, teacher, manager, admin, student2)
        User student1 = users.get(0); // student user
        User student2 = users.get(4); // student2 user
        User student3 = users.get(5); // student3 user
        User student4 = users.get(6); // student4 user
        User student5 = users.get(7); // student5 user
        
        LocalDateTime now = LocalDateTime.now();
        
        // Create some sample submissions
        int submissionCount = 0;
        
        for (int i = 0; i < Math.min(assignments.size(), 6); i++) {
            Assignment assignment = assignments.get(i);
            
            // Create submission for student1 (first student)
            Submission submission1 = new Submission();
            submission1.setAssignment(assignment);
            submission1.setStudent(student1);
            submission1.setComment("Sample submission for " + assignment.getTitle() + " by " + student1.getFullName());
            submission1.setFileSubmissionUrl("https://drive.google.com/file/sample-submission-" + (i + 1) + "-student1.pdf");
            submission1.setSubmittedAt(now.minusDays(i + 1));
            submission1.setScore((int)(85.0 + (i * 2))); // Varying grades
            submission1.setFeedback("Good work! Consider improving the analysis section.");
            submissionRepository.save(submission1);
            submissionCount++;
            
            // Create submission for student2 (every other assignment)
            if (i % 2 == 0) {
                Submission submission2 = new Submission();
                submission2.setAssignment(assignment);
                submission2.setStudent(student2);
                submission2.setComment("Sample submission for " + assignment.getTitle() + " by " + student2.getFullName());
                submission2.setFileSubmissionUrl("https://drive.google.com/file/sample-submission-" + (i + 1) + "-student2.pdf");
                submission2.setSubmittedAt(now.minusDays(i + 2));
                submission2.setScore((int)(78.0 + (i * 3))); // Different grades
                submission2.setFeedback("Well structured submission. Nice use of examples.");
                submissionRepository.save(submission2);
                submissionCount++;
            }
            
            // Create submission for student3 (first 3 assignments)
            if (i < 3) {
                Submission submission3 = new Submission();
                submission3.setAssignment(assignment);
                submission3.setStudent(student3);
                submission3.setComment("Sample submission for " + assignment.getTitle() + " by " + student3.getFullName());
                submission3.setFileSubmissionUrl("https://drive.google.com/file/sample-submission-" + (i + 1) + "-student3.pdf");
                submission3.setSubmittedAt(now.minusDays(i));
                submission3.setScore((int)(92.0 - (i * 2))); // High grades, slightly decreasing
                submission3.setFeedback("Excellent work! Very thorough analysis.");
                submissionRepository.save(submission3);
                submissionCount++;
            }
        }
        
        System.out.println("✅ Created " + submissionCount + " sample submissions across multiple students");
    }
    
    /**
     * Create sample timetable events for testing
     * 
     * @param users List of users
     * @param classrooms List of classrooms
     */
    private void CreateSampleTimetableEvents(List<User> users, List<Classroom> classrooms) {
        User teacher = users.get(1);  // teacher user
        User admin = users.get(3);    // admin user
        
        // Create events for specific week: 2025-06-23 to 2025-06-29
        LocalDateTime weekStart = LocalDateTime.of(2025, 6, 23, 0, 0); // Sunday
        
        // Week 1 Events (Current week: June 23-29, 2025)
        
        // Monday 24/6 - Mathematics Class 8:00-9:30
        TimetableEvent mathMonday = new TimetableEvent();
        mathMonday.setTitle("Advanced Mathematics");
        mathMonday.setDescription("Weekly mathematics class covering calculus and algebra topics");
        mathMonday.setStartDatetime(weekStart.plusDays(1).withHour(8).withMinute(0)); // Monday 24/6
        mathMonday.setEndDatetime(weekStart.plusDays(1).withHour(9).withMinute(30));
        mathMonday.setEventType(TimetableEvent.EventType.CLASS);
        mathMonday.setClassroomId(classrooms.get(0).getId());
        mathMonday.setCreatedBy(teacher.getId());
        mathMonday.setLocation("Room 101");
        mathMonday.setColor("#52c41a");
        timetableEventRepository.save(mathMonday);
        
        // Monday 24/6 - English Literature 14:00-15:30
        TimetableEvent englishMonday = new TimetableEvent();
        englishMonday.setTitle("English Literature");
        englishMonday.setDescription("Analysis of Shakespearean plays and poetry");
        englishMonday.setStartDatetime(weekStart.plusDays(1).withHour(14).withMinute(0)); // Monday 24/6
        englishMonday.setEndDatetime(weekStart.plusDays(1).withHour(15).withMinute(30));
        englishMonday.setEventType(TimetableEvent.EventType.CLASS);
        englishMonday.setClassroomId(classrooms.get(3).getId());
        englishMonday.setCreatedBy(teacher.getId());
        englishMonday.setLocation("Room 105");
        englishMonday.setColor("#1890ff");
        timetableEventRepository.save(englishMonday);
        
        // Tuesday 25/6 - Physics Lab 10:00-11:30
        TimetableEvent physicsTuesday = new TimetableEvent();
        physicsTuesday.setTitle("Classical Physics Lab");
        physicsTuesday.setDescription("Hands-on physics experiments and lab work");
        physicsTuesday.setStartDatetime(weekStart.plusDays(2).withHour(10).withMinute(0)); // Tuesday 25/6
        physicsTuesday.setEndDatetime(weekStart.plusDays(2).withHour(11).withMinute(30));
        physicsTuesday.setEventType(TimetableEvent.EventType.CLASS);
        physicsTuesday.setClassroomId(classrooms.get(1).getId());
        physicsTuesday.setCreatedBy(teacher.getId());
        physicsTuesday.setLocation("Lab 201");
        physicsTuesday.setColor("#722ed1");
        timetableEventRepository.save(physicsTuesday);
        
        // Wednesday 26/6 - Mathematics Class 8:00-9:30
        TimetableEvent mathWednesday = new TimetableEvent();
        mathWednesday.setTitle("Advanced Mathematics");
        mathWednesday.setDescription("Continuation of calculus topics and problem solving");
        mathWednesday.setStartDatetime(weekStart.plusDays(3).withHour(8).withMinute(0)); // Wednesday 26/6
        mathWednesday.setEndDatetime(weekStart.plusDays(3).withHour(9).withMinute(30));
        mathWednesday.setEventType(TimetableEvent.EventType.CLASS);
        mathWednesday.setClassroomId(classrooms.get(0).getId());
        mathWednesday.setCreatedBy(teacher.getId());
        mathWednesday.setLocation("Room 101");
        mathWednesday.setColor("#52c41a");
        timetableEventRepository.save(mathWednesday);
        
        // Thursday 27/6 - Physics Theory 10:00-11:30
        TimetableEvent physicsThursday = new TimetableEvent();
        physicsThursday.setTitle("Classical Physics Theory");
        physicsThursday.setDescription("Theoretical physics concepts and problem solving");
        physicsThursday.setStartDatetime(weekStart.plusDays(4).withHour(10).withMinute(0)); // Thursday 27/6
        physicsThursday.setEndDatetime(weekStart.plusDays(4).withHour(11).withMinute(30));
        physicsThursday.setEventType(TimetableEvent.EventType.CLASS);
        physicsThursday.setClassroomId(classrooms.get(1).getId());
        physicsThursday.setCreatedBy(teacher.getId());
        physicsThursday.setLocation("Lab 201");
        physicsThursday.setColor("#722ed1");
        timetableEventRepository.save(physicsThursday);
        
        // Friday 28/6 - Java Programming 13:00-14:30
        TimetableEvent javaFriday = new TimetableEvent();
        javaFriday.setTitle("Java Programming");
        javaFriday.setDescription("Object-oriented programming concepts and practical coding");
        javaFriday.setStartDatetime(weekStart.plusDays(5).withHour(13).withMinute(0)); // Friday 28/6
        javaFriday.setEndDatetime(weekStart.plusDays(5).withHour(14).withMinute(30));
        javaFriday.setEventType(TimetableEvent.EventType.CLASS);
        javaFriday.setClassroomId(classrooms.get(2).getId());
        javaFriday.setCreatedBy(admin.getId());
        javaFriday.setLocation("Computer Lab 301");
        javaFriday.setColor("#fa8c16");
        timetableEventRepository.save(javaFriday);
        
        // Add some exam events
        
        // Friday - Math Midterm Exam
        TimetableEvent mathExam = new TimetableEvent();
        mathExam.setTitle("Mathematics Midterm Exam");
        mathExam.setDescription("Comprehensive exam covering chapters 1-5 of calculus");
        mathExam.setStartDatetime(weekStart.plusDays(5).withHour(9).withMinute(0));
        mathExam.setEndDatetime(weekStart.plusDays(5).withHour(11).withMinute(0));
        mathExam.setEventType(TimetableEvent.EventType.EXAM);
        mathExam.setClassroomId(classrooms.get(0).getId());
        mathExam.setCreatedBy(teacher.getId());
        mathExam.setLocation("Exam Hall A");
        mathExam.setColor("#f5222d");
        mathExam.setReminderMinutes(60);
        timetableEventRepository.save(mathExam);
        
        // Week 2 Events (Next week)
        LocalDateTime nextWeekStart = weekStart.plusDays(7);
        
        // Monday - Mathematics Class 8:00-9:30
        TimetableEvent mathMondayW2 = new TimetableEvent();
        mathMondayW2.setTitle("Advanced Mathematics");
        mathMondayW2.setDescription("Integration techniques and applications");
        mathMondayW2.setStartDatetime(nextWeekStart.plusDays(1).withHour(8).withMinute(0));
        mathMondayW2.setEndDatetime(nextWeekStart.plusDays(1).withHour(9).withMinute(30));
        mathMondayW2.setEventType(TimetableEvent.EventType.CLASS);
        mathMondayW2.setClassroomId(classrooms.get(0).getId());
        mathMondayW2.setCreatedBy(teacher.getId());
        mathMondayW2.setLocation("Room 101");
        mathMondayW2.setColor("#52c41a");
        timetableEventRepository.save(mathMondayW2);
        
        // Tuesday - Physics Lab 10:00-11:30
        TimetableEvent physicsTuesdayW2 = new TimetableEvent();
        physicsTuesdayW2.setTitle("Classical Physics Lab");
        physicsTuesdayW2.setDescription("Pendulum motion and wave experiments");
        physicsTuesdayW2.setStartDatetime(nextWeekStart.plusDays(2).withHour(10).withMinute(0));
        physicsTuesdayW2.setEndDatetime(nextWeekStart.plusDays(2).withHour(11).withMinute(30));
        physicsTuesdayW2.setEventType(TimetableEvent.EventType.CLASS);
        physicsTuesdayW2.setClassroomId(classrooms.get(1).getId());
        physicsTuesdayW2.setCreatedBy(teacher.getId());
        physicsTuesdayW2.setLocation("Lab 201");
        physicsTuesdayW2.setColor("#722ed1");
        timetableEventRepository.save(physicsTuesdayW2);
        
        // Wednesday - Assignment Due
        TimetableEvent assignmentDue = new TimetableEvent();
        assignmentDue.setTitle("Calculus Problem Set Due");
        assignmentDue.setDescription("Submit completed calculus problem set from chapter 3");
        assignmentDue.setStartDatetime(nextWeekStart.plusDays(3).withHour(23).withMinute(59));
        assignmentDue.setEndDatetime(nextWeekStart.plusDays(3).withHour(23).withMinute(59));
        assignmentDue.setEventType(TimetableEvent.EventType.ASSIGNMENT_DUE);
        assignmentDue.setClassroomId(classrooms.get(0).getId());
        assignmentDue.setCreatedBy(teacher.getId());
        assignmentDue.setLocation("Online Submission");
        assignmentDue.setColor("#faad14");
        assignmentDue.setReminderMinutes(1440); // 24 hours reminder
        timetableEventRepository.save(assignmentDue);
        
        // Thursday - Physics Quiz
        TimetableEvent physicsQuiz = new TimetableEvent();
        physicsQuiz.setTitle("Physics Quiz - Thermodynamics");
        physicsQuiz.setDescription("Short quiz on thermodynamics concepts");
        physicsQuiz.setStartDatetime(nextWeekStart.plusDays(4).withHour(10).withMinute(0));
        physicsQuiz.setEndDatetime(nextWeekStart.plusDays(4).withHour(10).withMinute(30));
        physicsQuiz.setEventType(TimetableEvent.EventType.EXAM);
        physicsQuiz.setClassroomId(classrooms.get(1).getId());
        physicsQuiz.setCreatedBy(teacher.getId());
        physicsQuiz.setLocation("Lab 201");
        physicsQuiz.setColor("#f5222d");
        physicsQuiz.setReminderMinutes(30);
        timetableEventRepository.save(physicsQuiz);
        
        // Friday - Java Programming Project Presentation
        TimetableEvent javaPresentation = new TimetableEvent();
        javaPresentation.setTitle("Java Project Presentations");
        javaPresentation.setDescription("Students present their library management system projects");
        javaPresentation.setStartDatetime(nextWeekStart.plusDays(5).withHour(13).withMinute(0));
        javaPresentation.setEndDatetime(nextWeekStart.plusDays(5).withHour(15).withMinute(0));
        javaPresentation.setEventType(TimetableEvent.EventType.CLASS);
        javaPresentation.setClassroomId(classrooms.get(2).getId());
        javaPresentation.setCreatedBy(admin.getId());
        javaPresentation.setLocation("Computer Lab 301");
        javaPresentation.setColor("#fa8c16");
        timetableEventRepository.save(javaPresentation);
        
        // Week 3 Events (Two weeks from now)
        LocalDateTime week3Start = weekStart.plusDays(14);
        
        // Monday - Parent-Teacher Meeting
        TimetableEvent parentMeeting = new TimetableEvent();
        parentMeeting.setTitle("Parent-Teacher Conference");
        parentMeeting.setDescription("Individual meetings with parents to discuss student progress");
        parentMeeting.setStartDatetime(week3Start.plusDays(1).withHour(15).withMinute(0));
        parentMeeting.setEndDatetime(week3Start.plusDays(1).withHour(18).withMinute(0));
        parentMeeting.setEventType(TimetableEvent.EventType.MEETING);
        parentMeeting.setCreatedBy(teacher.getId());
        parentMeeting.setLocation("Conference Room");
        parentMeeting.setColor("#13c2c2");
        timetableEventRepository.save(parentMeeting);
        
        // Add some weekend events
        
        // Saturday - Study Group (Optional)
        TimetableEvent studyGroup = new TimetableEvent();
        studyGroup.setTitle("Mathematics Study Group");
        studyGroup.setDescription("Optional peer study session for upcoming exam preparation");
        studyGroup.setStartDatetime(weekStart.plusDays(6).withHour(10).withMinute(0));
        studyGroup.setEndDatetime(weekStart.plusDays(6).withHour(12).withMinute(0));
        studyGroup.setEventType(TimetableEvent.EventType.CLASS);
        studyGroup.setCreatedBy(teacher.getId());
        studyGroup.setLocation("Library Study Room");
        studyGroup.setColor("#52c41a");
        timetableEventRepository.save(studyGroup);
        
        System.out.println("✅ Created sample timetable events for multiple weeks with various event types");
    }

    /**
     * Create sample attendance sessions and records
     * @param users List of users
     * @param classrooms List of classrooms
     */
    private void CreateSampleAttendance(List<User> users, List<Classroom> classrooms) {
        try {
            // Get teachers
            User teacher1 = users.get(1);  // teacher user
            User teacher2 = users.get(8);  // Dr. Sarah Williams
            
            // Get all student users (indices 0, 4, 5, 6, 7)
            User student1 = users.get(0);  // student
            User student2 = users.get(4);  // Alice Johnson
            User student3 = users.get(5);  // Bob Wilson  
            User student4 = users.get(6);  // Carol Davis
            User student5 = users.get(7);  // David Chen
            
            List<User> students = Arrays.asList(student1, student2, student3, student4, student5);
            
            // Session 1: Math class - Already completed
            Classroom mathClass = classrooms.get(0);
            AttendanceSession pastMathSession = AttendanceSession.builder()
                .classroom(mathClass)
                .teacher(teacher1)
                .sessionName("Buổi học Cấu trúc dữ liệu")
                .sessionDate(LocalDateTime.now().minusDays(5))
                .startTime(LocalDateTime.now().minusDays(5).withHour(10).withMinute(0))
                .endTime(LocalDateTime.now().minusDays(5).withHour(13).withMinute(0))
                .status(SessionStatus.COMPLETED)
                .sessionType("OFFLINE")
                .title("Cấu trúc dữ liệu và giải thuật")
                .description("Buổi học về cấu trúc dữ liệu cơ bản")
                .isActive(false)
                .locationLatitude(21.028511)
                .locationLongitude(105.804817)
                .locationRadiusMeters(200)
                .createdAt(LocalDateTime.now().minusDays(6))
                .build();
            
            attendanceSessionRepository.save(pastMathSession);
            
            // Session 2: CS class - Active now
            Classroom csClass = classrooms.get(2);
            AttendanceSession activeSession = AttendanceSession.builder()
                .classroom(csClass)
                .teacher(teacher1)
                .sessionName("Buổi học Lập trình Web")
                .sessionDate(LocalDateTime.now())
                .startTime(LocalDateTime.now().withHour(8).withMinute(0))
                .endTime(LocalDateTime.now().withHour(11).withMinute(0))
                .status(SessionStatus.ACTIVE)
                .sessionType("OFFLINE")
                .title("Phát triển web với Spring Boot")
                .description("Buổi học về phát triển web sử dụng Spring Boot framework")
                .isActive(true)
                .locationLatitude(21.028511)
                .locationLongitude(105.804817)
                .locationRadiusMeters(200)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
            
            attendanceSessionRepository.save(activeSession);
            
            // Session 3: Physics class - Upcoming
            Classroom physicsClass = classrooms.get(1);
            AttendanceSession upcomingSession1 = AttendanceSession.builder()
                .classroom(physicsClass)
                .teacher(teacher2)
                .sessionName("Buổi học Cơ sở dữ liệu")
                .sessionDate(LocalDateTime.now().plusDays(1))
                .startTime(LocalDateTime.now().plusDays(1).withHour(13).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0))
                .status(SessionStatus.SCHEDULED)
                .sessionType("OFFLINE")
                .title("Thiết kế cơ sở dữ liệu quan hệ")
                .description("Buổi học về mô hình cơ sở dữ liệu quan hệ và SQL")
                .isActive(false)
                .locationLatitude(21.028511)
                .locationLongitude(105.804817)
                .locationRadiusMeters(200)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
            
            attendanceSessionRepository.save(upcomingSession1);
            
            // Session 4: CS class - Upcoming
            AttendanceSession upcomingSession2 = AttendanceSession.builder()
                .classroom(csClass)
                .teacher(teacher1)
                .sessionName("Buổi học Thiết kế phần mềm")
                .sessionDate(LocalDateTime.now().plusDays(2))
                .startTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(2).withHour(12).withMinute(0))
                .status(SessionStatus.SCHEDULED)
                .sessionType("OFFLINE")
                .title("Nguyên lý SOLID trong thiết kế phần mềm")
                .description("Buổi học về nguyên lý thiết kế SOLID và các mẫu thiết kế")
                .isActive(false)
                .locationLatitude(21.028511)
                .locationLongitude(105.804817)
                .locationRadiusMeters(200)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
            
            attendanceSessionRepository.save(upcomingSession2);
            
            // Session 5: Math class - Past session (OOAD)
            AttendanceSession pastOOADSession = AttendanceSession.builder()
                .classroom(mathClass)
                .teacher(teacher1)
                .sessionName("Buổi học Lập trình hướng đối tượng")
                .sessionDate(LocalDateTime.now().minusDays(10))
                .startTime(LocalDateTime.now().minusDays(10).withHour(9).withMinute(0))
                .endTime(LocalDateTime.now().minusDays(10).withHour(12).withMinute(0))
                .status(SessionStatus.COMPLETED)
                .sessionType("OFFLINE")
                .title("Lập trình hướng đối tượng với Java")
                .description("Giới thiệu về OOP và cách triển khai trong Java")
                .isActive(false)
                .createdAt(LocalDateTime.now().minusDays(12))
                .build();
            
            attendanceSessionRepository.save(pastOOADSession);
            
            // Session 6: Physics class - Past session (Network)
            AttendanceSession pastNetworkSession = AttendanceSession.builder()
                .classroom(physicsClass)
                .teacher(teacher2)
                .sessionName("Buổi học Mạng máy tính")
                .sessionDate(LocalDateTime.now().minusDays(12))
                .startTime(LocalDateTime.now().minusDays(12).withHour(14).withMinute(0))
                .endTime(LocalDateTime.now().minusDays(12).withHour(17).withMinute(0))
                .status(SessionStatus.COMPLETED)
                .sessionType("OFFLINE")
                .title("Kiến trúc mạng và giao thức")
                .description("Mô hình OSI và các giao thức mạng phổ biến")
                .isActive(false)
                .createdAt(LocalDateTime.now().minusDays(13))
                .build();
            
            attendanceSessionRepository.save(pastNetworkSession);
            
            // Create attendance records for past sessions
            // For pastMathSession (data structures)
            for (User student : students) {
                Attendance attendance = new Attendance();
                attendance.setStudent(student);
                attendance.setSession(pastMathSession);
                
                // First 3 students are present, others are absent
                if (students.indexOf(student) < 3) {
                    attendance.setStatus(AttendanceStatus.PRESENT);
                    attendance.setCheckInTime(pastMathSession.getStartTime().plusMinutes(15));
                } else if (students.indexOf(student) == 3) {
                    attendance.setStatus(AttendanceStatus.LATE);
                    attendance.setCheckInTime(pastMathSession.getStartTime().plusMinutes(45));
                } else {
                    attendance.setStatus(AttendanceStatus.ABSENT);
                    attendance.setCheckInTime(null);
                }
                
                attendanceRepository.save(attendance);
            }
            
            // For pastOOADSession
            for (User student : students) {
                Attendance attendance = new Attendance();
                attendance.setStudent(student);
                attendance.setSession(pastOOADSession);
                
                // All students are present except one
                if (students.indexOf(student) != 4) {
                    attendance.setStatus(AttendanceStatus.PRESENT);
                    attendance.setCheckInTime(pastOOADSession.getStartTime().plusMinutes(30));
                } else {
                    attendance.setStatus(AttendanceStatus.ABSENT);
                    attendance.setCheckInTime(null);
                }
                
                attendanceRepository.save(attendance);
            }
            
            // For pastNetworkSession
            for (User student : students.subList(0, 4)) { // Only for students enrolled in physics
                Attendance attendance = new Attendance();
                attendance.setStudent(student);
                attendance.setSession(pastNetworkSession);
                
                if (students.indexOf(student) == 2) {
                    attendance.setStatus(AttendanceStatus.ABSENT);
                    attendance.setCheckInTime(null);
                } else {
                    attendance.setStatus(AttendanceStatus.PRESENT);
                    attendance.setCheckInTime(pastNetworkSession.getStartTime().plusMinutes(10));
                }
                
                attendanceRepository.save(attendance);
            }
            
            // For active session (auto-mark teacher as present)
            Attendance teacherAttendance = new Attendance();
            teacherAttendance.setStudent(teacher1);
            teacherAttendance.setSession(activeSession);
            teacherAttendance.setStatus(AttendanceStatus.PRESENT);
            teacherAttendance.setCheckInTime(activeSession.getStartTime());
            attendanceRepository.save(teacherAttendance);
            
            // Mark some students as present in the active session
            Attendance student1Attendance = new Attendance();
            student1Attendance.setStudent(student1);
            student1Attendance.setSession(activeSession);
            student1Attendance.setStatus(AttendanceStatus.PRESENT);
            student1Attendance.setCheckInTime(LocalDateTime.now().minusMinutes(30));
            attendanceRepository.save(student1Attendance);
            
            System.out.println("✅ Created sample attendance sessions and records");
        } catch (Exception e) {
            System.err.println("❌ Error creating sample attendance data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create sample messages for manager
     * 
     * @param users List of users
     */
    private void CreateManagerMessages(List<User> users) {
        try {
            // Check if we have enough users
            if (users == null || users.size() < 6) {
                System.out.println("⚠️ Not enough users to create manager messages. Need at least 6 users.");
                return;
            }
            
            System.out.println("Creating sample messages for manager...");
            
            // Get manager user
            User manager = null;
            for (User user : users) {
                if (user.getRoleId() == 3) { // MANAGER role
                    manager = user;
                    break;
                }
            }
            
            if (manager == null) {
                System.out.println("⚠️ No manager user found to create messages for.");
                return;
            }
            
            User teacher = users.get(1); // teacher user
            User student = users.get(0); // student user
            User admin = users.get(3); // admin user
            
            // Message 1: Teacher to Manager
            StudentMessage msg1 = new StudentMessage();
            msg1.setSender(teacher);
            msg1.setRecipient(manager);
            msg1.setSubject("Yêu cầu hỗ trợ về lịch dạy");
            msg1.setContent("Kính gửi Ban quản lý,\n\nTôi cần được hỗ trợ về việc điều chỉnh lịch dạy tuần tới do có công việc đột xuất. Tôi phải tham gia một hội thảo vào thứ Ba tuần sau. Mong Ban quản lý xem xét và sắp xếp lại lịch dạy giúp tôi.\n\nTrân trọng cảm ơn.");
            msg1.setPriority("HIGH");
            msg1.setStatus("DELIVERED");
            msg1.setIsRead(false);
            msg1.setCreatedAt(LocalDateTime.now().minusDays(1));
            StudentMessage savedMsg1 = studentMessageRepository.save(msg1);
            
            // Message 2: Student to Manager
            StudentMessage msg2 = new StudentMessage();
            msg2.setSender(student);
            msg2.setRecipient(manager);
            msg2.setSubject("Thắc mắc về học phí");
            msg2.setContent("Kính gửi Ban quản lý,\n\nEm muốn hỏi thông tin về chính sách học phí kỳ tới. Em có được giảm học phí không nếu em đăng ký nhiều khóa học cùng lúc? Và thời hạn đóng học phí là khi nào ạ?\n\nEm xin cảm ơn.");
            msg2.setPriority("MEDIUM");
            msg2.setStatus("DELIVERED");
            msg2.setIsRead(true);
            msg2.setCreatedAt(LocalDateTime.now().minusDays(2));
            StudentMessage savedMsg2 = studentMessageRepository.save(msg2);
            
            // Message 3: Admin to Manager
            StudentMessage msg3 = new StudentMessage();
            msg3.setSender(admin);
            msg3.setRecipient(manager);
            msg3.setSubject("Cập nhật chính sách mới");
            msg3.setContent("Thông báo về việc cập nhật chính sách đánh giá giảng viên. Từ tháng sau, chúng ta sẽ áp dụng quy trình đánh giá mới cho tất cả giảng viên. Vui lòng chuẩn bị các tài liệu liên quan và thông báo cho các giảng viên trong phòng ban của bạn.");
            msg3.setPriority("HIGH");
            msg3.setStatus("DELIVERED");
            msg3.setIsRead(true);
            msg3.setCreatedAt(LocalDateTime.now().minusDays(3));
            StudentMessage savedMsg3 = studentMessageRepository.save(msg3);
            
            System.out.println("✅ Created sample messages for manager: " + studentMessageRepository.count() + " total messages in system");
            
        } catch (Exception e) {
            System.err.println("❌ Error creating sample messages for manager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create sample reports for manager
     */
    private void CreateManagerReports() {
        try {
            System.out.println("Creating sample reports for manager...");
            
            // Create attendance report data
            Map<String, Object> attendanceReport = new HashMap<>();
            attendanceReport.put("reportType", "ATTENDANCE");
            attendanceReport.put("title", "Báo cáo điểm danh tháng 6/2025");
            attendanceReport.put("description", "Tổng hợp tình hình điểm danh của học viên trong tháng 6/2025");
            attendanceReport.put("createdAt", LocalDateTime.now().minusDays(5));
            
            Map<String, Object> attendanceData = new HashMap<>();
            attendanceData.put("totalSessions", 45);
            attendanceData.put("totalStudents", 120);
            attendanceData.put("attendanceRate", 92.5);
            attendanceData.put("absentStudents", 8);
            
            List<Map<String, Object>> classAttendance = new ArrayList<>();
            classAttendance.add(Map.of("className", "Lớp A10", "attendanceRate", 95.2, "studentCount", 25));
            classAttendance.add(Map.of("className", "Lớp A11", "attendanceRate", 91.8, "studentCount", 30));
            classAttendance.add(Map.of("className", "Lớp A12", "attendanceRate", 89.5, "studentCount", 28));
            classAttendance.add(Map.of("className", "Lớp A13", "attendanceRate", 94.0, "studentCount", 22));
            classAttendance.add(Map.of("className", "Lớp B12", "attendanceRate", 90.2, "studentCount", 15));
            
            attendanceData.put("classData", classAttendance);
            attendanceReport.put("data", objectMapper.writeValueAsString(attendanceData));
            
            // Create performance report data
            Map<String, Object> performanceReport = new HashMap<>();
            performanceReport.put("reportType", "PERFORMANCE");
            performanceReport.put("title", "Báo cáo kết quả học tập học kỳ 1");
            performanceReport.put("description", "Tổng hợp kết quả học tập của học viên trong học kỳ 1 năm học 2024-2025");
            performanceReport.put("createdAt", LocalDateTime.now().minusDays(10));
            
            Map<String, Object> performanceData = new HashMap<>();
            performanceData.put("totalStudents", 120);
            performanceData.put("averageScore", 7.8);
            performanceData.put("excellentCount", 32);
            performanceData.put("goodCount", 45);
            performanceData.put("averageCount", 35);
            performanceData.put("belowAverageCount", 8);
            
            List<Map<String, Object>> subjectPerformance = new ArrayList<>();
            subjectPerformance.add(Map.of("subject", "Toán", "averageScore", 7.5, "highestScore", 9.8, "lowestScore", 4.5));
            subjectPerformance.add(Map.of("subject", "Lý", "averageScore", 7.2, "highestScore", 9.5, "lowestScore", 5.0));
            subjectPerformance.add(Map.of("subject", "Hóa", "averageScore", 7.8, "highestScore", 9.7, "lowestScore", 4.8));
            subjectPerformance.add(Map.of("subject", "Văn", "averageScore", 8.1, "highestScore", 9.9, "lowestScore", 5.5));
            subjectPerformance.add(Map.of("subject", "Anh", "averageScore", 8.0, "highestScore", 9.8, "lowestScore", 5.2));
            
            performanceData.put("subjectData", subjectPerformance);
            performanceReport.put("data", objectMapper.writeValueAsString(performanceData));
            
            // Create financial report data
            Map<String, Object> financialReport = new HashMap<>();
            financialReport.put("reportType", "FINANCIAL");
            financialReport.put("title", "Báo cáo tài chính quý 2/2025");
            financialReport.put("description", "Tổng hợp tình hình tài chính quý 2 năm 2025");
            financialReport.put("createdAt", LocalDateTime.now().minusDays(15));
            
            Map<String, Object> financialData = new HashMap<>();
            financialData.put("totalRevenue", 1250000000);
            financialData.put("totalExpense", 950000000);
            financialData.put("profit", 300000000);
            financialData.put("tuitionCollected", 1150000000);
            financialData.put("otherRevenue", 100000000);
            
            List<Map<String, Object>> expenseBreakdown = new ArrayList<>();
            expenseBreakdown.add(Map.of("category", "Lương giáo viên", "amount", 650000000, "percentage", 68.4));
            expenseBreakdown.add(Map.of("category", "Cơ sở vật chất", "amount", 150000000, "percentage", 15.8));
            expenseBreakdown.add(Map.of("category", "Học liệu", "amount", 80000000, "percentage", 8.4));
            expenseBreakdown.add(Map.of("category", "Chi phí hành chính", "amount", 70000000, "percentage", 7.4));
            
            financialData.put("expenseBreakdown", expenseBreakdown);
            financialReport.put("data", objectMapper.writeValueAsString(financialData));
            
            // Save reports to database - In a real application, you would have a Report entity and repository
            // For now, we'll just log the created reports
            System.out.println("✅ Created sample reports for manager:");
            System.out.println("  - Attendance Report: " + attendanceReport.get("title"));
            System.out.println("  - Performance Report: " + performanceReport.get("title"));
            System.out.println("  - Financial Report: " + financialReport.get("title"));
            
        } catch (Exception e) {
            System.err.println("❌ Error creating sample reports for manager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create sample schedules specifically for manager view
     * 
     * @param users List of users
     * @param classrooms List of classrooms
     */
    private void CreateManagerSchedules(List<User> users, List<Classroom> classrooms) {
        try {
            System.out.println("Creating sample schedules for manager view...");
            
            // Check if we have enough users and classrooms
            if (users == null || users.size() < 5 || classrooms == null || classrooms.size() < 3) {
                System.out.println("⚠️ Not enough users or classrooms to create manager schedules");
                return;
            }
            
            // Get teacher users
            User teacher1 = users.get(1);  // teacher user
            User teacher2 = users.get(8);  // Dr. Sarah Williams
            User teacher3 = users.get(9);  // Prof. Michael Brown
            
            // Get classrooms
            Classroom mathClass = classrooms.get(0);  // Math class
            Classroom physicsClass = classrooms.get(1);  // Physics class
            Classroom csClass = classrooms.get(2);  // CS class
            
            // Days of the week (0 = Monday, 6 = Sunday)
            int[] daysOfWeek = {0, 1, 2, 3, 4};
            String[] dayNames = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu"};
            String[] rooms = {"P201", "P202", "P203", "P204", "P205", "Online"};
            
            // Create schedules for Math class
            for (int i = 0; i < 3; i++) {
                Schedule mathSchedule = new Schedule();
                mathSchedule.setClassroom(mathClass);
                mathSchedule.setTeacher(teacher1);
                mathSchedule.setDayOfWeek(daysOfWeek[i]);
                mathSchedule.setStartTime(LocalTime.of(8 + i, 0));
                mathSchedule.setEndTime(LocalTime.of(10 + i, 0));
                mathSchedule.setRoom(rooms[i % rooms.length]);
                mathSchedule.setSubject(mathClass.getSubject());
                scheduleRepository.save(mathSchedule);
            }
            
            // Create schedules for Physics class
            for (int i = 0; i < 2; i++) {
                Schedule physicsSchedule = new Schedule();
                physicsSchedule.setClassroom(physicsClass);
                physicsSchedule.setTeacher(teacher2);
                physicsSchedule.setDayOfWeek(daysOfWeek[i + 1]);
                physicsSchedule.setStartTime(LocalTime.of(13 + i, 0));
                physicsSchedule.setEndTime(LocalTime.of(15 + i, 0));
                physicsSchedule.setRoom(rooms[(i + 2) % rooms.length]);
                physicsSchedule.setSubject(physicsClass.getSubject());
                scheduleRepository.save(physicsSchedule);
            }
            
            // Create schedules for CS class
            for (int i = 0; i < 2; i++) {
                Schedule csSchedule = new Schedule();
                csSchedule.setClassroom(csClass);
                csSchedule.setTeacher(teacher3);
                csSchedule.setDayOfWeek(daysOfWeek[i + 2]);
                csSchedule.setStartTime(LocalTime.of(15 + i, 30));
                csSchedule.setEndTime(LocalTime.of(17 + i, 30));
                csSchedule.setRoom(rooms[(i + 4) % rooms.length]);
                csSchedule.setSubject(csClass.getSubject());
                scheduleRepository.save(csSchedule);
            }
            
            // Create some special schedules (evening classes)
            Schedule eveningMath = new Schedule();
            eveningMath.setClassroom(mathClass);
            eveningMath.setTeacher(teacher1);
            eveningMath.setDayOfWeek(4); // Friday
            eveningMath.setStartTime(LocalTime.of(18, 0));
            eveningMath.setEndTime(LocalTime.of(20, 0));
            eveningMath.setRoom("Online");
            eveningMath.setSubject(mathClass.getSubject());
            scheduleRepository.save(eveningMath);
            
            Schedule weekendPhysics = new Schedule();
            weekendPhysics.setClassroom(physicsClass);
            weekendPhysics.setTeacher(teacher2);
            weekendPhysics.setDayOfWeek(5); // Saturday
            weekendPhysics.setStartTime(LocalTime.of(9, 0));
            weekendPhysics.setEndTime(LocalTime.of(12, 0));
            weekendPhysics.setRoom("P301");
            weekendPhysics.setSubject(physicsClass.getSubject());
            scheduleRepository.save(weekendPhysics);
            
            System.out.println("✅ Created comprehensive schedules for manager view");
            
        } catch (Exception e) {
            System.err.println("❌ Error creating sample schedules for manager: " + e.getMessage());
            e.printStackTrace();
        }
>>>>>>> Stashed changes
    }
}