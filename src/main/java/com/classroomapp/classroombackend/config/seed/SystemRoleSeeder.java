package com.classroomapp.classroombackend.config.seed;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SystemRoleSeeder {

    // Nếu sau này dùng thực tế, khai báo các repository ở đây
    // private final SystemRoleRepository systemRoleRepository;
    // private final RolePermissionRepository rolePermissionRepository;

    /**
     * Seed dữ liệu role hệ thống (tạm thời chỉ log ra để tránh lỗi).
     */
    @Transactional
    public void seed() {
        System.out.println("🔐 [SystemRoleSeeder] Seeder kích hoạt nhưng chưa có logic chi tiết.");
        // TODO: Thêm logic insert role mặc định nếu cần
    }
}
