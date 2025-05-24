package com.tushar.dto;

import com.tushar.model.Reminder;
import com.tushar.model.ReminderMethod;
import com.tushar.model.ReminderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReminderResponse {
    private Long id;
    private LocalDateTime reminderDateTime;
    private String message;
    private ReminderMethod reminderMethod;
    private ReminderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReminderResponse fromEntity(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getReminderDateTime(),
                reminder.getMessage(),
                reminder.getReminderMethod(),
                reminder.getStatus(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt()
        );
    }
}