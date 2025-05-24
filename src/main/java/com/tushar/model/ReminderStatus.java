package com.tushar.model;

public enum ReminderStatus {
    PENDING, // Reminder is scheduled but not yet due or sent
    SENT,    // Reminder has been processed/sent via SSE
    FAILED   // Optional: If sending attempt failed (not used in this basic SSE)
}