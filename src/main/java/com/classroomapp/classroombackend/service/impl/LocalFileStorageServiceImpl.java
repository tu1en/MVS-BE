package com.classroomapp.classroombackend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.service.FileStorageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Primary
@Slf4j
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public String store(MultipartFile file, String folder) {
        try {
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Invalid file name: " + originalFilename);
            }

            String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
            Path folderPath = Paths.get(uploadDir).resolve(folder);
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "http://localhost:" + serverPort + "/api/files/download/" + folder + "/" + uniqueFileName;
            log.info("File stored at: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public FileUploadResponse save(MultipartFile file, String folder) {
        try {
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Invalid file name: " + originalFilename);
            }

            String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
            Path folderPath = Paths.get(uploadDir).resolve(folder);
            Files.createDirectories(folderPath);
            Path filePath = folderPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "http://localhost:" + serverPort + "/api/files/download/" + folder + "/" + uniqueFileName;

            log.info("File saved successfully: {}", fileUrl);
            return new FileUploadResponse(uniqueFileName, fileUrl, file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new RuntimeException("Could not save file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("File name is null or empty, skipping delete");
            return;
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (Files.exists(uploadPath)) {
                Files.walk(uploadPath)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                log.info("Deleted file: {}", path);
                            } catch (IOException e) {
                                log.error("Error deleting file: {}", path, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.error("Error deleting file {}", fileName, e);
        }
    }
}
