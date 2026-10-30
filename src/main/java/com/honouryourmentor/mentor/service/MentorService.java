package com.honouryourmentor.mentor.service;

import com.honouryourmentor.common.exception.ResourceNotFoundException;
import com.honouryourmentor.mentor.dto.CreateMentorRequest;
import com.honouryourmentor.mentor.dto.MentorDto;
import com.honouryourmentor.mentor.dto.UpdateMentorRequest;
import com.honouryourmentor.mentor.entity.Mentor;
import com.honouryourmentor.mentor.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorRepository mentorRepository;

    @Transactional(readOnly = true)
    public List<MentorDto> getAllMentors(String userId) {
        return mentorRepository.findByCreatedByOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public MentorDto getMentorById(String mentorId, String userId) {
        // Enforce user ownership: users can only access their own mentors
        Mentor mentor = mentorRepository.findByIdAndCreatedBy(mentorId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found or unauthorized: " + mentorId));
        return mapToDto(mentor);
    }

    @Transactional
    public MentorDto createMentor(String userId, CreateMentorRequest request) {
        // Return existing mentor if name matches for this user
        mentorRepository.findByNameIgnoreCaseAndCreatedBy(request.getName().trim(), userId)
                .ifPresent(existing -> {
                    if (request.getMeaningfulQuote() != null) {
                        existing.setMeaningfulQuote(request.getMeaningfulQuote());
                        mentorRepository.save(existing);
                    }
                });

        Mentor mentor = Mentor.builder()
                .createdBy(userId)
                .name(request.getName().trim())
                .relationship(request.getRelationship().trim())
                .avatarUri(request.getAvatarUri() != null ? request.getAvatarUri() : "")
                .description(request.getDescription())
                .meaningfulQuote(request.getMeaningfulQuote())
                .coverColor(request.getCoverColor() != null ? request.getCoverColor() : "#54000E")
                .category(request.getCategory() != null ? request.getCategory() : "life")
                .duration(request.getDuration())
                .trait(request.getTrait())
                .traitIcon(request.getTraitIcon())
                .lessonLabel(request.getLessonLabel())
                .birthday(request.getBirthday())
                .itemCount(0)
                .latestActivity("Just added")
                .build();

        Mentor saved = mentorRepository.save(mentor);
        return mapToDto(saved);
    }

    @Transactional
    public MentorDto updateMentor(String mentorId, String userId, UpdateMentorRequest request) {
        // Enforce user ownership
        Mentor mentor = mentorRepository.findByIdAndCreatedBy(mentorId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found or unauthorized: " + mentorId));

        if (request.getName() != null && !request.getName().isBlank()) {
            mentor.setName(request.getName().trim());
        }
        if (request.getRelationship() != null && !request.getRelationship().isBlank()) {
            mentor.setRelationship(request.getRelationship().trim());
        }
        if (request.getAvatarUri() != null) {
            mentor.setAvatarUri(request.getAvatarUri());
        }
        if (request.getDescription() != null) {
            mentor.setDescription(request.getDescription());
        }
        if (request.getMeaningfulQuote() != null) {
            mentor.setMeaningfulQuote(request.getMeaningfulQuote());
        }
        if (request.getCoverColor() != null) {
            mentor.setCoverColor(request.getCoverColor());
        }
        if (request.getCategory() != null) {
            mentor.setCategory(request.getCategory());
        }
        if (request.getDuration() != null) {
            mentor.setDuration(request.getDuration());
        }
        if (request.getTrait() != null) {
            mentor.setTrait(request.getTrait());
        }
        if (request.getTraitIcon() != null) {
            mentor.setTraitIcon(request.getTraitIcon());
        }
        if (request.getLessonLabel() != null) {
            mentor.setLessonLabel(request.getLessonLabel());
        }
        if (request.getBirthday() != null) {
            mentor.setBirthday(request.getBirthday());
        }

        Mentor updated = mentorRepository.save(mentor);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteMentor(String mentorId, String userId) {
        // Enforce user ownership
        Mentor mentor = mentorRepository.findByIdAndCreatedBy(mentorId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found or unauthorized: " + mentorId));
        mentorRepository.delete(mentor);
    }

    public MentorDto mapToDto(Mentor mentor) {
        return MentorDto.builder()
                .id(mentor.getId())
                .name(mentor.getName())
                .relationship(mentor.getRelationship())
                .avatarUri(mentor.getAvatarUri())
                .description(mentor.getDescription())
                .meaningfulQuote(mentor.getMeaningfulQuote())
                .itemCount(mentor.getItemCount())
                .latestActivity(mentor.getLatestActivity())
                .coverColor(mentor.getCoverColor())
                .category(mentor.getCategory())
                .duration(mentor.getDuration())
                .trait(mentor.getTrait())
                .traitIcon(mentor.getTraitIcon())
                .lessonLabel(mentor.getLessonLabel())
                .birthday(mentor.getBirthday())
                .createdBy(mentor.getCreatedBy())
                .build();
    }
}
