package com.classroomapp.classroombackend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.config.seed.TonClassroomDataSeeder;

/**
 * Controller để trigger các data seeder
 */
@RestController
@RequestMapping("/api/public/data-seeder")
@CrossOrigin(origins = "*")
public class DataSeederController {

    @Autowired
    private TonClassroomDataSeeder tonClassroomDataSeeder;

    /**
     * Endpoint để tạo dữ liệu mẫu cho classroom "Tôn"
     */
    @PostMapping("/ton-classroom")
    public ResponseEntity<Map<String, Object>> seedTonClassroomData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🚀 [DataSeederController] Bắt đầu tạo dữ liệu mẫu cho classroom Tôn...");
            
            tonClassroomDataSeeder.seedTonClassroomData();
            
            response.put("success", true);
            response.put("message", "Đã tạo thành công dữ liệu mẫu cho classroom Tôn");
            response.put("details", "Đã tạo: 5 học sinh mới, 5 bài tập cần chấm điểm, 5 bài tập sắp hết hạn, 5 bài tập đã hết hạn");
            
            System.out.println("✅ [DataSeederController] Hoàn thành tạo dữ liệu mẫu cho classroom Tôn");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [DataSeederController] Lỗi khi tạo dữ liệu mẫu: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Lỗi khi tạo dữ liệu mẫu: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint để kiểm tra trạng thái dữ liệu hiện tại
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDataStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Có thể thêm logic để kiểm tra số lượng dữ liệu hiện tại
            response.put("success", true);
            response.put("message", "Data seeder controller đang hoạt động");
            response.put("available_seeders", new String[]{"ton-classroom"});
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi kiểm tra trạng thái: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
