package com.honouryourmentor.reminder.service;

import com.honouryourmentor.common.exception.ResourceNotFoundException;
import com.honouryourmentor.common.util.SanitizationUtil;
import com.honouryourmentor.mentor.entity.Mentor;
import com.honouryourmentor.mentor.repository.MentorRepository;
import com.honouryourmentor.reminder.dto.CreateReminderRequest;
import com.honouryourmentor.reminder.dto.ReminderDto;
import com.honouryourmentor.reminder.dto.UpdateReminderRequest;
import com.honouryourmentor.reminder.entity.Reminder;
import com.honouryourmentor.reminder.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final MentorRepository mentorRepository;

    @Transactional
    public ReminderDto createReminder(String userId, CreateReminderRequest request) {
        // Enforce user ownership: user can only set reminders for their own mentors
        Mentor mentor = mentorRepository.findByIdAndCreatedBy(request.getMentorId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found or unauthorized: " + request.getMentorId()));

        Reminder reminder = Reminder.builder()
                .userId(userId)
                .mentorId(mentor.getId())
                .title(SanitizationUtil.sanitize(request.getTitle().trim()))
                .reminderType(request.getReminderType() != null ? request.getReminderType() : "BIRTHDAY")
                .reminderDate(request.getReminderDate())
                .notes(SanitizationUtil.sanitize(request.getNotes()))
                .isActive(true)
                .build();

        Reminder saved = reminderRepository.save(reminder);
        return mapToDto(saved, mentor.getName());
    }

    @Transactional(readOnly = true)
    public List<ReminderDto> getReminders(String userId) {
        return reminderRepository.findByUserIdOrderByReminderDateAsc(userId)
                .stream()
                .map(r -> {
                    String mentorName = mentorRepository.findById(r.getMentorId())
                            .map(Mentor::getName)
                            .orElse("Mentor");
                    return mapToDto(r, mentorName);
                })
                .toList();
    }

    @Transactional
    public ReminderDto updateReminder(String reminderId, String userId, UpdateReminderRequest request) {
        // Enforce user ownership
        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found or unauthorized: " + reminderId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            reminder.setTitle(SanitizationUtil.sanitize(request.getTitle().trim()));
        }
        if (request.getReminderType() != null) {
            reminder.setReminderType(request.getReminderType());
        }
        if (request.getReminderDate() != null) {
            reminder.setReminderDate(request.getReminderDate());
        }
        if (request.getNotes() != null) {
            reminder.setNotes(SanitizationUtil.sanitize(request.getNotes()));
        }
        if (request.getIsActive() != null) {
            reminder.setActive(request.getIsActive());
        }

        Reminder updated = reminderRepository.save(reminder);
        String mentorName = mentorRepository.findById(updated.getMentorId())
                .map(Mentor::getName)
                .orElse("Mentor");
        return mapToDto(updated, mentorName);
    }

    @Transactional
    public void deleteReminder(String reminderId, String userId) {
        // Enforce user ownership
        Reminder reminder = reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found or unauthorized: " + reminderId));
        reminderRepository.delete(reminder);
    }

    private ReminderDto mapToDto(Reminder reminder, String mentorName) {
        return ReminderDto.builder()
                .id(reminder.getId())
                .userId(reminder.getUserId())
                .mentorId(reminder.getMentorId())
                .mentorName(mentorName)
                .title(reminder.getTitle())
                .reminderType(reminder.getReminderType())
                .reminderDate(reminder.getReminderDate())
                .notes(reminder.getNotes())
                .isActive(reminder.isActive())
                .build();
    }
}
