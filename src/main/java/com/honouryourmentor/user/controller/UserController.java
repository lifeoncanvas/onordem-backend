package com.honouryourmentor.user.controller;

import com.honouryourmentor.common.ApiResponse;
import com.honouryourmentor.user.dto.UpdateProfileRequest;
import com.honouryourmentor.user.dto.UserProfileDto;
import com.honouryourmentor.user.dto.UserSummaryDto;
import com.honouryourmentor.user.entity.User;
import com.honouryourmentor.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile retrieval and management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get profile of logged-in user")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(@AuthenticationPrincipal User currentUser) {
        UserProfileDto profile = userService.getProfileByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile of logged-in user")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileDto updated = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get public user summary by ID")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getUserById(@PathVariable String id) {
        UserSummaryDto summary = userService.getUserSummary(id);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
