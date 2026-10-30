package com.honouryourmentor.engagement.controller;

import com.honouryourmentor.common.ApiResponse;
import com.honouryourmentor.engagement.dto.CommentDto;
import com.honouryourmentor.engagement.dto.CreateCommentRequest;
import com.honouryourmentor.engagement.dto.LikeStatusDto;
import com.honouryourmentor.engagement.service.EngagementService;
import com.honouryourmentor.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Engagement", description = "Likes and comments on tributes and memories")
public class EngagementController {

    private final EngagementService engagementService;

    @PostMapping("/tributes/{id}/like")
    @Operation(summary = "Like or unlike a tribute (toggle)")
    public ResponseEntity<ApiResponse<LikeStatusDto>> toggleLike(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        LikeStatusDto status = engagementService.toggleLike(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    @GetMapping("/tributes/{id}/like-status")
    @Operation(summary = "Check like status for a tribute")
    public ResponseEntity<ApiResponse<LikeStatusDto>> getLikeStatus(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        LikeStatusDto status = engagementService.getLikeStatus(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    @PostMapping("/tributes/{id}/comments")
    @Operation(summary = "Add a comment to a tribute")
    public ResponseEntity<ApiResponse<CommentDto>> addComment(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentDto comment = engagementService.addComment(id, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Comment added successfully", comment));
    }

    @GetMapping("/tributes/{id}/comments")
    @Operation(summary = "Get all comments for a tribute")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getComments(@PathVariable String id) {
        List<CommentDto> comments = engagementService.getComments(id);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        engagementService.deleteComment(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Comment deleted successfully", null));
    }
}
