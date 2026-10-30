package com.honouryourmentor.tribute.service;

import com.honouryourmentor.common.PagedResponse;
import com.honouryourmentor.common.exception.ResourceNotFoundException;
import com.honouryourmentor.engagement.repository.TributeLikeRepository;
import com.honouryourmentor.mentor.entity.Mentor;
import com.honouryourmentor.mentor.repository.MentorRepository;
import com.honouryourmentor.tribute.dto.CreateTributeRequest;
import com.honouryourmentor.tribute.dto.TributeDto;
import com.honouryourmentor.tribute.dto.UpdateTributeRequest;
import com.honouryourmentor.tribute.entity.Tribute;
import com.honouryourmentor.tribute.entity.TributeMedia;
import com.honouryourmentor.tribute.repository.TributeMediaRepository;
import com.honouryourmentor.tribute.repository.TributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TributeService {

    private final TributeRepository tributeRepository;
    private final TributeMediaRepository mediaRepository;
    private final MentorRepository mentorRepository;
    private final TributeLikeRepository likeRepository;

    @Transactional
    public TributeDto createTribute(String userId, CreateTributeRequest request) {
        String mentorName = request.getMentorName().trim();
        String mentorId = request.getMentorId();

        // If mentorId is missing, resolve or auto-create mentor
        if (mentorId == null || mentorId.isBlank()) {
            Mentor mentor = mentorRepository.findByNameIgnoreCaseAndCreatedBy(mentorName, userId)
                    .orElseGet(() -> {
                        Mentor newMentor = Mentor.builder()
                                .createdBy(userId)
                                .name(mentorName)
                                .relationship(request.getRelationship() != null ? request.getRelationship() : "Mentor")
                                .avatarUri(request.getPhotoUri() != null ? request.getPhotoUri() : "")
                                .coverColor("#54000E")
                                .itemCount(0)
                                .latestActivity("Just added")
                                .build();
                        return mentorRepository.save(newMentor);
                    });
            mentorId = mentor.getId();
        }

        String kind = request.getKind() != null ? request.getKind().toLowerCase() : "tribute";

        Tribute tribute = Tribute.builder()
                .userId(userId)
                .mentorId(mentorId)
                .mentorName(mentorName)
                .kind(kind)
                .title(request.getTitle() != null ? request.getTitle() : "A Tribute to " + mentorName)
                .relationship(request.getRelationship())
                .photoUri(request.getPhotoUri())
                .quote(request.getQuote())
                .story(request.getStory())
                .excerpt(request.getExcerpt())
                .lessonCarried(request.getLessonCarried())
                .momentToRemember(request.getMomentToRemember())
                .templateId(request.getTemplateId() != null ? request.getTemplateId() : "classic-elegance")
                .frameStyle(request.getFrameStyle() != null ? request.getFrameStyle() : "gold-leaf")
                .typographyStyle(request.getTypographyStyle() != null ? request.getTypographyStyle() : "serif-editorial")
                .layoutStyle(request.getLayoutStyle())
                .year(request.getYear())
                .emotion(request.getEmotion())
                .emotionColor(request.getEmotionColor())
                .mood(request.getMood())
                .salutation(request.getSalutation())
                .signoff(request.getSignoff())
                .signature(request.getSignature())
                .privacy(request.getPrivacy() != null ? request.getPrivacy().toUpperCase() : "PUBLIC")
                .isPinned(request.isPinned())
                .isShared(request.isShared())
                .likesCount(0)
                .commentsCount(0)
                .build();

        Tribute saved = tributeRepository.save(tribute);

        // Save media list if present
        if (request.getAdditionalImages() != null && !request.getAdditionalImages().isEmpty()) {
            List<TributeMedia> mediaList = new ArrayList<>();
            for (int i = 0; i < request.getAdditionalImages().size(); i++) {
                TributeMedia media = TributeMedia.builder()
                        .tribute(saved)
                        .mediaUrl(request.getAdditionalImages().get(i))
                        .mediaType("IMAGE")
                        .sortOrder(i)
                        .build();
                mediaList.add(media);
            }
            mediaRepository.saveAll(mediaList);
            saved.setMediaList(mediaList);
        }

        // Update mentor item count & latest activity
        if (mentorId != null) {
            mentorRepository.findById(mentorId).ifPresent(m -> {
                int count = tributeRepository.countByMentorId(m.getId());
                m.setItemCount(count);
                m.setLatestActivity("Just now");
                mentorRepository.save(m);
            });
        }

        return mapToDto(saved, userId);
    }

    @Transactional(readOnly = true)
    public TributeDto getTributeById(String id, String currentUserId) {
        Tribute tribute = tributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tribute not found with id: " + id));
        return mapToDto(tribute, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<TributeDto> getUserTributes(String userId) {
        return tributeRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(t -> mapToDto(t, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TributeDto> getTributesByMentor(String mentorId, String currentUserId) {
        return tributeRepository.findByMentorIdOrderByCreatedAtDesc(mentorId)
                .stream()
                .map(t -> mapToDto(t, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<TributeDto> getPublicFeed(Pageable pageable, String currentUserId) {
        Page<Tribute> page = tributeRepository.findByPrivacyOrderByCreatedAtDesc("PUBLIC", pageable);
        List<TributeDto> dtos = page.getContent().stream()
                .map(t -> mapToDto(t, currentUserId))
                .toList();
        return PagedResponse.<TributeDto>builder()
                .content(dtos)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Transactional
    public TributeDto updateTribute(String tributeId, String userId, UpdateTributeRequest request) {
        Tribute tribute = tributeRepository.findByIdAndUserId(tributeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tribute not found or unauthorized: " + tributeId));

        if (request.getTitle() != null) tribute.setTitle(request.getTitle());
        if (request.getMentorName() != null) tribute.setMentorName(request.getMentorName());
        if (request.getRelationship() != null) tribute.setRelationship(request.getRelationship());
        if (request.getPhotoUri() != null) tribute.setPhotoUri(request.getPhotoUri());
        if (request.getQuote() != null) tribute.setQuote(request.getQuote());
        if (request.getStory() != null) tribute.setStory(request.getStory());
        if (request.getExcerpt() != null) tribute.setExcerpt(request.getExcerpt());
        if (request.getLessonCarried() != null) tribute.setLessonCarried(request.getLessonCarried());
        if (request.getMomentToRemember() != null) tribute.setMomentToRemember(request.getMomentToRemember());
        if (request.getTemplateId() != null) tribute.setTemplateId(request.getTemplateId());
        if (request.getFrameStyle() != null) tribute.setFrameStyle(request.getFrameStyle());
        if (request.getTypographyStyle() != null) tribute.setTypographyStyle(request.getTypographyStyle());
        if (request.getLayoutStyle() != null) tribute.setLayoutStyle(request.getLayoutStyle());
        if (request.getYear() != null) tribute.setYear(request.getYear());
        if (request.getEmotion() != null) tribute.setEmotion(request.getEmotion());
        if (request.getEmotionColor() != null) tribute.setEmotionColor(request.getEmotionColor());
        if (request.getMood() != null) tribute.setMood(request.getMood());
        if (request.getSalutation() != null) tribute.setSalutation(request.getSalutation());
        if (request.getSignoff() != null) tribute.setSignoff(request.getSignoff());
        if (request.getSignature() != null) tribute.setSignature(request.getSignature());
        if (request.getPrivacy() != null) tribute.setPrivacy(request.getPrivacy().toUpperCase());
        if (request.getIsPinned() != null) tribute.setPinned(request.getIsPinned());
        if (request.getIsShared() != null) tribute.setShared(request.getIsShared());

        Tribute updated = tributeRepository.save(tribute);
        return mapToDto(updated, userId);
    }

    @Transactional
    public void deleteTribute(String tributeId, String userId) {
        Tribute tribute = tributeRepository.findByIdAndUserId(tributeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tribute not found or unauthorized: " + tributeId));
        String mentorId = tribute.getMentorId();
        tributeRepository.delete(tribute);

        if (mentorId != null) {
            mentorRepository.findById(mentorId).ifPresent(m -> {
                int count = tributeRepository.countByMentorId(m.getId());
                m.setItemCount(count);
                mentorRepository.save(m);
            });
        }
    }

    @Transactional
    public boolean togglePin(String tributeId, String userId) {
        Tribute tribute = tributeRepository.findByIdAndUserId(tributeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tribute not found or unauthorized: " + tributeId));
        tribute.setPinned(!tribute.isPinned());
        tributeRepository.save(tribute);
        return tribute.isPinned();
    }

    public TributeDto mapToDto(Tribute tribute, String currentUserId) {
        List<String> mediaUrls = new ArrayList<>();
        if (tribute.getMediaList() != null) {
            mediaUrls = tribute.getMediaList().stream().map(TributeMedia::getMediaUrl).toList();
        }

        boolean isLiked = false;
        if (currentUserId != null) {
            isLiked = likeRepository.existsByUserIdAndTributeId(currentUserId, tribute.getId());
        }

        return TributeDto.builder()
                .id(tribute.getId())
                .userId(tribute.getUserId())
                .kind(tribute.getKind())
                .title(tribute.getTitle())
                .mentorId(tribute.getMentorId())
                .mentorName(tribute.getMentorName())
                .relationship(tribute.getRelationship())
                .photoUri(tribute.getPhotoUri())
                .additionalImages(mediaUrls)
                .quote(tribute.getQuote())
                .story(tribute.getStory())
                .excerpt(tribute.getExcerpt())
                .lessonCarried(tribute.getLessonCarried())
                .momentToRemember(tribute.getMomentToRemember())
                .templateId(tribute.getTemplateId())
                .frameStyle(tribute.getFrameStyle())
                .typographyStyle(tribute.getTypographyStyle())
                .layoutStyle(tribute.getLayoutStyle())
                .year(tribute.getYear())
                .emotion(tribute.getEmotion())
                .emotionColor(tribute.getEmotionColor())
                .mood(tribute.getMood())
                .salutation(tribute.getSalutation())
                .signoff(tribute.getSignoff())
                .signature(tribute.getSignature())
                .privacy(tribute.getPrivacy())
                .isPinned(tribute.isPinned())
                .isShared(tribute.isShared())
                .likesCount(tribute.getLikesCount())
                .commentsCount(tribute.getCommentsCount())
                .isLikedByMe(isLiked)
                .createdAt(tribute.getCreatedAt())
                .build();
    }
}
