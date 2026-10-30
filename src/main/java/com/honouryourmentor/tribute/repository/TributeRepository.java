package com.honouryourmentor.tribute.repository;

import com.honouryourmentor.tribute.entity.Tribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TributeRepository extends JpaRepository<Tribute, String>, JpaSpecificationExecutor<Tribute> {
    List<Tribute> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Tribute> findByMentorIdOrderByCreatedAtDesc(String mentorId);
    Page<Tribute> findByPrivacyOrderByCreatedAtDesc(String privacy, Pageable pageable);
    Optional<Tribute> findByIdAndUserId(String id, String userId);
    List<Tribute> findByUserIdAndIsPinnedTrue(String userId);
    int countByMentorId(String mentorId);
    int countByMentorNameIgnoreCase(String mentorName);
}
