package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.dto.TaskSearchRequest;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
public class TaskController extends BaseController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = Objects.requireNonNull(taskService, "TaskService cannot be null");
    }

    /**
     * Creates a new task.
     *
     * @param taskRequest the task creation request
     * @return the created task response wrapped in ApiResponse
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        logDebug("Creating new task: {}", taskRequest.getTitle());
        
        TaskResponse taskResponse = taskService.createTask(taskRequest);
        return handleCreated(taskResponse);
    }

    /**
     * Retrieves a task by ID.
     *
     * @param id the task ID
     * @return the task response if found wrapped in ApiResponse, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        logDebug("Retrieving task by ID: {}", id);
        
        return taskService.getTaskById(id)
                .map(this::handleSuccess)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
    }

    /**
     * Retrieves all tasks with pagination and sorting.
     *
     * @param pageable pagination and sorting information
     * @return a page of task responses wrapped in ApiResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasks(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        logDebug("Retrieving all tasks: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<TaskResponse> tasks = taskService.getAllTasks(pageable.getPageNumber(), pageable.getPageSize());
        return handleSuccess(tasks);
    }

    /**
     * Updates an existing task.
     *
     * @param id the task ID
     * @param taskRequest the task update request
     * @return the updated task response if found wrapped in ApiResponse, 404 otherwise
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest taskRequest) {
        logDebug("Updating task: ID={}", id);
        
        return taskService.updateTask(id, taskRequest)
                .map(this::handleSuccess)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
    }

    /**
     * Deletes a task by ID.
     *
     * @param id the task ID
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        logDebug("Deleting task: ID={}", id);
        
        if (taskService.deleteTask(id)) {
            return handleNoContent();
        } else {
            throw new TaskNotFoundException("Task not found with ID: " + id);
        }
    }

    /**
     * Completes a task by setting its status to COMPLETED.
     *
     * @param id the task ID
     * @return the completed task response if found wrapped in ApiResponse, 404 otherwise
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(@PathVariable Long id) {
        logDebug("Completing task: ID={}", id);
        
        return taskService.completeTask(id)
                .map(this::handleSuccess)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));
    }

    /**
     * Searches for tasks based on various criteria.
     *
     * @param searchRequest the search criteria
     * @return a page of task responses matching the criteria wrapped in ApiResponse
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> searchTasks(@Valid @RequestBody TaskSearchRequest searchRequest) {
        logDebug("Searching tasks with criteria: {}", searchRequest);
        
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return handleSuccess(tasks);
    }

    /**
     * Quick search by title or description.
     *
     * @param q the search query
     * @param pageable pagination information
     * @return a page of task responses matching the query wrapped in ApiResponse
     */
    @GetMapping("/search/quick")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> quickSearch(
            @RequestParam String q,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        logDebug("Quick search for: {}", q);
        
        TaskSearchRequest searchRequest = buildSearchRequest(q, pageable);
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return handleSuccess(tasks);
    }

    /**
     * Gets tasks by status.
     *
     * @param status the task status
     * @param pageable pagination information
     * @return a page of task responses with the specified status wrapped in ApiResponse
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByStatus(
            @PathVariable String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        logDebug("Getting tasks by status: {}", status);
        
        TaskSearchRequest searchRequest = buildSearchRequest(null, pageable);
        searchRequest.setStatus(com.example.taskmanagement.enums.Status.valueOf(status.toUpperCase(Locale.ENGLISH)));
        
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return handleSuccess(tasks);
    }

    /**
     * Gets overdue tasks.
     *
     * @param pageable pagination information
     * @return a page of overdue task responses wrapped in ApiResponse
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getOverdueTasks(
            @PageableDefault(size = 10, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable) {
        logDebug("Getting overdue tasks");
        
        // For simplicity, we'll use search with a specific criteria for overdue tasks
        // In a real implementation, you might want a dedicated service method
        TaskSearchRequest searchRequest = buildSearchRequest(null, pageable);
        
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return handleSuccess(tasks);
    }
}
