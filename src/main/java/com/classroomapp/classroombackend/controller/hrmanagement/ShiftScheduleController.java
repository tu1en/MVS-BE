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
import org.springframework.web.bind.annotation.PatchMapping;  // ADD THIS IMPORT
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ApiResponse;
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
        User creator = userService.findUserEntityByEmail(authentication.getName());
        if (creator == null) {
            throw new com.classroomapp.classroombackend.exception.ResourceNotFoundException("Không tìm thấy user: " + authentication.getName());
        }
        schedule.setCreatedBy(creator);

        ShiftSchedule created = shiftScheduleService.createSchedule(schedule);
        ShiftScheduleDto createdDto = modelMapper.map(created, ShiftScheduleDto.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo schedule thành công", createdDto));
    }

    @Operation(summary = "Cập nhật schedule", 
               description = "Cập nhật thông tin lịch làm việc")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> updateSchedule(
            @Parameter(description = "ID của schedule") @PathVariable Long id,
            @Parameter(description = "Thông tin cập nhật") @Valid @RequestBody UpdateShiftScheduleDto updateDto) {
        
        log.info("Cập nhật schedule {}: {}", id, updateDto.getScheduleName());

        ShiftSchedule updated = shiftScheduleService.updateSchedule(id, modelMapper.map(updateDto, ShiftSchedule.class));
        ShiftScheduleDto updatedDto = modelMapper.map(updated, ShiftScheduleDto.class);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật schedule thành công", updatedDto));
    }

    @Operation(summary = "Xóa schedule", 
               description = "Xóa lịch làm việc (chỉ ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @Parameter(description = "ID của schedule") @PathVariable Long id) {
        
        log.info("Xóa schedule: {}", id);
        shiftScheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa schedule thành công", null));
    }

    @Operation(summary = "Publish schedule", 
               description = "Publish lịch làm việc (đổi trạng thái từ Draft sang Published)")
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> publishSchedule(
            @Parameter(description = "ID của schedule") @PathVariable Long id,
            Authentication authentication) {  // ADD Authentication parameter
        
        log.info("Publish schedule: {}", id);
        
        // Get user from authentication
        User user = userService.findUserEntityByEmail(authentication.getName());
        if (user == null) {
            throw new com.classroomapp.classroombackend.exception.ResourceNotFoundException("Không tìm thấy user: " + authentication.getName());
        }
        
        ShiftSchedule published = shiftScheduleService.publishSchedule(id, user);  // Pass both parameters
        ShiftScheduleDto publishedDto = modelMapper.map(published, ShiftScheduleDto.class);
        return ResponseEntity.ok(ApiResponse.success("Publish schedule thành công", publishedDto));
    }

    @Operation(summary = "Archive schedule", 
               description = "Archive lịch làm việc (đổi trạng thái sang Archived)")
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> archiveSchedule(
            @Parameter(description = "ID của schedule") @PathVariable Long id) {
        
        log.info("Archive schedule: {}", id);
        ShiftSchedule archived = shiftScheduleService.archiveSchedule(id);
        ShiftScheduleDto archivedDto = modelMapper.map(archived, ShiftScheduleDto.class);
        return ResponseEntity.ok(ApiResponse.success("Archive schedule thành công", archivedDto));
    }

    @Operation(summary = "Tìm kiếm schedules theo người tạo")
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<ApiResponse<List<ShiftScheduleDto>>> getSchedulesByCreator(@PathVariable Long userId) {
        List<ShiftSchedule> schedules = shiftScheduleService.findByCreatedByUserId(userId);
        List<ShiftScheduleDto> scheduleDtos = schedules.stream()
            .map(s -> modelMapper.map(s, ShiftScheduleDto.class))
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy schedules thành công", scheduleDtos));
    }
}