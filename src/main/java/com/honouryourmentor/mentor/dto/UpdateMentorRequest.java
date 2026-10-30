package com.honouryourmentor.mentor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMentorRequest {
    private String name;
    private String relationship;
    private String avatarUri;
    private String description;
    private String meaningfulQuote;
    private String coverColor;
    private String category;
    private String duration;
    private String trait;
    private String traitIcon;
    private String lessonLabel;
    private LocalDate birthday;
}
