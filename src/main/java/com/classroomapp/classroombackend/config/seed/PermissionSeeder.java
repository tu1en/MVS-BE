package com.classroomapp.classroombackend.config.seed;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.administration.SystemPermission;
import com.classroomapp.classroombackend.model.administration.SystemPermission.PermissionType;
import com.classroomapp.classroombackend.model.administration.SystemPermission.ResourceType;
import com.classroomapp.classroombackend.repository.administration.SystemPermissionRepository;

@Component
public class PermissionSeeder {

    @Autowired
    private SystemPermissionRepository permissionRepository;

    @Transactional
    public void seed() {
        if (permissionRepository.count() > 0) {
            System.out.println("✅ [PermissionSeeder] Permissions already seeded.");
            return;
        }

        System.out.println("🌱 Seeding system permissions...");

        List<SystemPermission> permissions = new ArrayList<>();

        // === CLASSROOM MODULE ===
        permissions.add(createPermission("Xem danh sách lớp học", "READ_CLASSROOM", "CLASSROOM", "Quản lý lớp học"));
        permissions.add(createPermission("Tạo lớp học", "WRITE_CLASSROOM", "CLASSROOM", "Quản lý lớp học"));
        permissions.add(createPermission("Xóa lớp học", "DELETE_CLASSROOM", "CLASSROOM", "Quản lý lớp học"));

        // === ASSIGNMENT MODULE ===
        permissions.add(createPermission("Xem bài tập", "READ_ASSIGNMENT", "ASSIGNMENT", "Quản lý bài tập"));
        permissions.add(createPermission("Tạo bài tập", "WRITE_ASSIGNMENT", "ASSIGNMENT", "Quản lý bài tập"));
        permissions.add(createPermission("Nộp bài tập", "SUBMIT_ASSIGNMENT", "ASSIGNMENT", "Quản lý bài tập"));
        permissions.add(createPermission("Chấm bài", "GRADE_ASSIGNMENT", "ASSIGNMENT", "Quản lý bài tập"));

        // === USER MODULE ===
        permissions.add(createPermission("Quản lý người dùng", "MANAGE_USERS", "USER", "Quản lý người dùng"));

        // === REPORT MODULE ===
        permissions.add(createPermission("Xem báo cáo", "VIEW_REPORTS", "REPORT", "Báo cáo thống kê"));

        permissionRepository.saveAll(permissions);

        System.out.println("✅ [PermissionSeeder] " + permissions.size() + " permissions seeded successfully.");
    }

    private SystemPermission createPermission(String name, String code, String moduleCode, String moduleName) {
        SystemPermission permission = new SystemPermission();
        
        permission.setPermissionName(name);
        permission.setPermissionCode(code);
        permission.setModuleCode(moduleCode);
        permission.setModuleName(moduleName);
        permission.setDescription("Permission cho module " + moduleName);
        permission.setPermissionType(PermissionType.FUNCTIONAL);
        permission.setResourceType(ResourceType.ENDPOINT);
        permission.setResourcePattern("/api/" + moduleCode.toLowerCase() + "/**");
        permission.setIsSystemPermission(true);
        permission.setIsActive(true);
        return permission;
    }
}
