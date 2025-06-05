package com.classroomapp.classroombackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.repository.RequestRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.classroomapp.classroombackend.dto.TeacherRequestFormDTO;
import com.classroomapp.classroombackend.dto.StudentRequestFormDTO;

import java.time.LocalDateTime;
import java.time.LocalDate;


/**
 * Initialize test data when application starts
 */
@Component
public class DataLoader implements CommandLineRunner {    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final AccomplishmentRepository accomplishmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    @Autowired    public DataLoader(
        UserRepository userRepository, 
        RequestRepository requestRepository,
        AccomplishmentRepository accomplishmentRepository,
        PasswordEncoder passwordEncoder,
        ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.accomplishmentRepository = accomplishmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }
      @Override
    public void run(String... args) throws Exception {
        // Clear existing data
        userRepository.deleteAll();
        requestRepository.deleteAll();
        accomplishmentRepository.deleteAll();
        
        // Create sample users
        CreateUsers();
        
        // Create sample accomplishments
        CreateAccomplishments();
        
        // Create sample requests
        CreateRequests();
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
        
        // Create student user
        User student = new User();
        student.setUsername("student");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setEmail("student@classroomapp.com");
        student.setFullName("Ass Cracker");
        student.setRoleId(RoleConstants.STUDENT);
        userRepository.save(student);
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
}