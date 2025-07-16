package com.classroomapp.classroombackend.model.administration;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing system monitoring metrics
 */
@Entity
@Table(name = "system_monitoring", indexes = {
    @Index(name = "idx_monitoring_timestamp", columnList = "timestamp"),
    @Index(name = "idx_monitoring_metric", columnList = "metric_name"),
    @Index(name = "idx_monitoring_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMonitoring {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "metric_name", length = 100, nullable = false)
    private String metricName;
    
    @Column(name = "metric_value")
    private Double metricValue;
    
    @Column(name = "metric_unit", length = 20)
    private String metricUnit;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    private MonitoringCategory category;
    
    @Column(name = "instance_id", length = 100)
    private String instanceId;
    
    @Column(name = "host_name", length = 100)
    private String hostName;
    
    @Column(name = "tags", length = 500)
    private String tags; // JSON string for additional tags
    
    @Column(name = "threshold_warning")
    private Double thresholdWarning;
    
    @Column(name = "threshold_critical")
    private Double thresholdCritical;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MetricStatus status = MetricStatus.NORMAL;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * Monitoring category enumeration
     */
    public enum MonitoringCategory {
        SYSTEM("Hệ thống"),
        DATABASE("Cơ sở dữ liệu"),
        APPLICATION("Ứng dụng"),
        NETWORK("Mạng"),
        SECURITY("Bảo mật"),
        PERFORMANCE("Hiệu suất"),
        STORAGE("Lưu trữ"),
        MEMORY("Bộ nhớ"),
        CPU("CPU"),
        DISK("Ổ đĩa"),
        USER_ACTIVITY("Hoạt động người dùng"),
        ERROR_RATE("Tỷ lệ lỗi"),
        RESPONSE_TIME("Thời gian phản hồi"),
        THROUGHPUT("Throughput"),
        AVAILABILITY("Khả dụng");
        
        private final String description;
        
        MonitoringCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Metric status enumeration
     */
    public enum MetricStatus {
        NORMAL("Bình thường"),
        WARNING("Cảnh báo"),
        CRITICAL("Nghiêm trọng"),
        UNKNOWN("Không xác định");
        
        private final String description;
        
        MetricStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getColor() {
            switch (this) {
                case NORMAL:
                    return "#52c41a";
                case WARNING:
                    return "#faad14";
                case CRITICAL:
                    return "#ff4d4f";
                case UNKNOWN:
                default:
                    return "#d9d9d9";
            }
        }
    }
    
    // Business logic methods
    
    /**
     * Update metric status based on thresholds
     */
    public void updateStatus() {
        if (metricValue == null) {
            this.status = MetricStatus.UNKNOWN;
            return;
        }
        
        if (thresholdCritical != null && metricValue >= thresholdCritical) {
            this.status = MetricStatus.CRITICAL;
        } else if (thresholdWarning != null && metricValue >= thresholdWarning) {
            this.status = MetricStatus.WARNING;
        } else {
            this.status = MetricStatus.NORMAL;
        }
    }
    
    /**
     * Check if metric is in warning state
     */
    public boolean isWarning() {
        return status == MetricStatus.WARNING;
    }
    
    /**
     * Check if metric is in critical state
     */
    public boolean isCritical() {
        return status == MetricStatus.CRITICAL;
    }
    
    /**
     * Check if metric needs attention
     */
    public boolean needsAttention() {
        return status == MetricStatus.WARNING || status == MetricStatus.CRITICAL;
    }
    
    /**
     * Get formatted metric value
     */
    public String getFormattedValue() {
        if (metricValue == null) {
            return "N/A";
        }
        
        String unit = metricUnit != null ? " " + metricUnit : "";
        
        // Format based on value range
        if (metricValue >= 1000000) {
            return String.format("%.2fM%s", metricValue / 1000000, unit);
        } else if (metricValue >= 1000) {
            return String.format("%.2fK%s", metricValue / 1000, unit);
        } else if (metricValue % 1 == 0) {
            return String.format("%.0f%s", metricValue, unit);
        } else {
            return String.format("%.2f%s", metricValue, unit);
        }
    }
    
    /**
     * Get percentage of threshold usage
     */
    public Double getThresholdUsagePercentage() {
        if (metricValue == null) {
            return null;
        }
        
        Double threshold = thresholdCritical != null ? thresholdCritical : thresholdWarning;
        if (threshold == null || threshold == 0) {
            return null;
        }
        
        return (metricValue / threshold) * 100;
    }
    
    /**
     * Get metric summary
     */
    public String getMetricSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(metricName).append(": ").append(getFormattedValue());
        summary.append(" (").append(status.getDescription()).append(")");
        
        if (hostName != null) {
            summary.append(" [").append(hostName).append("]");
        }
        
        return summary.toString();
    }
    
    /**
     * Check if metric is system-critical
     */
    public boolean isSystemCritical() {
        return category == MonitoringCategory.SYSTEM ||
               category == MonitoringCategory.DATABASE ||
               category == MonitoringCategory.SECURITY;
    }
    
    /**
     * Get time since last update
     */
    public String getTimeSinceUpdate() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(timestamp, now).toMinutes();
        
        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else {
            long hours = minutes / 60;
            if (hours < 24) {
                return hours + " giờ trước";
            } else {
                long days = hours / 24;
                return days + " ngày trước";
            }
        }
    }
    
    /**
     * Check if metric data is stale
     */
    public boolean isStale(int maxAgeMinutes) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(timestamp, now).toMinutes();
        return minutes > maxAgeMinutes;
    }
    
    /**
     * Create monitoring metric builder
     */
    public static MonitoringBuilder builder() {
        return new MonitoringBuilder();
    }
    
    /**
     * Builder class for SystemMonitoring
     */
    public static class MonitoringBuilder {
        private SystemMonitoring monitoring = new SystemMonitoring();
        
        public MonitoringBuilder metric(String name, Double value, String unit) {
            monitoring.setMetricName(name);
            monitoring.setMetricValue(value);
            monitoring.setMetricUnit(unit);
            return this;
        }
        
        public MonitoringBuilder category(MonitoringCategory category) {
            monitoring.setCategory(category);
            return this;
        }
        
        public MonitoringBuilder instance(String instanceId, String hostName) {
            monitoring.setInstanceId(instanceId);
            monitoring.setHostName(hostName);
            return this;
        }
        
        public MonitoringBuilder thresholds(Double warning, Double critical) {
            monitoring.setThresholdWarning(warning);
            monitoring.setThresholdCritical(critical);
            return this;
        }
        
        public MonitoringBuilder description(String description) {
            monitoring.setDescription(description);
            return this;
        }
        
        public MonitoringBuilder tags(String tags) {
            monitoring.setTags(tags);
            return this;
        }
        
        public SystemMonitoring build() {
            monitoring.updateStatus();
            return monitoring;
        }
    }
    
    // Common metric factory methods
    
    /**
     * Create CPU usage metric
     */
    public static SystemMonitoring createCpuUsage(Double percentage, String hostName) {
        return builder()
            .metric("cpu_usage", percentage, "%")
            .category(MonitoringCategory.CPU)
            .instance(null, hostName)
            .thresholds(80.0, 95.0)
            .description("CPU usage percentage")
            .build();
    }
    
    /**
     * Create memory usage metric
     */
    public static SystemMonitoring createMemoryUsage(Double percentage, String hostName) {
        return builder()
            .metric("memory_usage", percentage, "%")
            .category(MonitoringCategory.MEMORY)
            .instance(null, hostName)
            .thresholds(85.0, 95.0)
            .description("Memory usage percentage")
            .build();
    }
    
    /**
     * Create disk usage metric
     */
    public static SystemMonitoring createDiskUsage(Double percentage, String hostName) {
        return builder()
            .metric("disk_usage", percentage, "%")
            .category(MonitoringCategory.DISK)
            .instance(null, hostName)
            .thresholds(80.0, 90.0)
            .description("Disk usage percentage")
            .build();
    }
    
    /**
     * Create response time metric
     */
    public static SystemMonitoring createResponseTime(Double milliseconds, String endpoint) {
        return builder()
            .metric("response_time", milliseconds, "ms")
            .category(MonitoringCategory.RESPONSE_TIME)
            .description("API response time for " + endpoint)
            .thresholds(1000.0, 5000.0)
            .build();
    }
    
    /**
     * Create error rate metric
     */
    public static SystemMonitoring createErrorRate(Double percentage, String component) {
        return builder()
            .metric("error_rate", percentage, "%")
            .category(MonitoringCategory.ERROR_RATE)
            .description("Error rate for " + component)
            .thresholds(5.0, 10.0)
            .build();
    }
    
    /**
     * Create active users metric
     */
    public static SystemMonitoring createActiveUsers(Double count) {
        return builder()
            .metric("active_users", count, "users")
            .category(MonitoringCategory.USER_ACTIVITY)
            .description("Number of active users")
            .build();
    }
    
    /**
     * Create database connections metric
     */
    public static SystemMonitoring createDatabaseConnections(Double count, Double maxConnections) {
        return builder()
            .metric("db_connections", count, "connections")
            .category(MonitoringCategory.DATABASE)
            .description("Active database connections")
            .thresholds(maxConnections * 0.8, maxConnections * 0.95)
            .build();
    }
}
