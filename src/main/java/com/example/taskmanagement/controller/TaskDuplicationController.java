package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for task duplication operations.
 * Provides endpoints for duplicating tasks with smart defaults.
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskDuplicationController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TaskDuplicationController.class);

    @Autowired
    private TaskService taskService;

    /**
     * Duplicates a task with smart defaults.
     *
     * @param id the ID of the task to duplicate
     * @return the duplicated task response
     */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ApiResponse<TaskResponse>> duplicateTask(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Duplicating task: ID={}", id);
        }
        
        try {
            // Use the service method to duplicate the task
            TaskResponse duplicatedTask = taskService.duplicateTask(id);
            return handleCreated(duplicatedTask);
        } catch (TaskNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Task not found for duplication: ID={}", id);
            }
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Duplicates a task with custom modifications.
     *
     * @param id the ID of the task to duplicate
     * @param modifications the modifications to apply to the duplicated task
     * @return the duplicated task response
     */
    @PostMapping("/{id}/custom")
    public ResponseEntity<ApiResponse<TaskResponse>> duplicateTaskWithModifications(
            @PathVariable Long id,
            @RequestBody TaskModificationRequest modifications) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Duplicating task with modifications: ID={}", id);
        }
        
        Optional<TaskResponse> originalTask = taskService.getTaskById(id);
        
        if (!originalTask.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskResponse original = originalTask.get();
        
        // Create a new task request based on the original task with modifications
        com.example.taskmanagement.dto.TaskRequest duplicateRequest = new com.example.taskmanagement.dto.TaskRequest();
        duplicateRequest.setTitle(modifications.getTitle() != null ? modifications.getTitle() : original.getTitle() + " (Copy)");
        duplicateRequest.setDescription(modifications.getDescription() != null ? modifications.getDescription() : original.getDescription());
        duplicateRequest.setPriority(modifications.getPriority() != null ? modifications.getPriority() : original.getPriority());
        duplicateRequest.setStatus(com.example.taskmanagement.enums.Status.TODO); // Reset status to TODO
        duplicateRequest.setDueDate(modifications.getDueDate() != null ? modifications.getDueDate() : original.getDueDate());
        duplicateRequest.setNotes(modifications.getNotes() != null ? modifications.getNotes() : original.getNotes());
        
        TaskResponse duplicatedTask = taskService.createTask(duplicateRequest);
        
        return handleCreated(duplicatedTask);
    }

    /**
     * Request class for task modifications during duplication.
     */
    public static class TaskModificationRequest {
        private String title;
        private String description;
        private com.example.taskmanagement.enums.Priority priority;
        private java.time.LocalDate dueDate;
        private String notes;

        // Getters and setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public com.example.taskmanagement.enums.Priority getPriority() {
            return priority;
        }

        public void setPriority(com.example.taskmanagement.enums.Priority priority) {
            this.priority = priority;
        }

        public java.time.LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(java.time.LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
