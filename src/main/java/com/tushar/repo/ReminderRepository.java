package com.tushar.repo;

import com.tushar.model.Reminder;
import com.tushar.model.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // Find reminders that are PENDING and their reminderDateTime is due (less than or equal to the current time)
    List<Reminder> findByStatusAndReminderDateTimeLessThanEqual(ReminderStatus status, LocalDateTime dateTime);

    // Find all reminders by status, ordered by their due date
    List<Reminder> findByStatusOrderByReminderDateTimeAsc(ReminderStatus status);
}