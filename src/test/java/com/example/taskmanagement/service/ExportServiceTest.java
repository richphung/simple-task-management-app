package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.util.DateUtil;
import com.example.taskmanagement.util.TaskConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExportService}.
 */
@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TaskConverter taskConverter;

    @Mock
    private DateUtil dateUtil;

    @InjectMocks
    private ExportService exportService;

    private Task testTask1;
    private Task testTask2;
    private List<Task> testTasks;

    @BeforeEach
    void setUp() {
        testTask1 = new Task();
        testTask1.setId(1L);
        testTask1.setTitle("Test Task 1");
        testTask1.setDescription("First test task");
        testTask1.setStatus(Status.TODO);
        testTask1.setPriority(Priority.HIGH);
        testTask1.setDueDate(LocalDate.now().plusDays(1));
        testTask1.setCreatedAt(LocalDateTime.now());
        testTask1.setUpdatedAt(LocalDateTime.now());
        testTask1.setNotes("Test notes");

        testTask2 = new Task();
        testTask2.setId(2L);
        testTask2.setTitle("Test Task 2");
        testTask2.setDescription("Second test task");
        testTask2.setStatus(Status.COMPLETED);
        testTask2.setPriority(Priority.MEDIUM);
        testTask2.setDueDate(LocalDate.now().minusDays(1));
        testTask2.setCreatedAt(LocalDateTime.now().minusDays(1));
        testTask2.setUpdatedAt(LocalDateTime.now());
        testTask2.setCompletedAt(LocalDateTime.now());
        testTask2.setNotes("Completed task notes");

        testTasks = Arrays.asList(testTask1, testTask2);
    }

    @Test
    void testExportTasksToCsv() {
        // Given
        when(taskRepository.findAll()).thenReturn(testTasks);
        when(dateUtil.formatDate(any())).thenReturn("2025-10-08");
        when(dateUtil.formatDateTime(any())).thenReturn("2025-10-08 10:00:00");

        // When
        byte[] result = exportService.exportTasksToCsv();

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        String csvContent = new String(result);
        assertTrue(csvContent.contains("ID,Title,Description,Status,Priority,Due Date,Created At,Updated At,Notes"));
        assertTrue(csvContent.contains("Test Task 1"));
        assertTrue(csvContent.contains("Test Task 2"));
        assertTrue(csvContent.contains("To Do"));
        assertTrue(csvContent.contains("Completed"));
        
        verify(taskRepository).findAll();
    }

    @Test
    void testExportTasksToCsvWithEmptyList() {
        // Given
        when(taskRepository.findAll()).thenReturn(Arrays.asList());

        // When
        byte[] result = exportService.exportTasksToCsv();

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        String csvContent = new String(result);
        assertTrue(csvContent.contains("ID,Title,Description,Status,Priority,Due Date,Created At,Updated At,Notes"));
        assertFalse(csvContent.contains("Test Task"));
        
        verify(taskRepository).findAll();
    }

    @Test
    void testExportTasksToJson() throws Exception {
        // Given
        TaskResponse response1 = createTaskResponse(testTask1);
        TaskResponse response2 = createTaskResponse(testTask2);
        List<TaskResponse> taskResponses = Arrays.asList(response1, response2);
        
        when(taskRepository.findAll()).thenReturn(testTasks);
        when(taskConverter.convertToResponseList(testTasks)).thenReturn(taskResponses);
        byte[] expectedJson = "{\"tasks\":[]}".getBytes();
        when(objectMapper.writeValueAsBytes(any(List.class))).thenReturn(expectedJson);

        // When
        byte[] result = exportService.exportTasksToJson();

        // Then
        assertNotNull(result);
        assertEquals(expectedJson, result);
        
        verify(taskRepository).findAll();
        verify(taskConverter).convertToResponseList(testTasks);
        verify(objectMapper).writeValueAsBytes(any(List.class));
    }

    @Test
    void testExportTasksToJsonWithException() throws Exception {
        // Given
        TaskResponse response1 = createTaskResponse(testTask1);
        TaskResponse response2 = createTaskResponse(testTask2);
        List<TaskResponse> taskResponses = Arrays.asList(response1, response2);
        
        when(taskRepository.findAll()).thenReturn(testTasks);
        when(taskConverter.convertToResponseList(testTasks)).thenReturn(taskResponses);
        when(objectMapper.writeValueAsBytes(any(List.class))).thenThrow(new RuntimeException("JSON error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> exportService.exportTasksToJson());
        
        verify(taskRepository).findAll();
        verify(taskConverter).convertToResponseList(testTasks);
        verify(objectMapper).writeValueAsBytes(any(List.class));
    }

    @Test
    void testExportAnalyticsToJson() throws Exception {
        // Given
        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.countByStatus(Status.COMPLETED)).thenReturn(7L);
        when(taskRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(2L);
        when(taskRepository.countByStatus(Status.TODO)).thenReturn(1L);
        
        byte[] expectedJson = "{\"analytics\":{}}".getBytes();
        when(objectMapper.writeValueAsBytes(any())).thenReturn(expectedJson);

        // When
        byte[] result = exportService.exportAnalyticsToJson();

        // Then
        assertNotNull(result);
        assertEquals(expectedJson, result);
        
        verify(taskRepository).count();
        verify(taskRepository).countByStatus(Status.COMPLETED);
        verify(taskRepository).countByStatus(Status.IN_PROGRESS);
        verify(taskRepository).countByStatus(Status.TODO);
        verify(objectMapper).writeValueAsBytes(any());
    }

    @Test
    void testExportAnalyticsToJsonWithException() throws Exception {
        // Given
        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.countByStatus(Status.COMPLETED)).thenReturn(7L);
        when(taskRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(2L);
        when(taskRepository.countByStatus(Status.TODO)).thenReturn(1L);
        when(objectMapper.writeValueAsBytes(any())).thenThrow(new RuntimeException("JSON error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> exportService.exportAnalyticsToJson());
        
        verify(taskRepository).count();
        verify(objectMapper).writeValueAsBytes(any());
    }

    @Test
    void testExportAnalyticsToJsonWithZeroTasks() throws Exception {
        // Given
        when(taskRepository.count()).thenReturn(0L);
        when(taskRepository.countByStatus(Status.COMPLETED)).thenReturn(0L);
        when(taskRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(0L);
        when(taskRepository.countByStatus(Status.TODO)).thenReturn(0L);
        
        byte[] expectedJson = "{\"analytics\":{}}".getBytes();
        when(objectMapper.writeValueAsBytes(any())).thenReturn(expectedJson);

        // When
        byte[] result = exportService.exportAnalyticsToJson();

        // Then
        assertNotNull(result);
        assertEquals(expectedJson, result);
        
        verify(taskRepository).count();
        verify(objectMapper).writeValueAsBytes(any());
    }

    private TaskResponse createTaskResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setCompletedAt(task.getCompletedAt());
        response.setNotes(task.getNotes());
        response.setOverdue(task.isOverdue());
        return response;
    }
}
