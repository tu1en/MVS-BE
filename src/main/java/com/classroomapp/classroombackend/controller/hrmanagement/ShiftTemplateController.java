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
 * Quản lý các mẫu ca làm việc với RBAC security
 */
@RestController
@RequestMapping("/api/hr/shift-templates")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Shift Template Management", description = "APIs cho quản lý mẫu ca làm việc")
@SecurityRequirement(name = "bearerAuth")
public class ShiftTemplateController {

    private final ShiftTemplateService shiftTemplateService;
    private final ModelMapper modelMapper;

    @Operation(summary = "Lấy danh sách tất cả shift templates", 
               description = "Lấy danh sách tất cả mẫu ca làm việc đang hoạt động")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<ShiftTemplateDto>>> getAllTemplates() {
        log.info("Lấy danh sách tất cả shift templates");
        
        List<ShiftTemplate> templates = shiftTemplateService.findAllActiveTemplates();
        List<ShiftTemplateDto> templateDtos = templates.stream()
            .map(template -> modelMapper.map(template, ShiftTemplateDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(templateDtos, "Lấy danh sách templates thành công"));
    }

    @Operation(summary = "Tìm kiếm shift templates với pagination", 
               description = "Tìm kiếm mẫu ca làm việc với filters và pagination")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Page<ShiftTemplateDto>>> searchTemplates(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String search,
            @Parameter(description = "Trạng thái active") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("Tìm kiếm shift templates với search: {}, isActive: {}, page: {}, size: {}", 
                search, isActive, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<ShiftTemplate> templates = shiftTemplateService.searchTemplates(search, isActive, pageable);
        Page<ShiftTemplateDto> templateDtos = templates.map(template -> 
            modelMapper.map(template, ShiftTemplateDto.class));

        return ResponseEntity.ok(ApiResponse.success(templateDtos, "Tìm kiếm templates thành công"));
    }

    @Operation(summary = "Lấy shift template theo ID", 
               description = "Lấy thông tin chi tiết của một mẫu ca làm việc")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<ShiftTemplateDto>> getTemplateById(
            @Parameter(description = "ID của shift template") @PathVariable Long id) {
        
        log.info("Lấy shift template với ID: {}", id);

        ShiftTemplate template = shiftTemplateService.findById(id)
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "Không tìm thấy shift template với ID: " + id));

        ShiftTemplateDto templateDto = modelMapper.map(template, ShiftTemplateDto.class);
        return ResponseEntity.ok(ApiResponse.success(templateDto, "Lấy template thành công"));
    }

    @Operation(summary = "Tạo shift template mới", 
               description = "Tạo mẫu ca làm việc mới (chỉ ADMIN và MANAGER)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftTemplateDto>> createTemplate(
            @Parameter(description = "Thông tin shift template mới") @Valid @RequestBody CreateShiftTemplateDto createDto) {
        
        log.info("Tạo shift template mới: {}", createDto.getTemplateName());

        ShiftTemplate template = modelMapper.map(createDto, ShiftTemplate.class);
        ShiftTemplate created = shiftTemplateService.createTemplate(template);
        ShiftTemplateDto createdDto = modelMapper.map(created, ShiftTemplateDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdDto, "Tạo template thành công"));
    }

    @Operation(summary = "Cập nhật shift template", 
               description = "Cập nhật thông tin mẫu ca làm việc (chỉ ADMIN và MANAGER)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftTemplateDto>> updateTemplate(
            @Parameter(description = "ID của shift template") @PathVariable Long id,
            @Parameter(description = "Thông tin cập nhật") @Valid @RequestBody UpdateShiftTemplateDto updateDto) {
        
        log.info("Cập nhật shift template ID: {}", id);

        ShiftTemplate template = modelMapper.map(updateDto, ShiftTemplate.class);
        ShiftTemplate updated = shiftTemplateService.updateTemplate(id, template);
        ShiftTemplateDto updatedDto = modelMapper.map(updated, ShiftTemplateDto.class);

        return ResponseEntity.ok(ApiResponse.success(updatedDto, "Cập nhật template thành công"));
    }

    @Operation(summary = "Xóa shift template", 
               description = "Xóa mẫu ca làm việc (soft delete - chỉ ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @Parameter(description = "ID của shift template") @PathVariable Long id) {
        
        log.info("Xóa shift template ID: {}", id);

        shiftTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa template thành công"));
    }

    @Operation(summary = "Lấy templates theo khoảng thời gian", 
               description = "Tìm templates trong khoảng thời gian cụ thể")
    @GetMapping("/by-time-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<ShiftTemplateDto>>> getTemplatesByTimeRange(
            @Parameter(description = "Thời gian bắt đầu (HH:mm:ss)") @RequestParam String startTime,
            @Parameter(description = "Thời gian kết thúc (HH:mm:ss)") @RequestParam String endTime) {
        
        log.info("Lấy templates theo time range: {} - {}", startTime, endTime);

        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        List<ShiftTemplate> templates = shiftTemplateService.findTemplatesByTimeRange(start, end);
        List<ShiftTemplateDto> templateDtos = templates.stream()
            .map(template -> modelMapper.map(template, ShiftTemplateDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(templateDtos, "Lấy templates theo time range thành công"));
    }

    @Operation(summary = "Lấy templates có thể làm tăng ca", 
               description = "Lấy danh sách templates được phép làm tăng ca")
    @GetMapping("/overtime-eligible")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftTemplateDto>>> getOvertimeEligibleTemplates() {
        log.info("Lấy overtime eligible templates");

        List<ShiftTemplate> templates = shiftTemplateService.findOvertimeEligibleTemplates();
        List<ShiftTemplateDto> templateDtos = templates.stream()
            .map(template -> modelMapper.map(template, ShiftTemplateDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(templateDtos, "Lấy overtime templates thành công"));
    }

    @Operation(summary = "Kiểm tra xung đột thời gian", 
               description = "Kiểm tra xung đột thời gian với templates khác")
    @GetMapping("/check-conflicts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftTemplateDto>>> checkTimeConflicts(
            @Parameter(description = "Thời gian bắt đầu") @RequestParam String startTime,
            @Parameter(description = "Thời gian kết thúc") @RequestParam String endTime,
            @Parameter(description = "ID template loại trừ") @RequestParam(required = false) Long excludeId) {
        
        log.info("Kiểm tra conflicts cho time range: {} - {}", startTime, endTime);

        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        List<ShiftTemplate> conflicts = shiftTemplateService.findConflictingTemplates(start, end, excludeId);
        List<ShiftTemplateDto> conflictDtos = conflicts.stream()
            .map(template -> modelMapper.map(template, ShiftTemplateDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(conflictDtos, 
            conflicts.isEmpty() ? "Không có xung đột" : "Phát hiện " + conflicts.size() + " xung đột"));
    }

    @Operation(summary = "Lấy thống kê templates", 
               description = "Lấy thống kê tổng quan về shift templates")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftTemplateService.TemplateStatistics>> getTemplateStatistics() {
        log.info("Lấy template statistics");

        ShiftTemplateService.TemplateStatistics stats = shiftTemplateService.getTemplateStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê thành công"));
    }

    @Operation(summary = "Cập nhật trạng thái active", 
               description = "Bật/tắt trạng thái hoạt động của template")
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateActiveStatus(
            @Parameter(description = "ID của shift template") @PathVariable Long id,
            @Parameter(description = "Trạng thái active") @RequestParam Boolean isActive) {
        
        log.info("Cập nhật active status cho template ID: {} thành {}", id, isActive);

        shiftTemplateService.updateActiveStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(null, 
            "Cập nhật trạng thái " + (isActive ? "kích hoạt" : "vô hiệu hóa") + " thành công"));
    }

    @Operation(summary = "Cập nhật thứ tự sắp xếp", 
               description = "Cập nhật sort order của template")
    @PatchMapping("/{id}/sort-order")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateSortOrder(
            @Parameter(description = "ID của shift template") @PathVariable Long id,
            @Parameter(description = "Thứ tự sắp xếp") @RequestParam Integer sortOrder) {
        
        log.info("Cập nhật sort order cho template ID: {} thành {}", id, sortOrder);

        shiftTemplateService.updateSortOrder(id, sortOrder);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật thứ tự sắp xếp thành công"));
    }
}
