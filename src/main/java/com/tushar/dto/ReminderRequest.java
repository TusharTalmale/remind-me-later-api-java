package com.tushar.dto;

import com.tushar.model.ReminderMethod;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReminderRequest {

    @NotNull(message = "Reminder date and time cannot be null")
    @FutureOrPresent(message = "Reminder date and time must be in the present or future")
    private LocalDateTime reminderDateTime;

    @NotBlank(message = "Message cannot be blank")
    @Size(min = 1, max = 500, message = "Message must be between 1 and 500 characters")
    private String message;

    @NotNull(message = "Reminder method cannot be null")
    private ReminderMethod reminderMethod;
}