package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import com.classroomapp.classroombackend.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.hrmanagement.CreateShiftSwapRequestDto;
import com.classroomapp.classroombackend.dto.hrmanagement.ShiftSwapRequestDto;
import com.classroomapp.classroombackend.model.hrmanagement.ShiftSwapRequest;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.service.UserService;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftSwapService;

import jakarta.validation.Valid;

/**
 * Controller cho quản lý Shift Swap Requests
 * Xử lý tất cả các yêu cầu đổi ca của nhân viên
 */
@RestController
@RequestMapping("/api/shift-swap")
@CrossOrigin(origins = "*")
public class ShiftSwapController {

    @Autowired
    private ShiftSwapService shiftSwapService;
    
    @Autowired
    private UserService userService;

    /**
     * Tạo yêu cầu đổi ca mới
     */
    @PostMapping("/requests")
    public ResponseEntity<?> createSwapRequest(@Valid @RequestBody CreateShiftSwapRequestDto dto,
                                              Authentication authentication) {
        try {
            User requester = getCurrentUser(authentication);
            
            ShiftSwapRequest request = new ShiftSwapRequest();
            request.setRequester(requester);
            request.setTargetEmployee(userService.findById(dto.getTargetEmployeeId()));
            request.setReason(dto.getReason());
            request.setPriority(ShiftSwapRequest.Priority.valueOf(dto.getPriority()));
            request.setIsEmergency(dto.getIsEmergency());
            request.setRequestTime(dto.getRequestTime());
            
            ShiftSwapRequest created = shiftSwapService.createSwapRequest(request);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Lỗi tạo yêu cầu đổi ca: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách yêu cầu của user hiện tại
     */
    @GetMapping("/my-requests")
    public ResponseEntity<List<ShiftSwapRequest>> getMyRequests(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<ShiftSwapRequest> requests = shiftSwapService.findByRequester(currentUser.getId());
        return ResponseEntity.ok(requests);
    }

    /**
     * Lấy danh sách yêu cầu gửi đến user hiện tại
     */
    @GetMapping("/requests-for-me")
    public ResponseEntity<List<ShiftSwapRequest>> getRequestsForMe(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<ShiftSwapRequest> requests = shiftSwapService.findByTargetEmployee(currentUser.getId());
        return ResponseEntity.ok(requests);
    }

    /**
     * Lấy danh sách yêu cầu chờ phản hồi
     */
    @GetMapping("/pending-response")
    public ResponseEntity<List<ShiftSwapRequest>> getPendingRequests(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<ShiftSwapRequest> requests = shiftSwapService.findPendingRequestsForTarget(currentUser.getId());
        return ResponseEntity.ok(requests);
    }

    /**
     * Tìm kiếm yêu cầu với filters (cho manager)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ShiftSwapRequest>> searchRequests(
            @RequestParam(required = false) Long requesterId,
            @RequestParam(required = false) Long targetEmployeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Boolean isEmergency,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        
        ShiftSwapRequest.SwapStatus swapStatus = status != null ? 
            ShiftSwapRequest.SwapStatus.valueOf(status) : null;
        ShiftSwapRequest.Priority swapPriority = priority != null ? 
            ShiftSwapRequest.Priority.valueOf(priority) : null;
            
        Page<ShiftSwapRequest> requests = shiftSwapService.searchRequests(
            requesterId, targetEmployeeId, swapStatus, swapPriority, 
            isEmergency, search, pageable);
        
        return ResponseEntity.ok(requests);
    }

    /**
     * Lấy chi tiết yêu cầu theo ID
     */
    @GetMapping("/requests/{id}")
    public ResponseEntity<ShiftSwapRequest> getRequestById(@PathVariable Long id) {
        Optional<ShiftSwapRequest> request = shiftSwapService.findById(id);
        return request.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Phản hồi yêu cầu từ target employee
     */
    @PostMapping("/requests/{id}/respond")
    public ResponseEntity<?> respondToRequest(@PathVariable Long id,
                                            @RequestParam String response,
                                            @RequestParam(required = false) String reason,
                                            Authentication authentication) {
        try {
            User targetEmployee = getCurrentUser(authentication);
            ShiftSwapRequest.TargetResponse targetResponse = 
                ShiftSwapRequest.TargetResponse.valueOf(response);
            
            ShiftSwapRequest updated = shiftSwapService.respondByTarget(
                id, targetResponse, reason, targetEmployee);
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Lỗi phản hồi yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Phê duyệt yêu cầu từ manager
     */
    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id,
                                          @RequestParam String response,
                                          @RequestParam(required = false) String reason,
                                          Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            ShiftSwapRequest.ManagerResponse managerResponse = 
                ShiftSwapRequest.ManagerResponse.valueOf(response);
            
            ShiftSwapRequest updated = shiftSwapService.approveByManager(
                id, managerResponse, reason, manager);
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Lỗi phê duyệt yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Hủy yêu cầu
     */
    @PostMapping("/requests/{id}/cancel")
    public ResponseEntity<?> cancelRequest(@PathVariable Long id,
                                         @RequestParam String reason,
                                         Authentication authentication) {
        try {
            shiftSwapService.cancelSwapRequest(id, reason);
            return ResponseEntity.ok("Đã hủy yêu cầu thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Lỗi hủy yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách yêu cầu chờ phê duyệt của manager
     */
    @GetMapping("/manager/pending")
    public ResponseEntity<List<ShiftSwapRequest>> getPendingManagerApproval() {
        List<ShiftSwapRequest> requests = shiftSwapService.findPendingManagerApproval();
        return ResponseEntity.ok(requests);
    }

    /**
     * Lấy danh sách yêu cầu khẩn cấp
     */
    @GetMapping("/emergency")
    public ResponseEntity<List<ShiftSwapRequest>> getEmergencyRequests() {
        List<ShiftSwapRequest> requests = shiftSwapService.findEmergencyRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Lấy thống kê swap requests
     */
    @GetMapping("/statistics")
    public ResponseEntity<ShiftSwapService.SwapStatistics> getSwapStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        ShiftSwapService.SwapStatistics stats = shiftSwapService.getSwapStatistics(startTime, endTime);
        return ResponseEntity.ok(stats);
    }

    /**
     * Lấy top requesters
     */
    @GetMapping("/top-requesters")
    public ResponseEntity<List<Object[]>> getTopRequesters(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Object[]> topRequesters = shiftSwapService.findTopRequesters(startTime, endTime, limit);
        return ResponseEntity.ok(topRequesters);
    }

    /**
     * Gợi ý partner để swap
     */
    @GetMapping("/suggestions/{assignmentId}")
    public ResponseEntity<List<ShiftSwapService.SwapSuggestion>> getSwapSuggestions(
            @PathVariable Long assignmentId) {
        
        List<ShiftSwapService.SwapSuggestion> suggestions = 
            shiftSwapService.suggestSwapPartners(assignmentId);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Bulk approve/reject requests
     */
    @PostMapping("/bulk-process")
    public ResponseEntity<?> bulkProcessRequests(@RequestBody List<Long> requestIds,
                                               @RequestParam String response,
                                               @RequestParam(required = false) String reason,
                                               Authentication authentication) {
        try {
            User manager = getCurrentUser(authentication);
            ShiftSwapRequest.ManagerResponse managerResponse = 
                ShiftSwapRequest.ManagerResponse.valueOf(response);
            
            List<ShiftSwapRequest> processed = shiftSwapService.bulkProcessRequests(
                requestIds, managerResponse, reason, manager);
            
            return ResponseEntity.ok(processed);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Lỗi xử lý hàng loạt: " + e.getMessage());
        }
    }

    /**
     * Export swap requests
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportSwapRequests(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "excel") String format) {
        
        try {
            byte[] data = shiftSwapService.exportSwapRequests(startTime, endTime, format);
            
            String filename = "shift_swap_requests_" + 
                startTime.toLocalDate() + "_to_" + endTime.toLocalDate();
            String contentType = format.equals("csv") ? 
                "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            String extension = format.equals("csv") ? ".csv" : ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=" + filename + extension)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cập nhật yêu cầu
     */
    @PutMapping("/requests/{id}")
    public ResponseEntity<?> updateRequest(@PathVariable Long id,
                                         @Valid @RequestBody ShiftSwapRequestDto dto,
                                         Authentication authentication) {
        try {
            // Convert DTO to entity logic here
            ShiftSwapRequest request = new ShiftSwapRequest();
            // Set properties from DTO
            
            ShiftSwapRequest updated = shiftSwapService.updateSwapRequest(id, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Lỗi cập nhật yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Thực hiện swap sau khi được phê duyệt
     */
    @PostMapping("/requests/{id}/execute")
    public ResponseEntity<?> executeSwap(@PathVariable Long id) {
        try {
            shiftSwapService.executeSwap(id);
            return ResponseEntity.ok("Đã thực hiện đổi ca thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body("Lỗi thực hiện đổi ca: " + e.getMessage());
        }
    }

private User getCurrentUser(Authentication authentication) {
    String usernameOrEmail = authentication.getName();
    
    // Try to find by username first using the DTO method and convert to entity
    UserDto userDto = userService.FindUserByUsername(usernameOrEmail);
    if (userDto != null) {
        return userService.findById(userDto.getId());
    }
    
    // Try to find by email as backup
    User user = userService.findUserEntityByEmail(usernameOrEmail);
    if (user != null) {
        return user;
    }
    
    throw new RuntimeException("Không tìm thấy user hiện tại");
}

}