package com.honouryourmentor.reminder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReminderRequest {

    @NotBlank(message = "Mentor ID is required")
    private String mentorId;

    @NotBlank(message = "Title is required")
    private String title;

    @Builder.Default
    private String reminderType = "BIRTHDAY";

    @NotNull(message = "Reminder date is required")
    private LocalDate reminderDate;

    private String notes;
}
