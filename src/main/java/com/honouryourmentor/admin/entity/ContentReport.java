package com.honouryourmentor.admin.entity;

import com.honouryourmentor.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentReport extends BaseEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "reporter_id", nullable = false, length = 64)
    private String reporterId;

    @Column(name = "tribute_id", nullable = false, length = 64)
    private String tributeId;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, REVIEWED, RESOLVED, DISMISSED

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null || this.id.isBlank()) {
            this.id = "rep_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
}
