package com.honouryourmentor.engagement.entity;

import com.honouryourmentor.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TributeComment extends BaseEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "tribute_id", nullable = false, length = 64)
    private String tributeId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null || this.id.isBlank()) {
            this.id = "cmt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
}
