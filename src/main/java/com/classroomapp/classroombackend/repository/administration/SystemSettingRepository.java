package com.classroomapp.classroombackend.repository.administration;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.entity.SystemSetting;

/**
 * Repository cho SystemSetting entity
 * Há»— trá»£ tÃ¬m kiáº¿m theo key vÃ  batch operations
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    
    /**
     * TÃ¬m setting theo key name
     */
    Optional<SystemSetting> findByKeyName(String keyName);
    
    /**
     * Kiá»ƒm tra xem key Ä‘Ã£ tá»“n táº¡i chÆ°a
     */
    boolean existsByKeyName(String keyName);
    
    /**
     * Láº¥y táº¥t cáº£ settings theo danh sÃ¡ch keys
     */
    List<SystemSetting> findByKeyNameIn(List<String> keyNames);
    
    /**
     * Láº¥y táº¥t cáº£ settings vá»›i key báº¯t Ä‘áº§u báº±ng prefix
     */
    List<SystemSetting> findByKeyNameStartingWith(String prefix);
    
    /**
     * Äáº¿m sá»‘ lÆ°á»£ng settings
     */
    @Query("SELECT COUNT(s) FROM SystemSetting s")
    long countAllSettings();
}
