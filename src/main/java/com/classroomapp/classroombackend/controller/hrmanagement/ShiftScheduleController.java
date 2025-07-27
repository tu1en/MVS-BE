package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doproject.common.ApiResponse;
import com.classroomapp.classroombackend.dto.hrmanagement.CreateShiftScheduleDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ShiftScheduleDto;
import com.classroomapp.classroombackend.dto.hrmanagement.UpdateShiftScheduleDto;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.service.UserService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftScheduleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller cho Shift Schedule Management
 * Quản lý lifecycle của lịch làm việc (Draft → Published → Archived)
 */
@RestController
@RequestMapping("/api/hr/shift-schedules")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Shift Schedule Management", description = "APIs cho quản lý lịch làm việc")
@SecurityRequirement(name = "bearerAuth")
public class ShiftScheduleController {

    private final ShiftScheduleService shiftScheduleService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Operation(summary = "Tìm kiếm shift schedules", 
               description = "Tìm kiếm lịch làm việc với filters và pagination")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Page<ShiftScheduleDto>>> searchSchedules(
            @Parameter(description = "Trạng thái schedule") @RequestParam(required = false) ShiftSchedule.ScheduleStatus status,
            @Parameter(description = "Loại schedule") @RequestParam(required = false) ShiftSchedule.ScheduleType scheduleType,
            @Parameter(description = "ID người tạo") @RequestParam(required = false) Long createdById,
            @Parameter(description = "Ngày bắt đầu") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String search,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("Tìm kiếm schedules với status: {}, type: {}", status, scheduleType);

        Pageable pageable = PageRequest.of(page, size);
        Page<ShiftSchedule> schedules = shiftScheduleService.searchSchedules(
            status, scheduleType, createdById, startDate, endDate, search, pageable);
        
        Page<ShiftScheduleDto> scheduleDtos = schedules.map(schedule -> 
            modelMapper.map(schedule, ShiftScheduleDto.class));

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm schedules thành công", scheduleDtos));
    }

    @Operation(summary = "Lấy schedule theo ID", 
               description = "Lấy thông tin chi tiết của schedule")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> getScheduleById(
            @Parameter(description = "ID của schedule") @PathVariable Long id) {
        
        log.info("Lấy schedule với ID: {}", id);

        ShiftSchedule schedule = shiftScheduleService.findById(id)
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "Không tìm thấy schedule với ID: " + id));

        ShiftScheduleDto scheduleDto = modelMapper.map(schedule, ShiftScheduleDto.class);
        return ResponseEntity.ok(ApiResponse.success("Lấy schedule thành công", scheduleDto));
    }

    @Operation(summary = "Tạo schedule mới", 
               description = "Tạo lịch làm việc mới (chỉ ADMIN và MANAGER)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> createSchedule(
            @Parameter(description = "Thông tin schedule mới") @Valid @RequestBody CreateShiftScheduleDto createDto,
            Authentication authentication) {
        
        log.info("Tạo schedule mới: {} bởi user: {}", createDto.getScheduleName(), authentication.getName());

        ShiftSchedule schedule = modelMapper.map(createDto, ShiftSchedule.class);
        
        // Set creator from authentication
        User creator = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "Không tìm thấy user: " + authentication.getName()));
        schedule.setCreatedBy(creator);

        ShiftSchedule created = shiftScheduleService.createSchedule(schedule);
        ShiftScheduleDto createdDto = modelMapper.map(created, ShiftScheduleDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Tạo schedule thành công", createdDto));
    }

    @Operation(summary = "Cập nhật schedule", 
               description = "Cập nhật thông tin schedule (chỉ draft schedules)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> updateSchedule(
            @Parameter(description = "ID của schedule") @PathVariable Long id,
            @Parameter(description = "Thông tin cập nhật") @Valid @RequestBody UpdateShiftScheduleDto updateDto) {
        
        log.info("Cập nhật schedule ID: {}", id);

        ShiftSchedule schedule = modelMapper.map(updateDto, ShiftSchedule.class);
        ShiftSchedule updated = shiftScheduleService.updateSchedule(id, schedule);
        ShiftScheduleDto updatedDto = modelMapper.map(updated, ShiftScheduleDto.class);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật schedule thành công", updatedDto));
    }

    @Operation(summary = "Xóa schedule", 
               description = "Xóa schedule (chỉ draft schedules không có assignments)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @Parameter(description = "ID của schedule") @PathVariable Long id) {
        
        log.info("Xóa schedule ID: {}", id);

        shiftScheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa schedule thành công", null));
    }

    @Operation(summary = "Xuất bản schedule", 
               description = "Xuất bản schedule từ draft sang published")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> publishSchedule(
            @Parameter(description = "ID của schedule") @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Xuất bản schedule ID: {} bởi user: {}", id, authentication.getName());

        User publisher = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "Không tìm thấy user: " + authentication.getName()));

        ShiftSchedule published = shiftScheduleService.publishSchedule(id, publisher);
        ShiftScheduleDto publishedDto = modelMapper.map(published, ShiftScheduleDto.class);

        return ResponseEntity.ok(ApiResponse.success("Xuất bản schedule thành công", publishedDto));
    }

    @Operation(summary = "Lưu trữ schedule", 
               description = "Lưu trữ schedule đã kết thúc")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> archiveSchedule(
            @Parameter(description = "ID của schedule") @PathVariable Long id) {
        
        log.info("Lưu trữ schedule ID: {}", id);

        ShiftSchedule archived = shiftScheduleService.archiveSchedule(id);
        ShiftScheduleDto archivedDto = modelMapper.map(archived, ShiftScheduleDto.class);

        return ResponseEntity.ok(ApiResponse.success("Lưu trữ schedule thành công", archivedDto));
    }

    @Operation(summary = "Hủy schedule", 
               description = "Hủy schedule với lý do")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> cancelSchedule(
            @Parameter(description = "ID của schedule") @PathVariable Long id,
            @Parameter(description = "Lý do hủy") @RequestParam String reason) {
        
        log.info("Hủy schedule ID: {} với lý do: {}", id, reason);

        shiftScheduleService.cancelSchedule(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Hủy schedule thành công", null));
    }

    @Operation(summary = "Lấy schedules theo trạng thái", 
               description = "Lấy tất cả schedules theo trạng thái cụ thể")
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<ShiftScheduleDto>>> getSchedulesByStatus(
            @Parameter(description = "Trạng thái schedule") @PathVariable ShiftSchedule.ScheduleStatus status) {
        
        log.info("Lấy schedules theo status: {}", status);

        List<ShiftSchedule> schedules = shiftScheduleService.findByStatus(status);
        List<ShiftScheduleDto> scheduleDtos = schedules.stream()
            .map(schedule -> modelMapper.map(schedule, ShiftScheduleDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Lấy schedules theo status thành công", scheduleDtos));
    }

    @Operation(summary = "Lấy active schedules", 
               description = "Lấy tất cả schedules đang hoạt động")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<ShiftScheduleDto>>> getActiveSchedules() {
        log.info("Lấy active schedules");

        List<ShiftSchedule> schedules = shiftScheduleService.findActiveSchedules();
        List<ShiftScheduleDto> scheduleDtos = schedules.stream()
            .map(schedule -> modelMapper.map(schedule, ShiftScheduleDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Lấy active schedules thành công", scheduleDtos));
    }

    @Operation(summary = "Lấy active schedule cho ngày", 
               description = "Lấy schedule đang hoạt động cho ngày cụ thể")
    @GetMapping("/active-for-date/{date}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> getActiveScheduleForDate(
            @Parameter(description = "Ngày") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Lấy active schedule cho ngày: {}", date);

        ShiftSchedule schedule = shiftScheduleService.findActiveScheduleForDate(date)
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "Không có schedule hoạt động cho ngày: " + date));

        ShiftScheduleDto scheduleDto = modelMapper.map(schedule, ShiftScheduleDto.class);
        return ResponseEntity.ok(ApiResponse.success("Lấy active schedule thành công", scheduleDto));
    }

    @Operation(summary = "Generate weekly schedule", 
               description = "Tự động tạo lịch làm việc hàng tuần")
    @PostMapping("/generate-weekly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> generateWeeklySchedule(
            @Parameter(description = "Ngày bắt đầu tuần") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Tên schedule") @RequestParam String name,
            Authentication authentication) {
        
        log.info("Generate weekly schedule từ {} với tên: {}", startDate, name);

        User creator = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "Không tìm thấy user: " + authentication.getName()));

        ShiftSchedule generated = shiftScheduleService.generateWeeklySchedule(startDate, name, creator);
        ShiftScheduleDto generatedDto = modelMapper.map(generated, ShiftScheduleDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Generate weekly schedule thành công", generatedDto));
    }

    @Operation(summary = "Generate monthly schedule", 
               description = "Tự động tạo lịch làm việc hàng tháng")
    @PostMapping("/generate-monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> generateMonthlySchedule(
            @Parameter(description = "Ngày trong tháng") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Tên schedule") @RequestParam String name,
            Authentication authentication) {
        
        log.info("Generate monthly schedule cho tháng {} với tên: {}", startDate, name);

        User creator = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "Không tìm thấy user: " + authentication.getName()));

        ShiftSchedule generated = shiftScheduleService.generateMonthlySchedule(startDate, name, creator);
        ShiftScheduleDto generatedDto = modelMapper.map(generated, ShiftScheduleDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Generate monthly schedule thành công", generatedDto));
    }

    @Operation(summary = "Copy schedule", 
               description = "Copy schedule từ schedule khác")
    @PostMapping("/{sourceId}/copy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> copySchedule(
            @Parameter(description = "ID source schedule") @PathVariable Long sourceId,
            @Parameter(description = "Ngày bắt đầu mới") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newStartDate,
            @Parameter(description = "Tên schedule mới") @RequestParam String newName) {
        
        log.info("Copy schedule từ ID: {} với start date: {}", sourceId, newStartDate);

        ShiftSchedule copied = shiftScheduleService.copySchedule(sourceId, newStartDate, newName);
        ShiftScheduleDto copiedDto = modelMapper.map(copied, ShiftScheduleDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Copy schedule thành công", copiedDto));
    }

    @Operation(summary = "Lấy thống kê schedules", 
               description = "Lấy thống kê tổng quan về schedules")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleService.ScheduleStatistics>> getScheduleStatistics(
            @Parameter(description = "Ngày bắt đầu") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Lấy schedule statistics từ {} đến {}", startDate, endDate);

        ShiftScheduleService.ScheduleStatistics stats = shiftScheduleService.getScheduleStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê thành công", stats));
    }

    @Operation(summary = "Auto-archive old schedules", 
               description = "Tự động lưu trữ schedules cũ")
    @PostMapping("/auto-archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> autoArchiveOldSchedules(
            @Parameter(description = "Số ngày sau khi kết thúc") @RequestParam(defaultValue = "30") int daysAfterEnd) {
        
        log.info("Auto-archive schedules cũ hơn {} ngày", daysAfterEnd);

        int archived = shiftScheduleService.autoArchiveOldSchedules(daysAfterEnd);
        return ResponseEntity.ok(ApiResponse.success(
            "Auto-archive " + archived + " schedules thành công", archived));
    }

    @Operation(summary = "Cleanup old drafts", 
               description = "Xóa draft schedules cũ không sử dụng")
    @PostMapping("/cleanup-drafts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldDrafts(
            @Parameter(description = "Số ngày cũ") @RequestParam(defaultValue = "7") int daysOld) {
        
        log.info("Cleanup draft schedules cũ hơn {} ngày", daysOld);

        int deleted = shiftScheduleService.cleanupOldDrafts(daysOld);
        return ResponseEntity.ok(ApiResponse.success(
            "Cleanup " + deleted + " draft schedules thành công", deleted));
    }

    @Operation(summary = "Validate schedule conflicts", 
               description = "Kiểm tra xung đột với schedules khác")
    @PostMapping("/validate-conflicts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleService.ScheduleConflictResult>> validateScheduleConflicts(
            @Parameter(description = "Thông tin schedule để kiểm tra") @Valid @RequestBody CreateShiftScheduleDto createDto) {
        
        log.info("Validate conflicts cho schedule");

        ShiftSchedule schedule = modelMapper.map(createDto, ShiftSchedule.class);
        ShiftScheduleService.ScheduleConflictResult result = shiftScheduleService.validateScheduleConflicts(schedule);

        return ResponseEntity.ok(ApiResponse.success(
            result.hasConflict() ? "Phát hiện xung đột" : "Không có xung đột", result));
    }
}