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
public class ReminderDto {
    private String id;
    private String userId;
    private String mentorId;
    private String mentorName;
    private String title;
    private String reminderType;
    private LocalDate reminderDate;
    private String notes;
    private boolean isActive;
}
