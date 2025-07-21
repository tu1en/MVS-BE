package com.classroomapp.classroombackend.service.file.image;

import com.classroomapp.classroombackend.config.FileUploadConfig;
import com.classroomapp.classroombackend.exception.ImageProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Image Processing Service
 * Xử lý resize, thumbnail generation, EXIF stripping
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingService {

    private final FileUploadConfig fileUploadConfig;

    /**
     * Process image (resize, optimize, strip EXIF)
     */
    public void processImage(Path imagePath, String category) {
        log.debug("Processing image: {}", imagePath);

        try {
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            if (originalImage == null) {
                throw new ImageProcessingException("Không thể đọc file ảnh: " + imagePath);
            }

            BufferedImage processedImage = originalImage;

            // Resize if needed
            if (fileUploadConfig.getImageProcessing().isEnableResizing()) {
                processedImage = resizeImage(processedImage);
            }

            // Strip EXIF data for privacy
            if (fileUploadConfig.getImageProcessing().isStripExifData()) {
                processedImage = stripExifData(processedImage);
            }

            // Save processed image
            saveProcessedImage(processedImage, imagePath);

            log.debug("Image processed successfully: {}", imagePath);

        } catch (IOException e) {
            log.error("Error processing image {}: {}", imagePath, e.getMessage());
            throw new ImageProcessingException("Lỗi xử lý ảnh: " + e.getMessage(), e);
        }
    }

    /**
     * Resize image if it exceeds maximum dimensions
     */
    private BufferedImage resizeImage(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        int maxWidth = fileUploadConfig.getImageProcessing().getMaxWidth();
        int maxHeight = fileUploadConfig.getImageProcessing().getMaxHeight();

        // Check if resizing is needed
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return originalImage;
        }

        // Calculate new dimensions maintaining aspect ratio
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        log.debug("Resizing image from {}x{} to {}x{}", 
                 originalWidth, originalHeight, newWidth, newHeight);

        // Create resized image with high quality
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // Set high quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * Strip EXIF data by creating new image without metadata
     */
    private BufferedImage stripExifData(BufferedImage image) {
        // Create new image without any metadata
        BufferedImage strippedImage = new BufferedImage(
            image.getWidth(), 
            image.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = strippedImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        
        return strippedImage;
    }

    /**
     * Save processed image with compression
     */
    private void saveProcessedImage(BufferedImage image, Path imagePath) throws IOException {
        String format = getImageFormat(imagePath);
        
        if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) {
            saveJpegWithQuality(image, imagePath);
        } else {
            ImageIO.write(image, format, imagePath.toFile());
        }
    }

    /**
     * Save JPEG with specified quality
     */
    private void saveJpegWithQuality(BufferedImage image, Path imagePath) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer available");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            float quality = fileUploadConfig.getImageProcessing().getQuality() / 100.0f;
            param.setCompressionQuality(quality);
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(imagePath.toFile())) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    /**
     * Generate thumbnails for image
     */
    public List<String> generateThumbnails(Path imagePath) {
        log.debug("Generating thumbnails for image: {}", imagePath);

        List<String> thumbnailPaths = new ArrayList<>();

        try {
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            if (originalImage == null) {
                throw new ImageProcessingException("Không thể đọc file ảnh để tạo thumbnail");
            }

            // Create thumbnails directory
            Path thumbnailDir = Paths.get(fileUploadConfig.getUploadDir(), "thumbnails");
            if (!Files.exists(thumbnailDir)) {
                Files.createDirectories(thumbnailDir);
            }

            String originalFilename = imagePath.getFileName().toString();
            String nameWithoutExtension = getFilenameWithoutExtension(originalFilename);
            String extension = getImageFormat(imagePath);

            // Generate thumbnails for each configured size
            for (String sizeStr : fileUploadConfig.getImageProcessing().getThumbnailSizes()) {
                String[] dimensions = sizeStr.split("x");
                if (dimensions.length != 2) {
                    log.warn("Invalid thumbnail size format: {}", sizeStr);
                    continue;
                }

                try {
                    int width = Integer.parseInt(dimensions[0]);
                    int height = Integer.parseInt(dimensions[1]);

                    BufferedImage thumbnail = createThumbnail(originalImage, width, height);
                    
                    String thumbnailFilename = String.format("%s_%dx%d.%s", 
                                                           nameWithoutExtension, width, height, extension);
                    Path thumbnailPath = thumbnailDir.resolve(thumbnailFilename);

                    saveProcessedImage(thumbnail, thumbnailPath);
                    thumbnailPaths.add("thumbnails/" + thumbnailFilename);

                    log.debug("Generated thumbnail: {}", thumbnailFilename);

                } catch (NumberFormatException e) {
                    log.warn("Invalid thumbnail dimensions: {}", sizeStr);
                }
            }

        } catch (IOException e) {
            log.error("Error generating thumbnails for {}: {}", imagePath, e.getMessage());
            throw new ImageProcessingException("Lỗi tạo thumbnail: " + e.getMessage(), e);
        }

        return thumbnailPaths;
    }

    /**
     * Create thumbnail with specified dimensions
     */
    private BufferedImage createThumbnail(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate scaling to fit within target dimensions while maintaining aspect ratio
        double scaleX = (double) targetWidth / originalWidth;
        double scaleY = (double) targetHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        // Create thumbnail image
        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();

        // Fill background with white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        // Set high quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Center the scaled image
        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;

        g2d.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return thumbnail;
    }

    /**
     * Get image format from file path
     */
    private String getImageFormat(Path imagePath) {
        String filename = imagePath.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "jpg"; // Default format
    }

    /**
     * Get filename without extension
     */
    private String getFilenameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }
        return filename;
    }

    /**
     * Validate image file
     */
    public boolean isValidImage(Path imagePath) {
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get image dimensions
     */
    public ImageDimensions getImageDimensions(Path imagePath) throws IOException {
        BufferedImage image = ImageIO.read(imagePath.toFile());
        if (image == null) {
            throw new IOException("Cannot read image file");
        }
        
        return new ImageDimensions(image.getWidth(), image.getHeight());
    }

    /**
     * Convert image format
     */
    public void convertImageFormat(Path sourcePath, Path targetPath, String targetFormat) throws IOException {
        BufferedImage image = ImageIO.read(sourcePath.toFile());
        if (image == null) {
            throw new IOException("Cannot read source image");
        }

        // Convert to RGB if necessary (for JPEG)
        if ("jpeg".equalsIgnoreCase(targetFormat) || "jpg".equalsIgnoreCase(targetFormat)) {
            BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            image = rgbImage;
        }

        ImageIO.write(image, targetFormat, targetPath.toFile());
    }

    /**
     * Optimize image for web
     */
    public void optimizeForWeb(Path imagePath) {
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                return;
            }

            // Resize if too large for web
            int maxWebWidth = 1920;
            int maxWebHeight = 1080;
            
            if (image.getWidth() > maxWebWidth || image.getHeight() > maxWebHeight) {
                double ratio = Math.min((double) maxWebWidth / image.getWidth(), 
                                      (double) maxWebHeight / image.getHeight());
                
                int newWidth = (int) (image.getWidth() * ratio);
                int newHeight = (int) (image.getHeight() * ratio);
                
                BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
                g2d.dispose();
                
                // Save with web-optimized quality
                saveJpegWithQuality(resized, imagePath);
            }

        } catch (IOException e) {
            log.error("Error optimizing image for web: {}", e.getMessage());
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ImageDimensions {
        private int width;
        private int height;
    }
}
