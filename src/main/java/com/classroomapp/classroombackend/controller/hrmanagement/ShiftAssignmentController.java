package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Operation(summary = "Tìm kiếm shift assignments", description = "Tìm kiếm phân công ca làm việc với filters và pagination")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Page<ShiftAssignmentDto>>> searchAssignments(
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ShiftAssignment.AssignmentStatus status,
            @RequestParam(required = false) ShiftAssignment.AttendanceStatus attendanceStatus,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ShiftAssignment> assignments = shiftAssignmentService.searchAssignments(assignedUserId, startDate, endDate, status, attendanceStatus, search, pageable);

        Page<ShiftAssignmentDto> assignmentDtos = assignments.map(a -> modelMapper.map(a, ShiftAssignmentDto.class));
        // ✅ FIX: đổi thứ tự thành (data, message)
        return ResponseEntity.ok(ApiResponse.success(assignmentDtos, "Tìm kiếm assignments thành công"));
    }

    @Operation(summary = "Lấy assignment theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('TEACHER') and @shiftSecurityService.canViewAssignment(#id, authentication.name)) or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> getAssignmentById(@PathVariable Long id) {
        ShiftAssignment assignment = shiftAssignmentService.findById(id)
                .orElseThrow(() -> new com.classroomapp.classroombackend.exception.ResourceNotFoundException("Không tìm thấy assignment với ID: " + id));
        // ✅ FIX: đổi thứ tự thành (data, message)
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(assignment, ShiftAssignmentDto.class), "Lấy assignment thành công"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> createAssignment(@Valid @RequestBody CreateSingleShiftAssignmentDto createDto) {
        ShiftAssignment assignment = modelMapper.map(createDto, ShiftAssignment.class);
        ShiftAssignment created = shiftAssignmentService.createAssignment(assignment);
        // ✅ FIX: đổi thứ tự thành (data, message)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(modelMapper.map(created, ShiftAssignmentDto.class), "Tạo assignment thành công"));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentDto>>> createBulkAssignments(@Valid @RequestBody List<CreateShiftAssignmentDto> createDtos) {
        List<ShiftAssignment> assignments = createDtos.stream().map(dto -> modelMapper.map(dto, ShiftAssignment.class)).collect(Collectors.toList());
        List<ShiftAssignment> created = shiftAssignmentService.createBulkAssignments(assignments);
        List<ShiftAssignmentDto> createdDtos = created.stream().map(a -> modelMapper.map(a, ShiftAssignmentDto.class)).collect(Collectors.toList());
        // ✅ FIX: đổi thứ tự thành (data, message)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdDtos, "Tạo bulk assignments thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> updateAssignment(@PathVariable Long id, @Valid @RequestBody UpdateShiftAssignmentDto updateDto) {
        ShiftAssignment updated = shiftAssignmentService.updateAssignment(id, modelMapper.map(updateDto, ShiftAssignment.class));
        // ✅ FIX: đổi thứ tự thành (data, message)
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(updated, ShiftAssignmentDto.class), "Cập nhật assignment thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        shiftAssignmentService.deleteAssignment(id);
        // ✅ FIX: cho delete, dùng success với message only
        return ResponseEntity.ok(ApiResponse.<Void>success(null, "Xóa assignment thành công"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> cancelAssignment(@PathVariable Long id, @RequestParam String reason) {
        shiftAssignmentService.cancelAssignment(id, reason);
        // ✅ FIX: cho cancel, dùng success với message only
        return ResponseEntity.ok(ApiResponse.<Void>success(null, "Hủy assignment thành công"));
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('TEACHER') and @shiftSecurityService.canCheckInAssignment(#id, authentication.name))")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> checkIn(@PathVariable Long id, @RequestParam(required = false) String location) {
        ShiftAssignment checkedIn = shiftAssignmentService.checkIn(id, location);
        // ✅ FIX: đổi thứ tự thành (data, message)
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(checkedIn, ShiftAssignmentDto.class), "Check-in thành công"));
    }

    @PostMapping("/{id}/check-out")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('TEACHER') and @shiftSecurityService.canCheckOutAssignment(#id, authentication.name))")
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> checkOut(@PathVariable Long id, @RequestParam(required = false) String location) {
        ShiftAssignment checkedOut = shiftAssignmentService.checkOut(id, location);
        // ✅ FIX: đổi thứ tự thành (data, message)
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(checkedOut, ShiftAssignmentDto.class), "Check-out thành công"));
    }

    @GetMapping("/my-current-week")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<ShiftAssignmentDto>>> getMyCurrentWeekAssignments(Authentication authentication) {
        String username = authentication.getName();
        UserDto userDto = Optional.ofNullable(userService.FindUserByUsername(username)).orElse(null);

        if (userDto == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy thông tin người dùng"));
        }

        Long assignedUserId = userDto.getId();
        List<ShiftAssignment> assignments = shiftAssignmentService.findCurrentWeekAssignments(assignedUserId);
        List<ShiftAssignmentDto> assignmentDtos = assignments.stream().map(a -> modelMapper.map(a, ShiftAssignmentDto.class)).collect(Collectors.toList());
        // ✅ FIX: đổi thứ tự thành (data, message)
        return ResponseEntity.ok(ApiResponse.success(assignmentDtos, "Lấy assignments tuần hiện tại thành công"));
    }
}