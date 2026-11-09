package com.honouryourmentor.engagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appreciation_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppreciationNotification {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "sender_id", nullable = false, length = 64)
    private String senderId;

    @Column(name = "recipient_id", nullable = false, length = 64)
    private String recipientId;

    @Column(name = "tribute_id", length = 64)
    private String tributeId;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = "apn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
