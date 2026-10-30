package com.honouryourmentor.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReportDto {
    private String id;
    private String reporterId;
    private String tributeId;
    private String reason;
    private String status;
    private Instant createdAt;
}
