package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.entity.TaskAudit;
import com.example.taskmanagement.repository.TaskAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuditController}.
 */
@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private TaskAuditRepository taskAuditRepository;

    @InjectMocks
    private AuditController auditController;

    private TaskAudit testAudit1;
    private TaskAudit testAudit2;
    private List<TaskAudit> testAuditRecords;

    @BeforeEach
    void setUp() {
        testAudit1 = new TaskAudit();
        testAudit1.setId(1L);
        testAudit1.setTaskId(1L);
        testAudit1.setAction("CREATED");
        testAudit1.setTaskTitle("Test Task 1");
        testAudit1.setTimestamp(LocalDateTime.now().minusHours(1));
        testAudit1.setChangedBy("SYSTEM");
        testAudit1.setEntityType("Task");

        testAudit2 = new TaskAudit();
        testAudit2.setId(2L);
        testAudit2.setTaskId(1L);
        testAudit2.setAction("UPDATED");
        testAudit2.setTaskTitle("Test Task 1");
        testAudit2.setTimestamp(LocalDateTime.now());
        testAudit2.setChangedBy("SYSTEM");
        testAudit2.setEntityType("Task");

        testAuditRecords = Arrays.asList(testAudit2, testAudit1); // Sorted by timestamp desc
    }

    @Test
    void testGetAuditHistoryForTaskWithExistingRecords() {
        // Given
        Long taskId = 1L;
        when(taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(taskId)).thenReturn(testAuditRecords);

        // When
        ResponseEntity<ApiResponse<List<TaskAudit>>> response = auditController.getAuditHistoryForTask(taskId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());
        assertEquals(testAuditRecords, response.getBody().getData());
        
        verify(taskAuditRepository).findByTaskIdOrderByChangeTimestampDesc(taskId);
    }

    @Test
    void testGetAuditHistoryForTaskWithNoRecords() {
        // Given
        Long taskId = 999L;
        when(taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(taskId)).thenReturn(Arrays.asList());

        // When
        ResponseEntity<ApiResponse<List<TaskAudit>>> response = auditController.getAuditHistoryForTask(taskId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().isEmpty());
        
        verify(taskAuditRepository).findByTaskIdOrderByChangeTimestampDesc(taskId);
    }

    @Test
    void testGetAuditHistoryForTaskWithNullTaskId() {
        // Given
        when(taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(null)).thenReturn(Arrays.asList());

        // When
        ResponseEntity<ApiResponse<List<TaskAudit>>> response = auditController.getAuditHistoryForTask(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().isEmpty());
        
        verify(taskAuditRepository).findByTaskIdOrderByChangeTimestampDesc(null);
    }

    @Test
    void testGetAllAuditRecordsWithExistingRecords() {
        // Given
        when(taskAuditRepository.findAll()).thenReturn(testAuditRecords);

        // When
        ResponseEntity<ApiResponse<List<TaskAudit>>> response = auditController.getAllAuditRecords();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());
        assertEquals(testAuditRecords, response.getBody().getData());
        
        verify(taskAuditRepository).findAll();
    }

    @Test
    void testGetAllAuditRecordsWithNoRecords() {
        // Given
        when(taskAuditRepository.findAll()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<ApiResponse<List<TaskAudit>>> response = auditController.getAllAuditRecords();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().isEmpty());
        
        verify(taskAuditRepository).findAll();
    }

    @Test
    void testGetAuditHistoryForTaskWithSingleRecord() {
        // Given
        Long taskId = 2L;
        List<TaskAudit> singleRecord = Arrays.asList(testAudit1);
        when(taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(taskId)).thenReturn(singleRecord);

        // When
        ResponseEntity<ApiResponse<List<TaskAudit>>> response = auditController.getAuditHistoryForTask(taskId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(singleRecord, response.getBody().getData());
        
        verify(taskAuditRepository).findByTaskIdOrderByChangeTimestampDesc(taskId);
    }

    @Test
    void testGetAuditHistoryForTaskWithMultipleTasks() {
        // Given
        Long taskId = 1L;
        TaskAudit auditForDifferentTask = new TaskAudit();
        auditForDifferentTask.setId(3L);
        auditForDifferentTask.setTaskId(2L);
        auditForDifferentTask.setAction("CREATED");
        auditForDifferentTask.setTaskTitle("Different Task");
        auditForDifferentTask.setTimestamp(LocalDateTime.now().minusMinutes(30));
        auditForDifferentTask.setChangedBy("USER");
        auditForDifferentTask.setEntityType("Task");

        List<TaskAudit> allAudits = Arrays.asList(auditForDifferentTask, testAudit2, testAudit1);
        when(taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(taskId)).thenReturn(testAuditRecords);

        // When
        ResponseEntity<ApiResponse<List<TaskAudit>>> response = auditController.getAuditHistoryForTask(taskId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());
        
        // Verify only records for the specific task are returned
        for (TaskAudit audit : response.getBody().getData()) {
            assertEquals(taskId, audit.getTaskId());
        }
        
        verify(taskAuditRepository).findByTaskIdOrderByChangeTimestampDesc(taskId);
    }

    @Test
    void testGetAllAuditRecordsWithLargeDataset() {
        // Given
        List<TaskAudit> largeDataset = new ArrayList<>(Arrays.asList(testAudit1, testAudit2));
        for (int i = 3; i <= 100; i++) {
            TaskAudit audit = new TaskAudit();
            audit.setId((long) i);
            audit.setTaskId((long) (i % 10 + 1));
            audit.setAction("UPDATED");
            audit.setTaskTitle("Task " + i);
            audit.setTimestamp(LocalDateTime.now().minusMinutes(i));
            audit.setChangedBy("SYSTEM");
            audit.setEntityType("Task");
            largeDataset.add(audit);
        }
        
        when(taskAuditRepository.findAll()).thenReturn(largeDataset);

        // When
        ResponseEntity<ApiResponse<List<TaskAudit>>> response = auditController.getAllAuditRecords();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(100, response.getBody().getData().size());
        
        verify(taskAuditRepository).findAll();
    }
}
