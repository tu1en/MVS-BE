package com.classroomapp.classroombackend.dto;

import java.util.Map;

/**
 * DTO for SMS statistics
 */
public class SMSStatistics {
    private Long totalSent;
    private Long totalPending;
    private Long totalFailed;
    private Long sentToday;
    private Map<String, Long> statusBreakdown;
    private Double successRate;

    public SMSStatistics() {}

    public SMSStatistics(Long totalSent, Long totalPending, Long totalFailed, Long sentToday,
                        Map<String, Long> statusBreakdown, Double successRate) {
        this.totalSent = totalSent;
        this.totalPending = totalPending;
        this.totalFailed = totalFailed;
        this.sentToday = sentToday;
        this.statusBreakdown = statusBreakdown;
        this.successRate = successRate;
    }

    // Getters and setters
    public Long getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(Long totalSent) {
        this.totalSent = totalSent;
    }

    public Long getTotalPending() {
        return totalPending;
    }

    public void setTotalPending(Long totalPending) {
        this.totalPending = totalPending;
    }

    public Long getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(Long totalFailed) {
        this.totalFailed = totalFailed;
    }

    public Long getSentToday() {
        return sentToday;
    }

    public void setSentToday(Long sentToday) {
        this.sentToday = sentToday;
    }

    public Map<String, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<String, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
}