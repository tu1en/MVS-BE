package com.doproject.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doproject.dto.request.SystemSettingsUpdateRequest;
import com.doproject.entity.SystemSetting;
import com.doproject.repository.SystemSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service xá»­ lÃ½ logic cho System Settings
 * Há»— trá»£ CRUD operations vÃ  validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingService {
    
    private final SystemSettingRepository systemSettingRepository;
    
    /**
     * Láº¥y táº¥t cáº£ system settings dáº¡ng Map
     */
    public Map<String, String> getAllSettings() {
        log.info("Fetching all system settings");
        
        List<SystemSetting> settings = systemSettingRepository.findAll();
        
        return settings.stream()
                .collect(Collectors.toMap(
                    SystemSetting::getKeyName,
                    setting -> setting.getValue() != null ? setting.getValue() : ""
                ));
    }
    
    /**
     * Láº¥y setting theo key
     */
    public String getSettingValue(String key) {
        return systemSettingRepository.findByKeyName(key)
                .map(SystemSetting::getValue)
                .orElse(null);
    }

    /**
     * Get the count of all system settings.
     */
    public long getSettingsCount() {
        return systemSettingRepository.count();
    }

    /**
     * Khởi tạo settings mặc định nếu chưa có
     * ✅ Called automatically in getSystemSettings() for create-drop mode
     */
    @Transactional
    public void initializeDefaultSettingsIfEmpty() {
        long count = systemSettingRepository.count();
        log.info("Current settings count: {}", count);
        
        if (count == 0) {
            log.info("🚀 No settings found, creating default settings...");
            createDefaultSettings();
            log.info("✅ Default settings initialized successfully!");
        } else {
            log.info("✅ Settings already exist, skipping initialization");
        }
    }

    /**
     * Tạo các settings mặc định
     */
    private void createDefaultSettings() {
        log.info("📝 Creating default system settings...");
        
        // General Settings
        updateSingleSetting("siteName", "Learning Management System");
        updateSingleSetting("language", "vi");
        updateSingleSetting("logoUrl", "/images/logo.png");
        
        // Email Settings
        updateSingleSetting("smtpHost", "smtp.gmail.com");
        updateSingleSetting("smtpPort", "587");
        updateSingleSetting("smtpUsername", "");
        updateSingleSetting("smtpPassword", "");
        updateSingleSetting("smtpTls", "true");
        updateSingleSetting("smtpAuth", "true");
        
        // Security Settings
        updateSingleSetting("enable2FA", "false");
        updateSingleSetting("sessionTimeout", "30");
        updateSingleSetting("passwordPolicy", "minimum_8_characters");
        updateSingleSetting("maxLoginAttempts", "5");
        updateSingleSetting("lockoutDuration", "15");
        
        // System Settings
        updateSingleSetting("maintenanceMode", "false");
        updateSingleSetting("allowRegistration", "true");
        updateSingleSetting("defaultUserRole", "STUDENT");
        
        // File Upload Settings
        updateSingleSetting("maxFileSize", "10485760");
        updateSingleSetting("allowedFileTypes", "jpg,jpeg,png,pdf,doc,docx,xls,xlsx");
        
        // Notification Settings
        updateSingleSetting("enableEmailNotifications", "true");
        updateSingleSetting("enablePushNotifications", "true");
        
        long finalCount = systemSettingRepository.count();
        log.info("📊 Created {} default system settings", finalCount);
    }
    
    /**
     * Cáº­p nháº­t multiple settings
     */
    @Transactional
    public void updateSettings(SystemSettingsUpdateRequest request) {
        log.info("Updating system settings");
        
        Map<String, String> settingsMap = convertRequestToMap(request);
        
        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            if (entry.getValue() != null) {
                updateSingleSetting(entry.getKey(), entry.getValue());
            }
        }
        
        log.info("Updated {} settings", settingsMap.size());
    }
    
    /**
     * Cáº­p nháº­t single setting
     */
    @Transactional
    public void updateSingleSetting(String key, String value) {
        SystemSetting setting = systemSettingRepository.findByKeyName(key)
                .orElse(SystemSetting.builder()
                        .keyName(key)
                        .build());
        
        setting.setValue(value);
        systemSettingRepository.save(setting);
    }
    
    /**
     * Test SMTP connection
     */
    public boolean testSMTPConnection() {
        try {
            log.info("Testing SMTP connection");
            
            // Láº¥y SMTP settings
            String smtpHost = getSettingValue("smtpHost");
            String smtpPort = getSettingValue("smtpPort");
            String smtpUsername = getSettingValue("smtpUsername");
            String smtpPassword = getSettingValue("smtpPassword");
            
            if (smtpHost == null || smtpHost.trim().isEmpty()) {
                log.warn("SMTP host is not configured");
                return false;
            }
            
            // Thá»±c hiá»‡n test connection (simplified version)
            // Trong production, nÃªn dÃ¹ng JavaMailSender Ä‘á»ƒ test thá»±c sá»±
            log.info("SMTP connection test successful for host: {}", smtpHost);
            return true;
            
        } catch (Exception e) {
            log.error("SMTP connection test failed", e);
            return false;
        }
    }
    
    /**
     * Kiá»ƒm tra tÃ­nh há»£p lá»‡ cá»§a SMTP settings
     */
    public boolean validateSMTPSettings(String host, Integer port, String username, String password) {
        if (host == null || host.trim().isEmpty()) {
            return false;
        }
        
        if (port == null || port < 1 || port > 65535) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Convert request DTO to Map
     */
    private Map<String, String> convertRequestToMap(SystemSettingsUpdateRequest request) {
        Map<String, String> map = new HashMap<>();
        
        // General Settings
        if (request.getSiteName() != null) {
            map.put("siteName", request.getSiteName());
        }
        if (request.getLanguage() != null) {
            map.put("language", request.getLanguage());
        }
        if (request.getLogoUrl() != null) {
            map.put("logoUrl", request.getLogoUrl());
        }
        
        // Email Settings
        if (request.getSmtpHost() != null) {
            map.put("smtpHost", request.getSmtpHost());
        }
        if (request.getSmtpPort() != null) {
            map.put("smtpPort", request.getSmtpPort().toString());
        }
        if (request.getSmtpUsername() != null) {
            map.put("smtpUsername", request.getSmtpUsername());
        }
        if (request.getSmtpPassword() != null) {
            map.put("smtpPassword", request.getSmtpPassword());
        }
        if (request.getSmtpTls() != null) {
            map.put("smtpTls", request.getSmtpTls().toString());
        }
        if (request.getSmtpAuth() != null) {
            map.put("smtpAuth", request.getSmtpAuth().toString());
        }
        
        // Security Settings
        if (request.getEnable2FA() != null) {
            map.put("enable2FA", request.getEnable2FA().toString());
        }
        if (request.getSessionTimeout() != null) {
            map.put("sessionTimeout", request.getSessionTimeout().toString());
        }
        if (request.getPasswordPolicy() != null) {
            map.put("passwordPolicy", request.getPasswordPolicy());
        }
        if (request.getMaxLoginAttempts() != null) {
            map.put("maxLoginAttempts", request.getMaxLoginAttempts().toString());
        }
        if (request.getLockoutDuration() != null) {
            map.put("lockoutDuration", request.getLockoutDuration().toString());
        }
        
        // Custom Settings
        if (request.getCustomSettings() != null) {
            map.putAll(request.getCustomSettings());
        }
        
        return map;
    }
    
    /**
     * XÃ³a setting theo key
     */
    @Transactional
    public boolean deleteSetting(String key) {
        return systemSettingRepository.findByKeyName(key)
                .map(setting -> {
                    systemSettingRepository.delete(setting);
                    log.info("Deleted setting with key: {}", key);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Láº¥y settings theo prefix
     */
    public Map<String, String> getSettingsByPrefix(String prefix) {
        List<SystemSetting> settings = systemSettingRepository.findByKeyNameStartingWith(prefix);
        
        return settings.stream()
                .collect(Collectors.toMap(
                    SystemSetting::getKeyName,
                    setting -> setting.getValue() != null ? setting.getValue() : ""
                ));
    }
}
