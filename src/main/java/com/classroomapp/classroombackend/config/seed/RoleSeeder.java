package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.administration.RolePermission;
import com.classroomapp.classroombackend.model.administration.SystemPermission;
import com.classroomapp.classroombackend.model.administration.SystemRole;
import com.classroomapp.classroombackend.model.usermanagement.Role;
import com.classroomapp.classroombackend.repository.administration.RolePermissionRepository;
import com.classroomapp.classroombackend.repository.administration.SystemPermissionRepository;
import com.classroomapp.classroombackend.repository.administration.SystemRoleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.RoleRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class RoleSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SystemRoleRepository systemRoleRepository;

    @Autowired
    private SystemPermissionRepository systemPermissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final Random random = new Random();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seed() {
        // Seed basic roles first
        seedBasicRoles();

        // Seed system roles and permissions
        seedSystemRoles();
        seedSystemPermissions();
        seedRolePermissions();

        System.out.println("✅ [RoleSeeder] Completed seeding all roles and permissions.");
    }

    private void seedBasicRoles() {
        if (roleRepository.count() == 0) {
            try {
                entityManager.createNativeQuery("SET IDENTITY_INSERT roles ON").executeUpdate();

                Role student = new Role("STUDENT");
                student.setId(1);
                roleRepository.save(student);

                Role teacher = new Role("TEACHER");
                teacher.setId(2);
                roleRepository.save(teacher);

                Role manager = new Role("MANAGER");
                manager.setId(3);
                roleRepository.save(manager);

                Role admin = new Role("ADMIN");
                admin.setId(4);
                roleRepository.save(admin);

                System.out.println("Ã¢Å“â€¦ [RoleSeeder] Created roles with explicit IDs.");

            } finally {
                entityManager.createNativeQuery("SET IDENTITY_INSERT roles OFF").executeUpdate();
            }
        } else {
            System.out.println("Ã¢Å“â€¦ [RoleSeeder] Roles already seeded.");
        }
    }

    private void seedSystemRoles() {
        if (systemRoleRepository.count() == 0) {
            System.out.println("🔄 [RoleSeeder] Seeding system roles...");

            // Create system roles with proper hierarchy
            SystemRole superAdmin = createSystemRole("SUPER_ADMIN", "Super Administrator",
                "Quyền cao nhất trong hệ thống", SystemRole.RoleLevel.SUPER_ADMIN, true, 1);
            systemRoleRepository.save(superAdmin);

            SystemRole systemManager = createSystemRole("SYSTEM_MANAGER", "System Manager",
                "Quản lý hệ thống và cấu hình", SystemRole.RoleLevel.ADMIN, true, 2);
            systemRoleRepository.save(systemManager);

            SystemRole dataAdmin = createSystemRole("DATA_ADMIN", "Data Administrator",
                "Quản lý dữ liệu và backup", SystemRole.RoleLevel.ADMIN, true, 3);
            systemRoleRepository.save(dataAdmin);

            SystemRole securityAdmin = createSystemRole("SECURITY_ADMIN", "Security Administrator",
                "Quản lý bảo mật và quyền truy cập", SystemRole.RoleLevel.ADMIN, true, 4);
            systemRoleRepository.save(securityAdmin);

            SystemRole contentModerator = createSystemRole("CONTENT_MODERATOR", "Content Moderator",
                "Kiểm duyệt nội dung và bài viết", SystemRole.RoleLevel.SUPERVISOR, false, 5);
            systemRoleRepository.save(contentModerator);

            SystemRole supportAgent = createSystemRole("SUPPORT_AGENT", "Support Agent",
                "Hỗ trợ người dùng và xử lý yêu cầu", SystemRole.RoleLevel.USER, false, 6);
            systemRoleRepository.save(supportAgent);

            System.out.println("✅ [RoleSeeder] Created 6 system roles successfully.");
        } else {
            System.out.println("✅ [RoleSeeder] System roles already seeded.");
        }
    }

    private SystemRole createSystemRole(String roleCode, String roleName, String description,
                                      SystemRole.RoleLevel roleLevel, boolean isSystemRole, int sortOrder) {
        SystemRole role = new SystemRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setRoleLevel(roleLevel);
        role.setIsSystemRole(isSystemRole);
        role.setIsActive(true);
        role.setSortOrder(sortOrder);
        role.setCreatedBy(1L); // System user
        return role;
    }

    private void seedSystemPermissions() {
        if (systemPermissionRepository.count() == 0) {
            System.out.println("🔄 [RoleSeeder] Seeding system permissions...");

            // System permissions
            createAndSavePermission("SYSTEM_CONFIG_READ", "Đọc cấu hình hệ thống", "SYSTEM", "System Management",
                SystemPermission.PermissionType.SYSTEM, SystemPermission.ResourceType.SYSTEM, "/api/system/config/*", true, 1);

            createAndSavePermission("SYSTEM_CONFIG_WRITE", "Ghi cấu hình hệ thống", "SYSTEM", "System Management",
                SystemPermission.PermissionType.SYSTEM, SystemPermission.ResourceType.SYSTEM, "/api/system/config/*", true, 2);

            createAndSavePermission("USER_MANAGEMENT", "Quản lý người dùng", "USER", "User Management",
                SystemPermission.PermissionType.FUNCTIONAL, SystemPermission.ResourceType.ENDPOINT, "/api/users/*", true, 3);

            createAndSavePermission("ROLE_MANAGEMENT", "Quản lý vai trò", "ROLE", "Role Management",
                SystemPermission.PermissionType.FUNCTIONAL, SystemPermission.ResourceType.ENDPOINT, "/api/roles/*", true, 4);

            createAndSavePermission("AUDIT_LOG_READ", "Đọc audit logs", "AUDIT", "Audit Management",
                SystemPermission.PermissionType.DATA, SystemPermission.ResourceType.DATA, "/api/audit/*", true, 5);

            createAndSavePermission("BACKUP_MANAGE", "Quản lý backup", "BACKUP", "Backup Management",
                SystemPermission.PermissionType.SYSTEM, SystemPermission.ResourceType.SYSTEM, "/api/backup/*", true, 6);

            // Content permissions
            createAndSavePermission("CONTENT_MODERATE", "Kiểm duyệt nội dung", "CONTENT", "Content Management",
                SystemPermission.PermissionType.FUNCTIONAL, SystemPermission.ResourceType.DATA, "/api/content/*", false, 7);

            createAndSavePermission("SUPPORT_ACCESS", "Truy cập hỗ trợ", "SUPPORT", "Support Management",
                SystemPermission.PermissionType.FUNCTIONAL, SystemPermission.ResourceType.ENDPOINT, "/api/support/*", false, 8);

            System.out.println("✅ [RoleSeeder] Created 8 system permissions successfully.");
        } else {
            System.out.println("✅ [RoleSeeder] System permissions already seeded.");
        }
    }

    private void createAndSavePermission(String permissionCode, String permissionName, String moduleCode,
                                       String moduleName, SystemPermission.PermissionType permissionType,
                                       SystemPermission.ResourceType resourceType, String resourcePattern,
                                       boolean isSystemPermission, int sortOrder) {
        SystemPermission permission = new SystemPermission();
        permission.setPermissionCode(permissionCode);
        permission.setPermissionName(permissionName);
        permission.setModuleCode(moduleCode);
        permission.setModuleName(moduleName);
        permission.setPermissionType(permissionType);
        permission.setResourceType(resourceType);
        permission.setResourcePattern(resourcePattern);
        permission.setIsSystemPermission(isSystemPermission);
        permission.setIsActive(true);
        permission.setSortOrder(sortOrder);
        permission.setCreatedBy(1L); // System user
        systemPermissionRepository.save(permission);
    }

    private void seedRolePermissions() {
        if (rolePermissionRepository.count() == 0) {
            System.out.println("🔄 [RoleSeeder] Seeding role permissions...");

            // Get roles and permissions
            List<SystemRole> roles = systemRoleRepository.findAll();
            List<SystemPermission> permissions = systemPermissionRepository.findAll();

            if (roles.isEmpty() || permissions.isEmpty()) {
                System.out.println("⚠️ [RoleSeeder] No roles or permissions found, skipping role permissions seeding.");
                return;
            }

            int assignmentCount = 0;

            // Assign permissions to roles based on hierarchy
            for (SystemRole role : roles) {
                switch (role.getRoleCode()) {
                    case "SUPER_ADMIN":
                        // Super admin gets all permissions
                        for (SystemPermission permission : permissions) {
                            createRolePermission(role, permission, "Full system access");
                            assignmentCount++;
                        }
                        break;

                    case "SYSTEM_MANAGER":
                        // System manager gets system and management permissions
                        for (SystemPermission permission : permissions) {
                            if (permission.getModuleCode().equals("SYSTEM") ||
                                permission.getModuleCode().equals("USER") ||
                                permission.getModuleCode().equals("ROLE")) {
                                createRolePermission(role, permission, "System management access");
                                assignmentCount++;
                            }
                        }
                        break;

                    case "DATA_ADMIN":
                        // Data admin gets backup and audit permissions
                        for (SystemPermission permission : permissions) {
                            if (permission.getModuleCode().equals("BACKUP") ||
                                permission.getModuleCode().equals("AUDIT")) {
                                createRolePermission(role, permission, "Data management access");
                                assignmentCount++;
                            }
                        }
                        break;

                    case "SECURITY_ADMIN":
                        // Security admin gets user and audit permissions
                        for (SystemPermission permission : permissions) {
                            if (permission.getModuleCode().equals("USER") ||
                                permission.getModuleCode().equals("ROLE") ||
                                permission.getModuleCode().equals("AUDIT")) {
                                createRolePermission(role, permission, "Security management access");
                                assignmentCount++;
                            }
                        }
                        break;

                    case "CONTENT_MODERATOR":
                        // Content moderator gets content permissions
                        for (SystemPermission permission : permissions) {
                            if (permission.getModuleCode().equals("CONTENT")) {
                                createRolePermission(role, permission, "Content moderation access");
                                assignmentCount++;
                            }
                        }
                        break;

                    case "SUPPORT_AGENT":
                        // Support agent gets support permissions
                        for (SystemPermission permission : permissions) {
                            if (permission.getModuleCode().equals("SUPPORT")) {
                                createRolePermission(role, permission, "Support access");
                                assignmentCount++;
                            }
                        }
                        break;
                }
            }

            System.out.println("✅ [RoleSeeder] Created " + assignmentCount + " role-permission assignments successfully.");
        } else {
            System.out.println("✅ [RoleSeeder] Role permissions already seeded.");
        }
    }

    private void createRolePermission(SystemRole role, SystemPermission permission, String notes) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        rolePermission.setIsGranted(true);
        rolePermission.setGrantedAt(LocalDateTime.now());
        rolePermission.setGrantedBy(1L); // System user
        rolePermission.setNotes(notes);
        rolePermissionRepository.save(rolePermission);
    }
}