package com.honouryourmentor.engagement.service;

import com.honouryourmentor.common.exception.ResourceNotFoundException;
import com.honouryourmentor.common.exception.UnauthorizedException;
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        TributeComment comment = TributeComment.builder()
                .userId(userId)
                .tributeId(tributeId)
                .content(request.getContent().trim())
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

        if (!comment.getUserId().equals(userId)) {
            throw new UnauthorizedException("You cannot delete another user's comment");
        }

        tributeRepository.findById(comment.getTributeId()).ifPresent(t -> {
            t.setCommentsCount(Math.max(0, t.getCommentsCount() - 1));
            tributeRepository.save(t);
        });

        commentRepository.delete(comment);
    }
}
