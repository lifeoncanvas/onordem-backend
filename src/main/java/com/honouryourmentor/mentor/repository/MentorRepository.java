package com.honouryourmentor.mentor.repository;

import com.honouryourmentor.mentor.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, String> {
    List<Mentor> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    Optional<Mentor> findByIdAndCreatedBy(String id, String createdBy);
    Optional<Mentor> findByNameIgnoreCaseAndCreatedBy(String name, String createdBy);
    long countByCreatedBy(String createdBy);
}
