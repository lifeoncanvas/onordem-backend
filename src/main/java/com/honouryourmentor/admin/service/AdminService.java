package com.honouryourmentor.admin.service;

import com.honouryourmentor.admin.dto.ContentReportDto;
import com.honouryourmentor.admin.dto.CreateReportRequest;
import com.honouryourmentor.admin.entity.ContentReport;
import com.honouryourmentor.admin.repository.ContentReportRepository;
import com.honouryourmentor.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ContentReportRepository reportRepository;

    @Transactional
    public ContentReportDto createReport(String reporterId, CreateReportRequest request) {
        ContentReport report = ContentReport.builder()
                .reporterId(reporterId)
                .tributeId(request.getTributeId())
                .reason(request.getReason().trim())
                .status("PENDING")
                .build();
        ContentReport saved = reportRepository.save(report);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ContentReportDto> getReports(String status) {
        if (status == null || status.isBlank()) {
            return reportRepository.findAll().stream().map(this::mapToDto).toList();
        }
        return reportRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public ContentReportDto updateReportStatus(String reportId, String newStatus) {
        ContentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
        report.setStatus(newStatus.toUpperCase());
        ContentReport updated = reportRepository.save(report);
        return mapToDto(updated);
    }

    private ContentReportDto mapToDto(ContentReport report) {
        return ContentReportDto.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .tributeId(report.getTributeId())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
