package com.classroomapp.classroombackend.repository.administration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.administration.SystemConfiguration;

/**
 * Repository for SystemConfiguration entity
 */
@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {
    
    /**
     * Find configuration by key
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.configKey = :configKey AND c.isActive = true")
    Optional<SystemConfiguration> findByConfigKeyAndIsActiveTrue(@Param("configKey") String configKey);
    
    /**
     * Find configuration by key (including inactive)
     */
    Optional<SystemConfiguration> findByConfigKey(String configKey);
    
    /**
     * Find configurations by category
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.configCategory = :category AND c.isActive = true ORDER BY c.sortOrder ASC, c.configName ASC")
    List<SystemConfiguration> findByConfigCategoryAndIsActiveTrue(@Param("category") SystemConfiguration.ConfigCategory category);
    
    /**
     * Find all active configurations
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isActive = true ORDER BY c.configCategory ASC, c.sortOrder ASC, c.configName ASC")
    List<SystemConfiguration> findAllActive();
    
    /**
     * Find active configurations paginated
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isActive = true ORDER BY c.configCategory ASC, c.sortOrder ASC, c.configName ASC")
    Page<SystemConfiguration> findAllActive(Pageable pageable);
    
    /**
     * Find configurations by data type
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.dataType = :dataType AND c.isActive = true ORDER BY c.configName ASC")
    List<SystemConfiguration> findByDataType(@Param("dataType") SystemConfiguration.DataType dataType);
    
    /**
     * Find system configurations
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isSystemConfig = true ORDER BY c.sortOrder ASC")
    List<SystemConfiguration> findSystemConfigurations();
    
    /**
     * Find custom configurations (non-system)
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isSystemConfig = false ORDER BY c.sortOrder ASC")
    Page<SystemConfiguration> findCustomConfigurations(Pageable pageable);
    
    /**
     * Find required configurations
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isRequired = true AND c.isActive = true ORDER BY c.configName ASC")
    List<SystemConfiguration> findRequiredConfigurations();
    
    /**
     * Find encrypted configurations
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isEncrypted = true AND c.isActive = true ORDER BY c.configName ASC")
    List<SystemConfiguration> findEncryptedConfigurations();
    
    /**
     * Search configurations by name or description
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isActive = true AND " +
           "(LOWER(c.configName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY c.configName ASC")
    Page<SystemConfiguration> searchConfigurations(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find configurations with default values
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.defaultValue IS NOT NULL AND c.isActive = true ORDER BY c.configName ASC")
    List<SystemConfiguration> findConfigurationsWithDefaults();
    
    /**
     * Find configurations without values
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.configValue IS NULL AND c.isActive = true ORDER BY c.configName ASC")
    List<SystemConfiguration> findConfigurationsWithoutValues();
    
    /**
     * Find configurations with validation patterns
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.validationPattern IS NOT NULL AND c.isActive = true ORDER BY c.configName ASC")
    List<SystemConfiguration> findConfigurationsWithValidation();
    
    /**
     * Find configurations with allowed values
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.allowedValues IS NOT NULL AND c.isActive = true ORDER BY c.configName ASC")
    List<SystemConfiguration> findConfigurationsWithAllowedValues();
    
    /**
     * Find configurations with min/max values
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE (c.minValue IS NOT NULL OR c.maxValue IS NOT NULL) AND c.isActive = true ORDER BY c.configName ASC")
    List<SystemConfiguration> findConfigurationsWithRange();
    
    /**
     * Check if config key exists
     */
    @Query("SELECT COUNT(c) > 0 FROM SystemConfiguration c WHERE c.configKey = :configKey AND (:excludeId IS NULL OR c.id != :excludeId)")
    boolean existsByConfigKey(@Param("configKey") String configKey, @Param("excludeId") Long excludeId);
    
    /**
     * Check if config name exists
     */
    @Query("SELECT COUNT(c) > 0 FROM SystemConfiguration c WHERE c.configName = :configName AND (:excludeId IS NULL OR c.id != :excludeId)")
    boolean existsByConfigName(@Param("configName") String configName, @Param("excludeId") Long excludeId);
    
    /**
     * Find configurations created by user
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.createdBy = :userId ORDER BY c.createdAt DESC")
    Page<SystemConfiguration> findByCreatedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find configurations updated by user
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.updatedBy = :userId ORDER BY c.updatedAt DESC")
    Page<SystemConfiguration> findByUpdatedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find configurations updated since date
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.updatedAt >= :sinceDate ORDER BY c.updatedAt DESC")
    List<SystemConfiguration> findUpdatedSince(@Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Find configurations by category and type
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.configCategory = :category AND c.dataType = :dataType AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<SystemConfiguration> findByCategoryAndType(@Param("category") SystemConfiguration.ConfigCategory category, 
                                                    @Param("dataType") SystemConfiguration.DataType dataType);
    
    /**
     * Get configuration statistics
     */
    @Query("SELECT " +
           "COUNT(c) as totalConfigs, " +
           "COUNT(CASE WHEN c.isActive = true THEN 1 END) as activeConfigs, " +
           "COUNT(CASE WHEN c.isSystemConfig = true THEN 1 END) as systemConfigs, " +
           "COUNT(CASE WHEN c.isRequired = true THEN 1 END) as requiredConfigs, " +
           "COUNT(CASE WHEN c.isEncrypted = true THEN 1 END) as encryptedConfigs " +
           "FROM SystemConfiguration c")
    Object[] getConfigurationStatistics();
    
    /**
     * Count configurations by category
     */
    @Query("SELECT c.configCategory, COUNT(c) FROM SystemConfiguration c WHERE c.isActive = true GROUP BY c.configCategory")
    List<Object[]> countConfigurationsByCategory();
    
    /**
     * Count configurations by data type
     */
    @Query("SELECT c.dataType, COUNT(c) FROM SystemConfiguration c WHERE c.isActive = true GROUP BY c.dataType")
    List<Object[]> countConfigurationsByDataType();
    
    /**
     * Find next sort order for category
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) + 1 FROM SystemConfiguration c WHERE c.configCategory = :category")
    Integer getNextSortOrderForCategory(@Param("category") SystemConfiguration.ConfigCategory category);
    
    /**
     * Find orphaned configurations (creator no longer exists)
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.createdBy NOT IN (SELECT u.id FROM User u) AND c.isSystemConfig = false")
    List<SystemConfiguration> findOrphanedConfigurations();
    
    /**
     * Find configurations that need validation
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.validationPattern IS NOT NULL AND c.configValue IS NOT NULL AND c.isActive = true")
    List<SystemConfiguration> findConfigurationsNeedingValidation();
    
    /**
     * Find configurations with invalid values
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.configValue IS NOT NULL AND c.allowedValues IS NOT NULL " +
           "AND c.configValue NOT LIKE CONCAT('%', c.allowedValues, '%') " +
           "AND c.isActive = true")
    List<SystemConfiguration> findConfigurationsWithInvalidValues();
    
    /**
     * Find configurations by key pattern
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.configKey LIKE :pattern AND c.isActive = true ORDER BY c.configKey ASC")
    List<SystemConfiguration> findByConfigKeyPattern(@Param("pattern") String pattern);
    
    /**
     * Find recently modified configurations
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.updatedAt >= :since ORDER BY c.updatedAt DESC")
    List<SystemConfiguration> findRecentlyModified(@Param("since") LocalDateTime since);
}
