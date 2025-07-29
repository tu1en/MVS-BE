package com.doproject.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.doproject.entity.SystemSetting;
import com.doproject.repository.SystemSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Khởi tạo dữ liệu mặc định cho System Settings
 * Chạy sau khi database schema được tạo
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SystemSettingsInitializer {
    
    @Bean
    @Order(100) // Chạy sau khi schema tạo xong
    public CommandLineRunner initializeSystemSettings(SystemSettingRepository systemSettingRepository) {
        return args -> {
            log.info("🚀 Initializing default system settings...");
            
            // Kiểm tra xem đã có settings chưa
            long existingCount = systemSettingRepository.count();
            if (existingCount > 0) {
                log.info("✅ System settings already exist ({}), skipping initialization", existingCount);
                return;
            }
            
            // Tạo các settings mặc định
            createDefaultSettings(systemSettingRepository);
            
            long finalCount = systemSettingRepository.count();
            log.info("✅ Default system settings initialized successfully! Total: {}", finalCount);
        };
    }
    
    private void createDefaultSettings(SystemSettingRepository repository) {
        log.info("📝 Creating default settings...");
        
        // General Settings
        repository.save(createSetting("siteName", "Learning Management System", "Tên trang web"));
        repository.save(createSetting("language", "vi", "Ngôn ngữ mặc định"));
        repository.save(createSetting("logoUrl", "/images/logo.png", "URL logo trang web"));
        
        // Email Settings
        repository.save(createSetting("smtpHost", "smtp.gmail.com", "SMTP Server"));
        repository.save(createSetting("smtpPort", "587", "SMTP Port"));
        repository.save(createSetting("smtpUsername", "", "SMTP Username"));
        repository.save(createSetting("smtpPassword", "", "SMTP Password", true));
        repository.save(createSetting("smtpTls", "true", "Sử dụng TLS"));
        repository.save(createSetting("smtpAuth", "true", "Yêu cầu xác thực"));
        
        // Security Settings
        repository.save(createSetting("enable2FA", "false", "Bật xác thực 2 yếu tố"));
        repository.save(createSetting("sessionTimeout", "30", "Thời gian timeout session (phút)"));
        repository.save(createSetting("passwordPolicy", "minimum_8_characters", "Chính sách mật khẩu"));
        repository.save(createSetting("maxLoginAttempts", "5", "Số lần đăng nhập tối đa"));
        repository.save(createSetting("lockoutDuration", "15", "Thời gian khóa tài khoản (phút)"));
        
        // System Settings
        repository.save(createSetting("maintenanceMode", "false", "Chế độ bảo trì"));
        repository.save(createSetting("allowRegistration", "true", "Cho phép đăng ký mới"));
        repository.save(createSetting("defaultUserRole", "STUDENT", "Role mặc định cho user mới"));
        
        // File Upload Settings
        repository.save(createSetting("maxFileSize", "10485760", "Kích thước file tối đa (bytes) - 10MB"));
        repository.save(createSetting("allowedFileTypes", "jpg,jpeg,png,pdf,doc,docx,xls,xlsx", "Các loại file được phép"));
        
        // Notification Settings
        repository.save(createSetting("enableEmailNotifications", "true", "Bật thông báo email"));
        repository.save(createSetting("enablePushNotifications", "true", "Bật thông báo push"));
        
        // Learning Settings
        repository.save(createSetting("defaultClassSize", "30", "Kích thước lớp mặc định"));
        repository.save(createSetting("enableOnlineLearning", "true", "Bật học trực tuyến"));
        repository.save(createSetting("autoGrading", "true", "Tự động chấm điểm"));
        
        log.info("📊 Created {} default system settings", repository.count());
    }
    
    private SystemSetting createSetting(String key, String value, String description) {
        return createSetting(key, value, description, false);
    }
    
    private SystemSetting createSetting(String key, String value, String description, boolean isEncrypted) {
        return SystemSetting.builder()
                .keyName(key)
                .value(value)
                .description(description)
                .isEncrypted(isEncrypted)
                .build();
    }
}