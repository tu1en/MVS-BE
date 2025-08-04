package com.classroomapp.classroombackend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.request.SystemChartRequest;
import com.classroomapp.classroombackend.dto.response.SystemChartResponse;
import com.classroomapp.classroombackend.entity.SystemChart;
import com.classroomapp.classroombackend.repository.administration.SystemChartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing system charts and statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemChartService {
    
    private final SystemChartRepository chartRepository;
    
    /**
     * Create new system chart
     */
    @Transactional
    public SystemChartResponse createChart(SystemChartRequest request) {
        log.info("Creating new system chart: {}", request.getTitle());
        
        SystemChart chart = SystemChart.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .chartType(request.getChartType())
                .chartData(request.getChartData())
                .chartConfig(request.getChartConfig())
                .isActive(request.getIsActive())
                .isPublic(request.getIsPublic())
                .createdBy(request.getCreatedBy())
                .build();
        
        SystemChart savedChart = chartRepository.save(chart);
        return convertToResponse(savedChart);
    }
    
    /**
     * Update existing chart
     */
    @Transactional
    public SystemChartResponse updateChart(Long id, SystemChartRequest request, String updatedBy) {
        log.info("Updating chart with ID: {}", id);
        
        SystemChart chart = chartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biểu đồ với ID: " + id));
        
        chart.setTitle(request.getTitle());
        chart.setDescription(request.getDescription());
        chart.setChartType(request.getChartType());
        chart.setChartData(request.getChartData());
        chart.setChartConfig(request.getChartConfig());
        chart.setIsActive(request.getIsActive());
        chart.setIsPublic(request.getIsPublic());
        chart.setUpdatedBy(updatedBy);
        
        SystemChart savedChart = chartRepository.save(chart);
        return convertToResponse(savedChart);
    }
    
    /**
     * Get all charts with pagination
     */
    public Page<SystemChartResponse> getAllCharts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemChart> charts = chartRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        return charts.map(this::convertToResponse);
    }
    
    /**
     * Get chart by ID
     */
    public SystemChartResponse getChartById(Long id) {
        SystemChart chart = chartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biểu đồ với ID: " + id));
        return convertToResponse(chart);
    }
    
    /**
     * Get charts by type
     */
    public List<SystemChartResponse> getChartsByType(SystemChart.ChartType chartType) {
        List<SystemChart> charts = chartRepository.findByChartTypeAndIsActiveTrue(chartType);
        return charts.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * Get public charts
     */
    public List<SystemChartResponse> getPublicCharts() {
        List<SystemChart> charts = chartRepository.findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc();
        return charts.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * Get charts by creator
     */
    public List<SystemChartResponse> getChartsByCreator(String createdBy) {
        List<SystemChart> charts = chartRepository.findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(createdBy);
        return charts.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * Delete chart (soft delete)
     */
    @Transactional
    public void deleteChart(Long id, String updatedBy) {
        log.info("Deleting chart with ID: {}", id);
        
        SystemChart chart = chartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biểu đồ với ID: " + id));
        
        chart.setIsActive(false);
        chart.setUpdatedBy(updatedBy);
        chartRepository.save(chart);
    }
    
    /**
     * Get chart statistics
     */
    public List<Object[]> getChartStatistics() {
        return chartRepository.countChartsByType();
    }
    
    /**
     * Generate student count by month chart data
     */
    public String generateStudentCountByMonthData() {
        // This would typically query the database for actual data
        // For now, returning sample data
        return "{"
                + "\"labels\": [\"Tháng 1\", \"Tháng 2\", \"Tháng 3\", \"Tháng 4\", \"Tháng 5\", \"Tháng 6\"],"
                + "\"datasets\": [{"
                + "\"label\": \"Số lượng học sinh\","
                + "\"data\": [120, 135, 142, 138, 145, 151],"
                + "\"backgroundColor\": \"rgba(54, 162, 235, 0.2)\","
                + "\"borderColor\": \"rgba(54, 162, 235, 1)\","
                + "\"borderWidth\": 1"
                + "}]"
                + "}";
    }
    
    /**
     * Generate student absence chart data
     */
    public String generateStudentAbsenceData() {
        return "{"
                + "\"labels\": [\"Tháng 1\", \"Tháng 2\", \"Tháng 3\", \"Tháng 4\", \"Tháng 5\", \"Tháng 6\"],"
                + "\"datasets\": [{"
                + "\"label\": \"Học sinh nghỉ học\","
                + "\"data\": [5, 8, 3, 12, 7, 4],"
                + "\"backgroundColor\": \"rgba(255, 99, 132, 0.2)\","
                + "\"borderColor\": \"rgba(255, 99, 132, 1)\","
                + "\"borderWidth\": 1"
                + "}]"
                + "}";
    }
    
    /**
     * Convert entity to response DTO
     */
    private SystemChartResponse convertToResponse(SystemChart chart) {
        return SystemChartResponse.builder()
                .id(chart.getId())
                .title(chart.getTitle())
                .description(chart.getDescription())
                .chartType(chart.getChartType())
                .chartData(chart.getChartData())
                .chartConfig(chart.getChartConfig())
                .isActive(chart.getIsActive())
                .isPublic(chart.getIsPublic())
                .createdBy(chart.getCreatedBy())
                .updatedBy(chart.getUpdatedBy())
                .createdAt(chart.getCreatedAt())
                .updatedAt(chart.getUpdatedAt())
                .build();
    }
}