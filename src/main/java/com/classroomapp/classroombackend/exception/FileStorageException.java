package com.classroomapp.classroombackend.exception;

/**
 * 🎯 MERGED FILE STORAGE EXCEPTION
 * Merged từ: FileStorageException + FileUploadException + FileSecurityException
 * 
 * ✅ Single exception class cho tất cả file operations
 * ✅ Simplified error handling
 * ✅ Compatible với existing error handling
 */
public class FileStorageException extends RuntimeException {
    
    /**
     * Constructor with message only
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 📁 File upload specific errors
     */
    public static class FileUploadException extends FileStorageException {
        public FileUploadException(String message) {
            super("File upload error: " + message);
        }
        
        public FileUploadException(String message, Throwable cause) {
            super("File upload error: " + message, cause);
        }
    }

    /**
     * 🔒 File security specific errors
     */
    public static class FileSecurityException extends FileStorageException {
        public FileSecurityException(String message) {
            super("File security violation: " + message);
        }
        
        public FileSecurityException(String message, Throwable cause) {
            super("File security violation: " + message, cause);
        }
    }

    /**
     * 📥 File download specific errors
     */
    public static class FileDownloadException extends FileStorageException {
        public FileDownloadException(String message) {
            super("File download error: " + message);
        }
        
        public FileDownloadException(String message, Throwable cause) {
            super("File download error: " + message, cause);
        }
    }

    /**
     * 🗑️ File deletion specific errors
     */
    public static class FileDeletionException extends FileStorageException {
        public FileDeletionException(String message) {
            super("File deletion error: " + message);
        }
        
        public FileDeletionException(String message, Throwable cause) {
            super("File deletion error: " + message, cause);
        }
    }

    // ===== STATIC FACTORY METHODS FOR COMMON ERRORS =====

    public static FileStorageException fileNotFound(String fileName) {
        return new FileStorageException("File not found: " + fileName);
    }

    public static FileStorageException fileTooLarge(String fileName, long maxSize) {
        return new FileUploadException(String.format(
            "File '%s' is too large. Maximum size allowed: %d MB", 
            fileName, maxSize / (1024 * 1024)));
    }

    public static FileStorageException invalidFileType(String fileName, String fileType) {
        return new FileSecurityException(String.format(
            "File '%s' has invalid type '%s'", fileName, fileType));
    }

    public static FileStorageException invalidFileName(String fileName) {
        return new FileSecurityException(String.format(
            "Invalid filename: '%s'", fileName));
    }

    public static FileStorageException pathTraversalAttempt(String fileName) {
        return new FileSecurityException(String.format(
            "Path traversal attempt detected in filename: '%s'", fileName));
    }

    public static FileStorageException storageNotAvailable(String storageType) {
        return new FileStorageException(String.format(
            "Storage '%s' is not available", storageType));
    }

    public static FileStorageException uploadFailed(String fileName, String reason) {
        return new FileUploadException(String.format(
            "Failed to upload file '%s': %s", fileName, reason));
    }

    public static FileStorageException downloadFailed(String fileName, String reason) {
        return new FileDownloadException(String.format(
            "Failed to download file '%s': %s", fileName, reason));
    }

    public static FileStorageException deletionFailed(String fileName, String reason) {
        return new FileDeletionException(String.format(
            "Failed to delete file '%s': %s", fileName, reason));
    }
}