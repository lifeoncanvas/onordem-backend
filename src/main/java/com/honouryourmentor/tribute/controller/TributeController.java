package com.honouryourmentor.tribute.controller;

import com.honouryourmentor.common.ApiResponse;
import com.honouryourmentor.common.PagedResponse;
import com.honouryourmentor.tribute.dto.CreateTributeRequest;
import com.honouryourmentor.tribute.dto.TributeDto;
import com.honouryourmentor.tribute.dto.UpdateTributeRequest;
import com.honouryourmentor.tribute.service.TributeService;
import com.honouryourmentor.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tributes")
@RequiredArgsConstructor
@Tag(name = "Tributes", description = "Create, view, update and manage tributes, memories and letters")
public class TributeController {

    private final TributeService tributeService;

    @PostMapping
    @Operation(summary = "Create a new tribute or memory")
    public ResponseEntity<ApiResponse<TributeDto>> createTribute(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateTributeRequest request
    ) {
        TributeDto created = tributeService.createTribute(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Tribute saved successfully", created));
    }

    @GetMapping("/my-tributes")
    @Operation(summary = "Get all tributes and memories created by the logged-in user")
    public ResponseEntity<ApiResponse<List<TributeDto>>> getMyTributes(@AuthenticationPrincipal User currentUser) {
        List<TributeDto> tributes = tributeService.getUserTributes(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(tributes));
    }

    @GetMapping("/public-feed")
    @Operation(summary = "Get paginated public tributes feed")
    public ResponseEntity<ApiResponse<PagedResponse<TributeDto>>> getPublicFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        String currentUserId = currentUser != null ? currentUser.getId() : null;
        PagedResponse<TributeDto> feed = tributeService.getPublicFeed(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")),
                currentUserId
        );
        return ResponseEntity.ok(ApiResponse.ok(feed));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tribute or memory by ID")
    public ResponseEntity<ApiResponse<TributeDto>> getTributeById(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        String currentUserId = currentUser != null ? currentUser.getId() : null;
        TributeDto tribute = tributeService.getTributeById(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(tribute));
    }

    @GetMapping("/mentor/{mentorId}")
    @Operation(summary = "Get all tributes for a specific mentor")
    public ResponseEntity<ApiResponse<List<TributeDto>>> getTributesByMentor(
            @PathVariable String mentorId,
            @AuthenticationPrincipal User currentUser
    ) {
        String currentUserId = currentUser != null ? currentUser.getId() : null;
        List<TributeDto> tributes = tributeService.getTributesByMentor(mentorId, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(tributes));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tribute or memory")
    public ResponseEntity<ApiResponse<TributeDto>> updateTribute(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateTributeRequest request
    ) {
        TributeDto updated = tributeService.updateTribute(id, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Tribute updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tribute or memory")
    public ResponseEntity<ApiResponse<Void>> deleteTribute(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        tributeService.deleteTribute(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Tribute deleted successfully", null));
    }

    @PostMapping("/{id}/pin")
    @Operation(summary = "Toggle pinned status for tribute or memory")
    public ResponseEntity<ApiResponse<Boolean>> togglePin(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        boolean pinned = tributeService.togglePin(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Pin status updated", pinned));
    }

    @PostMapping("/{id}/media")
    @Operation(summary = "Attach an uploaded picture, voice note, or letter document to a tribute")
    public ResponseEntity<ApiResponse<TributeDto>> addMediaToTribute(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser,
            @RequestParam("mediaUrl") String mediaUrl,
            @RequestParam(value = "mediaType", defaultValue = "IMAGE") String mediaType
    ) {
        TributeDto updated = tributeService.addMediaToTribute(id, currentUser.getId(), mediaUrl, mediaType);
        return ResponseEntity.ok(ApiResponse.ok("Media attached to tribute successfully", updated));
    }
}
