package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class cho tất cả seeders
 * Cung cấp common functionality và error handling
 * 
 * @param <T> Type of entity được seed
 */
public abstract class AbstractSeeder<T> implements BaseSeeder<T> {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Template method pattern - main seeding logic
     */
    @Override
    public final List<T> seed() {
        String seederName = getSeederName();
        
        if (!shouldSeed()) {
            log.info("⏭️ [{}] Skipping - data already exists (count: {})", 
                seederName, getExistingData().size());
            return getExistingData();
        }
        
        try {
            log.info("🔄 [{}] Starting seeding... (expected: {} items)", 
                seederName, getExpectedCount());
            
            List<T> result = doSeed();
            
            log.info("✅ [{}] Seeding completed - created {} items", 
                seederName, result.size());
            
            // Verify sau khi seed
            verify();
            log.info("✅ [{}] Verification passed", seederName);
            
            return result;
            
        } catch (Exception e) {
            log.error("❌ [{}] Seeding failed: {}", seederName, e.getMessage(), e);
            throw new SeedingException("Failed to seed " + seederName, e);
        }
    }
    
    /**
     * Abstract method để implement seeding logic cụ thể
     * @return List các entity đã tạo
     */
    protected abstract List<T> doSeed();
    
    /**
     * Default implementation - có thể override
     */
    @Override
    public void verify() {
        List<T> data = getExistingData();
        int expectedCount = getExpectedCount();
        
        if (data.size() < expectedCount) {
            throw new SeedingException(String.format(
                "Verification failed for %s: expected at least %d items, found %d", 
                getSeederName(), expectedCount, data.size()));
        }
        
        // Additional verification có thể được override
        doAdditionalVerification(data);
    }
    
    /**
     * Override method này để thêm verification logic cụ thể
     * @param data Dữ liệu cần verify
     */
    protected void doAdditionalVerification(List<T> data) {
        // Default: no additional verification
    }
    
    /**
     * Default implementation - lấy class name
     */
    @Override
    public String getSeederName() {
        return getClass().getSimpleName();
    }
    
    /**
     * Default cleanup - có thể override nếu cần
     */
    @Override
    public void cleanup() {
        log.warn("⚠️ [{}] Cleanup not implemented", getSeederName());
    }
    
    /**
     * Utility method để log progress
     */
    protected void logProgress(String message, Object... args) {
        log.info("🔄 [{}] " + message, getSeederName(), args);
    }
    
    /**
     * Utility method để log warning
     */
    protected void logWarning(String message, Object... args) {
        log.warn("⚠️ [{}] " + message, getSeederName(), args);
    }
    
    /**
     * Utility method để log success
     */
    protected void logSuccess(String message, Object... args) {
        log.info("✅ [{}] " + message, getSeederName(), args);
    }
    
    /**
     * Utility method để validate input
     */
    protected void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new SeedingException(String.format(
                "%s validation failed: %s cannot be null", 
                getSeederName(), fieldName));
        }
    }
    
    /**
     * Utility method để validate collection không empty
     */
    protected void validateNotEmpty(List<?> list, String fieldName) {
        if (list == null || list.isEmpty()) {
            throw new SeedingException(String.format(
                "%s validation failed: %s cannot be null or empty", 
                getSeederName(), fieldName));
        }
    }
}
