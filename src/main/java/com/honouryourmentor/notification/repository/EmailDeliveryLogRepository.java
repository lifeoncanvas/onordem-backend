package com.honouryourmentor.notification.repository;

import com.honouryourmentor.notification.entity.EmailDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailDeliveryLogRepository extends JpaRepository<EmailDeliveryLog, String> {

    List<EmailDeliveryLog> findTop50ByOrderBySentAtDesc();

    List<EmailDeliveryLog> findByRecipientOrderBySentAtDesc(String recipient);
}
