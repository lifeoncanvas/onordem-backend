package com.honouryourmentor.engagement.repository;

import com.honouryourmentor.engagement.entity.TributeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TributeCommentRepository extends JpaRepository<TributeComment, String> {
    List<TributeComment> findByTributeIdOrderByCreatedAtAsc(String tributeId);
    int countByTributeId(String tributeId);
}
