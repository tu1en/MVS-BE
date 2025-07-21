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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.hrmanagement.CreateShiftAssignmentDto;
import com.classroomapp.classroombackend.dto.hrmanagement.CreateSingleShiftAssignmentDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ShiftAssignmentDto;
import com.classroomapp.classroombackend.dto.hrmanagement.UpdateShiftAssignmentDto;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import com.classroomapp.classroombackend.service.UserService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftAssignmentService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftConflictDetectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller cho Shift Assignment Management
 * Quản lý phân công ca làm việc với check-in/out functionality
 */
@RestController
@RequestMapping("/api/hr/shift-assignments")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Shift Assignment Management", description = "APIs cho quản lý phân công ca làm việc")
@SecurityRequirement(name = "bearerAuth")
public class ShiftAssignmentController {

    private final ShiftAssignmentService shiftAssignmentService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Operation(summary = "Tìm kiếm shift assignments", 
               description = "Tìm kiếm phân công ca làm việc với filters và pagination")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Page<ShiftAssignmentDto>>> searchAssignments(
            @Parameter(description = "ID nhân viên") @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Ngày bắt đầu") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Trạng thái assignment") @RequestParam(required = false) ShiftAssignment.AssignmentStatus status,
            @Parameter(description = "Trạng thái attendance") @RequestParam(required = false) ShiftAssignment.AttendanceStatus attendanceStatus,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String search,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("Tìm kiếm assignments với employeeId: {}, startDate: {}, endDate: {}", 
                employeeId, startDate, endDate);

        Pageable pageable = PageRequest.of(page, size);
        Page<ShiftAssignment> assignments = shiftAssignmentService.searchAssignments(
            employeeId, startDate, endDate, status, attendanceStatus, search, pageable);
        
        Page<ShiftAssignmentDto> assignmentDtos = assignments.map(assignment -> 
            modelMapper.map(assignment, ShiftAssignmentDto.class));

        return ResponseEntity.ok(ApiResponse.success(assignmentDtos, "Tìm kiếm assignments thành công"));
    }

    @Operation(summary = "Lấy assignment theo ID", 
               description = "Lấy thông tin chi tiết của một assignment")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or " +
                 "(hasRole('TEACHER') and @shiftSecurityService.canViewAssignment(#id, authentication.name)) or " +
                 "hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> getAssignmentById(
            @Parameter(description = "ID của assignment") @PathVariable Long id) {
        
        log.info("Lấy assignment với ID: {}", id);

        ShiftAssignment assignment = shiftAssignmentService.findById(id)
            .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException(
                "Không tìm thấy assignment với ID: " + id));

        ShiftAssignmentDto assignmentDto = modelMapper.map(assignment, ShiftAssignmentDto.class);
        return ResponseEntity.ok(ApiResponse.success(assignmentDto, "Lấy assignment thành công"));
    }

    @Operation(summary = "Tạo assignment mới", 
               description = "Tạo phân công ca làm việc mới (chỉ ADMIN và MANAGER)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> createAssignment(
            @Parameter(description = "Thông tin assignment mới") @Valid @RequestBody CreateSingleShiftAssignmentDto createDto) {
        
        log.info("Tạo assignment mới cho employee: {}", createDto.getEmployeeId());

        ShiftAssignment assignment = modelMapper.map(createDto, ShiftAssignment.class);
        ShiftAssignment created = shiftAssignmentService.createAssignment(assignment);
        ShiftAssignmentDto createdDto = modelMapper.map(created, ShiftAssignmentDto.class);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdDto, "Tạo assignment thành công"));
    }

    @Operation(summary = "Tạo bulk assignments", 
               description = "Tạo nhiều assignments cùng lúc")
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentDto>>> createBulkAssignments(
            @Parameter(description = "Danh sách assignments") @Valid @RequestBody List<CreateShiftAssignmentDto> createDtos) {
        
        log.info("Tạo bulk assignments: {} assignments", createDtos.size());

        List<ShiftAssignment> assignments = createDtos.stream()
            .map(dto -> modelMapper.map(dto, ShiftAssignment.class))
            .collect(Collectors.toList());
        
        List<ShiftAssignment> created = shiftAssignmentService.createBulkAssignments(assignments);
        List<ShiftAssignmentDto> createdDtos = created.stream()
            .map(assignment -> modelMapper.map(assignment, ShiftAssignmentDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdDtos, "Tạo bulk assignments thành công"));
    }

    @Operation(summary = "Cập nhật assignment", 
               description = "Cập nhật thông tin assignment (chỉ ADMIN và MANAGER)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> updateAssignment(
            @Parameter(description = "ID của assignment") @PathVariable Long id,
            @Parameter(description = "Thông tin cập nhật") @Valid @RequestBody UpdateShiftAssignmentDto updateDto) {
        
        log.info("Cập nhật assignment ID: {}", id);

        ShiftAssignment assignment = modelMapper.map(updateDto, ShiftAssignment.class);
        ShiftAssignment updated = shiftAssignmentService.updateAssignment(id, assignment);
        ShiftAssignmentDto updatedDto = modelMapper.map(updated, ShiftAssignmentDto.class);

        return ResponseEntity.ok(ApiResponse.success(updatedDto, "Cập nhật assignment thành công"));
    }

    @Operation(summary = "Xóa assignment", 
               description = "Xóa assignment (chỉ ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @Parameter(description = "ID của assignment") @PathVariable Long id) {
        
        log.info("Xóa assignment ID: {}", id);

        shiftAssignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa assignment thành công"));
    }

    @Operation(summary = "Hủy assignment", 
               description = "Hủy assignment với lý do")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> cancelAssignment(
            @Parameter(description = "ID của assignment") @PathVariable Long id,
            @Parameter(description = "Lý do hủy") @RequestParam String reason) {
        
        log.info("Hủy assignment ID: {} với lý do: {}", id, reason);

        shiftAssignmentService.cancelAssignment(id, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Hủy assignment thành công"));
    }

    @Operation(summary = "Check-in cho assignment", 
               description = "Nhân viên check-in vào ca làm việc")
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or " +
                 "(hasRole('TEACHER') and @shiftSecurityService.canCheckInAssignment(#id, authentication.name))")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> checkIn(
            @Parameter(description = "ID của assignment") @PathVariable Long id,
            @Parameter(description = "Vị trí check-in (GPS coordinates)") @RequestParam(required = false) String location) {
        
        log.info("Check-in assignment ID: {}", id);

        ShiftAssignment checkedIn = shiftAssignmentService.checkIn(id, location);
        ShiftAssignmentDto checkedInDto = modelMapper.map(checkedIn, ShiftAssignmentDto.class);

        return ResponseEntity.ok(ApiResponse.success(checkedInDto, "Check-in thành công"));
    }

    @Operation(summary = "Check-out cho assignment", 
               description = "Nhân viên check-out khỏi ca làm việc")
    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or " +
                 "(hasRole('TEACHER') and @shiftSecurityService.canCheckOutAssignment(#id, authentication.name))")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> checkOut(
            @Parameter(description = "ID của assignment") @PathVariable Long id,
            @Parameter(description = "Vị trí check-out (GPS coordinates)") @RequestParam(required = false) String location) {
        
        log.info("Check-out assignment ID: {}", id);

        ShiftAssignment checkedOut = shiftAssignmentService.checkOut(id, location);
        ShiftAssignmentDto checkedOutDto = modelMapper.map(checkedOut, ShiftAssignmentDto.class);

        return ResponseEntity.ok(ApiResponse.success(checkedOutDto, "Check-out thành công"));
    }

    @Operation(summary = "Lấy assignments theo employee và ngày", 
               description = "Lấy tất cả assignments của employee trong ngày")
    @GetMapping("/employee/{employeeId}/date/{date}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or " +
                 "(hasRole('TEACHER') and @shiftSecurityService.canViewEmployeeAssignments(#employeeId, authentication.name)) or " +
                 "hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentDto>>> getAssignmentsByEmployeeAndDate(
            @Parameter(description = "ID nhân viên") @PathVariable Long employeeId,
            @Parameter(description = "Ngày") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Lấy assignments cho employee {} ngày {}", employeeId, date);

        List<ShiftAssignment> assignments = shiftAssignmentService.findByEmployeeAndDate(employeeId, date);
        List<ShiftAssignmentDto> assignmentDtos = assignments.stream()
            .map(assignment -> modelMapper.map(assignment, ShiftAssignmentDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(assignmentDtos, "Lấy assignments thành công"));
    }

    @Operation(summary = "Lấy assignments theo tuần", 
               description = "Lấy assignments trong tuần")
    @GetMapping("/week/{weekStart}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentDto>>> getAssignmentsByWeek(
            @Parameter(description = "Ngày bắt đầu tuần") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @Parameter(description = "ID nhân viên (optional)") @RequestParam(required = false) Long employeeId) {
        
        log.info("Lấy assignments tuần {} cho employee {}", weekStart, employeeId);

        List<ShiftAssignment> assignments = shiftAssignmentService.findByWeek(weekStart, employeeId);
        List<ShiftAssignmentDto> assignmentDtos = assignments.stream()
            .map(assignment -> modelMapper.map(assignment, ShiftAssignmentDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(assignmentDtos, "Lấy assignments tuần thành công"));
    }

    @Operation(summary = "Kiểm tra xung đột assignment", 
               description = "Kiểm tra xung đột trước khi tạo assignment")
    @PostMapping("/check-conflicts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftConflictDetectionService.ConflictCheckResult>> checkConflicts(
            @Parameter(description = "Thông tin assignment để kiểm tra") @Valid @RequestBody CreateShiftAssignmentDto createDto) {
        
        log.info("Kiểm tra conflicts cho assignment");

        ShiftAssignment assignment = modelMapper.map(createDto, ShiftAssignment.class);
        ShiftConflictDetectionService.ConflictCheckResult result = shiftAssignmentService.checkConflicts(assignment);

        return ResponseEntity.ok(ApiResponse.success(result, 
            result.hasConflict() ? "Phát hiện xung đột" : "Không có xung đột"));
    }

    @Operation(summary = "Lấy assignments cần check-in", 
               description = "Lấy danh sách assignments cần check-in")
    @GetMapping("/pending-check-ins")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentDto>>> getPendingCheckIns() {
        log.info("Lấy pending check-ins");

        List<ShiftAssignment> assignments = shiftAssignmentService.findPendingCheckIns();
        List<ShiftAssignmentDto> assignmentDtos = assignments.stream()
            .map(assignment -> modelMapper.map(assignment, ShiftAssignmentDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(assignmentDtos, "Lấy pending check-ins thành công"));
    }

    @Operation(summary = "Lấy assignments cần check-out", 
               description = "Lấy danh sách assignments cần check-out")
    @GetMapping("/pending-check-outs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentDto>>> getPendingCheckOuts() {
        log.info("Lấy pending check-outs");

        List<ShiftAssignment> assignments = shiftAssignmentService.findPendingCheckOuts();
        List<ShiftAssignmentDto> assignmentDtos = assignments.stream()
            .map(assignment -> modelMapper.map(assignment, ShiftAssignmentDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(assignmentDtos, "Lấy pending check-outs thành công"));
    }

    @Operation(summary = "Tính working hours", 
               description = "Tính tổng giờ làm việc của employee")
    @GetMapping("/working-hours/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or " +
                 "(hasRole('TEACHER') and @shiftSecurityService.canViewEmployeeWorkingHours(#employeeId, authentication.name)) or " +
                 "hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<ShiftAssignmentService.WorkingHoursSummary>> getWorkingHours(
            @Parameter(description = "ID nhân viên") @PathVariable Long employeeId,
            @Parameter(description = "Ngày bắt đầu") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Ngày kết thúc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Tính working hours cho employee {} từ {} đến {}", employeeId, startDate, endDate);

        ShiftAssignmentService.WorkingHoursSummary summary = 
            shiftAssignmentService.calculateWorkingHours(employeeId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(summary, "Tính working hours thành công"));
    }

    @Operation(summary = "Lấy assignments của tôi trong tuần hiện tại", 
               description = "Nhân viên xem assignments của mình trong tuần")
    @GetMapping("/my-current-week")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentDto>>> getMyCurrentWeekAssignments(Authentication authentication) {
        log.info("Lấy current week assignments cho user: {}", authentication.getName());

        // Get employee ID from authentication
        String username = authentication.getName();
        UserDto user = userService.FindUserByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Không tìm thấy thông tin người dùng", null));
        }
        
        Long employeeId = user.getId();
        
        List<ShiftAssignment> assignments = shiftAssignmentService.findCurrentWeekAssignments(employeeId);
        List<ShiftAssignmentDto> assignmentDtos = assignments.stream()
            .map(assignment -> modelMapper.map(assignment, ShiftAssignmentDto.class))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(assignmentDtos, "Lấy assignments tuần hiện tại thành công"));
    }
}
