package com.honouryourmentor.admin.controller;

import com.honouryourmentor.admin.dto.ContentReportDto;
import com.honouryourmentor.admin.dto.CreateReportRequest;
import com.honouryourmentor.admin.service.AdminService;
import com.honouryourmentor.common.ApiResponse;
import com.honouryourmentor.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Moderation & Admin", description = "Report inappropriate content and manage moderation flags")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/reports")
    @Operation(summary = "Report a tribute for inappropriate content or moderation")
    public ResponseEntity<ApiResponse<ContentReportDto>> reportTribute(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateReportRequest request
    ) {
        ContentReportDto report = adminService.createReport(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Report submitted for review", report));
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: list content reports")
    public ResponseEntity<ApiResponse<List<ContentReportDto>>> getReports(
            @RequestParam(required = false) String status
    ) {
        List<ContentReportDto> reports = adminService.getReports(status);
        return ResponseEntity.ok(ApiResponse.ok(reports));
    }

    @PatchMapping("/admin/reports/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: update report status")
    public ResponseEntity<ApiResponse<ContentReportDto>> updateReportStatus(
            @PathVariable String id,
            @RequestParam String status
    ) {
        ContentReportDto updated = adminService.updateReportStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Report status updated", updated));
    }
}
