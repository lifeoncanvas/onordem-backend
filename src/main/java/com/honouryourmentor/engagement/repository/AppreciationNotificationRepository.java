package com.honouryourmentor.engagement.repository;

import com.honouryourmentor.engagement.entity.AppreciationNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AppreciationNotificationRepository extends JpaRepository<AppreciationNotification, String> {

    boolean existsBySenderIdAndRecipientIdAndCreatedAtAfter(String senderId, String recipientId, Instant after);

    boolean existsBySenderIdAndTributeId(String senderId, String tributeId);

    List<AppreciationNotification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
}
