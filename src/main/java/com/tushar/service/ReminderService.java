package com.tushar.service;

import com.tushar.dto.ReminderRequest;
import com.tushar.dto.ReminderResponse;
import com.tushar.model.Reminder;
import com.tushar.model.ReminderStatus;
import com.tushar.repo.ReminderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j // Lombok for logging
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Creates a new reminder and saves it to the database.
     *
     * @param request The reminder creation request.
     * @return The created reminder details.
     */
    @Transactional
    public ReminderResponse createReminder(ReminderRequest request) {
        Reminder reminder = new Reminder();
        reminder.setReminderDateTime(request.getReminderDateTime());
        reminder.setMessage(request.getMessage());
        reminder.setReminderMethod(request.getReminderMethod());
        reminder.setStatus(ReminderStatus.PENDING); // New reminders are always PENDING

        Reminder savedReminder = reminderRepository.save(reminder);
        log.info("Created reminder with ID: {}", savedReminder.getId());
        return ReminderResponse.fromEntity(savedReminder);
    }

    /**
     * Retrieves all reminders from the database.
     *
     * @return A list of all reminders.
     */
    public List<ReminderResponse> getAllReminders() {
        return reminderRepository.findAll().stream()
                .map(ReminderResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves all PENDING reminders, ordered by their due date.
     *
     * @return A list of pending reminders.
     */
    public List<ReminderResponse> getPendingReminders() {
        return reminderRepository.findByStatusOrderByReminderDateTimeAsc(ReminderStatus.PENDING).stream()
                .map(ReminderResponse::fromEntity)
                .toList();
    }


    /**
     * Adds an SSE emitter to the list of active emitters.
     *
     * @param emitter The SseEmitter to add.
     */
    public void addEmitter(SseEmitter emitter) {
        // Set up handlers for completion, timeout, and errors to remove the emitter
        emitter.onCompletion(() -> {
            log.info("Emitter completed: {}", emitter);
            this.emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            log.info("Emitter timed out: {}", emitter);
            this.emitters.remove(emitter);
        });
        emitter.onError(e -> {
            log.error("Emitter error: {}", emitter, e);
            this.emitters.remove(emitter);
        });
        this.emitters.add(emitter);
        log.info("Added new SseEmitter: {}. Total emitters: {}", emitter, emitters.size());

        // Send a confirmation event
        try {
            emitter.send(SseEmitter.event().name("connection").data("SSE Connection Established"));
        } catch (IOException e) {
            log.error("Error sending initial connection event to emitter: {}", emitter, e);
            this.emitters.remove(emitter);
        }
    }

    /**
     * Scheduled task to check for due reminders and send them via SSE.
     * Runs every 10 seconds.
     */
    @Scheduled(fixedRate = 10000) // Check every 10 seconds
    @Transactional
    public void sendDueReminders() {
        if (emitters.isEmpty()) {
            return; // No active clients, no need to process
        }

        LocalDateTime now = LocalDateTime.now();
        List<Reminder> dueReminders = reminderRepository.findByStatusAndReminderDateTimeLessThanEqual(ReminderStatus.PENDING, now);

        if (!dueReminders.isEmpty()) {
            log.info("Found {} due reminders at {}", dueReminders.size(), now);
        }

        for (Reminder reminder : dueReminders) {
            ReminderResponse response = ReminderResponse.fromEntity(reminder);
            log.info("Processing due reminder ID: {}", reminder.getId());

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("reminder-due").data(response).id(String.valueOf(reminder.getId())));
                    log.info("Sent reminder ID {} to emitter {}", reminder.getId(), emitter);
                } catch (IOException e) {
                    log.error("Error sending reminder ID {} to emitter {}: {}", reminder.getId(), emitter, e.getMessage());
                    // Emitter might be dead, remove it. The onError handler should also do this.
                    emitters.remove(emitter);
                }
            }
            // Update status to SENT after successfully notifying all current emitters
            reminder.setStatus(ReminderStatus.SENT);
            reminderRepository.save(reminder);
            log.info("Updated reminder ID {} status to SENT", reminder.getId());
        }
    }
}