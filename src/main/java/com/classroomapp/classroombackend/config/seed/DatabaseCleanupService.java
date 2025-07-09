package com.classroomapp.classroombackend.config.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

/**
 * Database cleanup component that runs before seeders to ensure data integrity
 */
@Component
@Order(1) // Run before other seeders
public class DatabaseCleanupService {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseCleanupService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    @Transactional
    public void cleanupDuplicateSubmissions() {
        log.info("🧹 [DatabaseCleanupService] Starting duplicate submission cleanup...");
        
        try {
            // First, count existing duplicates
            String countDuplicatesQuery = """
                SELECT COUNT(*) FROM (
                    SELECT assignment_id, student_id, COUNT(*) as cnt
                    FROM submissions
                    GROUP BY assignment_id, student_id
                    HAVING COUNT(*) > 1
                ) as duplicates
                """;
            
            Integer duplicateGroups = jdbcTemplate.queryForObject(countDuplicatesQuery, Integer.class);
            
            if (duplicateGroups != null && duplicateGroups > 0) {
                log.warn("Found {} groups of duplicate submissions. Cleaning up...", duplicateGroups);
                
                // For SQL Server, use DELETE with EXISTS subquery instead of JOIN
                String cleanupQuery = """
                    DELETE FROM submissions 
                    WHERE id IN (
                        SELECT s1.id 
                        FROM submissions s1
                        INNER JOIN submissions s2 ON s1.assignment_id = s2.assignment_id 
                                                AND s1.student_id = s2.student_id 
                                                AND s1.id < s2.id
                    )
                    """;
                
                int deletedRows = jdbcTemplate.update(cleanupQuery);
                log.info("✅ [DatabaseCleanupService] Removed {} duplicate submission records", deletedRows);
                
                // Verify cleanup
                Integer remainingDuplicates = jdbcTemplate.queryForObject(countDuplicatesQuery, Integer.class);
                if (remainingDuplicates != null && remainingDuplicates > 0) {
                    log.warn("⚠️ [DatabaseCleanupService] {} duplicate groups still remain after cleanup", remainingDuplicates);
                } else {
                    log.info("✅ [DatabaseCleanupService] All duplicate submissions cleaned up successfully");
                }
            } else {
                log.info("✅ [DatabaseCleanupService] No duplicate submissions found");
            }
            
        } catch (Exception e) {
            log.error("❌ [DatabaseCleanupService] Error during duplicate cleanup: {}", e.getMessage(), e);
            // Don't throw the exception to prevent application startup failure
            // The unique constraint will prevent new duplicates
        }
    }
}
