package com.honouryourmentor.reminder.controller;

import com.honouryourmentor.common.ApiResponse;
import com.honouryourmentor.reminder.dto.CreateReminderRequest;
import com.honouryourmentor.reminder.dto.ReminderDto;
import com.honouryourmentor.reminder.dto.UpdateReminderRequest;
import com.honouryourmentor.reminder.service.ReminderService;
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
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
@Tag(name = "Reminders", description = "Birthday, anniversary and gratitude reminders for mentors")
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    @Operation(summary = "List all reminders for current user")
    public ResponseEntity<ApiResponse<List<ReminderDto>>> getMyReminders(@AuthenticationPrincipal User currentUser) {
        List<ReminderDto> reminders = reminderService.getReminders(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(reminders));
    }

    @PostMapping
    @Operation(summary = "Create a new reminder")
    public ResponseEntity<ApiResponse<ReminderDto>> createReminder(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateReminderRequest request
    ) {
        ReminderDto created = reminderService.createReminder(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Reminder created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a reminder")
    public ResponseEntity<ApiResponse<ReminderDto>> updateReminder(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateReminderRequest request
    ) {
        ReminderDto updated = reminderService.updateReminder(id, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Reminder updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reminder")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser
    ) {
        reminderService.deleteReminder(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Reminder deleted successfully", null));
    }
}
