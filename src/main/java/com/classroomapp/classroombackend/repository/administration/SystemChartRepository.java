package com.classroomapp.classroombackend.repository.administration;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.entity.SystemChart;

/**
 * Repository for SystemChart entity
 */
@Repository
public interface SystemChartRepository extends JpaRepository<SystemChart, Long> {
    
    /**
     * Find charts by type
     */
    List<SystemChart> findByChartTypeAndIsActiveTrue(SystemChart.ChartType chartType);
    
    /**
     * Find active charts with pagination
     */
    Page<SystemChart> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Find public charts
     */
    List<SystemChart> findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc();
    
    /**
     * Find charts by creator
     */
    List<SystemChart> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(String createdBy);
    
    /**
     * Find chart by title
     */
    Optional<SystemChart> findByTitleAndIsActiveTrue(String title);
    
    /**
     * Check if chart title exists
     */
    boolean existsByTitleAndIsActiveTrue(String title);
    
    /**
     * Count charts by type
     */
    @Query("SELECT c.chartType, COUNT(c) FROM SystemChart c WHERE c.isActive = true GROUP BY c.chartType")
    List<Object[]> countChartsByType();
}