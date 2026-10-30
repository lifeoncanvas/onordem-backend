package com.honouryourmentor.media.service;

import com.honouryourmentor.common.exception.BadRequestException;
import com.honouryourmentor.common.exception.ResourceNotFoundException;
import com.honouryourmentor.media.dto.MediaUploadResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/**
 * Media Storage Service
 * 
 * Handles storing pictures, scanned letters, and voice notes directly to storage
 * WITHOUT holding database connections. Database transactions are isolated and
 * short-lived, ensuring high concurrency under heavy media uploads.
 */
@Slf4j
@Service
public class MediaStorageService {

    private final Path rootLocation;
    private final String baseUrl;

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif", "heic",
            "pdf", "txt", "doc", "docx",
            "mp3", "m4a", "wav", "aac", "ogg"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            // Images
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/heic", "image/heif",
            // Letters & Documents
            "application/pdf", "text/plain", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            // Audio Notes
            "audio/mpeg", "audio/mp3", "audio/m4a", "audio/x-m4a", "audio/wav", "audio/x-wav",
            "audio/aac", "audio/ogg", "audio/webm"
    );

    public MediaStorageService(
            @Value("${app.storage.upload-dir:uploads/media}") String uploadDir,
            @Value("${app.storage.base-url:https://api.onodem.com/api/v1/media/files}") String baseUrl
    ) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Media storage directory ready at: {}", rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize media storage directory: {}", rootLocation, e);
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    /**
     * Stores a file to disk without holding any database connection.
     */
    public MediaUploadResponse store(MultipartFile file, String userId, String category) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Cannot upload an empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 25MB limit");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload"
        );

        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid filename with relative path sequence: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file extension: ." + extension);
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Uploaded file has uncommon content-type: {} for extension: {}", contentType, extension);
        }

        String mediaType = determineMediaType(extension, contentType);
        String uniqueId = "med_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String storedFileName = uniqueId + "." + extension;

        try {
            Path destinationFile = this.rootLocation.resolve(storedFileName).normalize().toAbsolutePath();

            // Security check: ensure file is inside storage root
            if (!destinationFile.getParent().equals(this.rootLocation)) {
                throw new BadRequestException("Cannot store file outside current directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Stored media file: {} ({} bytes, type: {}) by user: {}",
                    storedFileName, file.getSize(), mediaType, userId);

            String fileUrl = baseUrl + storedFileName;

            return MediaUploadResponse.builder()
                    .id(uniqueId)
                    .fileName(storedFileName)
                    .originalFileName(originalFilename)
                    .url(fileUrl)
                    .mediaType(mediaType)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .sizeBytes(file.getSize())
                    .uploadedAt(Instant.now())
                    .build();

        } catch (IOException e) {
            log.error("Failed to store file: {}", originalFilename, e);
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }
    }

    /**
     * Loads a stored media file as a Spring Resource for HTTP streaming.
     */
    public Resource loadAsResource(String filename) {
        try {
            String sanitizedFilename = Paths.get(filename).getFileName().toString();
            Path file = rootLocation.resolve(sanitizedFilename).normalize();

            // Ensure requested file stays inside root directory
            if (!file.startsWith(rootLocation)) {
                throw new BadRequestException("Invalid file access path");
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Media file not found: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Media file not found: " + filename);
        }
    }

    /**
     * Categorizes media into IMAGE, AUDIO, LETTER_DOC, or VIDEO.
     */
    public String determineMediaType(String extension, String contentType) {
        if (Set.of("jpg", "jpeg", "png", "webp", "gif", "heic").contains(extension)) {
            return "IMAGE";
        }
        if (Set.of("mp3", "m4a", "wav", "aac", "ogg").contains(extension)) {
            return "AUDIO";
        }
        if (Set.of("pdf", "txt", "doc", "docx").contains(extension)) {
            return "LETTER_DOC";
        }
        return "IMAGE";
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
    }
}

