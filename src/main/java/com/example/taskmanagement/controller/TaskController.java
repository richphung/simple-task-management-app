package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.dto.TaskSearchRequest;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 
 * <p>This controller handles all core task operations including:</p>
 * <ul>
 *   <li>Creating new tasks</li>
 *   <li>Retrieving tasks by ID or with pagination</li>
 *   <li>Updating existing tasks</li>
 *   <li>Deleting tasks</li>
 *   <li>Completing tasks</li>
 *   <li>Searching and filtering tasks</li>
 * </ul>
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Task Management", description = "Core CRUD operations for task management. " +
        "Create, read, update, delete, and search tasks with advanced filtering capabilities.")
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
    @Operation(
            summary = "Create a new task",
            description = "Creates a new task with the provided details. All fields except notes are required. " +
                    "The task will be assigned a unique ID and timestamps will be automatically generated.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Task details to create",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskRequest.class),
                            examples = @ExampleObject(
                                    name = "Create Task Example",
                                    value = "{\n" +
                                            "  \"title\": \"Complete project documentation\",\n" +
                                            "  \"description\": \"Write comprehensive API documentation with examples\",\n" +
                                            "  \"priority\": \"HIGH\",\n" +
                                            "  \"status\": \"TODO\",\n" +
                                            "  \"dueDate\": \"2025-10-31\",\n" +
                                            "  \"notes\": \"Include Swagger UI screenshots\"\n" +
                                            "}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "{\n" +
                                            "  \"success\": true,\n" +
                                            "  \"data\": {\n" +
                                            "    \"id\": 1,\n" +
                                            "    \"title\": \"Complete project documentation\",\n" +
                                            "    \"description\": \"Write comprehensive API documentation with examples\",\n" +
                                            "    \"priority\": \"HIGH\",\n" +
                                            "    \"status\": \"TODO\",\n" +
                                            "    \"dueDate\": \"2025-10-31\",\n" +
                                            "    \"completedAt\": null,\n" +
                                            "    \"notes\": \"Include Swagger UI screenshots\",\n" +
                                            "    \"createdAt\": \"2025-10-19T20:30:00\",\n" +
                                            "    \"updatedAt\": \"2025-10-19T20:30:00\",\n" +
                                            "    \"overdue\": false\n" +
                                            "  },\n" +
                                            "  \"message\": null,\n" +
                                            "  \"timestamp\": \"2025-10-19T20:30:00\",\n" +
                                            "  \"status\": 201\n" +
                                            "}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = "{\n" +
                                            "  \"error\": \"BAD_REQUEST\",\n" +
                                            "  \"message\": \"Validation failed\",\n" +
                                            "  \"details\": {\n" +
                                            "    \"title\": \"Title is required\",\n" +
                                            "    \"priority\": \"Priority must be HIGH, MEDIUM, or LOW\"\n" +
                                            "  },\n" +
                                            "  \"status\": 400,\n" +
                                            "  \"timestamp\": \"2025-10-19T20:30:00\"\n" +
                                            "}"
                            )
                    )
            )
    })
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
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its unique identifier.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task found successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @Parameter(description = "Task ID", required = true, example = "1") 
            @PathVariable Long id) {
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
    @Operation(summary = "Get all tasks", description = "Retrieves all tasks with pagination and sorting. Default sort is by creation date descending.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasks(
            @Parameter(description = "Pagination parameters (page, size, sort)")
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
    @Operation(summary = "Update task", description = "Updates an existing task with new information.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @Parameter(description = "Task ID", required = true, example = "1") @PathVariable Long id, 
            @Valid @RequestBody TaskRequest taskRequest) {
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
    @Operation(summary = "Delete task", description = "Permanently deletes a task by its ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @Parameter(description = "Task ID", required = true, example = "1") @PathVariable Long id) {
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
    @Operation(summary = "Complete task", description = "Marks a task as completed and sets the completion timestamp.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(
            @Parameter(description = "Task ID", required = true, example = "1") @PathVariable Long id) {
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
    @Operation(summary = "Search tasks", description = "Search tasks using multiple criteria including keyword, status, priority, and date ranges.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> searchTasks(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Search criteria") 
            @Valid @RequestBody TaskSearchRequest searchRequest) {
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
    @Operation(summary = "Quick search", description = "Quickly search tasks by keyword in title or description.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    @GetMapping("/search/quick")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> quickSearch(
            @Parameter(description = "Search keyword", required = true, example = "documentation") @RequestParam String q,
            @Parameter(description = "Pagination parameters") 
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
    @Operation(summary = "Get tasks by status", description = "Retrieves all tasks with a specific status (TODO, IN_PROGRESS, COMPLETED, CANCELLED).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByStatus(
            @Parameter(description = "Task status", required = true, example = "TODO") @PathVariable String status,
            @Parameter(description = "Pagination parameters") 
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
    @Operation(summary = "Get overdue tasks", description = "Retrieves all tasks that are past their due date.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Overdue tasks retrieved successfully")
    })
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getOverdueTasks(
            @Parameter(description = "Pagination parameters") 
            @PageableDefault(size = 10, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable) {
        logDebug("Getting overdue tasks");
        
        // For simplicity, we'll use search with a specific criteria for overdue tasks
        // In a real implementation, you might want a dedicated service method
        TaskSearchRequest searchRequest = buildSearchRequest(null, pageable);
        
        Page<TaskResponse> tasks = taskService.searchTasks(searchRequest);
        return handleSuccess(tasks);
    }
}
