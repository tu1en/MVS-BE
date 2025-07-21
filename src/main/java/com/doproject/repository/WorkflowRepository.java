package com.doproject.repository;

import com.doproject.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Workflow entity
 * Hỗ trợ tìm kiếm theo tên, version và trạng thái active
 */
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    
    /**
     * Tìm workflow theo tên
     */
    Optional<Workflow> findByName(String name);
    
    /**
     * Tìm tất cả workflows active
     */
    List<Workflow> findByIsActiveTrue();
    
    /**
     * Tìm workflows theo tên người tạo
     */
    List<Workflow> findByCreatedBy(String createdBy);
    
    /**
     * Tìm workflow với version cao nhất theo tên
     */
    @Query("SELECT w FROM Workflow w WHERE w.name = :name ORDER BY w.version DESC")
    List<Workflow> findLatestVersionByName(@Param("name") String name);
    
    /**
     * Kiểm tra xem tên workflow đã tồn tại chưa
     */
    boolean existsByName(String name);
    
    /**
     * Đếm số workflows active
     */
    long countByIsActiveTrue();
}
