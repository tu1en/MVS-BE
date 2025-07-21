package com.classroomapp.classroombackend.service.hrmanagement.shift;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Shift Template management
 * Cung cấp business logic cho quản lý mẫu ca làm việc
 */
public interface ShiftTemplateService {

    /**
     * Tạo shift template mới
     */
    ShiftTemplate createTemplate(ShiftTemplate template);

    /**
     * Cập nhật shift template
     */
    ShiftTemplate updateTemplate(Long id, ShiftTemplate template);

    /**
     * Xóa shift template (soft delete - set inactive)
     */
    void deleteTemplate(Long id);

    /**
     * Tìm template theo ID
     */
    Optional<ShiftTemplate> findById(Long id);

    /**
     * Tìm template theo code
     */
    Optional<ShiftTemplate> findByCode(String templateCode);

    /**
     * Lấy tất cả templates đang hoạt động
     */
    List<ShiftTemplate> findAllActiveTemplates();

    /**
     * Search templates với pagination
     */
    Page<ShiftTemplate> searchTemplates(String search, Boolean isActive, Pageable pageable);

    /**
     * Tìm templates theo khoảng thời gian
     */
    List<ShiftTemplate> findTemplatesByTimeRange(LocalTime startTime, LocalTime endTime);

    /**
     * Tìm templates có thể làm tăng ca
     */
    List<ShiftTemplate> findOvertimeEligibleTemplates();

    /**
     * Kiểm tra xung đột thời gian với templates khác
     */
    List<ShiftTemplate> findConflictingTemplates(LocalTime startTime, LocalTime endTime, Long excludeId);

    /**
     * Validate template trước khi lưu
     */
    void validateTemplate(ShiftTemplate template);

    /**
     * Kiểm tra xem template có thể xóa không (không có assignments)
     */
    boolean canDeleteTemplate(Long templateId);

    /**
     * Lấy templates được sử dụng nhiều nhất
     */
    List<ShiftTemplate> getMostUsedTemplates(int limit);

    /**
     * Tìm templates tương tự (cùng thời gian)
     */
    List<ShiftTemplate> findSimilarTemplates(LocalTime startTime, LocalTime endTime, Long excludeId);

    /**
     * Cập nhật trạng thái active
     */
    void updateActiveStatus(Long id, Boolean isActive);

    /**
     * Cập nhật thứ tự sắp xếp
     */
    void updateSortOrder(Long id, Integer sortOrder);

    /**
     * Lấy thống kê templates
     */
    TemplateStatistics getTemplateStatistics();

    /**
     * Tìm templates có break time
     */
    List<ShiftTemplate> findTemplatesWithBreak();

    /**
     * Tìm templates available cho employee và ngày cụ thể
     */
    List<ShiftTemplate> findAvailableTemplatesForEmployeeAndDate(Long employeeId, java.time.LocalDate date);

    /**
     * Bulk update templates
     */
    void bulkUpdateTemplates(List<ShiftTemplate> templates);

    /**
     * Export templates to CSV/Excel
     */
    byte[] exportTemplates(String format);

    /**
     * Import templates from CSV/Excel
     */
    List<ShiftTemplate> importTemplates(byte[] fileData, String format);

    /**
     * DTO class cho template statistics
     */
    class TemplateStatistics {
        private long activeCount;
        private long inactiveCount;
        private long totalCount;
        private long totalAssignments;

        // Constructors, getters, setters
        public TemplateStatistics() {}

        public TemplateStatistics(long activeCount, long inactiveCount, long totalCount, long totalAssignments) {
            this.activeCount = activeCount;
            this.inactiveCount = inactiveCount;
            this.totalCount = totalCount;
            this.totalAssignments = totalAssignments;
        }

        public long getActiveCount() { return activeCount; }
        public void setActiveCount(long activeCount) { this.activeCount = activeCount; }

        public long getInactiveCount() { return inactiveCount; }
        public void setInactiveCount(long inactiveCount) { this.inactiveCount = inactiveCount; }

        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

        public long getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(long totalAssignments) { this.totalAssignments = totalAssignments; }

        public double getActivePercentage() {
            return totalCount > 0 ? (double) activeCount / totalCount * 100 : 0;
        }

        public double getAverageAssignmentsPerTemplate() {
            return activeCount > 0 ? (double) totalAssignments / activeCount : 0;
        }
    }
}
