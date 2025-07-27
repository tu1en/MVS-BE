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
 * Há»— trá»£ tÃ¬m kiáº¿m theo tÃªn, version vÃ  tráº¡ng thÃ¡i active
 */
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    
    /**
     * TÃ¬m workflow theo tÃªn
     */
    Optional<Workflow> findByName(String name);
    
    /**
     * TÃ¬m táº¥t cáº£ workflows active
     */
    List<Workflow> findByIsActiveTrue();
    
    /**
     * TÃ¬m workflows theo tÃªn ngÆ°á»i táº¡o
     */
    List<Workflow> findByCreatedBy(String createdBy);
    
    /**
     * TÃ¬m workflow vá»›i version cao nháº¥t theo tÃªn
     */
    @Query("SELECT w FROM Workflow w WHERE w.name = :name ORDER BY w.version DESC")
    List<Workflow> findLatestVersionByName(@Param("name") String name);
    
    /**
     * Kiá»ƒm tra xem tÃªn workflow Ä‘Ã£ tá»“n táº¡i chÆ°a
     */
    boolean existsByName(String name);
    
    /**
     * Äáº¿m sá»‘ workflows active
     */
    long countByIsActiveTrue();
}
