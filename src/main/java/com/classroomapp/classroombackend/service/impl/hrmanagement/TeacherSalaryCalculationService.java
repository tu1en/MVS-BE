package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service để tính lương giáo viên dựa trên lịch sử giảng dạy
 * Thay vì sử dụng StaffAttendanceLog, sử dụng AttendanceSession để tính
 */
@Service
@Slf4j
public class TeacherSalaryCalculationService {

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;

    // Mỗi slot giảng dạy = 1.5 giờ (90 phút)
    private static final double SLOT_DURATION_HOURS = 1.5;
    
    /**
     * Tính lương giáo viên dựa trên lịch sử giảng dạy
     * @param teacherId ID giáo viên
     * @param periodStart Ngày bắt đầu kỳ lương
     * @param periodEnd Ngày kết thúc kỳ lương
     * @param contract Hợp đồng của giáo viên
     * @return Kết quả tính lương
     */
    public TeacherSalaryResult calculateSalaryFromTeachingHistory(
            Long teacherId, 
            LocalDate periodStart, 
            LocalDate periodEnd, 
            Contract contract) {
        
        log.info("🔄 Calculating salary for teacher {} from {} to {}", 
                teacherId, periodStart, periodEnd);
        
        try {
            // 1. Lấy lịch sử giảng dạy trong kỳ lương
            List<AttendanceSession> teachingSessions = getTeachingSessionsInPeriod(teacherId, periodStart, periodEnd);
            
            // 2. Lấy lịch giảng dạy của giáo viên
            List<Schedule> teacherSchedules = scheduleRepository.findByTeacherId(teacherId);
            
            // 3. Tính toán giờ làm việc
            TeachingWorkSummary workSummary = calculateTeachingWork(teachingSessions, teacherSchedules, periodStart, periodEnd);
            
            // 4. Tính lương
            BigDecimal totalSalary = calculateTotalSalary(workSummary, contract);
            
            log.info("✅ Salary calculation completed for teacher {}: {} hours, {} days, {} VND", 
                    teacherId, workSummary.getTotalTeachingHours(), workSummary.getTotalTeachingDays(), totalSalary);
            
            return new TeacherSalaryResult(
                teacherId,
                periodStart,
                periodEnd,
                workSummary.getTotalTeachingHours(),
                workSummary.getTotalTeachingDays(),
                workSummary.getWeekdayHours(),
                workSummary.getWeekendHours(),
                workSummary.getWeekdayDays(),
                workSummary.getWeekendDays(),
                contract.getHourlySalary(),
                totalSalary
            );
            
        } catch (Exception e) {
            log.error("❌ Error calculating salary for teacher {}: {}", teacherId, e.getMessage(), e);
            throw new RuntimeException("Failed to calculate teacher salary", e);
        }
    }
    
    /**
     * Lấy lịch sử giảng dạy trong kỳ lương
     */
    private List<AttendanceSession> getTeachingSessionsInPeriod(Long teacherId, LocalDate periodStart, LocalDate periodEnd) {
        // Lấy tất cả classroom của giáo viên
        List<Classroom> teacherClassrooms = classroomRepository.findByTeacherId(teacherId);
        List<Long> classroomIds = teacherClassrooms.stream()
                .map(Classroom::getId)
                .collect(Collectors.toList());
        
        if (classroomIds.isEmpty()) {
            log.warn("⚠️ Teacher {} has no classrooms assigned", teacherId);
            return List.of();
        }
        
        // Lấy các phiên điểm danh có teacherClockInTime trong kỳ lương
        return attendanceSessionRepository.findByClassroomIdInAndTeacherClockInTimeBetween(
                classroomIds, 
                periodStart.atStartOfDay(), 
                periodEnd.atTime(23, 59, 59)
        );
    }
    
    /**
     * Tính toán giờ làm việc từ lịch sử giảng dạy
     */
    private TeachingWorkSummary calculateTeachingWork(
            List<AttendanceSession> teachingSessions, 
            List<Schedule> teacherSchedules, 
            LocalDate periodStart, 
            LocalDate periodEnd) {
        
        TeachingWorkSummary summary = new TeachingWorkSummary();
        
        // Nhóm các phiên theo ngày
        Map<LocalDate, List<AttendanceSession>> sessionsByDate = teachingSessions.stream()
                .collect(Collectors.groupingBy(AttendanceSession::getSessionDate));
        
        // Tính toán cho từng ngày
        for (LocalDate date = periodStart; !date.isAfter(periodEnd); date = date.plusDays(1)) {
            List<AttendanceSession> daySessions = sessionsByDate.get(date);
            
            if (daySessions != null && !daySessions.isEmpty()) {
                // Tính số slot giảng dạy trong ngày
                int slotsInDay = daySessions.size();
                double hoursInDay = slotsInDay * SLOT_DURATION_HOURS;
                
                // Phân loại ngày thường/cuối tuần
                DayOfWeek dayOfWeek = date.getDayOfWeek();
                boolean isWeekend = dayOfWeek.getValue() >= 6; // Thứ 7, Chủ nhật
                
                if (isWeekend) {
                    summary.addWeekendHours(hoursInDay);
                    summary.addWeekendDays(1);
                } else {
                    summary.addWeekdayHours(hoursInDay);
                    summary.addWeekdayDays(1);
                }
                
                summary.addTotalTeachingHours(hoursInDay);
                summary.addTotalTeachingDays(1);
                
                log.debug("📅 Date: {}, Slots: {}, Hours: {}, Weekend: {}", 
                        date, slotsInDay, hoursInDay, isWeekend);
            }
        }
        
        return summary;
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
     * Lớp chứa kết quả tính lương
     */
    public static class TeacherSalaryResult {
        private final Long teacherId;
        private final LocalDate periodStart;
        private final LocalDate periodEnd;
        private final double totalTeachingHours;
        private final int totalTeachingDays;
        private final double weekdayHours;
        private final double weekendHours;
        private final int weekdayDays;
        private final int weekendDays;
        private final Long hourlyRate;
        private final BigDecimal totalSalary;
        
        public TeacherSalaryResult(Long teacherId, LocalDate periodStart, LocalDate periodEnd,
                                 double totalTeachingHours, int totalTeachingDays,
                                 double weekdayHours, double weekendHours,
                                 int weekdayDays, int weekendDays,
                                 Long hourlyRate, BigDecimal totalSalary) {
            this.teacherId = teacherId;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.totalTeachingHours = totalTeachingHours;
            this.totalTeachingDays = totalTeachingDays;
            this.weekdayHours = weekdayHours;
            this.weekendHours = weekendHours;
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
        public int getTotalTeachingDays() { return totalTeachingDays; }
        public double getWeekdayHours() { return weekdayHours; }
        public double getWeekendHours() { return weekendHours; }
        public int getWeekdayDays() { return weekdayDays; }
        public int getWeekendDays() { return weekendDays; }
        public Long getHourlyRate() { return hourlyRate; }
        public BigDecimal getTotalSalary() { return totalSalary; }
    }
    
    /**
     * Lớp chứa tổng kết giờ làm việc
     */
    private static class TeachingWorkSummary {
        private double totalTeachingHours = 0.0;
        private int totalTeachingDays = 0;
        private double weekdayHours = 0.0;
        private double weekendHours = 0.0;
        private int weekdayDays = 0;
        private int weekendDays = 0;
        
        public void addTotalTeachingHours(double hours) { this.totalTeachingHours += hours; }
        public void addTotalTeachingDays(int days) { this.totalTeachingDays += days; }
        public void addWeekdayHours(double hours) { this.weekdayHours += hours; }
        public void addWeekendHours(double hours) { this.weekendHours += hours; }
        public void addWeekdayDays(int days) { this.weekdayDays += days; }
        public void addWeekendDays(int days) { this.weekendDays += days; }
        
        public double getTotalTeachingHours() { return totalTeachingHours; }
        public int getTotalTeachingDays() { return totalTeachingDays; }
        public double getWeekdayHours() { return weekdayHours; }
        public double getWeekendHours() { return weekendHours; }
        public int getWeekdayDays() { return weekdayDays; }
        public int getWeekendDays() { return weekendDays; }
    }
}
