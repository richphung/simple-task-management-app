package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * REST Controller for bulk operations on tasks.
 * Provides endpoints for batch operations, bulk updates, and mass operations.
 */
@RestController
@RequestMapping("/api/tasks/bulk")
@CrossOrigin(origins = "*")
public class BulkOperationsController {

    private static final Logger logger = LoggerFactory.getLogger(BulkOperationsController.class);

    private final TaskService taskService;

    public BulkOperationsController(TaskService taskService) {
        this.taskService = Objects.requireNonNull(taskService, "TaskService cannot be null");
    }

    /**
     * Creates multiple tasks in a single request.
     *
     * @param taskRequests the list of task creation requests
     * @return a list of created task responses
     */
    @PostMapping("/create")
    public ResponseEntity<List<TaskResponse>> bulkCreateTasks(@Valid @RequestBody List<TaskRequest> taskRequests) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating {} tasks in bulk", taskRequests.size());
        }
        
        List<TaskResponse> responses = taskService.bulkCreateTasks(taskRequests);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Updates multiple tasks' status in bulk.
     *
     * @param request the bulk status update request
     * @return a response indicating the number of updated tasks
     */
    @PutMapping("/status")
    public ResponseEntity<String> bulkUpdateTaskStatus(@Valid @RequestBody BulkStatusUpdateRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating status for {} tasks to {}", request.getTaskIds().size(), request.getStatus());
        }
        
        int updatedCount = taskService.bulkUpdateTaskStatus(request.getTaskIds(), request.getStatus());
        
        return ResponseEntity.ok("Updated status for " + updatedCount + " tasks to " + request.getStatus().getDisplayName());
    }

    /**
     * Bulk update task status (overloaded method for testing).
     *
     * @param taskIds list of task IDs to update
     * @param status the new status
     * @return response with update result
     */
    public ResponseEntity<String> bulkUpdateStatus(List<Long> taskIds, Status status) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating status for {} tasks to {}", taskIds.size(), status);
        }
        
        int updatedCount = taskService.bulkUpdateTaskStatus(taskIds, status);
        
        return ResponseEntity.ok("Updated status for " + updatedCount + " tasks to " + status.getDisplayName());
    }

    /**
     * Deletes multiple tasks in bulk.
     *
     * @param taskIds the list of task IDs to delete
     * @return a response indicating the number of deleted tasks
     */
    @PostMapping("/delete")
    public ResponseEntity<Void> bulkDeleteTasks(@RequestBody List<Long> taskIds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting {} tasks in bulk", taskIds.size());
        }
        
        taskService.bulkDeleteTasks(taskIds);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Completes multiple tasks in bulk.
     *
     * @param taskIds the list of task IDs to complete
     * @return a response indicating the number of completed tasks
     */
    @PutMapping("/complete")
    public ResponseEntity<List<TaskResponse>> bulkCompleteTasks(@RequestBody List<Long> taskIds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Completing {} tasks in bulk", taskIds.size());
        }
        
        List<TaskResponse> completedTasks = taskService.bulkCompleteTasks(taskIds);
        
        return ResponseEntity.ok(completedTasks);
    }

    /**
     * Request class for bulk status updates.
     */
    public static class BulkStatusUpdateRequest {
        @NotNull(message = "Task IDs cannot be null")
        @NotEmpty(message = "Task IDs cannot be empty")
        private List<Long> taskIds;

        @NotNull(message = "Status cannot be null")
        private Status status;

        private com.example.taskmanagement.enums.Priority priority;

        // Getters and setters
        public List<Long> getTaskIds() {
            return taskIds != null ? new ArrayList<>(taskIds) : null;
        }

        public void setTaskIds(List<Long> taskIds) {
            this.taskIds = taskIds != null ? new ArrayList<>(taskIds) : null;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public com.example.taskmanagement.enums.Priority getPriority() {
            return priority;
        }

        public void setPriority(com.example.taskmanagement.enums.Priority priority) {
            this.priority = priority;
        }
    }
}
