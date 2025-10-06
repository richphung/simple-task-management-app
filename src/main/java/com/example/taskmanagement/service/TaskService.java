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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public TaskService(TaskRepository taskRepository, ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new task.
     *
     * @param taskRequest the task creation request
     * @return the created task response
     */
    public TaskResponse createTask(TaskRequest taskRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating new task: {}", taskRequest.getTitle());
        }

        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setPriority(taskRequest.getPriority());
        task.setStatus(taskRequest.getStatus());
        task.setDueDate(taskRequest.getDueDate());
        task.setNotes(taskRequest.getNotes());

        Task savedTask = taskRepository.save(task);
        eventPublisher.publishEvent(new TaskCreatedEvent(savedTask));

        if (logger.isInfoEnabled()) {
            logger.info("Task created successfully: ID={}, Title='{}'", savedTask.getId(), savedTask.getTitle());
        }
        return convertToResponse(savedTask);
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
        logger.debug("Retrieving task by ID: {}", id);
        return taskRepository.findById(id)
                .map(this::convertToResponse);
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
        logger.debug("Updating task: ID={}", id);

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

                    if (logger.isInfoEnabled()) {
                        logger.info("Task updated successfully: ID={}, Title='{}'", savedTask.getId(), savedTask.getTitle());
                    }
                    return convertToResponse(savedTask);
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
        logger.debug("Deleting task: ID={}", id);

        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            logger.info("Task deleted successfully: ID={}", id);
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
        logger.debug("Updating task status: ID={}, Status={}", id, status);

        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus(status);
            taskRepository.save(task);
            logger.info("Task status updated successfully: ID={}, Status={}", id, status);
            return true;
        }
        return false;
    }

    /**
     * Searches tasks with pagination and filtering.
     *
     * @param searchRequest the search criteria
     * @return a page of task responses
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> searchTasks(TaskSearchRequest searchRequest) {
        logger.debug("Searching tasks with criteria: {}", searchRequest);

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(searchRequest.getSortDirection()) 
                    ? Sort.Direction.DESC 
                    : Sort.Direction.ASC,
                searchRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        Page<Task> tasks;
        
        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            tasks = taskRepository.searchTasks(searchRequest.getSearchTerm(), pageable);
        } else if (searchRequest.getStatus() != null) {
            tasks = taskRepository.findByStatus(searchRequest.getStatus(), pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }

        return tasks.map(this::convertToResponse);
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
        logger.debug("Retrieving all tasks: page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> tasks = taskRepository.findAll(pageable);
        
        return tasks.map(this::convertToResponse);
    }

    /**
     * Retrieves overdue tasks.
     *
     * @return a list of overdue task responses
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks() {
        logger.debug("Retrieving overdue tasks");
        
        List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDate.now());
        return overdueTasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Completes a task by setting its status to COMPLETED.
     *
     * @param id the task ID
     * @return the updated task response if found
     */
    @CacheEvict(value = "tasks", key = "#id")
    public Optional<TaskResponse> completeTask(Long id) {
        logger.debug("Completing task: ID={}", id);

        return taskRepository.findById(id)
                .map(task -> {
                    task.setStatus(Status.COMPLETED);
                    task.setCompletedAt(LocalDateTime.now());
                    
                    Task savedTask = taskRepository.save(task);
                    eventPublisher.publishEvent(new TaskCompletedEvent(savedTask));

                    if (logger.isInfoEnabled()) {
                        logger.info("Task completed successfully: ID={}, Title='{}'", savedTask.getId(), savedTask.getTitle());
                    }
                    return convertToResponse(savedTask);
                });
    }

    /**
     * Gets task statistics by status.
     *
     * @return a map of status counts
     */
    @Transactional(readOnly = true)
    public long getTaskCountByStatus(Status status) {
        logger.debug("Getting task count by status: {}", status);
        return taskRepository.countByStatus(status);
    }

    /**
     * Gets task statistics by priority.
     *
     * @return a map of priority counts
     */
    @Transactional(readOnly = true)
    public long getTaskCountByPriority(Priority priority) {
        logger.debug("Getting task count by priority: {}", priority);
        return taskRepository.countByPriority(priority);
    }

    /**
     * Converts a Task entity to TaskResponse DTO.
     *
     * @param task the task entity
     * @return the task response DTO
     */
    private TaskResponse convertToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setDueDate(task.getDueDate());
        response.setCompletedAt(task.getCompletedAt());
        response.setNotes(task.getNotes());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setOverdue(task.isOverdue());
        
        return response;
    }

    /**
     * Bulk create tasks.
     *
     * @param taskRequests list of task requests
     * @return list of created task responses
     */
    public List<TaskResponse> bulkCreateTasks(List<TaskRequest> taskRequests) {
        if (logger.isDebugEnabled()) {
            logger.debug("Bulk creating {} tasks", taskRequests.size());
        }
        
        List<Task> tasks = taskRequests.stream()
            .map(this::convertToEntity)
            .collect(Collectors.toList());
        
        List<Task> savedTasks = taskRepository.saveAll(tasks);
        
        // Publish events for each created task
        savedTasks.forEach(task -> {
            eventPublisher.publishEvent(new TaskCreatedEvent(task));
        });
        
        return savedTasks.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Bulk update task status.
     *
     * @param taskIds list of task IDs
     * @param status new status
     * @return number of updated tasks
     */
    public int bulkUpdateTaskStatus(List<Long> taskIds, Status status) {
        if (logger.isDebugEnabled()) {
            logger.debug("Bulk updating {} tasks to status: {}", taskIds.size(), status);
        }
        
        int updatedCount = 0;
        for (Long taskId : taskIds) {
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setStatus(status);
                taskRepository.save(task);
                eventPublisher.publishEvent(new TaskUpdatedEvent(task));
                updatedCount++;
            }
        }
        
        return updatedCount;
    }

    /**
     * Bulk delete tasks.
     *
     * @param taskIds list of task IDs
     */
    public void bulkDeleteTasks(List<Long> taskIds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Bulk deleting {} tasks", taskIds.size());
        }
        
        for (Long taskId : taskIds) {
            taskRepository.deleteById(taskId);
        }
    }

    /**
     * Bulk complete tasks.
     *
     * @param taskIds list of task IDs
     * @return list of completed task responses
     */
    public List<TaskResponse> bulkCompleteTasks(List<Long> taskIds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Bulk completing {} tasks", taskIds.size());
        }
        
        List<TaskResponse> completedTasks = new ArrayList<>();
        for (Long taskId : taskIds) {
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setStatus(Status.COMPLETED);
                task.setCompletedAt(LocalDateTime.now());
                Task savedTask = taskRepository.save(task);
                eventPublisher.publishEvent(new TaskCompletedEvent(savedTask));
                completedTasks.add(convertToResponse(savedTask));
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
        if (logger.isDebugEnabled()) {
            logger.debug("Duplicating task with ID: {}", taskId);
        }
        
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
        
        return convertToResponse(savedTask);
    }






    /**
     * Converts TaskRequest to Task entity.
     *
     * @param request the task request
     * @return the task entity
     */
    private Task convertToEntity(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setNotes(request.getNotes());
        return task;
    }
}
