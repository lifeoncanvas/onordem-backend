package com.honouryourmentor.tribute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTributeRequest {
    private String title;
    private String mentorName;
    private String mentorId;
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
    private Boolean isPinned;
    private Boolean isShared;
}
