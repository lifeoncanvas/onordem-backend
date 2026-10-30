package com.honouryourmentor.media.controller;

import com.honouryourmentor.common.ApiResponse;
import com.honouryourmentor.config.DatabasePoolConfig;
import com.honouryourmentor.media.dto.MediaUploadResponse;
import com.honouryourmentor.media.service.MediaStorageService;
import com.honouryourmentor.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Media & Files", description = "Endpoints for uploading pictures, scanned letters, voice notes and files with connection pool isolation")
public class MediaController {

    private final MediaStorageService mediaStorageService;
    private final DatabasePoolConfig databasePoolConfig;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a single picture, voice note, letter or document")
    public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category,
            @AuthenticationPrincipal User currentUser
    ) {
        String userId = currentUser != null ? currentUser.getId() : "anonymous";
        MediaUploadResponse response = mediaStorageService.store(file, userId, category);
        return ResponseEntity.ok(ApiResponse.ok("File uploaded successfully", response));
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple pictures or letter attachments in a single request")
    public ResponseEntity<ApiResponse<List<MediaUploadResponse>>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "category", defaultValue = "general") String category,
            @AuthenticationPrincipal User currentUser
    ) {
        String userId = currentUser != null ? currentUser.getId() : "anonymous";
        List<MediaUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                responses.add(mediaStorageService.store(file, userId, category));
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(responses.size() + " files uploaded successfully", responses));
    }

    @GetMapping("/files/{filename:.+}")
    @Operation(summary = "Stream or view uploaded picture, audio note, or letter document")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = mediaStorageService.loadAsResource(filename);

        String contentType = null;
        try {
            contentType = Files.probeContentType(Paths.get(file.getFile().getAbsolutePath()));
        } catch (IOException e) {
            log.debug("Could not determine content type for: {}", filename);
        }

        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @GetMapping("/pool-health")
    @Operation(summary = "Inspect database connection pool metrics during media workloads")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConnectionPoolMetrics() {
        Map<String, Object> metrics = databasePoolConfig.getPoolMetrics();
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }
}

