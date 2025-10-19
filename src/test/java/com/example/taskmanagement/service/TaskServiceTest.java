package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.event.TaskCompletedEvent;
import com.example.taskmanagement.event.TaskCreatedEvent;
import com.example.taskmanagement.event.TaskUpdatedEvent;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.util.TaskConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TaskConverter taskConverter;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        task = new Task("Test Task", Priority.HIGH, Status.TODO);
        task.setId(1L);
        task.setDescription("Test Description");
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setNotes("Test Notes");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskRequest = new TaskRequest();
        taskRequest.setTitle("Test Task");
        taskRequest.setDescription("Test Description");
        taskRequest.setPriority(Priority.HIGH);
        taskRequest.setStatus(Status.TODO);
        taskRequest.setDueDate(LocalDate.now().plusDays(1));
        taskRequest.setNotes("Test Notes");
    }

    @Test
    void testCreateTask() {
        // Given
        when(taskConverter.convertToEntity(taskRequest)).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskConverter.convertToResponse(task)).thenReturn(createTaskResponse());

        // When
        TaskResponse result = taskService.createTask(taskRequest);

        // Then
        assertNotNull(result);
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getDescription(), result.getDescription());
        assertEquals(task.getPriority(), result.getPriority());
        assertEquals(task.getStatus(), result.getStatus());
        assertEquals(task.getDueDate(), result.getDueDate());
        assertEquals(task.getNotes(), result.getNotes());

        verify(taskConverter).convertToEntity(taskRequest);
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisher).publishEvent(any(TaskCreatedEvent.class));
        verify(taskConverter).convertToResponse(task);
    }

    @Test
    void testGetTaskById() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskConverter.convertToResponse(task)).thenReturn(createTaskResponse());

        // When
        Optional<TaskResponse> result = taskService.getTaskById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(task.getTitle(), result.get().getTitle());
        verify(taskRepository).findById(1L);
        verify(taskConverter).convertToResponse(task);
    }

    @Test
    void testGetTaskByIdNotFound() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<TaskResponse> result = taskService.getTaskById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(taskRepository).findById(1L);
    }

    @Test
    void testUpdateTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskConverter.convertToResponse(task)).thenReturn(createTaskResponse());

        // When
        Optional<TaskResponse> result = taskService.updateTask(1L, taskRequest);

        // Then
        assertTrue(result.isPresent());
        assertEquals(task.getTitle(), result.get().getTitle());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisher).publishEvent(any(TaskUpdatedEvent.class));
        verify(taskConverter).convertToResponse(task);
    }

    @Test
    void testUpdateTaskNotFound() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<TaskResponse> result = taskService.updateTask(1L, taskRequest);

        // Then
        assertFalse(result.isPresent());
        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testDeleteTask() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = taskService.deleteTask(1L);

        // Then
        assertTrue(result);
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void testDeleteTaskNotFound() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(false);

        // When
        boolean result = taskService.deleteTask(1L);

        // Then
        assertFalse(result);
        verify(taskRepository).existsById(1L);
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void testCompleteTask() {
        // Given
        Task completedTask = createTask();
        completedTask.setStatus(Status.COMPLETED);
        completedTask.setCompletedAt(LocalDateTime.now());
        
        TaskResponse completedTaskResponse = createTaskResponse();
        completedTaskResponse.setStatus(Status.COMPLETED);
        completedTaskResponse.setCompletedAt(LocalDateTime.now());
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(completedTask);
        when(taskConverter.convertToResponse(completedTask)).thenReturn(completedTaskResponse);

        // When
        Optional<TaskResponse> result = taskService.completeTask(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(Status.COMPLETED, result.get().getStatus());
        assertNotNull(result.get().getCompletedAt());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisher).publishEvent(any(TaskCompletedEvent.class));
        verify(taskConverter).convertToResponse(completedTask);
    }

    @Test
    void testCompleteTaskNotFound() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<TaskResponse> result = taskService.completeTask(1L);

        // Then
        assertFalse(result.isPresent());
        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testGetAllTasks() {
        // Given
        List<Task> tasks = Arrays.asList(task);
        Page<Task> taskPage = new PageImpl<>(tasks);
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(taskPage);
        when(taskConverter.convertToResponsePage(taskPage)).thenReturn(new PageImpl<>(Arrays.asList(createTaskResponse())));

        // When
        Page<TaskResponse> result = taskService.getAllTasks(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(task.getTitle(), result.getContent().get(0).getTitle());
        verify(taskRepository).findAll(any(Pageable.class));
        verify(taskConverter).convertToResponsePage(taskPage);
    }

    @Test
    void testGetTaskCountByStatus() {
        // Given
        when(taskRepository.countByStatus(Status.TODO)).thenReturn(5L);

        // When
        long result = taskService.getTaskCountByStatus(Status.TODO);

        // Then
        assertEquals(5L, result);
        verify(taskRepository).countByStatus(Status.TODO);
    }

    @Test
    void testGetTaskCountByPriority() {
        // Given
        when(taskRepository.countByPriority(Priority.HIGH)).thenReturn(3L);

        // When
        long result = taskService.getTaskCountByPriority(Priority.HIGH);

        // Then
        assertEquals(3L, result);
        verify(taskRepository).countByPriority(Priority.HIGH);
    }

    private Task createTask() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setPriority(Priority.HIGH);
        task.setStatus(Status.TODO);
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setNotes("Test Notes");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private TaskResponse createTaskResponse() {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setDueDate(task.getDueDate());
        response.setNotes(task.getNotes());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setOverdue(task.isOverdue());
        return response;
    }
}
