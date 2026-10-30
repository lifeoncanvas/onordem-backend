package com.honouryourmentor.tribute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TributeDto {
    private String id;
    private String kind; // tribute, memory, letter, lesson, milestone
    private String title;
    private String mentorId;
    private String mentorName;
    private String relationship;
    private String photoUri;
    private List<String> additionalImages;
    private String quote;
    private String story;
    private String excerpt;
    private String lessonCarried;
    private String momentToRemember;
    private String templateId;
    private String frameStyle;
    private String typographyStyle;
    private String layoutStyle;
    private String year;
    private String emotion;
    private String emotionColor;
    private String mood;
    private String salutation;
    private String signoff;
    private String signature;
    private String privacy;
    private boolean isPinned;
    private boolean isShared;
    private int likesCount;
    private int commentsCount;
    private boolean isLikedByMe;
    private String userId;
    private Instant createdAt;
}
