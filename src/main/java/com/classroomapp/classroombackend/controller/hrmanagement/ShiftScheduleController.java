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

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.hrmanagement.CreateShiftScheduleDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ShiftScheduleDto;
import com.classroomapp.classroombackend.dto.hrmanagement.UpdateShiftScheduleDto;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftScheduleService;
import com.classroomapp.classroombackend.service.usermanagement.UserService;

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
 * Quáº£n lÃ½ lifecycle cá»§a lá»‹ch lÃ m viá»‡c (Draft â†’ Published â†’ Archived)
 */
@RestController
@RequestMapping("/api/hr/shift-schedules")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Shift Schedule Management", description = "APIs cho quáº£n lÃ½ lá»‹ch lÃ m viá»‡c")
@SecurityRequirement(name = "bearerAuth")
public class ShiftScheduleController {

    private final ShiftScheduleService shiftScheduleService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Operation(summary = "TÃ¬m kiáº¿m shift schedules", 
               description = "TÃ¬m kiáº¿m lá»‹ch lÃ m viá»‡c vá»›i filters vÃ  pagination")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Page<ShiftScheduleDto>>> searchSchedules(
            @Parameter(description = "Tráº¡ng thÃ¡i schedule") @RequestParam(required = false) ShiftSchedule.ScheduleStatus status,
            @Parameter(description = "Loáº¡i schedule") @RequestParam(required = false) ShiftSchedule.ScheduleType scheduleType,
            @Parameter(description = "ID ngÆ°á»i táº¡o") @RequestParam(required = false) Long createdById,
            @Parameter(description = "NgÃ y báº¯t Ä‘áº§u") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "NgÃ y káº¿t thÃºc") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Tá»« khÃ³a tÃ¬m kiáº¿m") @RequestParam(required = false) String search,
            @Parameter(description = "Sá»‘ trang") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "KÃ­ch thÆ°á»›c trang") @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("TÃ¬m kiáº¿m schedules vá»›i status: {}, type: {}", status, scheduleType);

        Pageable pageable = PageRequest.of(page, size);
        Page<ShiftSchedule> schedules = shiftScheduleService.searchSchedules(
            status, scheduleType, createdById, startDate, endDate, search, pageable);
        
        Page<ShiftScheduleDto> scheduleDtos = schedules.map(schedule -> 
            modelMapper.map(schedule, ShiftScheduleDto.class));

        return ResponseEntity.ok(ApiResponse.success(scheduleDtos, "TÃ¬m kiáº¿m schedules thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Láº¥y schedule theo ID", 
               description = "Láº¥y thÃ´ng tin chi tiáº¿t cá»§a schedule")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> getScheduleById(
            @Parameter(description = "ID cá»§a schedule") @PathVariable Long id) {
        
        log.info("Láº¥y schedule vá»›i ID: {}", id);

        ShiftSchedule schedule = shiftScheduleService.findById(id)
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "KhÃ´ng tÃ¬m tháº¥y schedule vá»›i ID: " + id));

        ShiftScheduleDto scheduleDto = modelMapper.map(schedule, ShiftScheduleDto.class);
        return ResponseEntity.ok(ApiResponse.success(scheduleDto, "Láº¥y schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Táº¡o schedule má»›i", 
               description = "Táº¡o lá»‹ch lÃ m viá»‡c má»›i (chá»‰ ADMIN vÃ  MANAGER)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> createSchedule(
            @Parameter(description = "ThÃ´ng tin schedule má»›i") @Valid @RequestBody CreateShiftScheduleDto createDto,
            Authentication authentication) {
        
        log.info("Táº¡o schedule má»›i: {} bá»Ÿi user: {}", createDto.getScheduleName(), authentication.getName());

        ShiftSchedule schedule = modelMapper.map(createDto, ShiftSchedule.class);
        
        // Set creator from authentication
        User creator = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "KhÃ´ng tÃ¬m tháº¥y user: " + authentication.getName()));
        schedule.setCreatedBy(creator);

        ShiftSchedule created = shiftScheduleService.createSchedule(schedule);
        ShiftScheduleDto createdDto = modelMapper.map(created, ShiftScheduleDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdDto, "Táº¡o schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Cáº­p nháº­t schedule", 
               description = "Cáº­p nháº­t thÃ´ng tin schedule (chá»‰ draft schedules)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> updateSchedule(
            @Parameter(description = "ID cá»§a schedule") @PathVariable Long id,
            @Parameter(description = "ThÃ´ng tin cáº­p nháº­t") @Valid @RequestBody UpdateShiftScheduleDto updateDto) {
        
        log.info("Cáº­p nháº­t schedule ID: {}", id);

        ShiftSchedule schedule = modelMapper.map(updateDto, ShiftSchedule.class);
        ShiftSchedule updated = shiftScheduleService.updateSchedule(id, schedule);
        ShiftScheduleDto updatedDto = modelMapper.map(updated, ShiftScheduleDto.class);

        return ResponseEntity.ok(ApiResponse.success(updatedDto, "Cáº­p nháº­t schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "XÃ³a schedule", 
               description = "XÃ³a schedule (chá»‰ draft schedules khÃ´ng cÃ³ assignments)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @Parameter(description = "ID cá»§a schedule") @PathVariable Long id) {
        
        log.info("XÃ³a schedule ID: {}", id);

        shiftScheduleService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.success(null, "XÃ³a schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Xuáº¥t báº£n schedule", 
               description = "Xuáº¥t báº£n schedule tá»« draft sang published")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> publishSchedule(
            @Parameter(description = "ID cá»§a schedule") @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Xuáº¥t báº£n schedule ID: {} bá»Ÿi user: {}", id, authentication.getName());

        User publisher = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "KhÃ´ng tÃ¬m tháº¥y user: " + authentication.getName()));

        ShiftSchedule published = shiftScheduleService.publishSchedule(id, publisher);
        ShiftScheduleDto publishedDto = modelMapper.map(published, ShiftScheduleDto.class);

        return ResponseEntity.ok(ApiResponse.success(publishedDto, "Xuáº¥t báº£n schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "LÆ°u trá»¯ schedule", 
               description = "LÆ°u trá»¯ schedule Ä‘Ã£ káº¿t thÃºc")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> archiveSchedule(
            @Parameter(description = "ID cá»§a schedule") @PathVariable Long id) {
        
        log.info("LÆ°u trá»¯ schedule ID: {}", id);

        ShiftSchedule archived = shiftScheduleService.archiveSchedule(id);
        ShiftScheduleDto archivedDto = modelMapper.map(archived, ShiftScheduleDto.class);

        return ResponseEntity.ok(ApiResponse.success(archivedDto, "LÆ°u trá»¯ schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Há»§y schedule", 
               description = "Há»§y schedule vá»›i lÃ½ do")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> cancelSchedule(
            @Parameter(description = "ID cá»§a schedule") @PathVariable Long id,
            @Parameter(description = "LÃ½ do há»§y") @RequestParam String reason) {
        
        log.info("Há»§y schedule ID: {} vá»›i lÃ½ do: {}", id, reason);

        shiftScheduleService.cancelSchedule(id, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Há»§y schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Láº¥y schedules theo tráº¡ng thÃ¡i", 
               description = "Láº¥y táº¥t cáº£ schedules theo tráº¡ng thÃ¡i cá»¥ thá»ƒ")
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<ShiftScheduleDto>>> getSchedulesByStatus(
            @Parameter(description = "Tráº¡ng thÃ¡i schedule") @PathVariable ShiftSchedule.ScheduleStatus status) {
        
        log.info("Láº¥y schedules theo status: {}", status);

        List<ShiftSchedule> schedules = shiftScheduleService.findByStatus(status);
        List<ShiftScheduleDto> scheduleDtos = schedules.stream()
            .map(schedule -> modelMapper.map(schedule, ShiftScheduleDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(scheduleDtos, "Láº¥y schedules theo status thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Láº¥y active schedules", 
               description = "Láº¥y táº¥t cáº£ schedules Ä‘ang hoáº¡t Ä‘á»™ng")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<ShiftScheduleDto>>> getActiveSchedules() {
        log.info("Láº¥y active schedules");

        List<ShiftSchedule> schedules = shiftScheduleService.findActiveSchedules();
        List<ShiftScheduleDto> scheduleDtos = schedules.stream()
            .map(schedule -> modelMapper.map(schedule, ShiftScheduleDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(scheduleDtos, "Láº¥y active schedules thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Láº¥y active schedule cho ngÃ y", 
               description = "Láº¥y schedule Ä‘ang hoáº¡t Ä‘á»™ng cho ngÃ y cá»¥ thá»ƒ")
    @GetMapping("/active-for-date/{date}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> getActiveScheduleForDate(
            @Parameter(description = "NgÃ y") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Láº¥y active schedule cho ngÃ y: {}", date);

        ShiftSchedule schedule = shiftScheduleService.findActiveScheduleForDate(date)
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "KhÃ´ng cÃ³ schedule hoáº¡t Ä‘á»™ng cho ngÃ y: " + date));

        ShiftScheduleDto scheduleDto = modelMapper.map(schedule, ShiftScheduleDto.class);
        return ResponseEntity.ok(ApiResponse.success(scheduleDto, "Láº¥y active schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Generate weekly schedule", 
               description = "Tá»± Ä‘á»™ng táº¡o lá»‹ch lÃ m viá»‡c hÃ ng tuáº§n")
    @PostMapping("/generate-weekly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> generateWeeklySchedule(
            @Parameter(description = "NgÃ y báº¯t Ä‘áº§u tuáº§n") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "TÃªn schedule") @RequestParam String name,
            Authentication authentication) {
        
        log.info("Generate weekly schedule tá»« {} vá»›i tÃªn: {}", startDate, name);

        User creator = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "KhÃ´ng tÃ¬m tháº¥y user: " + authentication.getName()));

        ShiftSchedule generated = shiftScheduleService.generateWeeklySchedule(startDate, name, creator);
        ShiftScheduleDto generatedDto = modelMapper.map(generated, ShiftScheduleDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(generatedDto, "Generate weekly schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Generate monthly schedule", 
               description = "Tá»± Ä‘á»™ng táº¡o lá»‹ch lÃ m viá»‡c hÃ ng thÃ¡ng")
    @PostMapping("/generate-monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> generateMonthlySchedule(
            @Parameter(description = "NgÃ y trong thÃ¡ng") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "TÃªn schedule") @RequestParam String name,
            Authentication authentication) {
        
        log.info("Generate monthly schedule cho thÃ¡ng {} vá»›i tÃªn: {}", startDate, name);

        User creator = userService.findByEmail(authentication.getName())
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "KhÃ´ng tÃ¬m tháº¥y user: " + authentication.getName()));

        ShiftSchedule generated = shiftScheduleService.generateMonthlySchedule(startDate, name, creator);
        ShiftScheduleDto generatedDto = modelMapper.map(generated, ShiftScheduleDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(generatedDto, "Generate monthly schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Copy schedule", 
               description = "Copy schedule tá»« schedule khÃ¡c")
    @PostMapping("/{sourceId}/copy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleDto>> copySchedule(
            @Parameter(description = "ID source schedule") @PathVariable Long sourceId,
            @Parameter(description = "NgÃ y báº¯t Ä‘áº§u má»›i") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newStartDate,
            @Parameter(description = "TÃªn schedule má»›i") @RequestParam String newName) {
        
        log.info("Copy schedule tá»« ID: {} vá»›i start date: {}", sourceId, newStartDate);

        ShiftSchedule copied = shiftScheduleService.copySchedule(sourceId, newStartDate, newName);
        ShiftScheduleDto copiedDto = modelMapper.map(copied, ShiftScheduleDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(copiedDto, "Copy schedule thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Láº¥y thá»‘ng kÃª schedules", 
               description = "Láº¥y thá»‘ng kÃª tá»•ng quan vá» schedules")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleService.ScheduleStatistics>> getScheduleStatistics(
            @Parameter(description = "NgÃ y báº¯t Ä‘áº§u") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "NgÃ y káº¿t thÃºc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Láº¥y schedule statistics tá»« {} Ä‘áº¿n {}", startDate, endDate);

        ShiftScheduleService.ScheduleStatistics stats = shiftScheduleService.getScheduleStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats, "Láº¥y thá»‘ng kÃª thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Auto-archive old schedules", 
               description = "Tá»± Ä‘á»™ng lÆ°u trá»¯ schedules cÅ©")
    @PostMapping("/auto-archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> autoArchiveOldSchedules(
            @Parameter(description = "Sá»‘ ngÃ y sau khi káº¿t thÃºc") @RequestParam(defaultValue = "30") int daysAfterEnd) {
        
        log.info("Auto-archive schedules cÅ© hÆ¡n {} ngÃ y", daysAfterEnd);

        int archived = shiftScheduleService.autoArchiveOldSchedules(daysAfterEnd);
        return ResponseEntity.ok(ApiResponse.success(archived, 
            "Auto-archive " + archived + " schedules thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Cleanup old drafts", 
               description = "XÃ³a draft schedules cÅ© khÃ´ng sá»­ dá»¥ng")
    @PostMapping("/cleanup-drafts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldDrafts(
            @Parameter(description = "Sá»‘ ngÃ y cÅ©") @RequestParam(defaultValue = "7") int daysOld) {
        
        log.info("Cleanup draft schedules cÅ© hÆ¡n {} ngÃ y", daysOld);

        int deleted = shiftScheduleService.cleanupOldDrafts(daysOld);
        return ResponseEntity.ok(ApiResponse.success(deleted, 
            "Cleanup " + deleted + " draft schedules thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Validate schedule conflicts", 
               description = "Kiá»ƒm tra xung Ä‘á»™t vá»›i schedules khÃ¡c")
    @PostMapping("/validate-conflicts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftScheduleService.ScheduleConflictResult>> validateScheduleConflicts(
            @Parameter(description = "ThÃ´ng tin schedule Ä‘á»ƒ kiá»ƒm tra") @Valid @RequestBody CreateShiftScheduleDto createDto) {
        
        log.info("Validate conflicts cho schedule");

        ShiftSchedule schedule = modelMapper.map(createDto, ShiftSchedule.class);
        ShiftScheduleService.ScheduleConflictResult result = shiftScheduleService.validateScheduleConflicts(schedule);

        return ResponseEntity.ok(ApiResponse.success(result, 
            result.hasConflict() ? "PhÃ¡t hiá»‡n xung Ä‘á»™t" : "KhÃ´ng cÃ³ xung Ä‘á»™t"));
    }
}
