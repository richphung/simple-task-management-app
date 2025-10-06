package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.dto.TaskSearchRequest;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Locale;
import java.util.Objects;

/**
 * REST Controller for task management operations.
 * Provides endpoints for CRUD operations, search, filtering, and advanced features.
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = Objects.requireNonNull(taskService, "TaskService cannot be null");
    }

    /**
     * Creates a new task.
     *
     * @param taskRequest the task creation request
     * @return the created task response
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating new task: {}", taskRequest.getTitle());
        }
        
        TaskResponse taskResponse = taskService.createTask(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponse);
    }

    /**
     * Retrieves a task by ID.
     *
     * @param id the task ID
     * @return the task response if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving task by ID: {}", id);
        }
        
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
    }

    /**
     * Retrieves all tasks with pagination and sorting.
     *
     * @param pageable pagination and sorting information
     * @return a page of task responses
     */
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving all tasks: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        }
        
        Page<TaskResponse> tasks = taskService.getAllTasks(pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Updates an existing task.
     *
     * @param id the task ID
     * @param taskRequest the task update request
     * @return the updated task response if found, 404 otherwise
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest taskRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating task: ID={}", id);
        }
        
        return taskService.updateTask(id, taskRequest)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
    }

    /**
     * Deletes a task by ID.
     *
     * @param id the task ID
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting task: ID={}", id);
        }
        
        if (taskService.deleteTask(id)) {
            return ResponseEntity.noContent().build();
        } else {
            throw new TaskNotFoundException("Task not found with ID: " + id);
        }
    }

    /**
     * Completes a task by setting its status to COMPLETED.
     *
     * @param id the task ID
     * @return the completed task response if found, 404 otherwise
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Completing task: ID={}", id);
        }
        
        return taskService.completeTask(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
    }

    /**
     * Searches for tasks based on various criteria.
     *
     * @param searchRequest the search criteria
     * @return a page of task responses matching the criteria
     */
    @PostMapping("/search")
    public ResponseEntity<Page<TaskResponse>> searchTasks(@Valid @RequestBody TaskSearchRequest searchRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Searching tasks with criteria: {}", searchRequest);
        }
        
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Quick search by title or description.
     *
     * @param q the search query
     * @param pageable pagination information
     * @return a page of task responses matching the query
     */
    @GetMapping("/search/quick")
    public ResponseEntity<Page<TaskResponse>> quickSearch(
            @RequestParam String q,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (logger.isDebugEnabled()) {
            logger.debug("Quick search for: {}", q);
        }
        
        TaskSearchRequest searchRequest = new TaskSearchRequest();
        searchRequest.setSearchTerm(q);
        searchRequest.setPage(pageable.getPageNumber());
        searchRequest.setSize(pageable.getPageSize());
        searchRequest.setSortBy(pageable.getSort().iterator().next().getProperty());
        searchRequest.setSortDirection(pageable.getSort().iterator().next().getDirection().name());
        
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Gets tasks by status.
     *
     * @param status the task status
     * @param pageable pagination information
     * @return a page of task responses with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TaskResponse>> getTasksByStatus(
            @PathVariable String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting tasks by status: {}", status);
        }
        
        TaskSearchRequest searchRequest = new TaskSearchRequest();
        searchRequest.setStatus(com.example.taskmanagement.enums.Status.valueOf(status.toUpperCase(Locale.ENGLISH)));
        searchRequest.setPage(pageable.getPageNumber());
        searchRequest.setSize(pageable.getPageSize());
        searchRequest.setSortBy(pageable.getSort().iterator().next().getProperty());
        searchRequest.setSortDirection(pageable.getSort().iterator().next().getDirection().name());
        
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Gets overdue tasks.
     *
     * @param pageable pagination information
     * @return a page of overdue task responses
     */
    @GetMapping("/overdue")
    public ResponseEntity<Page<TaskResponse>> getOverdueTasks(
            @PageableDefault(size = 10, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting overdue tasks");
        }
        
        // For simplicity, we'll use search with a specific criteria for overdue tasks
        // In a real implementation, you might want a dedicated service method
        TaskSearchRequest searchRequest = new TaskSearchRequest();
        searchRequest.setPage(pageable.getPageNumber());
        searchRequest.setSize(pageable.getPageSize());
        searchRequest.setSortBy(pageable.getSort().iterator().next().getProperty());
        searchRequest.setSortDirection(pageable.getSort().iterator().next().getDirection().name());
        
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return ResponseEntity.ok(tasks);
    }
}
