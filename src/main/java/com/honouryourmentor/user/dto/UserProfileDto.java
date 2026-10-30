package com.honouryourmentor.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String id;
    private String name;
    private String username;
    private String email;
    private String bio;
    private String avatarUri;
    private String coverColor;
    private String phone;
    private String location;
}
