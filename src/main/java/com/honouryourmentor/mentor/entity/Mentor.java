package com.honouryourmentor.mentor.entity;

import com.honouryourmentor.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "mentors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mentor extends BaseEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "relationship", nullable = false, length = 80)
    @Builder.Default
    private String relationship = "Mentor";

    @Column(name = "avatar_uri", columnDefinition = "TEXT")
    private String avatarUri;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "meaningful_quote", columnDefinition = "TEXT")
    private String meaningfulQuote;

    @Column(name = "item_count")
    @Builder.Default
    private int itemCount = 0;

    @Column(name = "latest_activity", length = 80)
    @Builder.Default
    private String latestActivity = "Just added";

    @Column(name = "cover_color", length = 30)
    @Builder.Default
    private String coverColor = "#54000E";

    @Column(name = "category", length = 40)
    @Builder.Default
    private String category = "life"; // school, study, work, life

    @Column(name = "duration", length = 60)
    private String duration;

    @Column(name = "trait", length = 80)
    private String trait;

    @Column(name = "trait_icon", length = 60)
    private String traitIcon;

    @Column(name = "lesson_label", length = 120)
    private String lessonLabel;

    @Column(name = "birthday")
    private LocalDate birthday;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null || this.id.isBlank()) {
            this.id = "m_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
}
