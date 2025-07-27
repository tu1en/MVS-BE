package com.classroomapp.classroombackend.model.enums;

/**
 * User Status enum for account management với Vietnamese Support
 */
public enum UserStatus {
    ACTIVE("Hoạt động"),
    INACTIVE("Không hoạt động"),
    SUSPENDED("Đình chỉ"),
    PENDING("Chờ kích hoạt");

    private final String vietnameseName;

    UserStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    public static UserStatus fromString(String status) {
        if (status == null) return ACTIVE;
        switch (status.toLowerCase()) {
            case "active": return ACTIVE;
            case "inactive": return INACTIVE;
            case "suspended": return SUSPENDED;
            case "pending": return PENDING;
            default: return ACTIVE;
        }
    }
}
