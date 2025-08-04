package com.classroomapp.classroombackend.service.firebase.impl;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.service.firebase.FirebaseStorageService;

@Service
@ConditionalOnMissingBean(name = "firebaseStorageService")
public class MockFirebaseStorageServiceImpl implements FirebaseStorageService {

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String folder) {
        String fileName = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        return FileUploadResponse.builder()
                .filename(fileName)
                .fileUrl("https://fake.firebase.storage/" + fileName)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .build();
    }

    @Override
    public boolean deleteFile(String filePath) {
        return true;
    }

    @Override
    public String getDownloadUrl(String filePath) {
        return "https://fake.firebase.storage/" + filePath;
    }

    @Override
    public boolean fileExists(String filePath) {
        return true;
    }

    @Override
    public String generateSignedUrl(String filePath, int expirationMinutes) {
        return "https://fake.firebase.storage/" + filePath + "?signed=true";
    }
}