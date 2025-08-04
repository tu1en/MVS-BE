package com.classroomapp.classroombackend.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.entity.SystemChart;
import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AttendanceExplanationRepository;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.administration.SystemChartRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

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
        
        try {
            loadStaffAttendanceData();
        } catch (Exception e) {
            System.err.println("Error loading staff attendance data: " + e.getMessage());
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

    
    private void loadStaffAttendanceData() {
        List<User> managers = userRepository.findByRoleId(3); // Managers have roleId = 3
        List<User> accountants = userRepository.findByRoleId(5); // Accountants have roleId = 5
        List<User> teachers = userRepository.findByRoleId(2); // Teachers have roleId = 2
        
        List<User> allStaff = new ArrayList<>();
        allStaff.addAll(managers);
        allStaff.addAll(accountants);
        allStaff.addAll(teachers);
        
        if (allStaff.isEmpty()) {
            System.out.println("No staff members found. Skipping staff attendance data loading.");
            return;
        }

        Random random = new Random();
        String[] shifts = {"MORNING", "AFTERNOON", "EVENING"};
        String[] statuses = {"PRESENT", "ABSENT", "LATE"};

        System.out.println("Loading staff attendance data for " + allStaff.size() + " staff members...");

        // Create attendance logs for the last 30 days
        for (int dayOffset = 0; dayOffset < 30; dayOffset++) {
            LocalDate workDate = LocalDate.now().minusDays(dayOffset);
            
            // Skip weekends (optional - remove if staff work weekends)
            if (workDate.getDayOfWeek() == DayOfWeek.SATURDAY || workDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            
            for (User staff : allStaff) {
                // Create 1-2 shifts per day for each staff member
                int shiftsPerDay = 1 + random.nextInt(2); // 1 or 2 shifts
                
                for (int shiftCount = 0; shiftCount < shiftsPerDay; shiftCount++) {
                    AttendanceLog log = new AttendanceLog();
                    log.setUserId(staff.getId());
                    log.setUserName(staff.getFullName());
                    log.setRole(getRoleString(staff.getRoleId()));
                    log.setDepartment(staff.getDepartment() != null ? staff.getDepartment() : "Phòng " + getRoleString(staff.getRoleId()));
                    log.setDate(workDate);
                    
                    String shift = shifts[shiftCount % shifts.length];
                    log.setShift(shift);
                    
                    // 85% chance of being present, 10% late, 5% absent
                    double statusRandom = random.nextDouble();
                    String status;
                    if (statusRandom < 0.85) {
                        status = "PRESENT";
                    } else if (statusRandom < 0.95) {
                        status = "LATE";
                    } else {
                        status = "ABSENT";
                    }
                    log.setStatus(status);
                    
                    // Set check-in and check-out times based on shift and status
                    LocalTime baseCheckIn = getShiftStartTime(shift);
                    LocalTime baseCheckOut = getShiftEndTime(shift);
                    
                    if ("PRESENT".equals(status)) {
                        // On time or slightly early/late
                        log.setCheckIn(baseCheckIn.plusMinutes(random.nextInt(21) - 10)); // -10 to +10 minutes
                        log.setCheckOut(baseCheckOut.plusMinutes(random.nextInt(31) - 15)); // -15 to +15 minutes
                    } else if ("LATE".equals(status)) {
                        // Late check-in
                        log.setCheckIn(baseCheckIn.plusMinutes(15 + random.nextInt(45))); // 15-60 minutes late
                        log.setCheckOut(baseCheckOut.plusMinutes(random.nextInt(31) - 15));
                    }
                    // For ABSENT status, don't set check-in/check-out times
                    
                    try {
                        attendanceLogRepository.save(log);
                    } catch (Exception e) {
                        System.err.println("Error saving attendance log for user " + staff.getId() + ": " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("Successfully loaded staff attendance data for 30 days.");
        
        // Print summary
        long managerLogs = attendanceLogRepository.findByRole("Manager").size();
        long accountantLogs = attendanceLogRepository.findByRole("Accountant").size();
        long teacherLogs = attendanceLogRepository.findByRole("Teacher").size();
        
        System.out.println("Created attendance logs: " + managerLogs + " Manager, " + 
                          accountantLogs + " Accountant, " + teacherLogs + " Teacher records");
    }

    
    @Autowired
    private SystemChartRepository systemChartRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    private void loadSystemChartData() {
        System.out.println("Loading system chart data...");
        
        try {
            // Check if charts already exist
            if (systemChartRepository.count() > 0) {
                System.out.println("System charts already exist, skipping chart data loading.");
                return;
            }
            
            createStudentAttendanceCharts();
            createStaffAttendanceCharts();
            createSystemUsageCharts();
            createAcademicPerformanceCharts();
            
            System.out.println("Successfully loaded system chart data.");
            
        } catch (Exception e) {
            System.err.println("Error loading system chart data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createStudentAttendanceCharts() {
        // 1. Student Attendance Rate by Month
        SystemChart studentAttendanceChart = SystemChart.builder()
                .title("Tỷ lệ điểm danh học sinh theo tháng")
                .description("Thống kê tỷ lệ điểm danh của học sinh qua các tháng trong năm")
                .chartType(SystemChart.ChartType.LINE_CHART)
                .chartData(generateStudentAttendanceMonthlyData())
                .chartConfig(getLineChartConfig())
                .isActive(true)
                .isPublic(true)
                .createdBy("system")
                .build();
        systemChartRepository.save(studentAttendanceChart);
        
        // 2. Student Absence Distribution
        SystemChart absenceChart = SystemChart.builder()
                .title("Phân bố học sinh nghỉ học")
                .description("Thống kê phân bố lý do nghỉ học của học sinh")
                .chartType(SystemChart.ChartType.PIE_CHART)
                .chartData(generateStudentAbsenceDistributionData())
                .chartConfig(getPieChartConfig())
                .isActive(true)
                .isPublic(true)
                .createdBy("system")
                .build();
        systemChartRepository.save(absenceChart);
        
        // 3. Class Attendance Comparison
        SystemChart classComparisonChart = SystemChart.builder()
                .title("So sánh điểm danh giữa các lớp")
                .description("Biểu đồ so sánh tỷ lệ điểm danh giữa các lớp học")
                .chartType(SystemChart.ChartType.BAR_CHART)
                .chartData(generateClassAttendanceComparisonData())
                .chartConfig(getBarChartConfig())
                .isActive(true)
                .isPublic(true)
                .createdBy("system")
                .build();
        systemChartRepository.save(classComparisonChart);
    }
    
    private void createStaffAttendanceCharts() {
        // 4. Staff Attendance by Role
        SystemChart staffRoleChart = SystemChart.builder()
                .title("Điểm danh nhân viên theo vai trò")
                .description("Thống kê điểm danh của nhân viên theo từng vai trò")
                .chartType(SystemChart.ChartType.DOUGHNUT_CHART)
                .chartData(generateStaffAttendanceByRoleData())
                .chartConfig(getDoughnutChartConfig())
                .isActive(true)
                .isPublic(true)
                .createdBy("system")
                .build();
        systemChartRepository.save(staffRoleChart);
        
        // 5. Staff Attendance Weekly Trend
        SystemChart staffWeeklyChart = SystemChart.builder()
                .title("Xu hướng điểm danh nhân viên theo tuần")
                .description("Biểu đồ xu hướng điểm danh nhân viên trong 4 tuần gần nhất")
                .chartType(SystemChart.ChartType.AREA_CHART)
                .chartData(generateStaffWeeklyTrendData())
                .chartConfig(getAreaChartConfig())
                .isActive(true)
                .isPublic(true)
                .createdBy("system")
                .build();
        systemChartRepository.save(staffWeeklyChart);
    }
    
    private void createSystemUsageCharts() {
        // 6. Daily Login Activity
        SystemChart loginChart = SystemChart.builder()
                .title("Hoạt động đăng nhập hàng ngày")
                .description("Thống kê số lượt đăng nhập trong 7 ngày gần nhất")
                .chartType(SystemChart.ChartType.LINE_CHART)
                .chartData(generateDailyLoginData())
                .chartConfig(getLineChartConfig())
                .isActive(true)
                .isPublic(true)
                .createdBy("system")
                .build();
        systemChartRepository.save(loginChart);
        
        // 7. User Distribution by Role
        SystemChart userRoleChart = SystemChart.builder()
                .title("Phân bố người dùng theo vai trò")
                .description("Biểu đồ phân bố số lượng người dùng theo từng vai trò trong hệ thống")
                .chartType(SystemChart.ChartType.PIE_CHART)
                .chartData(generateUserRoleDistributionData())
                .chartConfig(getPieChartConfig())
                .isActive(true)
                .isPublic(true)
                .createdBy("system")
                .build();
        systemChartRepository.save(userRoleChart);
    }
    
    private void createAcademicPerformanceCharts() {
        // 8. Monthly Enrollment
        SystemChart enrollmentChart = SystemChart.builder()
                .title("Đăng ký học tập theo tháng")
                .description("Thống kê số lượng đăng ký học tập mới theo từng tháng")
                .chartType(SystemChart.ChartType.BAR_CHART)
                .chartData(generateMonthlyEnrollmentData())
                .chartConfig(getBarChartConfig())
                .isActive(true)
                .isPublic(true)
                .createdBy("system")
                .build();
        systemChartRepository.save(enrollmentChart);
        
        // 9. Course Completion Rate
        SystemChart completionChart = SystemChart.builder()
                .title("Tỷ lệ hoàn thành khóa học")
                .description("Thống kê tỷ lệ hoàn thành các khóa học")
                .chartType(SystemChart.ChartType.DOUGHNUT_CHART)
                .chartData(generateCourseCompletionData())
                .chartConfig(getDoughnutChartConfig())
                .isActive(true)
                .isPublic(true)
                .createdBy("system")
                .build();
        systemChartRepository.save(completionChart);
    }
    
    // Data generation methods
    private String generateStudentAttendanceMonthlyData() {
        Random random = new Random();
        int[] attendanceRates = new int[12];
        for (int i = 0; i < 12; i++) {
            attendanceRates[i] = 75 + random.nextInt(20); // 75-95% attendance
        }
        
        return String.format(
            "{" +
            "\"labels\": [\"Tháng 1\", \"Tháng 2\", \"Tháng 3\", \"Tháng 4\", \"Tháng 5\", \"Tháng 6\", \"Tháng 7\", \"Tháng 8\", \"Tháng 9\", \"Tháng 10\", \"Tháng 11\", \"Tháng 12\"]," +
            "\"datasets\": [{" +
            "\"label\": \"Tỷ lệ điểm danh (%%)\", " +
            "\"data\": [%d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d], " +
            "\"borderColor\": \"rgb(75, 192, 192)\", " +
            "\"backgroundColor\": \"rgba(75, 192, 192, 0.2)\", " +
            "\"tension\": 0.1" +
            "}]" +
            "}",
            attendanceRates[0], attendanceRates[1], attendanceRates[2], attendanceRates[3],
            attendanceRates[4], attendanceRates[5], attendanceRates[6], attendanceRates[7],
            attendanceRates[8], attendanceRates[9], attendanceRates[10], attendanceRates[11]
        );
    }
    
    private String generateStudentAbsenceDistributionData() {
        return "{" +
            "\"labels\": [\"Ốm đau\", \"Việc gia đình\", \"Đi muộn do kẹt xe\", \"Quên chấm công\", \"Khác\"], " +
            "\"datasets\": [{" +
            "\"data\": [35, 25, 20, 15, 5], " +
            "\"backgroundColor\": [" +
            "\"#FF6384\", \"#36A2EB\", \"#FFCE56\", \"#4BC0C0\", \"#9966FF\"" +
            "]" +
            "}]" +
            "}";
    }
    
    private String generateClassAttendanceComparisonData() {
        List<Classroom> classrooms = classroomRepository.findAll();
        if (classrooms.isEmpty()) {
            // Default data if no classrooms
            return "{" +
                "\"labels\": [\"Lớp A\", \"Lớp B\", \"Lớp C\", \"Lớp D\", \"Lớp E\"], " +
                "\"datasets\": [{" +
                "\"label\": \"Tỷ lệ điểm danh (%)\", " +
                "\"data\": [92, 88, 95, 87, 91], " +
                "\"backgroundColor\": \"rgba(54, 162, 235, 0.6)\"" +
                "}]" +
                "}";
        }
        
        Random random = new Random();
        StringBuilder labels = new StringBuilder();
        StringBuilder data = new StringBuilder();
        
        for (int i = 0; i < Math.min(classrooms.size(), 10); i++) {
            if (i > 0) {
                labels.append(", ");
                data.append(", ");
            }
            labels.append("\"").append(classrooms.get(i).getName()).append("\"");
            data.append(80 + random.nextInt(15)); // 80-95% attendance
        }
        
        return String.format("{" +
            "\"labels\": [%s], " +
            "\"datasets\": [{" +
            "\"label\": \"Tỷ lệ điểm danh (%%)\", " +
            "\"data\": [%s], " +
            "\"backgroundColor\": \"rgba(54, 162, 235, 0.6)\"" +
            "}]" +
            "}", labels.toString(), data.toString());
    }
    
    private String generateStaffAttendanceByRoleData() {
        return "{" +
            "\"labels\": [\"Giáo viên\", \"Quản lý\", \"Kế toán\", \"Admin\"], " +
            "\"datasets\": [{" +
            "\"data\": [85, 95, 90, 98], " +
            "\"backgroundColor\": [\"#FF6384\", \"#36A2EB\", \"#FFCE56\", \"#4BC0C0\"]" +
            "}]" +
            "}";
    }
    
    private String generateStaffWeeklyTrendData() {
        return "{" +
            "\"labels\": [\"Tuần 1\", \"Tuần 2\", \"Tuần 3\", \"Tuần 4\"], " +
            "\"datasets\": [{" +
            "\"label\": \"Giáo viên\", " +
            "\"data\": [88, 92, 87, 91], " +
            "\"borderColor\": \"rgb(255, 99, 132)\", " +
            "\"backgroundColor\": \"rgba(255, 99, 132, 0.2)\"" +
            "}, {" +
            "\"label\": \"Quản lý\", " +
            "\"data\": [95, 98, 94, 96], " +
            "\"borderColor\": \"rgb(54, 162, 235)\", " +
            "\"backgroundColor\": \"rgba(54, 162, 235, 0.2)\"" +
            "}, {" +
            "\"label\": \"Kế toán\", " +
            "\"data\": [90, 88, 92, 89], " +
            "\"borderColor\": \"rgb(255, 205, 86)\", " +
            "\"backgroundColor\": \"rgba(255, 205, 86, 0.2)\"" +
            "}]" +
            "}";
    }
    
    private String generateDailyLoginData() {
        Random random = new Random();
        int[] logins = new int[7];
        for (int i = 0; i < 7; i++) {
            logins[i] = 50 + random.nextInt(100); // 50-150 logins per day
        }
        
        return String.format("{" +
            "\"labels\": [\"Chủ nhật\", \"Thứ 2\", \"Thứ 3\", \"Thứ 4\", \"Thứ 5\", \"Thứ 6\", \"Thứ 7\"], " +
            "\"datasets\": [{" +
            "\"label\": \"Số lượt đăng nhập\", " +
            "\"data\": [%d, %d, %d, %d, %d, %d, %d], " +
            "\"borderColor\": \"rgb(75, 192, 192)\", " +
            "\"backgroundColor\": \"rgba(75, 192, 192, 0.2)\", " +
            "\"tension\": 0.1" +
            "}]" +
            "}", logins[0], logins[1], logins[2], logins[3], logins[4], logins[5], logins[6]);
    }
    
    private String generateUserRoleDistributionData() {
        List<User> students = userRepository.findByRoleId(1);
        List<User> teachers = userRepository.findByRoleId(2);
        List<User> managers = userRepository.findByRoleId(3);
        List<User> admins = userRepository.findByRoleId(4);
        List<User> accountants = userRepository.findByRoleId(5);
        
        return String.format("{" +
            "\"labels\": [\"Học sinh\", \"Giáo viên\", \"Quản lý\", \"Admin\", \"Kế toán\"], " +
            "\"datasets\": [{" +
            "\"data\": [%d, %d, %d, %d, %d], " +
            "\"backgroundColor\": [\"#FF6384\", \"#36A2EB\", \"#FFCE56\", \"#4BC0C0\", \"#9966FF\"]" +
            "}]" +
            "}", students.size(), teachers.size(), managers.size(), admins.size(), accountants.size());
    }
    
    private String generateMonthlyEnrollmentData() {
        Random random = new Random();
        int[] enrollments = new int[6];
        for (int i = 0; i < 6; i++) {
            enrollments[i] = 20 + random.nextInt(30); // 20-50 enrollments per month
        }
        
        return String.format("{" +
            "\"labels\": [\"Tháng 1\", \"Tháng 2\", \"Tháng 3\", \"Tháng 4\", \"Tháng 5\", \"Tháng 6\"], " +
            "\"datasets\": [{" +
            "\"label\": \"Số đăng ký mới\", " +
            "\"data\": [%d, %d, %d, %d, %d, %d], " +
            "\"backgroundColor\": \"rgba(153, 102, 255, 0.6)\"" +
            "}]" +
            "}", enrollments[0], enrollments[1], enrollments[2], enrollments[3], enrollments[4], enrollments[5]);
    }
    
    private String generateCourseCompletionData() {
        return "{" +
            "\"labels\": [\"Hoàn thành\", \"Đang học\", \"Bỏ học\"], " +
            "\"datasets\": [{" +
            "\"data\": [65, 25, 10], " +
            "\"backgroundColor\": [\"#4BC0C0\", \"#FFCE56\", \"#FF6384\"]" +
            "}]" +
            "}";
    }
    
    // Chart configuration methods
    private String getLineChartConfig() {
        return "{" +
            "\"responsive\": true, " +
            "\"scales\": {" +
            "\"y\": {\"beginAtZero\": true}" +
            "}" +
            "}";
    }
    
    private String getBarChartConfig() {
        return "{" +
            "\"responsive\": true, " +
            "\"scales\": {" +
            "\"y\": {\"beginAtZero\": true}" +
            "}" +
            "}";
    }
    
    private String getPieChartConfig() {
        return "{" +
            "\"responsive\": true, " +
            "\"plugins\": {" +
            "\"legend\": {\"position\": \"top\"}" +
            "}" +
            "}";
    }
    
    private String getDoughnutChartConfig() {
        return "{" +
            "\"responsive\": true, " +
            "\"plugins\": {" +
            "\"legend\": {\"position\": \"top\"}" +
            "}" +
            "}";
    }
    
    private String getAreaChartConfig() {
        return "{" +
            "\"responsive\": true, " +
            "\"scales\": {" +
            "\"y\": {\"beginAtZero\": true}" +
            "}, " +
            "\"elements\": {" +
            "\"line\": {\"fill\": true}" +
            "}" +
            "}";
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

/**
 * Test Controller for Staff Attendance - You can use this to test the attendance data
 */
@RestController
@RequestMapping("/api/test/attendance")
class StaffAttendanceTestController {
    
    @Autowired
    private AttendanceLogRepository attendanceLogRepository;
    
    @Autowired 
    private UserRepository userRepository;
    
    /**
     * Get all staff attendance logs for testing
     */
    @GetMapping("/all-staff-logs")
    public ResponseEntity<Map<String, Object>> getAllStaffLogs() {
        List<AttendanceLog> allLogs = attendanceLogRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalLogs", allLogs.size());
        response.put("logs", allLogs);
        
        // Group by role for summary
        Map<String, Long> roleStats = allLogs.stream()
                .collect(Collectors.groupingBy(AttendanceLog::getRole, Collectors.counting()));
        response.put("roleStats", roleStats);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get attendance logs by role
     */
    @GetMapping("/by-role/{role}")
    public ResponseEntity<List<AttendanceLog>> getLogsByRole(@PathVariable String role) {
        List<AttendanceLog> logs = attendanceLogRepository.findByRole(role);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get attendance logs for a specific date
     */
    @GetMapping("/by-date")
    public ResponseEntity<List<AttendanceLog>> getLogsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceLog> logs = attendanceLogRepository.findByDate(date);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get all managers and accountants for reference
     */
    @GetMapping("/staff-users")
    public ResponseEntity<Map<String, Object>> getStaffUsers() {
        List<User> managers = userRepository.findByRoleId(3);
        List<User> accountants = userRepository.findByRoleId(5);
        List<User> teachers = userRepository.findByRoleId(2);
        
        Map<String, Object> response = new HashMap<>();
        response.put("managers", managers.stream().map(u -> Map.of(
            "id", u.getId(),
            "name", u.getFullName(),
            "email", u.getEmail(),
            "department", u.getDepartment() != null ? u.getDepartment() : "N/A"
        )).collect(Collectors.toList()));
        
        response.put("accountants", accountants.stream().map(u -> Map.of(
            "id", u.getId(),
            "name", u.getFullName(),
            "email", u.getEmail(),
            "department", u.getDepartment() != null ? u.getDepartment() : "N/A"
        )).collect(Collectors.toList()));
        
        response.put("teachers", teachers.stream().map(u -> Map.of(
            "id", u.getId(),
            "name", u.getFullName(),
            "email", u.getEmail(),
            "department", u.getDepartment() != null ? u.getDepartment() : "N/A"
        )).collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Clear all attendance log data (for testing)
     */
    @DeleteMapping("/clear-logs")
    public ResponseEntity<String> clearAllLogs() {
        long count = attendanceLogRepository.count();
        attendanceLogRepository.deleteAll();
        return ResponseEntity.ok("Cleared " + count + " attendance log records");
    }
}
