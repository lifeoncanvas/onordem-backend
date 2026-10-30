package com.honouryourmentor.mentor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMentorRequest {

    @NotBlank(message = "Mentor name is required")
    private String name;

    @NotBlank(message = "Relationship is required")
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
