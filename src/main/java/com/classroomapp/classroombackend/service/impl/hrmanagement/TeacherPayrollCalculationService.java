package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service mới để tính lương giáo viên dựa trên lịch sử giảng dạy thực tế
 * Sử dụng AttendanceSession thay vì AttendanceLog vì teacher không có check-in/check-out
 * Teacher chỉ có điểm danh học sinh và được ghi nhận trong AttendanceSession
 */
@Service
@Slf4j
public class TeacherPayrollCalculationService {

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    // Mỗi slot giảng dạy = 1.5 giờ (90 phút)
    private static final double SLOT_DURATION_HOURS = 1.5;
    
    /**
     * Tính lương giáo viên dựa trên lịch sử giảng dạy từ AttendanceSession
     * @param teacherId ID giáo viên
     * @param periodStart Ngày bắt đầu kỳ lương
     * @param periodEnd Ngày kết thúc kỳ lương
     * @param contract Hợp đồng của giáo viên
     * @return Kết quả tính lương
     */
    public TeacherPayrollResult calculateSalaryFromTeachingSessions(
            Long teacherId, 
            LocalDate periodStart, 
            LocalDate periodEnd, 
            Contract contract) {
        
        log.info("🔄 Calculating teacher salary from teaching sessions for teacher {} from {} to {}", 
                teacherId, periodStart, periodEnd);
        
        try {
            // 1. Lấy lịch sử teaching sessions trong kỳ lương
            List<AttendanceSession> teachingSessions = getTeachingSessionsInPeriod(teacherId, periodStart, periodEnd);
            
            // 2. Tính toán teaching hours và slots
            TeachingWorkSummary workSummary = calculateTeachingWorkFromSessions(teachingSessions, periodStart, periodEnd);
            
            // 3. Tính lương
            BigDecimal totalSalary = calculateTotalSalary(workSummary, contract);
            
            log.info("✅ Teacher payroll calculation completed: {} sessions, {} hours, {} slots, {} VND", 
                    teachingSessions.size(), workSummary.getTotalTeachingHours(), 
                    workSummary.getTotalTeachingSlots(), totalSalary);
            
            return new TeacherPayrollResult(
                teacherId,
                periodStart,
                periodEnd,
                workSummary.getTotalTeachingHours(),
                workSummary.getTotalTeachingSlots(),
                workSummary.getTotalTeachingDays(),
                workSummary.getWeekdayHours(),
                workSummary.getWeekendHours(),
                workSummary.getWeekdaySlots(),
                workSummary.getWeekendSlots(),
                workSummary.getWeekdayDays(),
                workSummary.getWeekendDays(),
                contract.getHourlySalary(),
                totalSalary
            );
            
        } catch (Exception e) {
            log.error("❌ Error calculating teacher salary from teaching sessions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate teacher salary from teaching sessions", e);
        }
    }
    
    /**
     * Lấy lịch sử teaching sessions trong kỳ lương
     */
    private List<AttendanceSession> getTeachingSessionsInPeriod(Long teacherId, LocalDate periodStart, LocalDate periodEnd) {
        LocalDateTime startDateTime = periodStart.atStartOfDay();
        LocalDateTime endDateTime = periodEnd.atTime(23, 59, 59);
        
        // Lấy tất cả teaching sessions của giáo viên có teacherClockInTime trong kỳ
        List<AttendanceSession> sessions = attendanceSessionRepository.findTeachingHistoryByTeacherId(teacherId)
                .stream()
                .filter(session -> {
                    LocalDateTime clockInTime = session.getTeacherClockInTime();
                    return clockInTime != null && 
                           !clockInTime.isBefore(startDateTime) && 
                           !clockInTime.isAfter(endDateTime);
                })
                .collect(Collectors.toList());

        log.info("📊 Found {} teaching sessions for teacher {} in period {} to {}",
                sessions.size(), teacherId, periodStart, periodEnd);

        return sessions;
    }
    
    /**
     * Tính toán teaching work từ attendance sessions
     */
    private TeachingWorkSummary calculateTeachingWorkFromSessions(
            List<AttendanceSession> sessions,
            LocalDate periodStart,
            LocalDate periodEnd) {

        TeachingWorkSummary summary = new TeachingWorkSummary();

        for (AttendanceSession session : sessions) {
            if (session.getTeacherClockInTime() != null) {
                LocalDate sessionDate = session.getTeacherClockInTime().toLocalDate();
                
                // Tính số giờ dạy trong session
                double hoursInSession = calculateHoursFromSession(session);
                
                if (hoursInSession > 0) {
                    // Quy đổi sang slots (1 slot = 1.5 giờ)
                    double slotsInSession = hoursInSession / SLOT_DURATION_HOURS;
                    
                    // Phân loại ngày thường/cuối tuần
                    DayOfWeek dayOfWeek = sessionDate.getDayOfWeek();
                    boolean isWeekend = dayOfWeek.getValue() >= 6; // Thứ 7, Chủ nhật

                    if (isWeekend) {
                        summary.addWeekendHours(hoursInSession);
                        summary.addWeekendSlots(slotsInSession);
                        summary.addWeekendDays(1);
                    } else {
                        summary.addWeekdayHours(hoursInSession);
                        summary.addWeekdaySlots(slotsInSession);
                        summary.addWeekdayDays(1);
                    }

                    summary.addTotalTeachingHours(hoursInSession);
                    summary.addTotalTeachingSlots(slotsInSession);
                    summary.addTotalTeachingDays(1);

                    log.debug("📅 Session {}: Date: {}, Hours: {}, Slots: {}, Weekend: {}",
                            session.getId(), sessionDate, hoursInSession, slotsInSession, isWeekend);
                }
            }
        }

        return summary;
    }

    /**
     * Tính số giờ dạy từ một attendance session
     */
    private double calculateHoursFromSession(AttendanceSession session) {
        if (session.getTeacherClockInTime() == null) {
            return 0.0;
        }
        
        LocalDateTime clockInTime = session.getTeacherClockInTime();
        LocalDateTime clockOutTime = session.getTeacherClockOutTime();
        
        // Nếu không có clock-out time, sử dụng default 1.5 giờ (1 slot)
        if (clockOutTime == null) {
            log.debug("⏰ Session {} has no clock-out time, using default {} hours",
                    session.getId(), SLOT_DURATION_HOURS);
            return SLOT_DURATION_HOURS;
        }
        
        // Tính số giờ từ clock-in đến clock-out
        long minutes = java.time.Duration.between(clockInTime, clockOutTime).toMinutes();
        double hours = minutes / 60.0;
        
        log.debug("⏰ Session {}: {} to {} = {} hours",
                session.getId(), clockInTime, clockOutTime, hours);
        
        return hours;
    }
    
    /**
     * Tính tổng lương
     */
    private BigDecimal calculateTotalSalary(TeachingWorkSummary workSummary, Contract contract) {
        if (contract.getHourlySalary() == null || contract.getHourlySalary() <= 0) {
            log.warn("⚠️ Contract has no hourly salary, using default 100,000 VND/hour");
            contract.setHourlySalary(100000L);
        }
        
        BigDecimal hourlyRate = new BigDecimal(contract.getHourlySalary());
        
        // Lương ngày thường
        BigDecimal weekdayPay = hourlyRate.multiply(BigDecimal.valueOf(workSummary.getWeekdayHours()));
        
        // Lương cuối tuần (gấp đôi)
        BigDecimal weekendPay = hourlyRate.multiply(BigDecimal.valueOf(2))
                .multiply(BigDecimal.valueOf(workSummary.getWeekendHours()));
        
        BigDecimal totalSalary = weekdayPay.add(weekendPay);
        
        log.info("💰 Salary breakdown - Weekday: {} VND, Weekend: {} VND, Total: {} VND", 
                weekdayPay, weekendPay, totalSalary);
        
        return totalSalary.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Lớp chứa kết quả tính lương teacher
     */
    public static class TeacherPayrollResult {
        private final Long teacherId;
        private final LocalDate periodStart;
        private final LocalDate periodEnd;
        private final double totalTeachingHours;
        private final double totalTeachingSlots;
        private final int totalTeachingDays;
        private final double weekdayHours;
        private final double weekendHours;
        private final double weekdaySlots;
        private final double weekendSlots;
        private final int weekdayDays;
        private final int weekendDays;
        private final Long hourlyRate;
        private final BigDecimal totalSalary;

        public TeacherPayrollResult(Long teacherId, LocalDate periodStart, LocalDate periodEnd,
                                 double totalTeachingHours, double totalTeachingSlots, int totalTeachingDays,
                                 double weekdayHours, double weekendHours,
                                 double weekdaySlots, double weekendSlots,
                                 int weekdayDays, int weekendDays,
                                 Long hourlyRate, BigDecimal totalSalary) {
            this.teacherId = teacherId;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.totalTeachingHours = totalTeachingHours;
            this.totalTeachingSlots = totalTeachingSlots;
            this.totalTeachingDays = totalTeachingDays;
            this.weekdayHours = weekdayHours;
            this.weekendHours = weekendHours;
            this.weekdaySlots = weekdaySlots;
            this.weekendSlots = weekendSlots;
            this.weekdayDays = weekdayDays;
            this.weekendDays = weekendDays;
            this.hourlyRate = hourlyRate;
            this.totalSalary = totalSalary;
        }

        // Getters
        public Long getTeacherId() { return teacherId; }
        public LocalDate getPeriodStart() { return periodStart; }
        public LocalDate getPeriodEnd() { return periodEnd; }
        public double getTotalTeachingHours() { return totalTeachingHours; }
        public double getTotalTeachingSlots() { return totalTeachingSlots; }
        public int getTotalTeachingDays() { return totalTeachingDays; }
        public double getWeekdayHours() { return weekdayHours; }
        public double getWeekendHours() { return weekendHours; }
        public double getWeekdaySlots() { return weekdaySlots; }
        public double getWeekendSlots() { return weekendSlots; }
        public int getWeekdayDays() { return weekdayDays; }
        public int getWeekendDays() { return weekendDays; }
        public Long getHourlyRate() { return hourlyRate; }
        public BigDecimal getTotalSalary() { return totalSalary; }
    }

    /**
     * Lớp chứa tổng kết giờ làm việc và slots
     */
    private static class TeachingWorkSummary {
        private double totalTeachingHours = 0.0;
        private double totalTeachingSlots = 0.0;
        private int totalTeachingDays = 0;
        private double weekdayHours = 0.0;
        private double weekendHours = 0.0;
        private double weekdaySlots = 0.0;
        private double weekendSlots = 0.0;
        private int weekdayDays = 0;
        private int weekendDays = 0;

        public void addTotalTeachingHours(double hours) { this.totalTeachingHours += hours; }
        public void addTotalTeachingSlots(double slots) { this.totalTeachingSlots += slots; }
        public void addTotalTeachingDays(int days) { this.totalTeachingDays += days; }
        public void addWeekdayHours(double hours) { this.weekdayHours += hours; }
        public void addWeekendHours(double hours) { this.weekendHours += hours; }
        public void addWeekdaySlots(double slots) { this.weekdaySlots += slots; }
        public void addWeekendSlots(double slots) { this.weekendSlots += slots; }
        public void addWeekdayDays(int days) { this.weekdayDays += days; }
        public void addWeekendDays(int days) { this.weekendDays += days; }

        public double getTotalTeachingHours() { return totalTeachingHours; }
        public double getTotalTeachingSlots() { return totalTeachingSlots; }
        public int getTotalTeachingDays() { return totalTeachingDays; }
        public double getWeekdayHours() { return weekdayHours; }
        public double getWeekendHours() { return weekendHours; }
        public double getWeekdaySlots() { return weekdaySlots; }
        public double getWeekendSlots() { return weekendSlots; }
        public int getWeekdayDays() { return weekdayDays; }
        public int getWeekendDays() { return weekendDays; }
    }
}
