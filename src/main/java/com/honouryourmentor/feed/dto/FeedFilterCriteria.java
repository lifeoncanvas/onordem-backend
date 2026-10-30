package com.honouryourmentor.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedFilterCriteria {
    private String category;      // all, school, study, work, life
    private String relationship;  // Teacher, Parent, Coach, etc.
    private String kind;          // tribute, memory, letter, lesson, milestone
    private String search;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 20;
}
