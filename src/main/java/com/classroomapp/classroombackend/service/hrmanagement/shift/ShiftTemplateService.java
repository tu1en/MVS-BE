package com.classroomapp.classroombackend.service.hrmanagement.shift;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Shift Template management
 * Cung cáº¥p business logic cho quáº£n lÃ½ máº«u ca lÃ m viá»‡c
 */
public interface ShiftTemplateService {

    /**
     * Táº¡o shift template má»›i
     */
    ShiftTemplate createTemplate(ShiftTemplate template);

    /**
     * Cáº­p nháº­t shift template
     */
    ShiftTemplate updateTemplate(Long id, ShiftTemplate template);

    /**
     * XÃ³a shift template (soft delete - set inactive)
     */
    void deleteTemplate(Long id);

    /**
     * TÃ¬m template theo ID
     */
    Optional<ShiftTemplate> findById(Long id);

    /**
     * TÃ¬m template theo code
     */
    Optional<ShiftTemplate> findByCode(String templateCode);

    /**
     * Láº¥y táº¥t cáº£ templates Ä‘ang hoáº¡t Ä‘á»™ng
     */
    List<ShiftTemplate> findAllActiveTemplates();

    /**
     * Search templates vá»›i pagination
     */
    Page<ShiftTemplate> searchTemplates(String search, Boolean isActive, Pageable pageable);

    /**
     * TÃ¬m templates theo khoáº£ng thá»i gian
     */
    List<ShiftTemplate> findTemplatesByTimeRange(LocalTime startTime, LocalTime endTime);

    /**
     * TÃ¬m templates cÃ³ thá»ƒ lÃ m tÄƒng ca
     */
    List<ShiftTemplate> findOvertimeEligibleTemplates();

    /**
     * Kiá»ƒm tra xung Ä‘á»™t thá»i gian vá»›i templates khÃ¡c
     */
    List<ShiftTemplate> findConflictingTemplates(LocalTime startTime, LocalTime endTime, Long excludeId);

    /**
     * Validate template trÆ°á»›c khi lÆ°u
     */
    void validateTemplate(ShiftTemplate template);

    /**
     * Kiá»ƒm tra xem template cÃ³ thá»ƒ xÃ³a khÃ´ng (khÃ´ng cÃ³ assignments)
     */
    boolean canDeleteTemplate(Long templateId);

    /**
     * Láº¥y templates Ä‘Æ°á»£c sá»­ dá»¥ng nhiá»u nháº¥t
     */
    List<ShiftTemplate> getMostUsedTemplates(int limit);

    /**
     * TÃ¬m templates tÆ°Æ¡ng tá»± (cÃ¹ng thá»i gian)
     */
    List<ShiftTemplate> findSimilarTemplates(LocalTime startTime, LocalTime endTime, Long excludeId);

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i active
     */
    void updateActiveStatus(Long id, Boolean isActive);

    /**
     * Cáº­p nháº­t thá»© tá»± sáº¯p xáº¿p
     */
    void updateSortOrder(Long id, Integer sortOrder);

    /**
     * Láº¥y thá»‘ng kÃª templates
     */
    TemplateStatistics getTemplateStatistics();

    /**
     * TÃ¬m templates cÃ³ break time
     */
    List<ShiftTemplate> findTemplatesWithBreak();

    /**
     * TÃ¬m templates available cho employee vÃ  ngÃ y cá»¥ thá»ƒ
     */
    List<ShiftTemplate> findAvailableTemplatesForEmployeeAndDate(Long assignedUserId, java.time.LocalDate date);

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
