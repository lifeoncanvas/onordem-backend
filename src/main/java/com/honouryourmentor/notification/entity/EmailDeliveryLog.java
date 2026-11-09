package com.honouryourmentor.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_delivery_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDeliveryLog {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "recipient", nullable = false, length = 160)
    private String recipient;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "email_type", nullable = false, length = 60)
    private String emailType;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "SENT";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    @Builder.Default
    private Instant sentAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = "eml_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        if (this.sentAt == null) {
            this.sentAt = Instant.now();
        }
    }
}

