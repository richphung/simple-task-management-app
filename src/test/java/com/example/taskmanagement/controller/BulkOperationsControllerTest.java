package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BulkOperationsController}.
 */
@ExtendWith(MockitoExtension.class)
class BulkOperationsControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private BulkOperationsController bulkOperationsController;

    private TaskRequest testRequest1;
    private TaskRequest testRequest2;
    private List<TaskRequest> testRequests;
    private TaskResponse testTask1;
    private TaskResponse testTask2;
    private List<TaskResponse> testTasks;

    @BeforeEach
    void setUp() {
        testRequest1 = new TaskRequest();
        testRequest1.setTitle("Bulk Task 1");
        testRequest1.setDescription("First bulk task");
        testRequest1.setStatus(Status.TODO);
        testRequest1.setPriority(Priority.HIGH);
        testRequest1.setDueDate(LocalDate.now().plusDays(1));

        testRequest2 = new TaskRequest();
        testRequest2.setTitle("Bulk Task 2");
        testRequest2.setDescription("Second bulk task");
        testRequest2.setStatus(Status.IN_PROGRESS);
        testRequest2.setPriority(Priority.MEDIUM);
        testRequest2.setDueDate(LocalDate.now().plusDays(2));

        testRequests = Arrays.asList(testRequest1, testRequest2);

        testTask1 = new TaskResponse();
        testTask1.setId(1L);
        testTask1.setTitle("Bulk Task 1");
        testTask1.setDescription("First bulk task");
        testTask1.setStatus(Status.TODO);
        testTask1.setPriority(Priority.HIGH);
        testTask1.setDueDate(LocalDate.now().plusDays(1));

        testTask2 = new TaskResponse();
        testTask2.setId(2L);
        testTask2.setTitle("Bulk Task 2");
        testTask2.setDescription("Second bulk task");
        testTask2.setStatus(Status.IN_PROGRESS);
        testTask2.setPriority(Priority.MEDIUM);
        testTask2.setDueDate(LocalDate.now().plusDays(2));

        testTasks = Arrays.asList(testTask1, testTask2);
    }

    @Test
    void testBulkCreateTasks() {
        // Given
        when(taskService.bulkCreateTasks(testRequests)).thenReturn(testTasks);

        // When
        ResponseEntity<List<TaskResponse>> response = bulkOperationsController.bulkCreateTasks(testRequests);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(testTasks, response.getBody());
        
        verify(taskService).bulkCreateTasks(testRequests);
    }

    @Test
    void testBulkCreateTasksWithEmptyList() {
        // Given
        List<TaskRequest> emptyRequests = Arrays.asList();

        // When
        ResponseEntity<List<TaskResponse>> response = bulkOperationsController.bulkCreateTasks(emptyRequests);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        
        verify(taskService).bulkCreateTasks(Collections.emptyList());
    }

    @Test
    void testBulkCreateTasksWithNullList() {
        // Given
        List<TaskRequest> nullRequests = null;

        // When
        ResponseEntity<List<TaskResponse>> response = bulkOperationsController.bulkCreateTasks(nullRequests);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        
        verify(taskService).bulkCreateTasks(null);
    }

    @Test
    void testBulkUpdateStatus() {
        // Given
        List<Long> taskIds = Arrays.asList(1L, 2L);
        Status newStatus = Status.COMPLETED;
        when(taskService.bulkUpdateTaskStatus(taskIds, newStatus)).thenReturn(2);

        // When
        ResponseEntity<String> response = bulkOperationsController.bulkUpdateStatus(taskIds, newStatus);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Updated status for 2 tasks"));
        
        verify(taskService).bulkUpdateTaskStatus(taskIds, newStatus);
    }

    @Test
    void testBulkUpdateStatusWithEmptyList() {
        // Given
        List<Long> emptyIds = Arrays.asList();
        Status newStatus = Status.COMPLETED;

        // When
        ResponseEntity<String> response = bulkOperationsController.bulkUpdateStatus(emptyIds, newStatus);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Updated status for 0 tasks"));
        
        verify(taskService).bulkUpdateTaskStatus(emptyIds, newStatus);
    }

    @Test
    void testBulkDeleteTasks() {
        // Given
        List<Long> taskIds = Arrays.asList(1L, 2L);

        // When
        ResponseEntity<Void> response = bulkOperationsController.bulkDeleteTasks(taskIds);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(taskService).bulkDeleteTasks(taskIds);
    }

    @Test
    void testBulkDeleteTasksWithEmptyList() {
        // Given
        List<Long> emptyIds = Arrays.asList();

        // When
        ResponseEntity<Void> response = bulkOperationsController.bulkDeleteTasks(emptyIds);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(taskService).bulkDeleteTasks(emptyIds);
    }

    @Test
    void testBulkCompleteTasks() {
        // Given
        List<Long> taskIds = Arrays.asList(1L, 2L);
        when(taskService.bulkCompleteTasks(taskIds)).thenReturn(testTasks);

        // When
        ResponseEntity<List<TaskResponse>> response = bulkOperationsController.bulkCompleteTasks(taskIds);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(testTasks, response.getBody());
        
        verify(taskService).bulkCompleteTasks(taskIds);
    }

    @Test
    void testBulkCompleteTasksWithEmptyList() {
        // Given
        List<Long> emptyIds = Arrays.asList();

        // When
        ResponseEntity<List<TaskResponse>> response = bulkOperationsController.bulkCompleteTasks(emptyIds);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        
        verify(taskService).bulkCompleteTasks(Collections.emptyList());
    }

    @Test
    void testBulkCreateTasksWithSingleTask() {
        // Given
        List<TaskRequest> singleRequest = Arrays.asList(testRequest1);
        when(taskService.bulkCreateTasks(singleRequest)).thenReturn(Arrays.asList(testTask1));

        // When
        ResponseEntity<List<TaskResponse>> response = bulkOperationsController.bulkCreateTasks(singleRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testTask1, response.getBody().get(0));
        
        verify(taskService).bulkCreateTasks(singleRequest);
    }

    @Test
    void testBulkUpdateStatusWithSingleTask() {
        // Given
        List<Long> singleId = Arrays.asList(1L);
        Status newStatus = Status.COMPLETED;
        when(taskService.bulkUpdateTaskStatus(singleId, newStatus)).thenReturn(1);

        // When
        ResponseEntity<String> response = bulkOperationsController.bulkUpdateStatus(singleId, newStatus);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Updated status for 1 tasks"));
        
        verify(taskService).bulkUpdateTaskStatus(singleId, newStatus);
    }
}
