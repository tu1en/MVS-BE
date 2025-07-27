package com.classroomapp.classroombackend.model.enums;

/**
 * User Role enum với Vietnamese Support
 * Maps với role_enum column trong database
 */
public enum UserRole {
    ADMIN("Quản trị viên"),
    MANAGER("Quản lý"),
    TEACHER("Giáo viên"),
    STUDENT("Học viên"),
    ACCOUNTANT("Kế toán");

    private final String vietnameseName;

    UserRole(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    public static UserRole fromRoleId(Integer roleId) {
        if (roleId == null) return STUDENT;
        switch (roleId) {
            case 1: return ADMIN;
            case 2: return MANAGER;
            case 3: return TEACHER;
            case 4: return STUDENT;
            case 5: return ACCOUNTANT;
            default: return STUDENT;
        }
    }

    public Integer toRoleId() {
        return this.ordinal() + 1; // Simpler mapping
    }
    
    public Integer getRoleId() {
        return toRoleId(); // Alias for toRoleId() for compatibility
    }
    
}