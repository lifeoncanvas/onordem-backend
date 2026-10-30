package com.honouryourmentor.engagement.service;

import com.honouryourmentor.common.exception.BadRequestException;
import com.honouryourmentor.common.exception.ResourceNotFoundException;
import com.honouryourmentor.common.exception.UnauthorizedException;
import com.honouryourmentor.common.util.SanitizationUtil;
import com.honouryourmentor.engagement.dto.CommentDto;
import com.honouryourmentor.engagement.dto.CreateCommentRequest;
import com.honouryourmentor.engagement.dto.LikeStatusDto;
import com.honouryourmentor.engagement.entity.TributeComment;
import com.honouryourmentor.engagement.entity.TributeLike;
import com.honouryourmentor.engagement.repository.TributeCommentRepository;
import com.honouryourmentor.engagement.repository.TributeLikeRepository;
import com.honouryourmentor.tribute.entity.Tribute;
import com.honouryourmentor.tribute.repository.TributeRepository;
import com.honouryourmentor.user.entity.User;
import com.honouryourmentor.user.entity.UserProfile;
import com.honouryourmentor.user.repository.UserProfileRepository;
import com.honouryourmentor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EngagementService {

    private final TributeLikeRepository likeRepository;
    private final TributeCommentRepository commentRepository;
    private final TributeRepository tributeRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public LikeStatusDto toggleLike(String tributeId, String userId) {
        Tribute tribute = tributeRepository.findById(tributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tribute not found: " + tributeId));

        if ("PRIVATE".equalsIgnoreCase(tribute.getPrivacy())) {
            throw new BadRequestException("Likes are only enabled for public tributes");
        }

        boolean exists = likeRepository.existsByUserIdAndTributeId(userId, tributeId);
        boolean likedNow;

        if (exists) {
            likeRepository.deleteByUserIdAndTributeId(userId, tributeId);
            tribute.setLikesCount(Math.max(0, tribute.getLikesCount() - 1));
            likedNow = false;
        } else {
            TributeLike like = TributeLike.builder()
                    .userId(userId)
                    .tributeId(tributeId)
                    .build();
            likeRepository.save(like);
            tribute.setLikesCount(tribute.getLikesCount() + 1);
            likedNow = true;
        }
        tributeRepository.save(tribute);

        return LikeStatusDto.builder()
                .liked(likedNow)
                .likesCount(tribute.getLikesCount())
                .build();
    }

    @Transactional(readOnly = true)
    public LikeStatusDto getLikeStatus(String tributeId, String userId) {
        Tribute tribute = tributeRepository.findById(tributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tribute not found: " + tributeId));
        boolean exists = likeRepository.existsByUserIdAndTributeId(userId, tributeId);
        return LikeStatusDto.builder()
                .liked(exists)
                .likesCount(tribute.getLikesCount())
                .build();
    }

    @Transactional
    public CommentDto addComment(String tributeId, String userId, CreateCommentRequest request) {
        Tribute tribute = tributeRepository.findById(tributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tribute not found: " + tributeId));

        if ("PRIVATE".equalsIgnoreCase(tribute.getPrivacy()) && !tribute.getUserId().equals(userId)) {
            throw new BadRequestException("Comments are only enabled for public tributes");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String sanitizedContent = SanitizationUtil.sanitize(request.getContent().trim());
        if (sanitizedContent.isEmpty()) {
            throw new BadRequestException("Comment cannot be empty");
        }

        TributeComment comment = TributeComment.builder()
                .userId(userId)
                .tributeId(tributeId)
                .content(sanitizedContent)
                .build();

        TributeComment saved = commentRepository.save(comment);

        tribute.setCommentsCount(tribute.getCommentsCount() + 1);
        tributeRepository.save(tribute);

        String avatar = userProfileRepository.findByUserId(userId)
                .map(UserProfile::getAvatarUri)
                .orElse("");

        return CommentDto.builder()
                .id(saved.getId())
                .tributeId(tributeId)
                .userId(userId)
                .authorName(user.getName())
                .authorAvatar(avatar)
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(String tributeId) {
        return commentRepository.findByTributeIdOrderByCreatedAtAsc(tributeId)
                .stream()
                .map(cmt -> {
                    User user = userRepository.findById(cmt.getUserId()).orElse(null);
                    String avatar = userProfileRepository.findByUserId(cmt.getUserId())
                            .map(UserProfile::getAvatarUri)
                            .orElse("");
                    return CommentDto.builder()
                            .id(cmt.getId())
                            .tributeId(cmt.getTributeId())
                            .userId(cmt.getUserId())
                            .authorName(user != null ? user.getName() : "Anonymous")
                            .authorAvatar(avatar)
                            .content(cmt.getContent())
                            .createdAt(cmt.getCreatedAt())
                            .build();
                })
                .toList();
    }

    @Transactional
    public void deleteComment(String commentId, String userId) {
        TributeComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        Tribute tribute = tributeRepository.findById(comment.getTributeId()).orElse(null);

        // Allow comment author OR tribute author to delete
        boolean isCommentAuthor = comment.getUserId().equals(userId);
        boolean isTributeAuthor = tribute != null && tribute.getUserId().equals(userId);

        if (!isCommentAuthor && !isTributeAuthor) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }

        if (tribute != null) {
            tribute.setCommentsCount(Math.max(0, tribute.getCommentsCount() - 1));
            tributeRepository.save(tribute);
        }

        commentRepository.delete(comment);
    }
}
