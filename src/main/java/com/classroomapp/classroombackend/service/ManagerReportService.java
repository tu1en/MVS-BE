package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.Payment;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerReportService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final SubmissionRepository submissionRepository;
    private final PaymentRepository paymentRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomEnrollmentRepository enrollmentRepository;

    public Map<String, Object> getAttendanceReportData(String period) {
        try {
            log.info("Generating attendance report for period: {}", period);
            
            LocalDate[] dateRange = calculateDateRange(period);
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];
            
            // Query attendance sessions in period
            List<AttendanceSession> sessions = attendanceSessionRepository
                .findBySessionDateBetween(startDate, endDate);
            
            // Get total sessions
            Long totalSessions = (long) sessions.size();
            
            // Get all enrolled students
            List<ClassroomEnrollment> enrollments = enrollmentRepository.findAll();
            Set<Long> studentIds = enrollments.stream()
                .map(e -> e.getUser().getId())
                .collect(Collectors.toSet());
            Long totalStudents = (long) studentIds.size();
            
            // Calculate attendance rate
            Double attendanceRate = 0.0;
            Long absentStudents = 0L;
            
            if (!sessions.isEmpty()) {
                List<Attendance> allAttendance = attendanceRepository.findAll().stream()
                    .filter(a -> sessions.contains(a.getSession()))
                    .collect(Collectors.toList());
                
                long presentCount = allAttendance.stream()
                    .mapToLong(a -> (a.getStatus() == AttendanceStatus.PRESENT || 
                                   a.getStatus() == AttendanceStatus.LATE) ? 1 : 0)
                    .sum();
                
                if (!allAttendance.isEmpty()) {
                    attendanceRate = (double) presentCount / allAttendance.size() * 100;
                }
                
                absentStudents = allAttendance.size() - presentCount;
            }
            
            // Get class data
            List<Map<String, Object>> classData = getClassAttendanceData(sessions);
            
            Map<String, Object> data = new HashMap<>();
            data.put("totalSessions", totalSessions);
            data.put("totalStudents", totalStudents);
            data.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
            data.put("absentStudents", absentStudents);
            data.put("classData", classData);
            
            return data;
            
        } catch (Exception e) {
            log.error("Error generating attendance report: {}", e.getMessage(), e);
            return createEmptyAttendanceData();
        }
    }

    public Map<String, Object> getPerformanceReportData(String period) {
        try {
            log.info("Generating performance report for period: {}", period);
            
            LocalDate[] dateRange = calculateDateRange(period);
            LocalDateTime startDateTime = dateRange[0].atStartOfDay();
            LocalDateTime endDateTime = dateRange[1].atTime(23, 59, 59);
            
            // Get all submissions with scores in period
            List<Submission> allSubmissions = submissionRepository.findAll().stream()
                .filter(s -> s.getSubmittedAt() != null && 
                           s.getSubmittedAt().isAfter(startDateTime) && 
                           s.getSubmittedAt().isBefore(endDateTime) &&
                           s.getScore() != null)
                .collect(Collectors.toList());
            
            // Calculate statistics
            Set<Long> studentIds = allSubmissions.stream()
                .map(s -> s.getStudent().getId())
                .collect(Collectors.toSet());
            Long totalStudents = (long) studentIds.size();
            
            Double averageScore = allSubmissions.stream()
                .mapToDouble(s -> s.getScore().doubleValue())
                .average()
                .orElse(0.0);
            
            // Grade distribution
            Map<String, Long> gradeDistribution = calculateGradeDistribution(allSubmissions);
            
            // Subject data
            List<Map<String, Object>> subjectData = getSubjectPerformanceData(allSubmissions);
            
            Map<String, Object> data = new HashMap<>();
            data.put("totalStudents", totalStudents);
            data.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
            data.put("excellentCount", gradeDistribution.get("excellent"));
            data.put("goodCount", gradeDistribution.get("good"));
            data.put("averageCount", gradeDistribution.get("average"));
            data.put("belowAverageCount", gradeDistribution.get("belowAverage"));
            data.put("subjectData", subjectData);
            
            return data;
            
        } catch (Exception e) {
            log.error("Error generating performance report: {}", e.getMessage(), e);
            return createEmptyPerformanceData();
        }
    }

    public Map<String, Object> getFinancialReportData(String period) {
        try {
            log.info("Generating financial report for period: {}", period);
            
            LocalDate[] dateRange = calculateDateRange(period);
            LocalDateTime startDateTime = dateRange[0].atStartOfDay();
            LocalDateTime endDateTime = dateRange[1].atTime(23, 59, 59);
            
            // Get payments in period
            List<Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentDate() != null &&
                           p.getPaymentDate().isAfter(startDateTime) &&
                           p.getPaymentDate().isBefore(endDateTime) &&
                           p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .collect(Collectors.toList());
            
            // Calculate revenue
            BigDecimal totalRevenue = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Estimate expenses (70% of revenue)
            BigDecimal totalExpenses = totalRevenue.multiply(new BigDecimal("0.70"));
            BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
            
            // Monthly data
            List<Map<String, Object>> monthlyData = getMonthlyFinancialData(payments, dateRange);
            
            // Expense data (estimated breakdown)
            List<Map<String, Object>> expenseData = getExpenseBreakdown(totalExpenses);
            
            Map<String, Object> data = new HashMap<>();
            data.put("totalRevenue", totalRevenue.longValue());
            data.put("totalExpenses", totalExpenses.longValue());
            data.put("netProfit", netProfit.longValue());
            data.put("profitMargin", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
                netProfit.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).doubleValue() : 0.0);
            data.put("studentCount", getActiveStudentCount());
            data.put("monthlyData", monthlyData);
            data.put("expenseData", expenseData);
            
            return data;
            
        } catch (Exception e) {
            log.error("Error generating financial report: {}", e.getMessage(), e);
            return createEmptyFinancialData();
        }
    }

    // Helper methods
    private LocalDate[] calculateDateRange(String period) {
        LocalDate now = LocalDate.now();
        switch(period.toLowerCase()) {
            case "month":
                return new LocalDate[]{now.withDayOfMonth(1), now};
            case "quarter":
                int currentQuarter = (now.getMonthValue() - 1) / 3;
                LocalDate quarterStart = now.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1);
                return new LocalDate[]{quarterStart, now};
            case "semester":
                // Assume semester starts in September or February
                LocalDate semesterStart = now.getMonthValue() >= 9 ?
                    now.withMonth(9).withDayOfMonth(1) :
                    now.withMonth(2).withDayOfMonth(1);
                return new LocalDate[]{semesterStart, now};
            case "year":
                return new LocalDate[]{now.withDayOfYear(1), now};
            default:
                return new LocalDate[]{now.minusMonths(1), now};
        }
    }

    private List<Map<String, Object>> getClassAttendanceData(List<AttendanceSession> sessions) {
        try {
            List<Classroom> classrooms = classroomRepository.findAll();
            return classrooms.stream()
                .map(classroom -> {
                    List<AttendanceSession> classSessions = sessions.stream()
                        .filter(s -> s.getClassroom().getId().equals(classroom.getId()))
                        .collect(Collectors.toList());

                    int studentCount = enrollmentRepository.findByClassroomId(classroom.getId()).size();

                    double attendanceRate = 0.0;
                    if (!classSessions.isEmpty()) {
                        List<Attendance> classAttendance = attendanceRepository.findAll().stream()
                            .filter(a -> classSessions.contains(a.getSession()))
                            .collect(Collectors.toList());

                        if (!classAttendance.isEmpty()) {
                            long presentCount = classAttendance.stream()
                                .mapToLong(a -> (a.getStatus() == AttendanceStatus.PRESENT ||
                                               a.getStatus() == AttendanceStatus.LATE) ? 1 : 0)
                                .sum();
                            attendanceRate = (double) presentCount / classAttendance.size() * 100;
                        }
                    }

                    Map<String, Object> classInfo = new HashMap<>();
                    classInfo.put("className", classroom.getName());
                    classInfo.put("studentCount", studentCount);
                    classInfo.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
                    return classInfo;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting class attendance data: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private Map<String, Long> calculateGradeDistribution(List<Submission> submissions) {
        Map<String, Long> distribution = new HashMap<>();

        Map<Long, List<Double>> studentScores = submissions.stream()
            .collect(Collectors.groupingBy(
                s -> s.getStudent().getId(),
                Collectors.mapping(s -> s.getScore().doubleValue(), Collectors.toList())
            ));

        long excellent = 0, good = 0, average = 0, belowAverage = 0;

        for (List<Double> scores : studentScores.values()) {
            double avgScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            if (avgScore >= 8.5) excellent++;
            else if (avgScore >= 7.0) good++;
            else if (avgScore >= 5.5) average++;
            else belowAverage++;
        }

        distribution.put("excellent", excellent);
        distribution.put("good", good);
        distribution.put("average", average);
        distribution.put("belowAverage", belowAverage);

        return distribution;
    }

    private List<Map<String, Object>> getSubjectPerformanceData(List<Submission> submissions) {
        try {
            Map<String, List<Double>> subjectScores = submissions.stream()
                .filter(s -> s.getAssignment() != null && s.getAssignment().getClassroom() != null)
                .collect(Collectors.groupingBy(
                    s -> s.getAssignment().getClassroom().getSubject() != null ?
                         s.getAssignment().getClassroom().getSubject() : "Unknown Subject",
                    Collectors.mapping(s -> s.getScore().doubleValue(), Collectors.toList())
                ));

            return subjectScores.entrySet().stream()
                .map(entry -> {
                    List<Double> scores = entry.getValue();
                    Map<String, Object> subjectData = new HashMap<>();
                    subjectData.put("subject", entry.getKey());
                    subjectData.put("averageScore", Math.round(scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) * 100.0) / 100.0);
                    subjectData.put("highestScore", Math.round(scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0) * 100.0) / 100.0);
                    subjectData.put("lowestScore", Math.round(scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0) * 100.0) / 100.0);
                    return subjectData;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting subject performance data: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getMonthlyFinancialData(List<Payment> payments, LocalDate[] dateRange) {
        try {
            Map<String, BigDecimal> monthlyRevenue = payments.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getPaymentDate().getMonth().toString(),
                    Collectors.mapping(Payment::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

            List<Map<String, Object>> monthlyData = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> entry : monthlyRevenue.entrySet()) {
                BigDecimal revenue = entry.getValue();
                BigDecimal expenses = revenue.multiply(new BigDecimal("0.72"));
                BigDecimal profit = revenue.subtract(expenses);

                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", entry.getKey());
                monthData.put("revenue", revenue.longValue());
                monthData.put("expenses", expenses.longValue());
                monthData.put("profit", profit.longValue());
                monthlyData.add(monthData);
            }

            return monthlyData;
        } catch (Exception e) {
            log.error("Error getting monthly financial data: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getExpenseBreakdown(BigDecimal totalExpenses) {
        List<Map<String, Object>> expenseData = new ArrayList<>();

        // Industry standard breakdown for education
        String[] categories = {"Lương giáo viên", "Cơ sở vật chất", "Học liệu", "Quản lý", "Marketing", "Khác"};
        double[] percentages = {60.0, 15.0, 10.0, 8.0, 5.0, 2.0};

        for (int i = 0; i < categories.length; i++) {
            Map<String, Object> category = new HashMap<>();
            BigDecimal amount = totalExpenses.multiply(new BigDecimal(percentages[i] / 100));
            category.put("category", categories[i]);
            category.put("amount", amount.longValue());
            category.put("percentage", percentages[i]);
            expenseData.add(category);
        }

        return expenseData;
    }

    private Long getActiveStudentCount() {
        try {
            return (long) enrollmentRepository.findAll().stream()
                .map(e -> e.getUser().getId())
                .collect(Collectors.toSet())
                .size();
        } catch (Exception e) {
            log.error("Error getting active student count: {}", e.getMessage());
            return 0L;
        }
    }

    // Empty data fallback methods
    private Map<String, Object> createEmptyAttendanceData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalSessions", 0L);
        data.put("totalStudents", 0L);
        data.put("attendanceRate", 0.0);
        data.put("absentStudents", 0L);
        data.put("classData", new ArrayList<>());
        return data;
    }

    private Map<String, Object> createEmptyPerformanceData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", 0L);
        data.put("averageScore", 0.0);
        data.put("excellentCount", 0L);
        data.put("goodCount", 0L);
        data.put("averageCount", 0L);
        data.put("belowAverageCount", 0L);
        data.put("subjectData", new ArrayList<>());
        return data;
    }

    private Map<String, Object> createEmptyFinancialData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRevenue", 0L);
        data.put("totalExpenses", 0L);
        data.put("netProfit", 0L);
        data.put("profitMargin", 0.0);
        data.put("studentCount", 0L);
        data.put("monthlyData", new ArrayList<>());
        data.put("expenseData", new ArrayList<>());
        return data;
    }
}
