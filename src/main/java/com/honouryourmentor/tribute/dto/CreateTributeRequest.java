package com.honouryourmentor.tribute.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTributeRequest {

    @NotBlank(message = "Mentor name is required")
    private String mentorName;

    private String mentorId;

    @Builder.Default
    private String kind = "tribute"; // tribute, memory, letter, lesson, milestone

    private String title;
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

    @Builder.Default
    private String privacy = "PUBLIC"; // PUBLIC, PRIVATE

    @Builder.Default
    private boolean isPinned = false;

    @Builder.Default
    private boolean isShared = false;
}
