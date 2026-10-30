package com.honouryourmentor.engagement.repository;

import com.honouryourmentor.engagement.entity.TributeLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TributeLikeRepository extends JpaRepository<TributeLike, String> {
    boolean existsByUserIdAndTributeId(String userId, String tributeId);
    Optional<TributeLike> findByUserIdAndTributeId(String userId, String tributeId);
    void deleteByUserIdAndTributeId(String userId, String tributeId);
    int countByTributeId(String tributeId);
}
