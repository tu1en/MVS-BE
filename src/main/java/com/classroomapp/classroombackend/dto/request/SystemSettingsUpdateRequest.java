package com.classroomapp.classroombackend.dto.request;

import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho request cáº­p nháº­t system settings
 */
@Data
public class SystemSettingsUpdateRequest {
    
    // General Settings
    @Size(max = 255, message = "Site name khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 255 kÃ½ tá»±")
    private String siteName;
    
    @Size(max = 100, message = "Language khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 100 kÃ½ tá»±")
    private String language;
    
    private String logoUrl;
    
    // Email Settings
    @Size(max = 255, message = "SMTP Host khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 255 kÃ½ tá»±")
    private String smtpHost;
    
    @Min(value = 1, message = "SMTP Port pháº£i tá»« 1-65535")
    @Max(value = 65535, message = "SMTP Port pháº£i tá»« 1-65535")
    private Integer smtpPort;
    
    @Email(message = "SMTP Username pháº£i lÃ  email há»£p lá»‡")
    private String smtpUsername;
    
    private String smtpPassword;
    
    private Boolean smtpTls;
    
    private Boolean smtpAuth;
    
    // Security Settings
    private Boolean enable2FA;
    
    @Min(value = 5, message = "Session timeout tá»‘i thiá»ƒu 5 phÃºt")
    @Max(value = 1440, message = "Session timeout tá»‘i Ä‘a 1440 phÃºt (24h)")
    private Integer sessionTimeout;
    
    @Size(max = 1000, message = "Password policy khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String passwordPolicy;
    
    @Min(value = 1, message = "Max login attempts tá»‘i thiá»ƒu 1")
    @Max(value = 20, message = "Max login attempts tá»‘i Ä‘a 20")
    private Integer maxLoginAttempts;
    
    @Min(value = 1, message = "Lockout duration tá»‘i thiá»ƒu 1 phÃºt")
    @Max(value = 1440, message = "Lockout duration tá»‘i Ä‘a 1440 phÃºt")
    private Integer lockoutDuration;
    
    // Additional dynamic settings
    private Map<String, String> customSettings;
}
