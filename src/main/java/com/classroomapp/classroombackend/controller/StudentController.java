package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class StudentCourseDto {
    private Long id;
    private String name;
    private String instructor;
    private String status;
    private int progressPercentage;
    private String nextClass;
    
    public StudentCourseDto(Long id, String name, String instructor, String status, int progressPercentage, String nextClass) {
        this.id = id;
        this.name = name;
        this.instructor = instructor;
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.nextClass = nextClass;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
    public String getNextClass() { return nextClass; }
    public void setNextClass(String nextClass) { this.nextClass = nextClass; }
}

class StudentTimetableDto {
    private Long id;
    private String courseName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String room;
    private String instructor;
    private String type;
    
    public StudentTimetableDto(Long id, String courseName, String dayOfWeek, String startTime, String endTime, String room, String instructor, String type) {
        this.id = id;
        this.courseName = courseName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.instructor = instructor;
        this.type = type;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

class StudentProgressDto {
    private Long courseId;
    private String courseName;
    private int completedLectures;
    private int totalLectures;
    private int completedAssignments;
    private int totalAssignments;
    private double averageGrade;
    private String lastAccessed;
    
    public StudentProgressDto(Long courseId, String courseName, int completedLectures, int totalLectures, int completedAssignments, int totalAssignments, double averageGrade, String lastAccessed) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.completedLectures = completedLectures;
        this.totalLectures = totalLectures;
        this.completedAssignments = completedAssignments;
        this.totalAssignments = totalAssignments;
        this.averageGrade = averageGrade;
        this.lastAccessed = lastAccessed;
    }
    
    // Getters and setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public int getCompletedLectures() { return completedLectures; }
    public void setCompletedLectures(int completedLectures) { this.completedLectures = completedLectures; }
    public int getTotalLectures() { return totalLectures; }
    public void setTotalLectures(int totalLectures) { this.totalLectures = totalLectures; }
    public int getCompletedAssignments() { return completedAssignments; }
    public void setCompletedAssignments(int completedAssignments) { this.completedAssignments = completedAssignments; }
    public int getTotalAssignments() { return totalAssignments; }
    public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }
    public double getAverageGrade() { return averageGrade; }
    public void setAverageGrade(double averageGrade) { this.averageGrade = averageGrade; }
    public String getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(String lastAccessed) { this.lastAccessed = lastAccessed; }
}

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    private final Map<Long, List<StudentCourseDto>> studentCourses = new ConcurrentHashMap<>();
    private final Map<Long, List<StudentTimetableDto>> studentTimetables = new ConcurrentHashMap<>();
    private final Map<Long, List<StudentProgressDto>> studentProgress = new ConcurrentHashMap<>();

    public StudentController() {
        // Initialize mock data for student ID 1
        List<StudentCourseDto> courses1 = new ArrayList<>();
        courses1.add(new StudentCourseDto(1L, "Lập trình Java cơ bản", "Thầy Nguyễn Văn A", "Đang học", 75, "Thứ 2, 08:00"));
        courses1.add(new StudentCourseDto(2L, "Cơ sở dữ liệu", "Cô Trần Thị B", "Đang học", 60, "Thứ 3, 10:00"));
        courses1.add(new StudentCourseDto(3L, "Phát triển Web", "Thầy Lê Văn C", "Hoàn thành", 100, "Đã hoàn thành"));
        studentCourses.put(1L, courses1);
        
        // Initialize timetable for student ID 1
        List<StudentTimetableDto> timetable1 = new ArrayList<>();
        timetable1.add(new StudentTimetableDto(1L, "Lập trình Java cơ bản", "Thứ 2", "08:00", "10:00", "P.101", "Thầy Nguyễn Văn A", "Lý thuyết"));
        timetable1.add(new StudentTimetableDto(2L, "Lập trình Java cơ bản", "Thứ 4", "14:00", "16:00", "Lab.A1", "Thầy Nguyễn Văn A", "Thực hành"));
        timetable1.add(new StudentTimetableDto(3L, "Cơ sở dữ liệu", "Thứ 3", "10:00", "12:00", "P.205", "Cô Trần Thị B", "Lý thuyết"));
        timetable1.add(new StudentTimetableDto(4L, "Cơ sở dữ liệu", "Thứ 5", "08:00", "10:00", "Lab.B2", "Cô Trần Thị B", "Thực hành"));
        studentTimetables.put(1L, timetable1);
        
        // Initialize progress for student ID 1
        List<StudentProgressDto> progress1 = new ArrayList<>();
        progress1.add(new StudentProgressDto(1L, "Lập trình Java cơ bản", 8, 12, 3, 5, 8.5, "2025-06-10 15:30:00"));
        progress1.add(new StudentProgressDto(2L, "Cơ sở dữ liệu", 6, 10, 2, 4, 7.8, "2025-06-09 14:20:00"));
        progress1.add(new StudentProgressDto(3L, "Phát triển Web", 15, 15, 6, 6, 9.2, "2025-05-28 16:45:00"));
        studentProgress.put(1L, progress1);
    }

    // Get student's courses
    @GetMapping("/{studentId}/courses")
    public ResponseEntity<List<StudentCourseDto>> getStudentCourses(@PathVariable Long studentId) {
        System.out.println("Yêu cầu lấy danh sách khóa học của sinh viên ID: " + studentId);
        List<StudentCourseDto> courses = studentCourses.getOrDefault(studentId, new ArrayList<>());
        return ResponseEntity.ok(courses);
    }

    // Get student's timetable
    @GetMapping("/{studentId}/timetable")
    public ResponseEntity<List<StudentTimetableDto>> getStudentTimetable(@PathVariable Long studentId) {
        System.out.println("Yêu cầu lấy thời khóa biểu của sinh viên ID: " + studentId);
        List<StudentTimetableDto> timetable = studentTimetables.getOrDefault(studentId, new ArrayList<>());
        return ResponseEntity.ok(timetable);
    }

    // Get student's learning progress
    @GetMapping("/{studentId}/progress")
    public ResponseEntity<List<StudentProgressDto>> getStudentProgress(@PathVariable Long studentId) {
        System.out.println("Yêu cầu lấy tiến độ học tập của sinh viên ID: " + studentId);
        List<StudentProgressDto> progress = studentProgress.getOrDefault(studentId, new ArrayList<>());
        return ResponseEntity.ok(progress);
    }
}
