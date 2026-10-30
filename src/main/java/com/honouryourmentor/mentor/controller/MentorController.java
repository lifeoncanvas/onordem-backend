package com.honouryourmentor.mentor.controller;

import com.honouryourmentor.common.ApiResponse;
import com.honouryourmentor.mentor.dto.CreateMentorRequest;
import com.honouryourmentor.mentor.dto.MentorDto;
import com.honouryourmentor.mentor.dto.UpdateMentorRequest;
import com.honouryourmentor.mentor.service.MentorService;
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
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
@Tag(name = "Mentors", description = "Create, view, update and manage mentor profiles")
public class MentorController {

    private final MentorService mentorService;

    @GetMapping
    @Operation(summary = "List all mentors created by the current user")
    public ResponseEntity<ApiResponse<List<MentorDto>>> getMyMentors(@AuthenticationPrincipal User currentUser) {
        List<MentorDto> mentors = mentorService.getAllMentors(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(mentors));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mentor profile by ID")
    public ResponseEntity<ApiResponse<MentorDto>> getMentorById(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        MentorDto mentor = mentorService.getMentorById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(mentor));
    }

    @PostMapping
    @Operation(summary = "Create a new mentor profile")
    public ResponseEntity<ApiResponse<MentorDto>> createMentor(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateMentorRequest request
    ) {
        MentorDto created = mentorService.createMentor(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Mentor created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update mentor profile")
    public ResponseEntity<ApiResponse<MentorDto>> updateMentor(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateMentorRequest request
    ) {
        MentorDto updated = mentorService.updateMentor(id, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Mentor updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete mentor profile")
    public ResponseEntity<ApiResponse<Void>> deleteMentor(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        mentorService.deleteMentor(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Mentor deleted successfully", null));
    }
}
