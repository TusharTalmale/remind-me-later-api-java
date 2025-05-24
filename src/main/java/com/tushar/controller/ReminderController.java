package com.tushar.controller;

import com.tushar.dto.ReminderRequest;
import com.tushar.dto.ReminderResponse;
import com.tushar.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@Slf4j
public class ReminderController {

    private final ReminderService reminderService;

    /**
     * Endpoint to create a new reminder.
     * @param reminderRequest The details of the reminder to create.
     * @return The created reminder with HTTP status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(@Valid @RequestBody ReminderRequest reminderRequest) {
        log.info("Received request to create reminder: {}", reminderRequest);
        ReminderResponse createdReminder = reminderService.createReminder(reminderRequest);
        return new ResponseEntity<>(createdReminder, HttpStatus.CREATED);
    }

    /**
     * Endpoint to get all reminders.
     * @return A list of all reminders with HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getAllReminders() {
        log.info("Received request to get all reminders");
        List<ReminderResponse> reminders = reminderService.getAllReminders();
        return ResponseEntity.ok(reminders);
    }

    /**
     * Endpoint to get all PENDING reminders.
     * @return A list of pending reminders with HTTP status 200 (OK).
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ReminderResponse>> getPendingReminders() {
        log.info("Received request to get pending reminders");
        List<ReminderResponse> reminders = reminderService.getPendingReminders();
        return ResponseEntity.ok(reminders);
    }


    /**
     * Server-Sent Events endpoint to stream due reminders.
     * Keeps the connection open and pushes events when reminders are due.
     * @return SseEmitter for the client to subscribe to.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDueReminders() {
        // Timeout set to a very long period, or rely on client to reconnect.
        // Spring Boot default is 30 seconds, which might be too short for long-lived SSE.
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Indefinite timeout
        log.info("Client connected to SSE stream. Emitter: {}", emitter);
        reminderService.addEmitter(emitter);
        return emitter;
    }
}