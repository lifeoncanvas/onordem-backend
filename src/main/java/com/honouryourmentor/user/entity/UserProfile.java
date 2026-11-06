package com.honouryourmentor.user.entity;

import com.honouryourmentor.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_uri", columnDefinition = "TEXT")
    private String avatarUri;

    @Column(name = "cover_color", length = 30)
    @Builder.Default
    private String coverColor = "#54000E";

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "location", length = 120)
    private String location;

    /** Private: used for the age check, never shown publicly. */
    @Column(name = "birthday")
    private java.time.LocalDate birthday;

    @Column(name = "gender", length = 40)
    private String gender;

    /** Comma-separated kinds of people the user honours, e.g. "teacher,parent". */
    @Column(name = "honouring", columnDefinition = "TEXT")
    private String honouring;

    @Column(name = "setup_complete", nullable = false)
    @Builder.Default
    private boolean setupComplete = false;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.id == null || this.id.isBlank()) {
            this.id = "up_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }
}
