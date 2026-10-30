package com.honouryourmentor.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadResponse {
    private String id;
    private String fileName;
    private String originalFileName;
    private String url;
    private String mediaType; // IMAGE, AUDIO, LETTER_DOC, VIDEO
    private String contentType;
    private long sizeBytes;
    @Builder.Default
    private Instant uploadedAt = Instant.now();
}

