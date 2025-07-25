package com.classroomapp.classroombackend.controller.hrmanagement;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.hrmanagement.CreateShiftTemplateDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ShiftTemplateDto;
import com.classroomapp.classroombackend.dto.hrmanagement.UpdateShiftTemplateDto;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftTemplate;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller cho Shift Template Management
 * Quáº£n lÃ½ cÃ¡c máº«u ca lÃ m viá»‡c vá»›i RBAC security
 */
@RestController
@RequestMapping("/api/hr/shift-templates")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Shift Template Management", description = "APIs cho quáº£n lÃ½ máº«u ca lÃ m viá»‡c")
@SecurityRequirement(name = "bearerAuth")
public class ShiftTemplateController {

    private final ShiftTemplateService shiftTemplateService;
    private final ModelMapper modelMapper;

    @Operation(summary = "Láº¥y danh sÃ¡ch táº¥t cáº£ shift templates", 
               description = "Láº¥y danh sÃ¡ch táº¥t cáº£ máº«u ca lÃ m viá»‡c Ä‘ang hoáº¡t Ä‘á»™ng")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "ThÃ nh cÃ´ng"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "KhÃ´ng cÃ³ quyá»n truy cáº­p")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<ShiftTemplateDto>>> getAllTemplates() {
        log.info("Láº¥y danh sÃ¡ch táº¥t cáº£ shift templates");
        
        List<ShiftTemplate> templates = shiftTemplateService.findAllActiveTemplates();
        List<ShiftTemplateDto> templateDtos = templates.stream()
            .map(template -> modelMapper.map(template, ShiftTemplateDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(templateDtos, "Láº¥y danh sÃ¡ch templates thÃ nh cÃ´ng"));
    }

    @Operation(summary = "TÃ¬m kiáº¿m shift templates vá»›i pagination", 
               description = "TÃ¬m kiáº¿m máº«u ca lÃ m viá»‡c vá»›i filters vÃ  pagination")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Page<ShiftTemplateDto>>> searchTemplates(
            @Parameter(description = "Tá»« khÃ³a tÃ¬m kiáº¿m") @RequestParam(required = false) String search,
            @Parameter(description = "Tráº¡ng thÃ¡i active") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Sá»‘ trang (báº¯t Ä‘áº§u tá»« 0)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "KÃ­ch thÆ°á»›c trang") @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("TÃ¬m kiáº¿m shift templates vá»›i search: {}, isActive: {}, page: {}, size: {}", 
                search, isActive, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<ShiftTemplate> templates = shiftTemplateService.searchTemplates(search, isActive, pageable);
        Page<ShiftTemplateDto> templateDtos = templates.map(template -> 
            modelMapper.map(template, ShiftTemplateDto.class));

        return ResponseEntity.ok(ApiResponse.success(templateDtos, "TÃ¬m kiáº¿m templates thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Láº¥y shift template theo ID", 
               description = "Láº¥y thÃ´ng tin chi tiáº¿t cá»§a má»™t máº«u ca lÃ m viá»‡c")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<ShiftTemplateDto>> getTemplateById(
            @Parameter(description = "ID cá»§a shift template") @PathVariable Long id) {
        
        log.info("Láº¥y shift template vá»›i ID: {}", id);

        ShiftTemplate template = shiftTemplateService.findById(id)
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "KhÃ´ng tÃ¬m tháº¥y shift template vá»›i ID: " + id));

        ShiftTemplateDto templateDto = modelMapper.map(template, ShiftTemplateDto.class);
        return ResponseEntity.ok(ApiResponse.success(templateDto, "Láº¥y template thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Táº¡o shift template má»›i", 
               description = "Táº¡o máº«u ca lÃ m viá»‡c má»›i (chá»‰ ADMIN vÃ  MANAGER)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftTemplateDto>> createTemplate(
            @Parameter(description = "ThÃ´ng tin shift template má»›i") @Valid @RequestBody CreateShiftTemplateDto createDto) {
        
        log.info("Táº¡o shift template má»›i: {}", createDto.getTemplateName());

        ShiftTemplate template = modelMapper.map(createDto, ShiftTemplate.class);
        ShiftTemplate created = shiftTemplateService.createTemplate(template);
        ShiftTemplateDto createdDto = modelMapper.map(created, ShiftTemplateDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdDto, "Táº¡o template thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Cáº­p nháº­t shift template", 
               description = "Cáº­p nháº­t thÃ´ng tin máº«u ca lÃ m viá»‡c (chá»‰ ADMIN vÃ  MANAGER)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftTemplateDto>> updateTemplate(
            @Parameter(description = "ID cá»§a shift template") @PathVariable Long id,
            @Parameter(description = "ThÃ´ng tin cáº­p nháº­t") @Valid @RequestBody UpdateShiftTemplateDto updateDto) {
        
        log.info("Cáº­p nháº­t shift template ID: {}", id);

        ShiftTemplate template = modelMapper.map(updateDto, ShiftTemplate.class);
        ShiftTemplate updated = shiftTemplateService.updateTemplate(id, template);
        ShiftTemplateDto updatedDto = modelMapper.map(updated, ShiftTemplateDto.class);

        return ResponseEntity.ok(ApiResponse.success(updatedDto, "Cáº­p nháº­t template thÃ nh cÃ´ng"));
    }

    @Operation(summary = "XÃ³a shift template", 
               description = "XÃ³a máº«u ca lÃ m viá»‡c (soft delete - chá»‰ ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @Parameter(description = "ID cá»§a shift template") @PathVariable Long id) {
        
        log.info("XÃ³a shift template ID: {}", id);

        shiftTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "XÃ³a template thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Láº¥y templates theo khoáº£ng thá»i gian", 
               description = "TÃ¬m templates trong khoáº£ng thá»i gian cá»¥ thá»ƒ")
    @GetMapping("/by-time-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<ShiftTemplateDto>>> getTemplatesByTimeRange(
            @Parameter(description = "Thá»i gian báº¯t Ä‘áº§u (HH:mm:ss)") @RequestParam String startTime,
            @Parameter(description = "Thá»i gian káº¿t thÃºc (HH:mm:ss)") @RequestParam String endTime) {
        
        log.info("Láº¥y templates theo time range: {} - {}", startTime, endTime);

        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        List<ShiftTemplate> templates = shiftTemplateService.findTemplatesByTimeRange(start, end);
        List<ShiftTemplateDto> templateDtos = templates.stream()
            .map(template -> modelMapper.map(template, ShiftTemplateDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(templateDtos, "Láº¥y templates theo time range thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Láº¥y templates cÃ³ thá»ƒ lÃ m tÄƒng ca", 
               description = "Láº¥y danh sÃ¡ch templates Ä‘Æ°á»£c phÃ©p lÃ m tÄƒng ca")
    @GetMapping("/overtime-eligible")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftTemplateDto>>> getOvertimeEligibleTemplates() {
        log.info("Láº¥y overtime eligible templates");

        List<ShiftTemplate> templates = shiftTemplateService.findOvertimeEligibleTemplates();
        List<ShiftTemplateDto> templateDtos = templates.stream()
            .map(template -> modelMapper.map(template, ShiftTemplateDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(templateDtos, "Láº¥y overtime templates thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Kiá»ƒm tra xung Ä‘á»™t thá»i gian", 
               description = "Kiá»ƒm tra xung Ä‘á»™t thá»i gian vá»›i templates khÃ¡c")
    @GetMapping("/check-conflicts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftTemplateDto>>> checkTimeConflicts(
            @Parameter(description = "Thá»i gian báº¯t Ä‘áº§u") @RequestParam String startTime,
            @Parameter(description = "Thá»i gian káº¿t thÃºc") @RequestParam String endTime,
            @Parameter(description = "ID template loáº¡i trá»«") @RequestParam(required = false) Long excludeId) {
        
        log.info("Kiá»ƒm tra conflicts cho time range: {} - {}", startTime, endTime);

        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        List<ShiftTemplate> conflicts = shiftTemplateService.findConflictingTemplates(start, end, excludeId);
        List<ShiftTemplateDto> conflictDtos = conflicts.stream()
            .map(template -> modelMapper.map(template, ShiftTemplateDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(conflictDtos, 
            conflicts.isEmpty() ? "KhÃ´ng cÃ³ xung Ä‘á»™t" : "PhÃ¡t hiá»‡n " + conflicts.size() + " xung Ä‘á»™t"));
    }

    @Operation(summary = "Láº¥y thá»‘ng kÃª templates", 
               description = "Láº¥y thá»‘ng kÃª tá»•ng quan vá» shift templates")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftTemplateService.TemplateStatistics>> getTemplateStatistics() {
        log.info("Láº¥y template statistics");

        ShiftTemplateService.TemplateStatistics stats = shiftTemplateService.getTemplateStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Láº¥y thá»‘ng kÃª thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Cáº­p nháº­t tráº¡ng thÃ¡i active", 
               description = "Báº­t/táº¯t tráº¡ng thÃ¡i hoáº¡t Ä‘á»™ng cá»§a template")
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateActiveStatus(
            @Parameter(description = "ID cá»§a shift template") @PathVariable Long id,
            @Parameter(description = "Tráº¡ng thÃ¡i active") @RequestParam Boolean isActive) {
        
        log.info("Cáº­p nháº­t active status cho template ID: {} thÃ nh {}", id, isActive);

        shiftTemplateService.updateActiveStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(null, 
            "Cáº­p nháº­t tráº¡ng thÃ¡i " + (isActive ? "kÃ­ch hoáº¡t" : "vÃ´ hiá»‡u hÃ³a") + " thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Cáº­p nháº­t thá»© tá»± sáº¯p xáº¿p", 
               description = "Cáº­p nháº­t sort order cá»§a template")
    @PatchMapping("/{id}/sort-order")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateSortOrder(
            @Parameter(description = "ID cá»§a shift template") @PathVariable Long id,
            @Parameter(description = "Thá»© tá»± sáº¯p xáº¿p") @RequestParam Integer sortOrder) {
        
        log.info("Cáº­p nháº­t sort order cho template ID: {} thÃ nh {}", id, sortOrder);

        shiftTemplateService.updateSortOrder(id, sortOrder);
        return ResponseEntity.ok(ApiResponse.success(null, "Cáº­p nháº­t thá»© tá»± sáº¯p xáº¿p thÃ nh cÃ´ng"));
    }
}
