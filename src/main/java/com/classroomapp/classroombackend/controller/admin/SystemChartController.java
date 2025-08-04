package com.classroomapp.classroombackend.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.classroomapp.classroombackend.dto.request.SystemChartRequest;
import com.classroomapp.classroombackend.dto.response.SystemChartResponse;
import com.classroomapp.classroombackend.entity.SystemChart;
import com.classroomapp.classroombackend.service.SystemChartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for admin system chart management
 */
@RestController
@RequestMapping("/api/admin/system-charts")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class SystemChartController {
    
    private final SystemChartService chartService;
    
    /**
     * Create new system chart
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SystemChartResponse>> createChart(
            @Valid @RequestBody SystemChartRequest request) {
        
        log.info("Creating new system chart: {}", request.getTitle());
        
        try {
            SystemChartResponse response = chartService.createChart(request);
            
            return ResponseEntity.ok(
                ApiResponse.<SystemChartResponse>builder()
                    .success(true)
                    .message("Tạo biểu đồ thành công")
                    .data(response)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error creating chart", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<SystemChartResponse>builder()
                    .success(false)
                    .message("Lỗi khi tạo biểu đồ: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get all charts with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SystemChartResponse>>> getAllCharts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting all charts - page: {}, size: {}", page, size);
        
        try {
            Page<SystemChartResponse> charts = chartService.getAllCharts(page, size);
            
            return ResponseEntity.ok(
                ApiResponse.<Page<SystemChartResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách biểu đồ thành công")
                    .data(charts)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting charts", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<Page<SystemChartResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách biểu đồ: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get chart by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemChartResponse>> getChartById(@PathVariable Long id) {
        log.info("Getting chart with ID: {}", id);
        
        try {
            SystemChartResponse chart = chartService.getChartById(id);
            
            return ResponseEntity.ok(
                ApiResponse.<SystemChartResponse>builder()
                    .success(true)
                    .message("Lấy biểu đồ thành công")
                    .data(chart)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting chart by ID", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update chart
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemChartResponse>> updateChart(
            @PathVariable Long id,
            @Valid @RequestBody SystemChartRequest request,
            @RequestParam(defaultValue = "admin") String updatedBy) {
        
        log.info("Updating chart with ID: {}", id);
        
        try {
            SystemChartResponse response = chartService.updateChart(id, request, updatedBy);
            
            return ResponseEntity.ok(
                ApiResponse.<SystemChartResponse>builder()
                    .success(true)
                    .message("Cập nhật biểu đồ thành công")
                    .data(response)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error updating chart", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<SystemChartResponse>builder()
                    .success(false)
                    .message("Lỗi khi cập nhật biểu đồ: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Delete chart
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteChart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "admin") String updatedBy) {
        
        log.info("Deleting chart with ID: {}", id);
        
        try {
            chartService.deleteChart(id, updatedBy);
            
            return ResponseEntity.ok(
                ApiResponse.<String>builder()
                    .success(true)
                    .message("Xóa biểu đồ thành công")
                    .data("Chart deleted successfully")
                    .build()
            );
        } catch (Exception e) {
            log.error("Error deleting chart", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Lỗi khi xóa biểu đồ: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get charts by type
     */
    @GetMapping("/by-type/{chartType}")
    public ResponseEntity<ApiResponse<List<SystemChartResponse>>> getChartsByType(
            @PathVariable SystemChart.ChartType chartType) {
        
        log.info("Getting charts by type: {}", chartType);
        
        try {
            List<SystemChartResponse> charts = chartService.getChartsByType(chartType);
            
            return ResponseEntity.ok(
                ApiResponse.<List<SystemChartResponse>>builder()
                    .success(true)
                    .message("Lấy biểu đồ theo loại thành công")
                    .data(charts)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting charts by type", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<List<SystemChartResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy biểu đồ theo loại: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get public charts
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<SystemChartResponse>>> getPublicCharts() {
        log.info("Getting public charts");
        
        try {
            List<SystemChartResponse> charts = chartService.getPublicCharts();
            
            return ResponseEntity.ok(
                ApiResponse.<List<SystemChartResponse>>builder()
                    .success(true)
                    .message("Lấy biểu đồ công khai thành công")
                    .data(charts)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting public charts", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<List<SystemChartResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy biểu đồ công khai: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get chart statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<Object[]>>> getChartStatistics() {
        log.info("Getting chart statistics");
        
        try {
            List<Object[]> statistics = chartService.getChartStatistics();
            
            return ResponseEntity.ok(
                ApiResponse.<List<Object[]>>builder()
                    .success(true)
                    .message("Lấy thống kê biểu đồ thành công")
                    .data(statistics)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting chart statistics", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<List<Object[]>>builder()
                    .success(false)
                    .message("Lỗi khi lấy thống kê: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Generate sample chart data for student count by month
     */
    @GetMapping("/generate/student-count-monthly")
    public ResponseEntity<ApiResponse<String>> generateStudentCountData() {
        log.info("Generating student count by month chart data");
        
        try {
            String chartData = chartService.generateStudentCountByMonthData();
            
            return ResponseEntity.ok(
                ApiResponse.<String>builder()
                    .success(true)
                    .message("Tạo dữ liệu biểu đồ học sinh theo tháng thành công")
                    .data(chartData)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error generating student count data", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Lỗi khi tạo dữ liệu biểu đồ: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Generate sample chart data for student absence
     */
    @GetMapping("/generate/student-absence")
    public ResponseEntity<ApiResponse<String>> generateStudentAbsenceData() {
        log.info("Generating student absence chart data");
        
        try {
            String chartData = chartService.generateStudentAbsenceData();
            
            return ResponseEntity.ok(
                ApiResponse.<String>builder()
                    .success(true)
                    .message("Tạo dữ liệu biểu đồ học sinh nghỉ học thành công")
                    .data(chartData)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error generating student absence data", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Lỗi khi tạo dữ liệu biểu đồ: " + e.getMessage())
                    .build()
            );
        }
    }
}