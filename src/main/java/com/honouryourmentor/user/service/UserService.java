package com.honouryourmentor.user.service;

import com.honouryourmentor.common.exception.BadRequestException;
import com.honouryourmentor.common.exception.ResourceNotFoundException;
import com.honouryourmentor.user.dto.UpdateProfileRequest;
import com.honouryourmentor.user.dto.UserProfileDto;
import com.honouryourmentor.user.dto.UserSummaryDto;
import com.honouryourmentor.user.entity.User;
import com.honouryourmentor.user.entity.UserProfile;
import com.honouryourmentor.user.repository.UserProfileRepository;
import com.honouryourmentor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getProfileByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().user(user).build());

        return mapToDto(user, profile);
    }

    @Transactional
    public UserProfileDto updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if username taken by another user
        userRepository.findByUsername(request.getUsername())
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> {
                    throw new BadRequestException("Username is already taken");
                });

        // Check if email taken by another user
        userRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> {
                    throw new BadRequestException("Email is already in use");
                });

        user.setName(request.getName().trim());
        user.setUsername(request.getUsername().trim().replace("@", ""));
        user.setEmail(request.getEmail().trim().toLowerCase());
        userRepository.save(user);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().user(user).build());

        profile.setBio(request.getBio());
        profile.setAvatarUri(request.getAvatarUri());
        if (request.getCoverColor() != null && !request.getCoverColor().isBlank()) {
            profile.setCoverColor(request.getCoverColor());
        }
        profile.setPhone(request.getPhone());
        profile.setLocation(request.getLocation());
        userProfileRepository.save(profile);

        return mapToDto(user, profile);
    }

    @Transactional(readOnly = true)
    public UserSummaryDto getUserSummary(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String avatar = userProfileRepository.findByUserId(userId)
                .map(UserProfile::getAvatarUri)
                .orElse("");

        return UserSummaryDto.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .avatarUri(avatar)
                .build();
    }

    private UserProfileDto mapToDto(User user, UserProfile profile) {
        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(profile != null ? profile.getBio() : "")
                .avatarUri(profile != null ? profile.getAvatarUri() : "")
                .coverColor(profile != null ? profile.getCoverColor() : "#54000E")
                .phone(profile != null ? profile.getPhone() : "")
                .location(profile != null ? profile.getLocation() : "")
                .build();
    }
}
