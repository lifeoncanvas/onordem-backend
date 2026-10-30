package com.honouryourmentor.reminder.repository;

import com.honouryourmentor.reminder.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, String> {
    List<Reminder> findByUserIdOrderByReminderDateAsc(String userId);
    Optional<Reminder> findByIdAndUserId(String id, String userId);
}
