package com.classroomapp.classroombackend.model.administration;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing system configuration parameters
 */
@Entity
@Table(name = "system_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_key", length = 100, nullable = false, unique = true)
    private String configKey;
    
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;
    
    @Column(name = "config_name", length = 200, nullable = false)
    private String configName;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 20, nullable = false)
    private DataType dataType = DataType.STRING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "config_category", length = 30, nullable = false)
    private ConfigCategory configCategory = ConfigCategory.GENERAL;
    
    @Column(name = "is_system_config", columnDefinition = "BIT DEFAULT 0")
    private Boolean isSystemConfig = false;
    
    @Column(name = "is_encrypted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isEncrypted = false;
    
    @Column(name = "is_required", columnDefinition = "BIT DEFAULT 0")
    private Boolean isRequired = false;
    
    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;
    
    @Column(name = "validation_pattern", length = 500)
    private String validationPattern;
    
    @Column(name = "allowed_values", columnDefinition = "TEXT")
    private String allowedValues; // JSON array for enum-like values
    
    @Column(name = "min_value")
    private Double minValue;
    
    @Column(name = "max_value")
    private Double maxValue;
    
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    /**
     * Data type enumeration
     */
    public enum DataType {
        STRING("Chuỗi"),
        INTEGER("Số nguyên"),
        DECIMAL("Số thập phân"),
        BOOLEAN("Boolean"),
        DATE("Ngày"),
        DATETIME("Ngày giờ"),
        JSON("JSON"),
        EMAIL("Email"),
        URL("URL"),
        PASSWORD("Mật khẩu"),
        FILE_PATH("Đường dẫn file");
        
        private final String description;
        
        DataType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Configuration category enumeration
     */
    public enum ConfigCategory {
        GENERAL("Tổng quát"),
        SECURITY("Bảo mật"),
        DATABASE("Cơ sở dữ liệu"),
        EMAIL("Email"),
        FILE_STORAGE("Lưu trữ file"),
        AUTHENTICATION("Xác thực"),
        AUTHORIZATION("Phân quyền"),
        LOGGING("Logging"),
        MONITORING("Giám sát"),
        BACKUP("Sao lưu"),
        INTEGRATION("Tích hợp"),
        UI("Giao diện"),
        PERFORMANCE("Hiệu suất"),
        NOTIFICATION("Thông báo");
        
        private final String description;
        
        ConfigCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business logic methods
    
    /**
     * Get typed value based on data type
     */
    public Object getTypedValue() {
        if (configValue == null) {
            return getTypedDefaultValue();
        }
        
        try {
            switch (dataType) {
                case INTEGER:
                    return Integer.parseInt(configValue);
                case DECIMAL:
                    return Double.parseDouble(configValue);
                case BOOLEAN:
                    return Boolean.parseBoolean(configValue);
                case DATE:
                    return java.time.LocalDate.parse(configValue);
                case DATETIME:
                    return LocalDateTime.parse(configValue);
                case JSON:
                    return configValue; // Return as string, parse externally
                default:
                    return configValue;
            }
        } catch (Exception e) {
            return getTypedDefaultValue();
        }
    }
    
    /**
     * Get typed default value
     */
    public Object getTypedDefaultValue() {
        if (defaultValue == null) {
            return getDefaultForType();
        }
        
        try {
            switch (dataType) {
                case INTEGER:
                    return Integer.parseInt(defaultValue);
                case DECIMAL:
                    return Double.parseDouble(defaultValue);
                case BOOLEAN:
                    return Boolean.parseBoolean(defaultValue);
                case DATE:
                    return java.time.LocalDate.parse(defaultValue);
                case DATETIME:
                    return LocalDateTime.parse(defaultValue);
                default:
                    return defaultValue;
            }
        } catch (Exception e) {
            return getDefaultForType();
        }
    }
    
    /**
     * Get default value for data type
     */
    private Object getDefaultForType() {
        switch (dataType) {
            case INTEGER:
                return 0;
            case DECIMAL:
                return 0.0;
            case BOOLEAN:
                return false;
            case DATE:
                return java.time.LocalDate.now();
            case DATETIME:
                return LocalDateTime.now();
            default:
                return "";
        }
    }
    
    /**
     * Validate configuration value
     */
    public boolean isValidValue(String value) {
        if (value == null) {
            return !Boolean.TRUE.equals(isRequired);
        }
        
        // Check data type validation
        try {
            switch (dataType) {
                case INTEGER:
                    int intValue = Integer.parseInt(value);
                    if (minValue != null && intValue < minValue) return false;
                    if (maxValue != null && intValue > maxValue) return false;
                    break;
                case DECIMAL:
                    double doubleValue = Double.parseDouble(value);
                    if (minValue != null && doubleValue < minValue) return false;
                    if (maxValue != null && doubleValue > maxValue) return false;
                    break;
                case BOOLEAN:
                    Boolean.parseBoolean(value);
                    break;
                case EMAIL:
                    if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return false;
                    break;
                case URL:
                    if (!value.matches("^https?://.*")) return false;
                    break;
            }
        } catch (Exception e) {
            return false;
        }
        
        // Check validation pattern
        if (validationPattern != null && !value.matches(validationPattern)) {
            return false;
        }
        
        // Check allowed values
        if (allowedValues != null && !allowedValues.trim().isEmpty()) {
            // Simple check - can be enhanced with JSON parsing
            return allowedValues.contains(value);
        }
        
        return true;
    }
    
    /**
     * Check if configuration can be modified
     */
    public boolean canBeModified() {
        return !Boolean.TRUE.equals(isSystemConfig);
    }
    
    /**
     * Check if configuration can be deleted
     */
    public boolean canBeDeleted() {
        return !Boolean.TRUE.equals(isSystemConfig) && !Boolean.TRUE.equals(isRequired);
    }
    
    /**
     * Set configuration value with validation
     */
    public boolean setValue(String value) {
        if (isValidValue(value)) {
            this.configValue = value;
            return true;
        }
        return false;
    }
    
    /**
     * Reset to default value
     */
    public void resetToDefault() {
        this.configValue = this.defaultValue;
    }
    
    /**
     * Check if value is default
     */
    public boolean isDefaultValue() {
        if (configValue == null && defaultValue == null) {
            return true;
        }
        
        if (configValue == null || defaultValue == null) {
            return false;
        }
        
        return configValue.equals(defaultValue);
    }
    
    /**
     * Get display value (masked for passwords)
     */
    public String getDisplayValue() {
        if (dataType == DataType.PASSWORD && configValue != null) {
            return "*".repeat(Math.min(configValue.length(), 8));
        }
        
        return configValue;
    }
    
    /**
     * Get configuration summary
     */
    public String getConfigSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Config: ").append(configName);
        summary.append(", Category: ").append(configCategory.getDescription());
        summary.append(", Type: ").append(dataType.getDescription());
        summary.append(", Value: ").append(getDisplayValue());
        
        if (Boolean.TRUE.equals(isSystemConfig)) {
            summary.append(" (System Config)");
        }
        
        if (Boolean.TRUE.equals(isRequired)) {
            summary.append(" (Required)");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if configuration is currently active
     */
    public boolean isCurrentlyActive() {
        return Boolean.TRUE.equals(isActive);
    }
    
    /**
     * Activate configuration
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * Deactivate configuration
     */
    public void deactivate() {
        if (canBeModified() && !Boolean.TRUE.equals(isRequired)) {
            this.isActive = false;
        } else {
            throw new IllegalStateException("Không thể vô hiệu hóa cấu hình bắt buộc hoặc hệ thống");
        }
    }
    
    /**
     * Clone configuration for customization
     */
    public SystemConfiguration cloneForCustomization(String newKey, String newName) {
        SystemConfiguration clone = new SystemConfiguration();
        clone.setConfigKey(newKey);
        clone.setConfigName(newName);
        clone.setConfigValue(this.configValue);
        clone.setDefaultValue(this.defaultValue);
        clone.setDescription("Sao chép từ: " + this.configName);
        clone.setDataType(this.dataType);
        clone.setConfigCategory(this.configCategory);
        clone.setIsSystemConfig(false);
        clone.setIsEncrypted(this.isEncrypted);
        clone.setIsRequired(false);
        clone.setIsActive(true);
        clone.setValidationPattern(this.validationPattern);
        clone.setAllowedValues(this.allowedValues);
        clone.setMinValue(this.minValue);
        clone.setMaxValue(this.maxValue);
        clone.setSortOrder(this.sortOrder);
        
        return clone;
    }
    
    /**
     * Validate configuration data
     */
    public boolean isValidConfiguration() {
        if (configKey == null || configKey.trim().isEmpty()) {
            return false;
        }
        
        if (configName == null || configName.trim().isEmpty()) {
            return false;
        }
        
        if (dataType == null) {
            return false;
        }
        
        if (configCategory == null) {
            return false;
        }
        
        // Config key should be uppercase and contain only letters, numbers, dots, and underscores
        if (!configKey.matches("^[A-Z0-9._]+$")) {
            return false;
        }
        
        // Validate current value if set
        if (configValue != null && !isValidValue(configValue)) {
            return false;
        }
        
        return true;
    }
}
