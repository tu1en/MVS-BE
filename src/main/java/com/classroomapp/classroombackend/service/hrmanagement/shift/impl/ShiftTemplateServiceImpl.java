package com.classroomapp.classroombackend.service.hrmanagement.shift.impl;

import com.classroomapp.classroombackend.exception.BusinessLogicException;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftTemplate;
import com.classroomapp.classroombackend.repository.hrmanagement.ShiftTemplateRepository;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation cá»§a ShiftTemplateService
 * Xá»­ lÃ½ business logic cho shift template management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShiftTemplateServiceImpl implements ShiftTemplateService {

    private final ShiftTemplateRepository shiftTemplateRepository;

    @Override
    public ShiftTemplate createTemplate(ShiftTemplate template) {
        log.info("Táº¡o shift template má»›i: {}", template.getTemplateName());
        
        validateTemplate(template);
        
        // Kiá»ƒm tra template code Ä‘Ã£ tá»“n táº¡i
        if (shiftTemplateRepository.existsByTemplateCode(template.getTemplateCode())) {
            throw new BusinessLogicException("MÃ£ template Ä‘Ã£ tá»“n táº¡i: " + template.getTemplateCode());
        }
        
        // Kiá»ƒm tra xung Ä‘á»™t thá»i gian
        List<ShiftTemplate> conflicts = findConflictingTemplates(
            template.getStartTime(), template.getEndTime(), null);
        if (!conflicts.isEmpty()) {
            log.warn("PhÃ¡t hiá»‡n xung Ä‘á»™t thá»i gian vá»›i {} templates khÃ¡c", conflicts.size());
        }
        
        // Set default values
        if (template.getSortOrder() == null) {
            template.setSortOrder(getNextSortOrder());
        }
        
        ShiftTemplate saved = shiftTemplateRepository.save(template);
        log.info("ÄÃ£ táº¡o shift template vá»›i ID: {}", saved.getId());
        
        return saved;
    }

    @Override
    public ShiftTemplate updateTemplate(Long id, ShiftTemplate template) {
        log.info("Cáº­p nháº­t shift template ID: {}", id);
        
        ShiftTemplate existing = shiftTemplateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y shift template vá»›i ID: " + id));
        
        validateTemplate(template);
        
        // Kiá»ƒm tra template code Ä‘Ã£ tá»“n táº¡i (exclude current)
        if (!existing.getTemplateCode().equals(template.getTemplateCode()) &&
            shiftTemplateRepository.existsByTemplateCodeAndIdNot(template.getTemplateCode(), id)) {
            throw new BusinessLogicException("MÃ£ template Ä‘Ã£ tá»“n táº¡i: " + template.getTemplateCode());
        }
        
        // Kiá»ƒm tra xung Ä‘á»™t thá»i gian
        List<ShiftTemplate> conflicts = findConflictingTemplates(
            template.getStartTime(), template.getEndTime(), id);
        if (!conflicts.isEmpty()) {
            log.warn("PhÃ¡t hiá»‡n xung Ä‘á»™t thá»i gian vá»›i {} templates khÃ¡c", conflicts.size());
        }
        
        // Update fields
        existing.setTemplateName(template.getTemplateName());
        existing.setTemplateCode(template.getTemplateCode());
        existing.setDescription(template.getDescription());
        existing.setStartTime(template.getStartTime());
        existing.setEndTime(template.getEndTime());
        existing.setBreakStartTime(template.getBreakStartTime());
        existing.setBreakEndTime(template.getBreakEndTime());
        existing.setBreakDurationMinutes(template.getBreakDurationMinutes());
        existing.setTotalHours(template.getTotalHours());
        existing.setIsOvertimeEligible(template.getIsOvertimeEligible());
        existing.setColorCode(template.getColorCode());
        existing.setSortOrder(template.getSortOrder());
        
        ShiftTemplate updated = shiftTemplateRepository.save(existing);
        log.info("ÄÃ£ cáº­p nháº­t shift template ID: {}", id);
        
        return updated;
    }

    @Override
    public void deleteTemplate(Long id) {
        log.info("XÃ³a shift template ID: {}", id);
        
        if (!canDeleteTemplate(id)) {
            throw new BusinessLogicException("KhÃ´ng thá»ƒ xÃ³a template Ä‘ang Ä‘Æ°á»£c sá»­ dá»¥ng");
        }
        
        // Soft delete - set inactive
        updateActiveStatus(id, false);
        log.info("ÄÃ£ Ä‘Ã¡nh dáº¥u inactive shift template ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftTemplate> findById(Long id) {
        return shiftTemplateRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftTemplate> findByCode(String templateCode) {
        return shiftTemplateRepository.findByTemplateCodeAndIsActiveTrue(templateCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftTemplate> findAllActiveTemplates() {
        return shiftTemplateRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftTemplate> searchTemplates(String search, Boolean isActive, Pageable pageable) {
        return shiftTemplateRepository.searchTemplates(search, isActive, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftTemplate> findTemplatesByTimeRange(LocalTime startTime, LocalTime endTime) {
        return shiftTemplateRepository.findByTimeRange(startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftTemplate> findOvertimeEligibleTemplates() {
        return shiftTemplateRepository.findByIsOvertimeEligibleTrueAndIsActiveTrueOrderBySortOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftTemplate> findConflictingTemplates(LocalTime startTime, LocalTime endTime, Long excludeId) {
        return shiftTemplateRepository.findConflictingTemplates(startTime, endTime, excludeId != null ? excludeId : -1L);
    }

    @Override
    public void validateTemplate(ShiftTemplate template) {
        if (template == null) {
            throw new BusinessLogicException("Template khÃ´ng Ä‘Æ°á»£c null");
        }
        
        if (!template.isValidShift()) {
            throw new BusinessLogicException("ThÃ´ng tin ca lÃ m viá»‡c khÃ´ng há»£p lá»‡");
        }
        
        // Validate break time
        if (template.getBreakStartTime() != null && template.getBreakEndTime() != null) {
            if (!template.getBreakStartTime().isAfter(template.getStartTime()) ||
                !template.getBreakEndTime().isBefore(template.getEndTime()) ||
                !template.getBreakStartTime().isBefore(template.getBreakEndTime())) {
                throw new BusinessLogicException("Thá»i gian nghá»‰ khÃ´ng há»£p lá»‡");
            }
        }
        
        // Validate total hours
        if (template.getTotalHours() == null || template.getTotalHours().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessLogicException("Tá»•ng sá»‘ giá» pháº£i lá»›n hÆ¡n 0");
        }
        
        // Validate template code format
        if (template.getTemplateCode() == null || !template.getTemplateCode().matches("^[A-Z]{2,10}$")) {
            throw new BusinessLogicException("MÃ£ template pháº£i lÃ  chá»¯ hoa, 2-10 kÃ½ tá»±");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteTemplate(Long templateId) {
        long assignmentCount = shiftTemplateRepository.countAssignmentsByTemplate(templateId);
        return assignmentCount == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftTemplate> getMostUsedTemplates(int limit) {
        return shiftTemplateRepository.findMostUsedTemplates(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftTemplate> findSimilarTemplates(LocalTime startTime, LocalTime endTime, Long excludeId) {
        return shiftTemplateRepository.findSimilarTemplates(startTime, endTime, excludeId != null ? excludeId : -1L);
    }

    @Override
    public void updateActiveStatus(Long id, Boolean isActive) {
        int updated = shiftTemplateRepository.updateActiveStatus(id, isActive);
        if (updated == 0) {
            throw new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y shift template vá»›i ID: " + id);
        }
        log.info("ÄÃ£ cáº­p nháº­t tráº¡ng thÃ¡i active = {} cho template ID: {}", isActive, id);
    }

    @Override
    public void updateSortOrder(Long id, Integer sortOrder) {
        int updated = shiftTemplateRepository.updateSortOrder(id, sortOrder);
        if (updated == 0) {
            throw new ResourceNotFoundException("KhÃ´ng tÃ¬m tháº¥y shift template vá»›i ID: " + id);
        }
        log.info("ÄÃ£ cáº­p nháº­t sort order = {} cho template ID: {}", sortOrder, id);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateStatistics getTemplateStatistics() {
        Object[] stats = shiftTemplateRepository.getTemplateStatistics();
        if (stats != null && stats.length >= 3) {
            long activeCount = ((Number) stats[0]).longValue();
            long inactiveCount = ((Number) stats[1]).longValue();
            long totalCount = ((Number) stats[2]).longValue();
            
            return new TemplateStatistics(activeCount, inactiveCount, totalCount, 0);
        }
        return new TemplateStatistics(0, 0, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftTemplate> findTemplatesWithBreak() {
        return shiftTemplateRepository.findTemplatesWithBreak();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftTemplate> findAvailableTemplatesForEmployeeAndDate(Long assignedUserId, java.time.LocalDate date) {
        return shiftTemplateRepository.findAvailableTemplatesForEmployeeAndDate(assignedUserId, date);
    }

    @Override
    public void bulkUpdateTemplates(List<ShiftTemplate> templates) {
        log.info("Bulk update {} templates", templates.size());
        
        for (ShiftTemplate template : templates) {
            validateTemplate(template);
        }
        
        shiftTemplateRepository.saveAll(templates);
        log.info("ÄÃ£ bulk update {} templates", templates.size());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTemplates(String format) {
        // TODO: Implement export functionality
        throw new BusinessLogicException("Export functionality chÆ°a Ä‘Æ°á»£c implement");
    }

    @Override
    public List<ShiftTemplate> importTemplates(byte[] fileData, String format) {
        // TODO: Implement import functionality
        throw new BusinessLogicException("Import functionality chÆ°a Ä‘Æ°á»£c implement");
    }

    /**
     * Láº¥y sort order tiáº¿p theo
     */
    private Integer getNextSortOrder() {
        List<ShiftTemplate> templates = shiftTemplateRepository.findByIsActiveTrueOrderBySortOrderAsc();
        if (templates.isEmpty()) {
            return 1;
        }
        return templates.get(templates.size() - 1).getSortOrder() + 1;
    }
}
