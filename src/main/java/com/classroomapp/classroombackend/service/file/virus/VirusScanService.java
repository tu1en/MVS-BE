package com.classroomapp.classroombackend.service.file.virus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.config.FileUploadConfig;
import com.classroomapp.classroombackend.exception.VirusScanException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Virus Scan Service
 * Tích hợp với ClamAV và fallback basic scan
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VirusScanService {

    private final FileUploadConfig fileUploadConfig;

    // ClamAV commands
    private static final String CLAMD_PING = "zPING\0";
    private static final String CLAMD_SCAN = "zSCAN ";
    private static final String CLAMD_INSTREAM = "zINSTREAM\0";

    /**
     * Scan file for viruses
     */
    public VirusScanResult scanFile(Path filePath) {
        log.debug("Scanning file for viruses: {}", filePath);

        VirusScanResult result = VirusScanResult.builder().build();
        result.setFilePath(filePath.toString());
        result.setScanStartTime(LocalDateTime.now());

        try {
            if (fileUploadConfig.getVirusScan().isEnableClamAV()) {
                result = scanWithClamAV(filePath);
            } else {
                result = performBasicScan(filePath);
            }

            result.setScanEndTime(LocalDateTime.now());

            if (result.isVirusDetected()) {
                handleVirusDetection(filePath, result);
            }

            log.debug("Virus scan completed for file: {} - Status: {}",
                    filePath, result.isVirusDetected() ? "INFECTED" : "CLEAN");

            return result;

        } catch (Exception e) {
            log.error("Error during virus scan for file {}: {}", filePath, e.getMessage());
            result.setScanEndTime(LocalDateTime.now());
            result.setError(true);
            result.setErrorMessage(e.getMessage());

            if ("REJECT_ON_ERROR".equals(fileUploadConfig.getVirusScan().getVirusAction())) {
                throw new VirusScanException("Virus scan failed: " + e.getMessage(), e);
            }

            return result;
        }
    }

    /**
     * Scan file using ClamAV daemon
     */
    private VirusScanResult scanWithClamAV(Path filePath) throws IOException {
        VirusScanResult result = VirusScanResult.builder().build();
        result.setScanMethod("ClamAV");

        String host = fileUploadConfig.getVirusScan().getClamAVHost();
        int port = fileUploadConfig.getVirusScan().getClamAVPort();
        int timeout = fileUploadConfig.getVirusScan().getScanTimeout();

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(timeout * 1000);

            if (!pingClamAV(socket)) {
                throw new IOException("ClamAV daemon không phản hồi");
            }

            String scanResult = scanFileWithInstream(socket, filePath);

            result.setScanOutput(scanResult);

            if (scanResult.contains("FOUND")) {
                result.setVirusDetected(true);
                result.setVirusName(extractVirusName(scanResult));
                result.setThreatLevel("HIGH");
            } else if (scanResult.contains("OK")) {
                result.setVirusDetected(false);
                result.setThreatLevel("NONE");
            } else if (scanResult.contains("ERROR")) {
                result.setError(true);
                result.setErrorMessage("ClamAV scan error: " + scanResult);
            }

        } catch (IOException e) {
            log.error("ClamAV connection error: {}", e.getMessage());
            throw new IOException("Không thể kết nối đến ClamAV daemon: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Ping ClamAV daemon
     */
    private boolean pingClamAV(Socket socket) throws IOException {
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.write(CLAMD_PING.getBytes());
            out.flush();

            String response = in.readLine();
            return "PONG".equals(response);
        }
    }

    /**
     * Scan file using INSTREAM command
     */
    private String scanFileWithInstream(Socket socket, Path filePath) throws IOException {
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             FileInputStream fileInput = new FileInputStream(filePath.toFile())) {

            out.write(CLAMD_INSTREAM.getBytes());

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fileInput.read(buffer)) != -1) {
                out.writeInt(bytesRead);
                out.write(buffer, 0, bytesRead);
            }

            out.writeInt(0);
            out.flush();

            return in.readLine();
        }
    }

    /**
     * Extract virus name from ClamAV output
     */
    private String extractVirusName(String scanOutput) {
        if (scanOutput != null && scanOutput.contains("FOUND")) {
            String[] parts = scanOutput.split(":");
            if (parts.length >= 2) {
                return parts[1].replace("FOUND", "").trim();
            }
        }
        return "Unknown virus";
    }

    /**
     * Perform basic pattern-based virus scan (fallback)
     */
    private VirusScanResult performBasicScan(Path filePath) throws IOException {
        VirusScanResult result = VirusScanResult.builder().build();
        result.setScanMethod("Basic Pattern Scan");

        byte[] fileContent = Files.readAllBytes(filePath);

        if (containsMaliciousPatterns(fileContent)) {
            result.setVirusDetected(true);
            result.setVirusName("Suspicious pattern detected");
            result.setThreatLevel("MEDIUM");
        } else {
            result.setVirusDetected(false);
            result.setThreatLevel("NONE");
        }

        return result;
    }

    /**
     * Check for malicious patterns in file content
     */
    private boolean containsMaliciousPatterns(byte[] content) {
        int scanLength = Math.min(content.length, 1024);
        String contentStr = new String(content, 0, scanLength).toLowerCase();

        String[] maliciousPatterns = {
                "eval(", "exec(", "system(", "shell_exec(",
                "<script", "javascript:", "vbscript:",
                "<?php", "<%", "<jsp:",
                "cmd.exe", "powershell.exe",
                "wget ", "curl ", "nc -",
                "base64_decode", "gzinflate"
        };

        for (String pattern : maliciousPatterns) {
            if (contentStr.contains(pattern)) {
                log.warn("Malicious pattern detected: {}", pattern);
                return true;
            }
        }

        if (content.length >= 2 && content[0] == 0x4D && content[1] == 0x5A) return true;
        if (content.length >= 4 && content[0] == 0x7F && content[1] == 0x45 && content[2] == 0x4C && content[3] == 0x46) return true;

        return false;
    }

    /**
     * Handle virus detection
     */
    private void handleVirusDetection(Path filePath, VirusScanResult result) {
        String action = fileUploadConfig.getVirusScan().getVirusAction();

        log.warn("Virus detected in file {}: {} - Action: {}", filePath, result.getVirusName(), action);

        try {
            switch (action.toUpperCase()) {
                case "DELETE":
                    Files.deleteIfExists(filePath);
                    log.info("Infected file deleted: {}", filePath);
                    break;

                case "QUARANTINE":
                    quarantineFile(filePath, result);
                    break;

                case "REJECT":
                default:
                    throw new VirusScanException("File bị từ chối do phát hiện virus: " + result.getVirusName());
            }
        } catch (IOException e) {
            log.error("Error handling virus detection for file {}: {}", filePath, e.getMessage());
        }
    }

    /**
     * Quarantine infected file
     */
    private void quarantineFile(Path filePath, VirusScanResult result) throws IOException {
        String quarantineDir = fileUploadConfig.getQuarantinePath();
        Path quarantinePath = Paths.get(quarantineDir);

        if (!Files.exists(quarantinePath)) {
            Files.createDirectories(quarantinePath);
        }

        String quarantineFilename = System.currentTimeMillis() + "_" +
                filePath.getFileName().toString() + ".quarantine";
        Path quarantineFilePath = quarantinePath.resolve(quarantineFilename);

        Files.move(filePath, quarantineFilePath);

        Path infoFilePath = quarantinePath.resolve(quarantineFilename + ".info");
        String info = String.format(
                "Original file: %s\nVirus detected: %s\nScan time: %s\nScan method: %s\n",
                filePath.toString(),
                result.getVirusName(),
                result.getScanStartTime(),
                result.getScanMethod()
        );
        Files.write(infoFilePath, info.getBytes());

        log.info("File quarantined: {} -> {}", filePath, quarantineFilePath);
    }

    /**
     * Async virus scan
     */
    public CompletableFuture<VirusScanResult> scanFileAsync(Path filePath) {
        return CompletableFuture.supplyAsync(() -> scanFile(filePath))
                .orTimeout(fileUploadConfig.getVirusScan().getScanTimeout(), TimeUnit.SECONDS);
    }

    /**
     * Check ClamAV daemon status
     */
    public boolean isClamAVAvailable() {
        if (!fileUploadConfig.getVirusScan().isEnableClamAV()) {
            return false;
        }

        try (Socket socket = new Socket(
                fileUploadConfig.getVirusScan().getClamAVHost(),
                fileUploadConfig.getVirusScan().getClamAVPort())) {

            socket.setSoTimeout(5000);
            return pingClamAV(socket);

        } catch (IOException e) {
            log.debug("ClamAV not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get virus scan statistics
     */
    public VirusScanStats getVirusScanStats() {
        return VirusScanStats.builder()
                .totalScans(0L)
                .virusesDetected(0L)
                .quarantinedFiles(0L)
                .lastScanTime(LocalDateTime.now())
                .clamAVAvailable(isClamAVAvailable())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VirusScanResult {
        private String filePath;
        private boolean virusDetected;
        private String virusName;
        private String threatLevel;
        private String scanMethod;
        private String scanOutput;
        private LocalDateTime scanStartTime;
        private LocalDateTime scanEndTime;
        private boolean error;
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VirusScanStats {
        private long totalScans;
        private long virusesDetected;
        private long quarantinedFiles;
        private LocalDateTime lastScanTime;
        private boolean clamAVAvailable;
    }
}
