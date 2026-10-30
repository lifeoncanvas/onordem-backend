package com.honouryourmentor.reminder.entity;

import com.honouryourmentor.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder extends BaseEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "mentor_id", nullable = false, length = 64)
    private String mentorId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "reminder_type", nullable = false, length = 40)
    @Builder.Default
    private String reminderType = "BIRTHDAY"; // BIRTHDAY, ANNIVERSARY, MENTOR_DAY, CUSTOM

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null || this.id.isBlank()) {
            this.id = "rem_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
}
