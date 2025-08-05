package com.classroomapp.classroombackend.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private List<String> days;
    private String startTime;
    private String endTime;
    private String room;
    private String subject;
    
    public ScheduleDTO(List<String> days, String startTime, String endTime) {
        this.days = days;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public List<String> getDays() {
        return days;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
}