package com.classroomapp.classroombackend.service.impl;

import java.util.Objects;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.exception.FileStorageException;
import com.classroomapp.classroombackend.service.FileStorageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Profile("dev") // Only active in 'dev' profile
public class DummyFileStorageServiceImpl implements FileStorageService {

    @Override
    public FileUploadResponse save(MultipartFile file, String folder) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        log.info("Received file to upload to folder {}: {}", folder, originalFilename);

        if (originalFilename.contains("..")) {
            throw new FileStorageException("Invalid path sequence in filename: " + originalFilename);
        }

        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
        String dummyFileUrl = "https://dummy-storage-provider.com/files/" + folder + "/" + uniqueFileName;

        log.info("Simulating file upload. Storing as {} with URL {}", uniqueFileName, dummyFileUrl);

        return new FileUploadResponse(
                uniqueFileName,
                dummyFileUrl,
                file.getContentType(),
                file.getSize()
        );
    }

    @Override
    public String store(MultipartFile file, String folder) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
        String dummyFileUrl = "https://dummy-storage-provider.com/files/" + folder + "/" + uniqueFileName;
        log.info("Simulating file storage for {} in folder {} -> URL: {}", originalFilename, folder, dummyFileUrl);
        return dummyFileUrl;
    }

    @Override
    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("Attempted to delete a file with null or blank name.");
            return;
        }
        log.info("Simulating deletion of file from storage: {}", fileName);
    }
}
