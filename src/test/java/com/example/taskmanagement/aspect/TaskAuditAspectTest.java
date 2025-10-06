package com.example.taskmanagement.aspect;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskAudit;
import com.example.taskmanagement.event.TaskCreatedEvent;
import com.example.taskmanagement.event.TaskUpdatedEvent;
import com.example.taskmanagement.repository.TaskAuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TaskAuditAspect}.
 */
@ExtendWith(MockitoExtension.class)
class TaskAuditAspectTest {

    @Mock
    private TaskAuditRepository taskAuditRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskAuditAspect taskAuditAspect;

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testAuditTaskCreation() throws Exception {
        // Given
        String taskJson = "{\"id\":1,\"title\":\"Test Task\"}";
        when(objectMapper.writeValueAsString(testTask)).thenReturn(taskJson);

        // When
        taskAuditAspect.auditTaskCreation(mock(JoinPoint.class), testTask);

        // Then
        verify(objectMapper).writeValueAsString(testTask);
        verify(taskAuditRepository).save(argThat(audit -> 
            audit.getTaskId().equals(1L) &&
            "CREATED".equals(audit.getAction()) &&
            "SYSTEM".equals(audit.getChangedBy())
        ));
    }

    @Test
    void testAuditTaskCreationWithNonTaskObject() throws Exception {
        // Given
        String nonTaskObject = "Not a task";

        // When
        taskAuditAspect.auditTaskCreation(null, nonTaskObject);

        // Then
        verify(objectMapper, never()).writeValueAsString(any());
        verify(taskAuditRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testAuditTaskCreationWithException() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(testTask)).thenThrow(new RuntimeException("JSON error"));

        // When
        taskAuditAspect.auditTaskCreation(null, testTask);

        // Then
        verify(objectMapper).writeValueAsString(testTask);
        verify(taskAuditRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testAuditTaskUpdate() throws Exception {
        // Given
        Optional<Task> taskOptional = Optional.of(testTask);
        String taskJson = "{\"id\":1,\"title\":\"Test Task\"}";
        when(objectMapper.writeValueAsString(testTask)).thenReturn(taskJson);

        // When
        taskAuditAspect.auditTaskUpdate(mock(JoinPoint.class), testTask);

        // Then
        verify(objectMapper).writeValueAsString(testTask);
        verify(taskAuditRepository).save(argThat(audit -> 
            audit.getTaskId().equals(1L) &&
            "UPDATED".equals(audit.getAction()) &&
            "SYSTEM".equals(audit.getChangedBy())
        ));
    }

    @Test
    void testAuditTaskUpdateWithEmptyOptional() throws Exception {
        // Given
        Optional<Task> emptyOptional = Optional.empty();

        // When
        taskAuditAspect.auditTaskUpdate(mock(JoinPoint.class), emptyOptional);

        // Then
        verify(objectMapper, never()).writeValueAsString(any());
        verify(taskAuditRepository, never()).save(any());
    }

    @Test
    void testAuditTaskUpdateWithException() throws Exception {
        // Given
        Optional<Task> taskOptional = Optional.of(testTask);
        when(objectMapper.writeValueAsString(testTask)).thenThrow(new RuntimeException("JSON error"));

        // When
        taskAuditAspect.auditTaskUpdate(mock(JoinPoint.class), testTask);

        // Then
        verify(objectMapper).writeValueAsString(testTask);
        verify(taskAuditRepository, never()).save(any());
    }

    @Test
    void testAuditTaskDeletion() throws Exception {
        // Given
        Long taskId = 1L;

        // When
        taskAuditAspect.auditTaskDeletion(null, taskId);

        // Then
        verify(taskAuditRepository).save(argThat(audit -> 
            audit.getTaskId().equals(taskId) &&
            "DELETED".equals(audit.getAction()) &&
            "SYSTEM".equals(audit.getChangedBy())
        ));
    }

    @Test
    void testAuditTaskDeletionWithNoExistingAudit() {
        // Given
        Long taskId = 1L;

        // When
        taskAuditAspect.auditTaskDeletion(null, taskId);

        // Then
        verify(taskAuditRepository).save(argThat(audit -> 
            audit.getTaskId().equals(taskId) &&
            "DELETED".equals(audit.getAction()) &&
            "SYSTEM".equals(audit.getChangedBy())
        ));
    }

    @Test
    void testAuditTaskDeletionWithException() throws Exception {
        // Given
        Long taskId = 1L;

        // When
        taskAuditAspect.auditTaskDeletion(null, taskId);

        // Then
        verify(taskAuditRepository).save(argThat(audit -> 
            audit.getTaskId().equals(taskId) &&
            "DELETED".equals(audit.getAction()) &&
            "SYSTEM".equals(audit.getChangedBy())
        ));
    }

    @Test
    void testAuditTaskDeletionWithNullTaskId() {
        // Given
        Long taskId = null;

        // When
        taskAuditAspect.auditTaskDeletion(null, taskId);

        // Then
        verify(taskAuditRepository).save(argThat(audit -> 
            audit.getTaskId() == null &&
            "DELETED".equals(audit.getAction()) &&
            "SYSTEM".equals(audit.getChangedBy())
        ));
    }
}
