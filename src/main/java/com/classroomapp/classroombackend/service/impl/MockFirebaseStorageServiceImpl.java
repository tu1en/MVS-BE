package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.service.FileStorageService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Profile("mock-firebase")
public class MockFirebaseStorageServiceImpl implements FileStorageService {

    @Override
    public FileUploadResponse save(MultipartFile file, String folder) {
        // Tạo tên file duy nhất
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Trả về URL giả Firebase
        String mockFirebaseUrl = "https://firebasestorage.googleapis.com/v0/b/mock-bucket/o/" + folder + "%2F" + uniqueFilename + "?alt=media";
        
        return FileUploadResponse.success(
            uniqueFilename,
            mockFirebaseUrl,
            file.getContentType(),
            file.getSize()
        );
    }

    @Override
    public void delete(String fileName) {
        // Không làm gì trong mock implementation
        System.out.println("Mock Firebase delete called for file: " + fileName);
    }
} 