package com.classroomapp.classroombackend.constants;

import java.util.Arrays;
import java.util.List;

public class RoleConstants {
    // Existing role IDs
    public static final int STUDENT = 1;
    public static final int TEACHER = 2;
    public static final int MANAGER = 3;
    public static final int ADMIN = 4;
    public static final int ACCOUNTANT = 5;
    
    // Role name constants
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_ACCOUNTANT = "ACCOUNTANT";
    
    // Role groupings (logical groups to replace EMPLOYEE/STAFF references)
    public static final List<Integer> STAFF_ROLES = Arrays.asList(TEACHER, MANAGER, ADMIN, ACCOUNTANT);
    public static final List<Integer> ADMIN_STAFF_ROLES = Arrays.asList(MANAGER, ADMIN, ACCOUNTANT);
    public static final List<Integer> HR_STAFF_ROLES = Arrays.asList(MANAGER, ADMIN);
    public static final List<String> STAFF_ROLE_NAMES = Arrays.asList(ROLE_TEACHER, ROLE_MANAGER, ROLE_ADMIN, ROLE_ACCOUNTANT);
    
    /**
     * Check if role ID represents staff (replaces EMPLOYEE checks)
     */
    public static boolean isStaffRole(int roleId) {
        return STAFF_ROLES.contains(roleId);
    }
    
    /**
     * Check if role name represents staff (replaces EMPLOYEE checks)
     */
    public static boolean isStaffRole(String roleName) {
        return STAFF_ROLE_NAMES.contains(roleName);
    }
    
    /**
     * Check if role represents admin staff (management roles)
     */
    public static boolean isAdminStaffRole(int roleId) {
        return ADMIN_STAFF_ROLES.contains(roleId);
    }
    
    /**
     * Check if role represents HR staff (can manage HR functions)
     */
    public static boolean isHRStaffRole(int roleId) {
        return HR_STAFF_ROLES.contains(roleId);
    }
}