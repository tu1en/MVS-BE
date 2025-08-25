package com.classroomapp.classroombackend.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.constants.RoleConstants;

/**
 * Service để xử lý việc redirect dựa trên role của user
 * Cung cấp logic tập trung để xác định dashboard path cho từng role
 */
@Service
public class RoleBasedRedirectService {

    private static final Logger log = LoggerFactory.getLogger(RoleBasedRedirectService.class);

    // Map role constants để dễ quản lý
    private static final Map<String, String> ROLE_DASHBOARD_MAPPING = Map.of(
            RoleConstants.ROLE_STUDENT, "/student",
            RoleConstants.ROLE_TEACHER, "/teacher",
            RoleConstants.ROLE_MANAGER, "/manager",
            RoleConstants.ROLE_ADMIN, "/admin",
            RoleConstants.ROLE_ACCOUNTANT, "/accountant",
            RoleConstants.ROLE_TEACHING_ASSISTANT, "/teaching-assistant",
            RoleConstants.ROLE_PARENT, "/parent"
    );

    /**
     * Lấy dashboard path dựa trên role từ authentication
     */
    public String getDashboardPath(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("No authentication or not authenticated, returning login path");
            return "/login";
        }

        String role = extractRoleFromAuthentication(authentication);
        return getDashboardPathByRole(role);
    }

    /**
     * Lấy dashboard path dựa trên role string
     */
    public String getDashboardPathByRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            log.warn("Role is null or empty, returning login path");
            return "/login";
        }

        // Normalize role name (remove ROLE_ prefix if present)
        String normalizedRole = role.replace("ROLE_", "").toUpperCase();

        String dashboardPath = ROLE_DASHBOARD_MAPPING.get(normalizedRole);

        if (dashboardPath != null) {
            log.debug("Found dashboard path for role {}: {}", normalizedRole, dashboardPath);
            return dashboardPath;
        } else {
            log.warn("Unknown role: {}, returning login path", normalizedRole);
            return "/login";
        }
    }

    /**
     * Trích xuất role từ Authentication object
     */
    private String extractRoleFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse(null);
    }

    /**
     * Kiểm tra xem role có được phép truy cập path không
     */
    public boolean isRoleAllowedForPath(String role, String path) {
        if (role == null || path == null) {
            return false;
        }

        String normalizedRole = role.replace("ROLE_", "").toLowerCase();
        String normalizedPath = path.toLowerCase();

        // Kiểm tra dựa trên path pattern
        switch (normalizedRole) {
            case "student":
                return normalizedPath.startsWith("/student") ||
                       normalizedPath.startsWith("/api/student");
            case "teacher":
                return normalizedPath.startsWith("/teacher") ||
                       normalizedPath.startsWith("/api/teacher");
            case "manager":
                return normalizedPath.startsWith("/manager") ||
                       normalizedPath.startsWith("/api/manager") ||
                       normalizedPath.startsWith("/api/classes") ||
                       normalizedPath.startsWith("/api/classrooms") ||
                       normalizedPath.startsWith("/api/lectures") ||
                       normalizedPath.startsWith("/api/materials") ||
                       normalizedPath.startsWith("/api/contracts");
            case "admin":
                return normalizedPath.startsWith("/admin") ||
                       normalizedPath.startsWith("/api/admin");
            case "accountant":
                return normalizedPath.startsWith("/accountant") ||
                       normalizedPath.startsWith("/api/accountant");
            case "teaching_assistant":
                return normalizedPath.startsWith("/teaching-assistant") ||
                       normalizedPath.startsWith("/api/teaching-assistant");
            case "parent":
                return normalizedPath.startsWith("/parent") ||
                       normalizedPath.startsWith("/api/parent");
            default:
                return false;
        }
    }

    /**
     * Lấy tất cả dashboard paths có sẵn
     */
    public Map<String, String> getAllDashboardPaths() {
        return ROLE_DASHBOARD_MAPPING;
    }
}
