package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.LectureDto;
import com.classroomapp.classroombackend.dto.StudentDto;

// Additional DTOs for expanded functionality
class CourseDetailsDto {
    private Long id;
    private String name;
    private String description;
    private String instructor;
    private int totalStudents;
    private int totalLectures;
    private int progressPercentage;
    private String syllabus;
    
    public CourseDetailsDto(Long id, String name, String description, String instructor) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.instructor = instructor;
        this.totalStudents = 0;
        this.totalLectures = 0;
        this.progressPercentage = 0;
        this.syllabus = "";
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public int getTotalLectures() { return totalLectures; }
    public void setTotalLectures(int totalLectures) { this.totalLectures = totalLectures; }
    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
    public String getSyllabus() { return syllabus; }
    public void setSyllabus(String syllabus) { this.syllabus = syllabus; }
}

class CourseMaterialDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String downloadUrl;
    private String uploadDate;
    private String uploadedBy;
    private long fileSize;
    
    public CourseMaterialDto(Long id, String fileName, String fileType, String uploadedBy) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploadedBy = uploadedBy;
        this.downloadUrl = "/api/materials/download/" + id;
        this.uploadDate = java.time.LocalDateTime.now().toString();
        this.fileSize = 1024000; // Mock size
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
}

class CourseScheduleDto {
    private Long id;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String room;
    private String type;
    
    public CourseScheduleDto(Long id, String dayOfWeek, String startTime, String endTime, String room, String type) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.type = type;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {

    // Mock Database for Lectures
    final Map<Long, List<LectureDto>> lecturesByCourse = new ConcurrentHashMap<>();
    private final AtomicLong lectureIdCounter = new AtomicLong(100);

    // Mock Database for Students
    private final Map<Long, List<StudentDto>> studentsByCourse = new ConcurrentHashMap<>();
    
    // Additional Mock Databases for new functionality
    private final Map<Long, CourseDetailsDto> courseDetails = new ConcurrentHashMap<>();
    private final Map<Long, List<CourseMaterialDto>> courseMaterials = new ConcurrentHashMap<>();
    private final Map<Long, List<CourseScheduleDto>> courseSchedules = new ConcurrentHashMap<>();
    private final AtomicLong materialIdCounter = new AtomicLong(1000);
    private final AtomicLong scheduleIdCounter = new AtomicLong(2000);

    public CourseController() {
        // Khởi tạo dữ liệu giả lập cho sinh viên
        List<StudentDto> studentsCourse1 = new ArrayList<>();
        studentsCourse1.add(new StudentDto(101L, "Nguyễn Văn An", "an.nv@example.com"));
        studentsCourse1.add(new StudentDto(102L, "Trần Thị Bình", "binh.tt@example.com"));
        studentsByCourse.put(1L, studentsCourse1);

        List<StudentDto> studentsCourse2 = new ArrayList<>();
        studentsCourse2.add(new StudentDto(201L, "Phạm Văn Cảnh", "canh.pv@example.com"));
        studentsByCourse.put(2L, studentsCourse2);

        // Khởi tạo dữ liệu giả lập cho bài giảng
        List<LectureDto> lecturesCourse1 = new ArrayList<>();
        lecturesCourse1.add(new LectureDto(lectureIdCounter.getAndIncrement(), "Bài 1: Giới thiệu Java", "Nội dung cơ bản về Java", 1L));
        lecturesCourse1.add(new LectureDto(lectureIdCounter.getAndIncrement(), "Bài 2: Biến và Kiểu dữ liệu", "Tìm hiểu về biến trong Java", 1L));
        lecturesByCourse.put(1L, lecturesCourse1);
        
        // Initialize course details
        courseDetails.put(1L, new CourseDetailsDto(1L, "Lập trình Java cơ bản", 
            "Khóa học về ngôn ngữ lập trình Java từ cơ bản đến nâng cao", 
            "Thầy Nguyễn Văn A"));
        courseDetails.put(2L, new CourseDetailsDto(2L, "Cơ sở dữ liệu", 
            "Khóa học về thiết kế và quản lý cơ sở dữ liệu", 
            "Cô Trần Thị B"));
            
        // Initialize course materials
        List<CourseMaterialDto> materialsCourse1 = new ArrayList<>();
        materialsCourse1.add(new CourseMaterialDto(materialIdCounter.getAndIncrement(), 
            "Java_Basics.pdf", "PDF", "Thầy Nguyễn Văn A"));
        materialsCourse1.add(new CourseMaterialDto(materialIdCounter.getAndIncrement(), 
            "Java_Examples.zip", "ZIP", "Thầy Nguyễn Văn A"));
        materialsCourse1.add(new CourseMaterialDto(materialIdCounter.getAndIncrement(), 
            "Video_Tutorial_1.mp4", "VIDEO", "Thầy Nguyễn Văn A"));
        courseMaterials.put(1L, materialsCourse1);
        
        // Initialize course schedules
        List<CourseScheduleDto> scheduleCourse1 = new ArrayList<>();
        scheduleCourse1.add(new CourseScheduleDto(scheduleIdCounter.getAndIncrement(), 
            "Thứ 2", "08:00", "10:00", "P.101", "Lý thuyết"));
        scheduleCourse1.add(new CourseScheduleDto(scheduleIdCounter.getAndIncrement(), 
            "Thứ 4", "14:00", "16:00", "Lab.A1", "Thực hành"));
        courseSchedules.put(1L, scheduleCourse1);
        
        List<CourseScheduleDto> scheduleCourse2 = new ArrayList<>();
        scheduleCourse2.add(new CourseScheduleDto(scheduleIdCounter.getAndIncrement(), 
            "Thứ 3", "10:00", "12:00", "P.205", "Lý thuyết"));
        courseSchedules.put(2L, scheduleCourse2);
    }

    // Task 30: Tạo bài giảng mới cho một khóa học
    @PostMapping("/{courseId}/lectures")
    public ResponseEntity<LectureDto> createLecture(@PathVariable Long courseId, @RequestBody LectureDto lectureDto) {
        System.out.println("Yêu cầu tạo bài giảng cho khóa học ID: " + courseId + " với tiêu đề: " + lectureDto.getTitle());

        lectureDto.setId(lectureIdCounter.getAndIncrement());
        lectureDto.setClassroomId(courseId);
        lectureDto.setMaterials(new ArrayList<>());

        lecturesByCourse.computeIfAbsent(courseId, k -> new ArrayList<>()).add(lectureDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(lectureDto);
    }

    // Lấy danh sách bài giảng của một khóa học
    @GetMapping("/{courseId}/lectures")
    public ResponseEntity<List<LectureDto>> getLecturesByCourse(@PathVariable Long courseId) {
        System.out.println("Yêu cầu lấy danh sách bài giảng cho khóa học ID: " + courseId);
        List<LectureDto> lectures = lecturesByCourse.getOrDefault(courseId, new ArrayList<>());
        return ResponseEntity.ok(lectures);
    }

    // Task 35: Xem danh sách sinh viên trong lớp
    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<StudentDto>> getStudentsInCourse(@PathVariable Long courseId) {
        System.out.println("Yêu cầu lấy danh sách sinh viên cho khóa học ID: " + courseId);
        List<StudentDto> students = studentsByCourse.getOrDefault(courseId, new ArrayList<>());
        return ResponseEntity.ok(students);
    }
    
    // NEW API: Get course details
    @GetMapping("/{courseId}/details")
    public ResponseEntity<CourseDetailsDto> getCourseDetails(@PathVariable Long courseId) {
        System.out.println("Yêu cầu lấy chi tiết khóa học ID: " + courseId);
        CourseDetailsDto details = courseDetails.get(courseId);
        if (details != null) {
            // Update statistics
            details.setTotalStudents(studentsByCourse.getOrDefault(courseId, new ArrayList<>()).size());
            details.setTotalLectures(lecturesByCourse.getOrDefault(courseId, new ArrayList<>()).size());
            details.setProgressPercentage(75); // Mock progress
            return ResponseEntity.ok(details);
        }
        return ResponseEntity.notFound().build();
    }
    
    // NEW API: Get course materials
    @GetMapping("/{courseId}/materials")
    public ResponseEntity<List<CourseMaterialDto>> getCourseMaterials(@PathVariable Long courseId) {
        System.out.println("Yêu cầu lấy tài liệu khóa học ID: " + courseId);
        List<CourseMaterialDto> materials = courseMaterials.getOrDefault(courseId, new ArrayList<>());
        return ResponseEntity.ok(materials);
    }
    
    // NEW API: Get course schedule
    @GetMapping("/{courseId}/schedule")
    public ResponseEntity<List<CourseScheduleDto>> getCourseSchedule(@PathVariable Long courseId) {
        System.out.println("Yêu cầu lấy lịch học khóa học ID: " + courseId);
        List<CourseScheduleDto> schedule = courseSchedules.getOrDefault(courseId, new ArrayList<>());
        return ResponseEntity.ok(schedule);
    }
    

    
    // NEW API: Upload course material
    @PostMapping("/{courseId}/materials")
    public ResponseEntity<CourseMaterialDto> uploadCourseMaterial(
            @PathVariable Long courseId, 
            @RequestBody CourseMaterialDto materialDto) {
        System.out.println("Yêu cầu upload tài liệu cho khóa học ID: " + courseId);
        
        materialDto.setId(materialIdCounter.getAndIncrement());
        materialDto.setDownloadUrl("/api/materials/download/" + materialDto.getId());
        materialDto.setUploadDate(java.time.LocalDateTime.now().toString());
        
        courseMaterials.computeIfAbsent(courseId, k -> new ArrayList<>()).add(materialDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(materialDto);
    }
}
