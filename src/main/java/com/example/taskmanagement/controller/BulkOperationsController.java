package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * REST Controller for bulk operations on tasks.
 * Provides endpoints for batch operations, bulk updates, and mass operations.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Bulk Operations", description = "Efficient batch operations for managing multiple tasks simultaneously. " +
        "Create, update, complete, or delete multiple tasks in a single request for improved performance.")
@RestController
@RequestMapping("/api/tasks/bulk")
@CrossOrigin(origins = "*")
@Validated
public class BulkOperationsController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BulkOperationsController.class);

    private final TaskService taskService;

    public BulkOperationsController(TaskService taskService) {
        this.taskService = Objects.requireNonNull(taskService, "TaskService cannot be null");
    }

    /**
     * Creates multiple tasks in a single request.
     *
     * @param taskRequests the list of task creation requests
     * @return a list of created task responses wrapped in ApiResponse
     */
    @Operation(summary = "Bulk create tasks", description = "Creates multiple tasks in a single batch operation for improved performance.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tasks created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> bulkCreateTasks(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of task creation requests") 
            @RequestBody @NotNull List<@Valid TaskRequest> taskRequests) {
        if (logger.isDebugEnabled()) {
            int size = (taskRequests != null) ? taskRequests.size() : 0;
            logger.debug("Creating {} tasks in bulk", size);
        }
        
        // Handle null input gracefully
        if (taskRequests == null) {
            return handleCreated(new ArrayList<>());
        }
        
        List<TaskResponse> responses = taskService.bulkCreateTasks(taskRequests);
        
        return handleCreated(responses);
    }

    /**
     * Updates multiple tasks' status in bulk.
     *
     * @param request the bulk status update request
     * @return a response indicating the number of updated tasks wrapped in ApiResponse
     */
    @Operation(summary = "Bulk update task status", description = "Updates the status of multiple tasks in a single operation.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task statuses updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/status")
    public ResponseEntity<ApiResponse<String>> bulkUpdateTaskStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Bulk status update request") 
            @Valid @RequestBody BulkStatusUpdateRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating status for {} tasks to {}", request.getTaskIds().size(), request.getStatus());
        }
        
        int updatedCount = taskService.bulkUpdateTaskStatus(request.getTaskIds(), request.getStatus());
        String message = "Updated status for " + updatedCount + " tasks to " + request.getStatus().getDisplayName();
        
        return handleSuccess(message);
    }

    /**
     * Bulk update task status (overloaded method for testing).
     *
     * @param taskIds list of task IDs to update
     * @param status the new status
     * @return response with update result wrapped in ApiResponse
     */
    public ResponseEntity<ApiResponse<String>> bulkUpdateStatus(List<Long> taskIds, Status status) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating status for {} tasks to {}", taskIds.size(), status);
        }
        
        int updatedCount = taskService.bulkUpdateTaskStatus(taskIds, status);
        String message = "Updated status for " + updatedCount + " tasks to " + status.getDisplayName();
        
        return handleSuccess(message);
    }

    /**
     * Deletes multiple tasks in bulk.
     *
     * @param taskIds the list of task IDs to delete
     * @return a response indicating the number of deleted tasks wrapped in ApiResponse
     */
    @Operation(summary = "Bulk delete tasks", description = "Permanently deletes multiple tasks in a single operation.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Tasks deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteTasks(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of task IDs to delete") 
            @RequestBody List<Long> taskIds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting {} tasks in bulk", taskIds != null ? taskIds.size() : 0);
        }
        
        // Handle null input gracefully
        if (taskIds == null) {
            return handleNoContent();
        }
        
        taskService.bulkDeleteTasks(taskIds);
        
        return handleNoContent();
    }

    /**
     * Completes multiple tasks in bulk.
     *
     * @param taskIds the list of task IDs to complete
     * @return a response indicating the number of completed tasks wrapped in ApiResponse
     */
    @Operation(summary = "Bulk complete tasks", description = "Marks multiple tasks as completed in a single operation.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tasks completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/complete")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> bulkCompleteTasks(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of task IDs to complete") 
            @RequestBody List<Long> taskIds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Completing {} tasks in bulk", taskIds.size());
        }
        
        List<TaskResponse> completedTasks = taskService.bulkCompleteTasks(taskIds);
        
        return handleSuccess(completedTasks);
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
