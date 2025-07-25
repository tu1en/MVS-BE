package com.doproject.controller.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doproject.common.ApiResponse;
import com.doproject.dto.request.SystemSettingsUpdateRequest;
import com.doproject.service.SystemSettingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller xá»­ lÃ½ System Settings cho Admin
 * YÃªu cáº§u quyá»n TEACHER Ä‘á»ƒ truy cáº­p
 */
@RestController
@RequestMapping("/api/admin/system-settings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class SystemSettingsController {
    
    private final SystemSettingService systemSettingService;
    
    /**
     * Láº¥y táº¥t cáº£ cáº¥u hÃ¬nh há»‡ thá»‘ng
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getSystemSettings() {
        log.info("Getting all system settings");
        
        try {
            Map<String, String> settings = systemSettingService.getAllSettings();
            
            return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                    .success(true)
                    .message("Láº¥y cáº¥u hÃ¬nh há»‡ thá»‘ng thÃ nh cÃ´ng")
                    .data(settings)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error getting system settings", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("Lá»—i khi láº¥y cáº¥u hÃ¬nh há»‡ thá»‘ng: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Láº¥y cáº¥u hÃ¬nh theo prefix
     */
    @GetMapping("/prefix/{prefix}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSettingsByPrefix(
            @PathVariable String prefix) {
        log.info("Getting settings with prefix: {}", prefix);
        
        try {
            Map<String, String> settings = systemSettingService.getSettingsByPrefix(prefix);
            
            return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                    .success(true)
                    .message("Láº¥y cáº¥u hÃ¬nh thÃ nh cÃ´ng")
                    .data(settings)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error getting settings by prefix", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("Lá»—i khi láº¥y cáº¥u hÃ¬nh: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Cáº­p nháº­t cáº¥u hÃ¬nh há»‡ thá»‘ng
     */
    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateSystemSettings(
            @Valid @RequestBody SystemSettingsUpdateRequest request) {
        log.info("Updating system settings");
        
        try {
            systemSettingService.updateSettings(request);
            
            return ResponseEntity.ok(
                ApiResponse.<String>builder()
                    .success(true)
                    .message("Cáº­p nháº­t cáº¥u hÃ¬nh há»‡ thá»‘ng thÃ nh cÃ´ng")
                    .data("Settings updated successfully")
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error updating system settings", e);
            
            return ResponseEntity.badRequest().body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Lá»—i khi cáº­p nháº­t cáº¥u hÃ¬nh: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Test káº¿t ná»‘i SMTP
     */
    @PostMapping("/test-smtp")
    public ResponseEntity<ApiResponse<String>> testSMTPConnection() {
        log.info("Testing SMTP connection");
        
        try {
            boolean isConnected = systemSettingService.testSMTPConnection();
            
            if (isConnected) {
                return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                        .success(true)
                        .message("Káº¿t ná»‘i SMTP thÃ nh cÃ´ng")
                        .data("SMTP connection test passed")
                        .build()
                );
            } else {
                return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder()
                        .success(false)
                        .message("Káº¿t ná»‘i SMTP tháº¥t báº¡i")
                        .data("SMTP connection test failed")
                        .build()
                );
            }
            
        } catch (Exception e) {
            log.error("Error testing SMTP connection", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Lá»—i khi test SMTP: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Láº¥y má»™t setting cá»¥ thá»ƒ
     */
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<String>> getSetting(@PathVariable String key) {
        log.info("Getting setting with key: {}", key);
        
        try {
            String value = systemSettingService.getSettingValue(key);
            
            if (value != null) {
                return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                        .success(true)
                        .message("Láº¥y setting thÃ nh cÃ´ng")
                        .data(value)
                        .build()
                );
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting setting", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Lá»—i khi láº¥y setting: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * XÃ³a má»™t setting
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<String>> deleteSetting(@PathVariable String key) {
        log.info("Deleting setting with key: {}", key);
        
        try {
            boolean deleted = systemSettingService.deleteSetting(key);
            
            if (deleted) {
                return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                        .success(true)
                        .message("XÃ³a setting thÃ nh cÃ´ng")
                        .data("Setting deleted successfully")
                        .build()
                );
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error deleting setting", e);
            
            return ResponseEntity.internalServerError().body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Lá»—i khi xÃ³a setting: " + e.getMessage())
                    .build()
            );
        }
    }
}
