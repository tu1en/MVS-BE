package com.classroomapp.classroombackend.model;

/**
 * Enum for attendance status
 */
public enum AttendanceStatus {
    PRESENT("Present"),
    LATE("Late"), 
    EARLY_LEAVE("Early Leave"),
    ABSENT("Absent"),
    PARTIAL("Partial");
    
    private final String displayName;
    
    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}