package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.exception.TaskNotFoundException;
import com.example.taskmanagement.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link TaskDuplicationController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskDuplicationControllerTest {

    @MockBean
    private TaskService taskService;

    @Autowired
    private MockMvc mockMvc;

    private TaskResponse originalTask;
    private TaskResponse duplicatedTask;

    @BeforeEach
    void setUp() {
        originalTask = new TaskResponse();
        originalTask.setId(1L);
        originalTask.setTitle("Original Task");
        originalTask.setDescription("This is the original task");
        originalTask.setStatus(Status.TODO);
        originalTask.setPriority(Priority.HIGH);
        originalTask.setDueDate(LocalDate.now().plusDays(1));
        originalTask.setNotes("Original notes");
        originalTask.setCreatedAt(LocalDateTime.now().minusDays(1));
        originalTask.setUpdatedAt(LocalDateTime.now().minusHours(1));

        duplicatedTask = new TaskResponse();
        duplicatedTask.setId(2L);
        duplicatedTask.setTitle("Original Task (Copy)");
        duplicatedTask.setDescription("This is the original task");
        duplicatedTask.setStatus(Status.TODO);
        duplicatedTask.setPriority(Priority.HIGH);
        duplicatedTask.setDueDate(LocalDate.now().plusDays(1));
        duplicatedTask.setNotes("Original notes");
        duplicatedTask.setCreatedAt(LocalDateTime.now());
        duplicatedTask.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testDuplicateTaskWithValidId() throws Exception {
        // Given
        Long taskId = 1L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(originalTask));
        when(taskService.duplicateTask(taskId)).thenReturn(duplicatedTask);

        // When & Then
        mockMvc.perform(post("/api/tasks/{id}/duplicate", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(duplicatedTask.getId()))
                .andExpect(jsonPath("$.data.title").value("Original Task (Copy)"))
                .andExpect(jsonPath("$.data.status").value("TODO"));
        
        verify(taskService).duplicateTask(taskId);
    }

        @Test
        void testDuplicateTaskWithNullId() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/tasks/null/duplicate")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }

    @Test
    void testDuplicateTaskWithNonExistentId() throws Exception {
        // Given
        Long taskId = 999L;
        when(taskService.duplicateTask(taskId)).thenThrow(new TaskNotFoundException("Task not found with id: " + taskId));

        // When & Then
        mockMvc.perform(post("/api/tasks/{id}/duplicate", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        
        verify(taskService).duplicateTask(taskId);
    }

    @Test
    void testDuplicateTaskWithZeroId() throws Exception {
        // Given
        Long taskId = 0L;
        when(taskService.duplicateTask(taskId)).thenThrow(new TaskNotFoundException("Task not found with id: " + taskId));

        // When & Then
        mockMvc.perform(post("/api/tasks/{id}/duplicate", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        
        verify(taskService).duplicateTask(taskId);
    }

    @Test
    void testDuplicateTaskWithNegativeId() throws Exception {
        // Given
        Long taskId = -1L;
        when(taskService.duplicateTask(taskId)).thenThrow(new TaskNotFoundException("Task not found with id: " + taskId));

        // When & Then
        mockMvc.perform(post("/api/tasks/{id}/duplicate", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        
        verify(taskService).duplicateTask(taskId);
    }

    @Test
    void testDuplicateTaskWithCompletedTask() throws Exception {
        // Given
        Long taskId = 1L;
        originalTask.setStatus(Status.COMPLETED);
        originalTask.setCompletedAt(LocalDateTime.now());
        
        duplicatedTask.setStatus(Status.TODO); // Duplicated task should reset to TODO
        duplicatedTask.setCompletedAt(null); // Completed date should be cleared
        
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(originalTask));
        when(taskService.duplicateTask(taskId)).thenReturn(duplicatedTask);

        // When & Then
        mockMvc.perform(post("/api/tasks/{id}/duplicate", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("TODO"))
                .andExpect(jsonPath("$.data.completedAt").doesNotExist());
        
        verify(taskService).duplicateTask(taskId);
    }

    @Test
    void testDuplicateTaskWithInProgressTask() throws Exception {
        // Given
        Long taskId = 1L;
        originalTask.setStatus(Status.IN_PROGRESS);
        
        duplicatedTask.setStatus(Status.TODO); // Duplicated task should reset to TODO
        
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(originalTask));
        when(taskService.duplicateTask(taskId)).thenReturn(duplicatedTask);

        // When & Then
        mockMvc.perform(post("/api/tasks/{id}/duplicate", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("TODO"));
        
        verify(taskService).duplicateTask(taskId);
    }

    @Test
    void testDuplicateTaskWithHighPriorityTask() throws Exception {
        // Given
        Long taskId = 1L;
        originalTask.setPriority(Priority.HIGH);
        duplicatedTask.setPriority(Priority.HIGH);
        
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(originalTask));
        when(taskService.duplicateTask(taskId)).thenReturn(duplicatedTask);

        // When & Then
        mockMvc.perform(post("/api/tasks/{id}/duplicate", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("HIGH"));
        
        verify(taskService).duplicateTask(taskId);
    }

    @Test
    void testDuplicateTaskWithTaskHavingNotes() throws Exception {
        // Given
        Long taskId = 1L;
        originalTask.setNotes("Important notes to preserve");
        duplicatedTask.setNotes("Important notes to preserve");
        
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(originalTask));
        when(taskService.duplicateTask(taskId)).thenReturn(duplicatedTask);

        // When & Then
        mockMvc.perform(post("/api/tasks/{id}/duplicate", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notes").value("Important notes to preserve"));
        
        verify(taskService).duplicateTask(taskId);
    }

    @Test
    void testDuplicateTaskWithTaskHavingDueDate() throws Exception {
        // Given
        Long taskId = 1L;
        LocalDate dueDate = LocalDate.now().plusDays(5);
        originalTask.setDueDate(dueDate);
        duplicatedTask.setDueDate(dueDate);
        
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(originalTask));
        when(taskService.duplicateTask(taskId)).thenReturn(duplicatedTask);

        // When & Then
        mockMvc.perform(post("/api/tasks/{id}/duplicate", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dueDate").value(dueDate.toString()));
        
        verify(taskService).duplicateTask(taskId);
    }
}
