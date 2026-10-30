package com.honouryourmentor.tribute.entity;

import com.honouryourmentor.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tribute_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TributeMedia extends BaseEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tribute_id", nullable = false)
    private Tribute tribute;

    @Column(name = "media_url", nullable = false, columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(name = "media_type", nullable = false, length = 30)
    @Builder.Default
    private String mediaType = "IMAGE"; // IMAGE, AUDIO, VIDEO

    @Column(name = "sort_order")
    @Builder.Default
    private int sortOrder = 0;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null || this.id.isBlank()) {
            this.id = "tm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
}
