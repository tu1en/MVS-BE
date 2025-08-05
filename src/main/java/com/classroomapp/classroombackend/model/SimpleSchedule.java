package com.classroomapp.classroombackend.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple schedule model for conflict checking
 * Used to parse JSON schedule from ClassEntity.scheduleJson
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleSchedule {
    private List<String> days;      // ["MON", "WED", "FRI"]
    private String startTime;       // "18:00"
    private String endTime;         // "20:00"
    
    public boolean isValid() {
        return days != null && !days.isEmpty() && 
               startTime != null && !startTime.isEmpty() && 
               endTime != null && !endTime.isEmpty();
    }
    
    // Explicit getters for field access
    public List<String> getDays() {
        return days;
    }
    
    public void setDays(List<String> days) {
        this.days = days;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}