package com.honouryourmentor.feed.controller;

import com.honouryourmentor.common.ApiResponse;
import com.honouryourmentor.common.PagedResponse;
import com.honouryourmentor.feed.dto.FeedFilterCriteria;
import com.honouryourmentor.feed.service.FeedService;
import com.honouryourmentor.tribute.dto.TributeDto;
import com.honouryourmentor.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "Public tribute and memory feed with categories and filters")
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    @Operation(summary = "Explore public feed with category, relationship, kind and search filters")
    public ResponseEntity<ApiResponse<PagedResponse<TributeDto>>> getFeed(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String relationship,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        FeedFilterCriteria criteria = FeedFilterCriteria.builder()
                .category(category)
                .relationship(relationship)
                .kind(kind)
                .search(search)
                .page(page)
                .size(size)
                .build();

        String currentUserId = currentUser != null ? currentUser.getId() : null;
        PagedResponse<TributeDto> result = feedService.getFilteredFeed(criteria, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
