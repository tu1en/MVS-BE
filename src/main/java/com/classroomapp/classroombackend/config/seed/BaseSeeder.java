package com.classroomapp.classroombackend.config.seed;

import java.util.List;

/**
 * Base interface for all data seeders
 * Định nghĩa contract chung cho tất cả các seeder
 * 
 * @param <T> Type of entity được seed
 */
public interface BaseSeeder<T> {
    
    /**
     * Thực hiện seeding dữ liệu
     * @return List các entity đã được tạo
     */
    List<T> seed();
    
    /**
     * Kiểm tra xem có nên chạy seeding không
     * @return true nếu cần seed, false nếu đã có dữ liệu
     */
    boolean shouldSeed();
    
    /**
     * Verify dữ liệu sau khi seed
     * Throw exception nếu verification fail
     */
    void verify();
    
    /**
     * Lấy tên của seeder (để logging và debugging)
     * @return Tên seeder
     */
    String getSeederName();
    
    /**
     * Lấy dữ liệu hiện có (nếu đã seed rồi)
     * @return List các entity hiện có
     */
    List<T> getExistingData();
    
    /**
     * Cleanup dữ liệu (để re-seed)
     */
    void cleanup();
    
    /**
     * Lấy số lượng entities dự kiến sẽ tạo
     * @return Số lượng dự kiến
     */
    int getExpectedCount();
}
