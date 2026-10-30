package com.honouryourmentor.reminder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReminderRequest {
    private String title;
    private String reminderType;
    private LocalDate reminderDate;
    private String notes;
    private Boolean isActive;
}
