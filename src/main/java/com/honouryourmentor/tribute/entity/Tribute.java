package com.honouryourmentor.tribute.entity;

import com.honouryourmentor.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tribute extends BaseEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "mentor_id", length = 64)
    private String mentorId;

    @Column(name = "mentor_name", nullable = false, length = 120)
    private String mentorName;

    @Column(name = "kind", nullable = false, length = 30)
    @Builder.Default
    private String kind = "tribute"; // tribute, memory, letter, lesson, milestone

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "relationship", length = 80)
    private String relationship;

    @Column(name = "photo_uri", columnDefinition = "TEXT")
    private String photoUri;

    @Column(name = "quote", columnDefinition = "TEXT")
    private String quote;

    @Column(name = "story", columnDefinition = "TEXT")
    private String story;

    @Column(name = "excerpt", columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "lesson_carried", columnDefinition = "TEXT")
    private String lessonCarried;

    @Column(name = "moment_to_remember", columnDefinition = "TEXT")
    private String momentToRemember;

    @Column(name = "template_id", length = 60)
    private String templateId;

    @Column(name = "frame_style", length = 60)
    private String frameStyle;

    @Column(name = "typography_style", length = 60)
    private String typographyStyle;

    @Column(name = "layout_style", length = 60)
    private String layoutStyle;

    @Column(name = "year", length = 20)
    private String year;

    @Column(name = "emotion", length = 60)
    private String emotion;

    @Column(name = "emotion_color", length = 30)
    private String emotionColor;

    @Column(name = "mood", length = 60)
    private String mood;

    @Column(name = "salutation", length = 120)
    private String salutation;

    @Column(name = "signoff", length = 120)
    private String signoff;

    @Column(name = "signature", length = 120)
    private String signature;

    @Column(name = "privacy", nullable = false, length = 20)
    @Builder.Default
    private String privacy = "PUBLIC"; // PUBLIC, PRIVATE

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private boolean isPinned = false;

    @Column(name = "is_shared", nullable = false)
    @Builder.Default
    private boolean isShared = false;

    @Column(name = "likes_count", nullable = false)
    @Builder.Default
    private int likesCount = 0;

    @Column(name = "comments_count", nullable = false)
    @Builder.Default
    private int commentsCount = 0;

    @OneToMany(mappedBy = "tribute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TributeMedia> mediaList = new ArrayList<>();

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null || this.id.isBlank()) {
            this.id = "trib_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
}
