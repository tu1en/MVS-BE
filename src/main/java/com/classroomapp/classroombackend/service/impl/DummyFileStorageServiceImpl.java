package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.service.FileStorageService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Profile("dev")
public class DummyFileStorageServiceImpl implements FileStorageService {

    @Override
    public FileUploadResponse save(MultipartFile file, String folder) {
        // Tạo tên file duy nhất
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Trả về URL giả
        String dummyUrl = "https://dummy-storage-provider.com/files/" + folder + "/" + uniqueFilename;
        
        return FileUploadResponse.success(
            uniqueFilename,
            dummyUrl,
            file.getContentType(),
            file.getSize()
        );
    }

    @Override
    public void delete(String fileName) {
        // Không làm gì trong dummy implementation
        System.out.println("Dummy delete called for file: " + fileName);
    }
} 