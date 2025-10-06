package com.example.taskmanagement.event;

import com.example.taskmanagement.entity.Task;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.LocalDateTime;

/**
 * Event published when a task is completed.
 * Used for audit trail and notifications.
 */
public class TaskCompletedEvent {

    private final Task task;
    private final LocalDateTime timestamp;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public TaskCompletedEvent(Task task) {
        this.task = task;
        this.timestamp = LocalDateTime.now();
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Task getTask() {
        return task;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TaskCompletedEvent{" +
                "taskId=" + task.getId() +
                ", taskTitle='" + task.getTitle() + '\'' +
                ", completedAt=" + task.getCompletedAt() +
                ", timestamp=" + timestamp +
                '}';
    }
}
