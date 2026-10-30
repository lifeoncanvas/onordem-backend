package com.honouryourmentor.engagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private String id;
    private String tributeId;
    private String userId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private Instant createdAt;
}
