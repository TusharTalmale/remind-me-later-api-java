package com.tushar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Data // Lombok: getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime reminderDateTime;

    @Column(nullable = false, length = 500) // Increased message length
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderMethod reminderMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderStatus status;

    @CreationTimestamp // Automatically set on creation
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Automatically set on update
    private LocalDateTime updatedAt;
}