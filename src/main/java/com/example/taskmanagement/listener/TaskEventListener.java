package com.example.taskmanagement.listener;

import com.example.taskmanagement.event.TaskCompletedEvent;
import com.example.taskmanagement.event.TaskCreatedEvent;
import com.example.taskmanagement.event.TaskUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for task-related events.
 * Handles audit trail and notifications.
 */
@Component
public class TaskEventListener {

    private static final Logger logger = LoggerFactory.getLogger(TaskEventListener.class);

    /**
     * Handles task creation events.
     * Logs the event for audit trail purposes.
     *
     * @param event the task created event
     */
    @EventListener
    public void handleTaskCreated(TaskCreatedEvent event) {
        if (logger.isInfoEnabled()) {
            logger.info("Task created: ID={}, Title='{}', Priority={}, Status={}", 
                    event.getTask().getId(),
                    event.getTask().getTitle(),
                    event.getTask().getPriority(),
                    event.getTask().getStatus());
        }
    }

    /**
     * Handles task update events.
     * Logs the event for audit trail purposes.
     *
     * @param event the task updated event
     */
    @EventListener
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        if (logger.isInfoEnabled()) {
            logger.info("Task updated: ID={}, Title='{}', Priority={}, Status={}", 
                    event.getTask().getId(),
                    event.getTask().getTitle(),
                    event.getTask().getPriority(),
                    event.getTask().getStatus());
        }
    }

    /**
     * Handles task completion events.
     * Logs the event for audit trail purposes.
     *
     * @param event the task completed event
     */
    @EventListener
    public void handleTaskCompleted(TaskCompletedEvent event) {
        if (logger.isInfoEnabled()) {
            logger.info("Task completed: ID={}, Title='{}', CompletedAt={}", 
                    event.getTask().getId(),
                    event.getTask().getTitle(),
                    event.getTask().getCompletedAt());
        }
    }
}
