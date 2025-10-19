package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.dto.TaskSearchRequest;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.event.TaskCompletedEvent;
import com.example.taskmanagement.event.TaskCreatedEvent;
import com.example.taskmanagement.event.TaskUpdatedEvent;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.util.TaskConverter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for task management operations.
 * Contains business logic, validation, and caching.
 */
@Service
@Transactional
public class TaskService extends BaseService {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TaskConverter taskConverter;

    @Autowired
    public TaskService(TaskRepository taskRepository, ApplicationEventPublisher eventPublisher, TaskConverter taskConverter) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
        this.taskConverter = taskConverter;
    }

    /**
     * Creates a new task.
     *
     * @param taskRequest the task creation request
     * @return the created task response
     */
    public TaskResponse createTask(TaskRequest taskRequest) {
        logDebug("Creating new task: {}", taskRequest.getTitle());

        Task task = taskConverter.convertToEntity(taskRequest);
        Task savedTask = taskRepository.save(task);
        eventPublisher.publishEvent(new TaskCreatedEvent(savedTask));

        logInfo("Task created successfully: ID={}, Title='{}'", savedTask.getId(), savedTask.getTitle());
        return taskConverter.convertToResponse(savedTask);
    }

    /**
     * Retrieves a task by ID.
     *
     * @param id the task ID
     * @return the task response if found
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#id")
    public Optional<TaskResponse> getTaskById(Long id) {
        logDebug("Retrieving task by ID: {}", id);
        return taskRepository.findById(id)
                .map(taskConverter::convertToResponse);
    }

    /**
     * Updates an existing task.
     *
     * @param id the task ID
     * @param taskRequest the task update request
     * @return the updated task response if found
     */
    @CacheEvict(value = "tasks", key = "#id")
    public Optional<TaskResponse> updateTask(Long id, TaskRequest taskRequest) {
        logDebug("Updating task: ID={}", id);

        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setTitle(taskRequest.getTitle());
                    existingTask.setDescription(taskRequest.getDescription());
                    existingTask.setPriority(taskRequest.getPriority());
                    existingTask.setStatus(taskRequest.getStatus());
                    existingTask.setDueDate(taskRequest.getDueDate());
                    existingTask.setNotes(taskRequest.getNotes());
                    // Manually set updatedAt to ensure it's updated
                    existingTask.setUpdatedAt(LocalDateTime.now());

                    Task savedTask = taskRepository.save(existingTask);
                    eventPublisher.publishEvent(new TaskUpdatedEvent(savedTask));

                    logInfo("Task updated successfully: ID={}, Title='{}'", savedTask.getId(), savedTask.getTitle());
                    return taskConverter.convertToResponse(savedTask);
                });
    }

    /**
     * Deletes a task by ID.
     *
     * @param id the task ID
     * @return true if the task was deleted, false if not found
     */
    @CacheEvict(value = "tasks", key = "#id")
    public boolean deleteTask(Long id) {
        logDebug("Deleting task: ID={}", id);

        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            logInfo("Task deleted successfully: ID={}", id);
            return true;
        }
        return false;
    }

    /**
     * Update task status.
     *
     * @param id the task ID
     * @param status the new status
     * @return true if updated successfully
     */
    @CacheEvict(value = "tasks", key = "#id")
    public boolean updateTaskStatus(Long id, Status status) {
        logDebug("Updating task status: ID={}, Status={}", id, status);

        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus(status);
            taskRepository.save(task);
            logInfo("Task status updated successfully: ID={}, Status={}", id, status);
            return true;
        }
        return false;
    }

    /**
     * Searches tasks with pagination and filtering.
     * Uses optimized repository methods for better performance.
     *
     * @param searchRequest the search criteria
     * @return a page of task responses
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> searchTasks(TaskSearchRequest searchRequest) {
        logDebug("Searching tasks with criteria: {}", searchRequest);

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(searchRequest.getSortDirection()) 
                    ? Sort.Direction.DESC 
                    : Sort.Direction.ASC,
                searchRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        Page<Task> tasks;
        
        // Use optimized advanced search for complex criteria
        if (hasMultipleCriteria(searchRequest)) {
            List<Status> statuses = searchRequest.getStatus() != null ? 
                Arrays.asList(searchRequest.getStatus()) : null;
            List<Priority> priorities = searchRequest.getPriority() != null ? 
                Arrays.asList(searchRequest.getPriority()) : null;
            
            tasks = taskRepository.findTasksWithAdvancedSearch(
                searchRequest.getSearchTerm(),
                statuses,
                priorities,
                searchRequest.getDueDateFrom(),
                searchRequest.getDueDateTo(),
                pageable
            );
        } else if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            tasks = taskRepository.searchTasks(searchRequest.getSearchTerm(), pageable);
        } else if (searchRequest.getStatus() != null) {
            tasks = taskRepository.findByStatus(searchRequest.getStatus(), pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }

        return taskConverter.convertToResponsePage(tasks);
    }

    /**
     * Checks if the search request has multiple criteria that would benefit from advanced search.
     */
    private boolean hasMultipleCriteria(TaskSearchRequest searchRequest) {
        int criteriaCount = 0;
        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            criteriaCount++;
        }
        if (searchRequest.getStatus() != null) {
            criteriaCount++;
        }
        if (searchRequest.getPriority() != null) {
            criteriaCount++;
        }
        if (searchRequest.getDueDateFrom() != null) {
            criteriaCount++;
        }
        if (searchRequest.getDueDateTo() != null) {
            criteriaCount++;
        }
        return criteriaCount >= 2;
    }

    /**
     * Retrieves all tasks with pagination.
     *
     * @param page the page number
     * @param size the page size
     * @return a page of task responses
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(int page, int size) {
        logDebug("Retrieving all tasks: page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> tasks = taskRepository.findAll(pageable);
        
        return taskConverter.convertToResponsePage(tasks);
    }

    /**
     * Retrieves overdue tasks.
     *
     * @return a list of overdue task responses
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks() {
        logDebug("Retrieving overdue tasks");
        
        List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDate.now());
        return taskConverter.convertToResponseList(overdueTasks);
    }

    /**
     * Completes a task by setting its status to COMPLETED.
     *
     * @param id the task ID
     * @return the updated task response if found
     */
    @CacheEvict(value = "tasks", key = "#id")
    public Optional<TaskResponse> completeTask(Long id) {
        logDebug("Completing task: ID={}", id);

        return taskRepository.findById(id)
                .map(task -> {
                    task.setStatus(Status.COMPLETED);
                    task.setCompletedAt(LocalDateTime.now());
                    
                    Task savedTask = taskRepository.save(task);
                    eventPublisher.publishEvent(new TaskCompletedEvent(savedTask));

                    logInfo("Task completed successfully: ID={}, Title='{}'", savedTask.getId(), savedTask.getTitle());
                    return taskConverter.convertToResponse(savedTask);
                });
    }

    /**
     * Gets task statistics by status.
     *
     * @return a map of status counts
     */
    @Transactional(readOnly = true)
    public long getTaskCountByStatus(Status status) {
        logDebug("Getting task count by status: {}", status);
        return taskRepository.countByStatus(status);
    }

    /**
     * Gets task statistics by priority.
     *
     * @return a map of priority counts
     */
    @Transactional(readOnly = true)
    public long getTaskCountByPriority(Priority priority) {
        logDebug("Getting task count by priority: {}", priority);
        return taskRepository.countByPriority(priority);
    }


    /**
     * Bulk create tasks.
     *
     * @param taskRequests list of task requests
     * @return list of created task responses
     */
    public List<TaskResponse> bulkCreateTasks(List<TaskRequest> taskRequests) {
        logDebug("Bulk creating {} tasks", taskRequests.size());
        
        List<Task> tasks = taskRequests.stream()
            .map(taskConverter::convertToEntity)
            .collect(Collectors.toList());
        
        List<Task> savedTasks = taskRepository.saveAll(tasks);
        
        // Publish events for each created task
        savedTasks.forEach(task -> {
            eventPublisher.publishEvent(new TaskCreatedEvent(task));
        });
        
        return taskConverter.convertToResponseList(savedTasks);
    }

    /**
     * Bulk update task status using optimized repository method.
     *
     * @param taskIds list of task IDs
     * @param status new status
     * @return number of updated tasks
     */
    @CacheEvict(value = "tasks", allEntries = true)
    public int bulkUpdateTaskStatus(List<Long> taskIds, Status status) {
        logDebug("Bulk updating {} tasks to status: {}", taskIds.size(), status);
        
        if (taskIds.isEmpty()) {
            return 0;
        }
        
        int updatedCount = taskRepository.bulkUpdateTaskStatus(taskIds, status, LocalDateTime.now());
        
        logInfo("Bulk update completed: {} tasks updated to status {}", updatedCount, status);
        return updatedCount;
    }

    /**
     * Bulk delete tasks using optimized repository method.
     *
     * @param taskIds list of task IDs
     * @return number of deleted tasks
     */
    @CacheEvict(value = "tasks", allEntries = true)
    public int bulkDeleteTasks(List<Long> taskIds) {
        logDebug("Bulk deleting {} tasks", taskIds.size());
        
        if (taskIds.isEmpty()) {
            return 0;
        }
        
        int deletedCount = taskRepository.bulkDeleteTasks(taskIds);
        
        logInfo("Bulk delete completed: {} tasks deleted", deletedCount);
        return deletedCount;
    }

    /**
     * Bulk complete tasks.
     *
     * @param taskIds list of task IDs
     * @return list of completed task responses
     */
    public List<TaskResponse> bulkCompleteTasks(List<Long> taskIds) {
        logDebug("Bulk completing {} tasks", taskIds.size());
        
        List<TaskResponse> completedTasks = new ArrayList<>();
        for (Long taskId : taskIds) {
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setStatus(Status.COMPLETED);
                task.setCompletedAt(LocalDateTime.now());
                Task savedTask = taskRepository.save(task);
                eventPublisher.publishEvent(new TaskCompletedEvent(savedTask));
                completedTasks.add(taskConverter.convertToResponse(savedTask));
            }
        }
        
        return completedTasks;
    }

    /**
     * Duplicate a task.
     *
     * @param taskId the ID of the task to duplicate
     * @return the duplicated task response
     */
    public TaskResponse duplicateTask(Long taskId) {
        logDebug("Duplicating task with ID: {}", taskId);
        
        Task originalTask = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        
        Task duplicatedTask = new Task();
        duplicatedTask.setTitle(originalTask.getTitle() + " (Copy)");
        duplicatedTask.setDescription(originalTask.getDescription());
        duplicatedTask.setPriority(originalTask.getPriority());
        duplicatedTask.setStatus(Status.TODO); // Reset status to TODO
        duplicatedTask.setDueDate(originalTask.getDueDate());
        duplicatedTask.setNotes(originalTask.getNotes());
        
        Task savedTask = taskRepository.save(duplicatedTask);
        eventPublisher.publishEvent(new TaskCreatedEvent(savedTask));
        
        return taskConverter.convertToResponse(savedTask);
    }






}
