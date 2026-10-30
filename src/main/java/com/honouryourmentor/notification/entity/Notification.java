package com.honouryourmentor.notification.entity;

import com.honouryourmentor.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "type", length = 40)
    @Builder.Default
    private String type = "GENERAL";

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null || this.id.isBlank()) {
            this.id = "notif_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
}
