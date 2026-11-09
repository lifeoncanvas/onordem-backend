package com.honouryourmentor.auth.repository;

import com.honouryourmentor.auth.entity.AccountVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountVerificationTokenRepository extends JpaRepository<AccountVerificationToken, String> {

    Optional<AccountVerificationToken> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);
}

