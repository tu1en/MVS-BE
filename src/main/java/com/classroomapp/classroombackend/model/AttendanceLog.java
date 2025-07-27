package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.classroomapp.classroombackend.model.hrmanagement.Shift;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.model.AttendanceStatus;

@Entity
@Table(name = "attendance_logs")
public class AttendanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_attendance_staff"))
    private User staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", foreignKey = @ForeignKey(name = "fk_attendance_shift"))
    private Shift shift;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "expected_check_in")
    private LocalTime expectedCheckIn;

    @Column(name = "expected_check_out")
    private LocalTime expectedCheckOut;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(name = "working_hours", precision = 4, scale = 2)
    private BigDecimal workingHours = BigDecimal.ZERO;

    // Constructors
    public AttendanceLog() {}


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStaff() {
        return staff;
    }

    public void setStaff(User staff) {
        this.staff = staff;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public LocalTime getExpectedCheckIn() {
        return expectedCheckIn;
    }

    public void setExpectedCheckIn(LocalTime expectedCheckIn) {
        this.expectedCheckIn = expectedCheckIn;
    }

    public LocalTime getExpectedCheckOut() {
        return expectedCheckOut;
    }

    public void setExpectedCheckOut(LocalTime expectedCheckOut) {
        this.expectedCheckOut = expectedCheckOut;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public BigDecimal getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(BigDecimal workingHours) {
        this.workingHours = workingHours;
    }
    
    public void calculateWorkingHours() {
        if (checkInTime != null && checkOutTime != null) {
            long minutes = java.time.Duration.between(checkInTime, checkOutTime).toMinutes();
            workingHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}
