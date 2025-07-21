package com.doproject.dto.request;

import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho request cập nhật system settings
 */
@Data
public class SystemSettingsUpdateRequest {
    
    // General Settings
    @Size(max = 255, message = "Site name không được vượt quá 255 ký tự")
    private String siteName;
    
    @Size(max = 100, message = "Language không được vượt quá 100 ký tự")
    private String language;
    
    private String logoUrl;
    
    // Email Settings
    @Size(max = 255, message = "SMTP Host không được vượt quá 255 ký tự")
    private String smtpHost;
    
    @Min(value = 1, message = "SMTP Port phải từ 1-65535")
    @Max(value = 65535, message = "SMTP Port phải từ 1-65535")
    private Integer smtpPort;
    
    @Email(message = "SMTP Username phải là email hợp lệ")
    private String smtpUsername;
    
    private String smtpPassword;
    
    private Boolean smtpTls;
    
    private Boolean smtpAuth;
    
    // Security Settings
    private Boolean enable2FA;
    
    @Min(value = 5, message = "Session timeout tối thiểu 5 phút")
    @Max(value = 1440, message = "Session timeout tối đa 1440 phút (24h)")
    private Integer sessionTimeout;
    
    @Size(max = 1000, message = "Password policy không được vượt quá 1000 ký tự")
    private String passwordPolicy;
    
    @Min(value = 1, message = "Max login attempts tối thiểu 1")
    @Max(value = 20, message = "Max login attempts tối đa 20")
    private Integer maxLoginAttempts;
    
    @Min(value = 1, message = "Lockout duration tối thiểu 1 phút")
    @Max(value = 1440, message = "Lockout duration tối đa 1440 phút")
    private Integer lockoutDuration;
    
    // Additional dynamic settings
    private Map<String, String> customSettings;
}
