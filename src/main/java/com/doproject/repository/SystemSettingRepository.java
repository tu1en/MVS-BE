package com.doproject.repository;

import com.doproject.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho SystemSetting entity
 * Hỗ trợ tìm kiếm theo key và batch operations
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    
    /**
     * Tìm setting theo key name
     */
    Optional<SystemSetting> findByKeyName(String keyName);
    
    /**
     * Kiểm tra xem key đã tồn tại chưa
     */
    boolean existsByKeyName(String keyName);
    
    /**
     * Lấy tất cả settings theo danh sách keys
     */
    List<SystemSetting> findByKeyNameIn(List<String> keyNames);
    
    /**
     * Lấy tất cả settings với key bắt đầu bằng prefix
     */
    List<SystemSetting> findByKeyNameStartingWith(String prefix);
    
    /**
     * Đếm số lượng settings
     */
    @Query("SELECT COUNT(s) FROM SystemSetting s")
    long countAllSettings();
}
