package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TaskAnalyticsService.
 */
@ExtendWith(MockitoExtension.class)
class TaskAnalyticsServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskAnalyticsService taskAnalyticsService;

    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        task1 = new Task("Task 1", Priority.HIGH, Status.TODO);
        task1.setId(1L);
        task1.setCreatedAt(LocalDateTime.now());

        task2 = new Task("Task 2", Priority.MEDIUM, Status.IN_PROGRESS);
        task2.setId(2L);
        task2.setCreatedAt(LocalDateTime.now());

        task3 = new Task("Task 3", Priority.LOW, Status.COMPLETED);
        task3.setId(3L);
        task3.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetTaskStatistics() {
        // Given
        when(taskRepository.countByStatus(Status.TODO)).thenReturn(1L);
        when(taskRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(1L);
        when(taskRepository.countByStatus(Status.COMPLETED)).thenReturn(1L);
        when(taskRepository.countByStatus(Status.CANCELLED)).thenReturn(0L);
        when(taskRepository.countByStatus(Status.ON_HOLD)).thenReturn(0L);

        when(taskRepository.countByPriority(Priority.HIGH)).thenReturn(1L);
        when(taskRepository.countByPriority(Priority.MEDIUM)).thenReturn(1L);
        when(taskRepository.countByPriority(Priority.LOW)).thenReturn(1L);
        when(taskRepository.countByPriority(Priority.URGENT)).thenReturn(0L);

        when(taskRepository.findOverdueTasks(any(LocalDate.class))).thenReturn(Arrays.asList(task1));
        when(taskRepository.count()).thenReturn(3L);

        // When
        Map<String, Object> result = taskAnalyticsService.getTaskStatistics();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("statusCounts"));
        assertTrue(result.containsKey("priorityCounts"));
        assertTrue(result.containsKey("overdueCount"));
        assertTrue(result.containsKey("totalTasks"));
        assertTrue(result.containsKey("completionRate"));

        assertEquals(3L, result.get("totalTasks"));
        assertEquals(1L, result.get("overdueCount"));
        assertEquals(33.33, result.get("completionRate"));
    }

    @Test
    void testGetTaskStatisticsForDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Task> tasks = Arrays.asList(task1, task2, task3);
        when(taskRepository.findTasksCreatedBetween(any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(tasks));

        // When
        Map<String, Object> result = taskAnalyticsService.getTaskStatisticsForDateRange(startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("tasksCreated"));
        assertTrue(result.containsKey("statusCounts"));

        assertEquals(3L, result.get("tasksCreated"));
    }

    @Test
    void testGetProductivityMetrics() {
        // Given
        when(taskRepository.count()).thenReturn(3L);
        when(taskRepository.countByStatus(Status.COMPLETED)).thenReturn(1L);
        when(taskRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(1L);
        when(taskRepository.findOverdueTasks(any(LocalDate.class))).thenReturn(Arrays.asList(task1));

        // When
        Map<String, Object> result = taskAnalyticsService.getProductivityMetrics();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("totalTasks"));
        assertTrue(result.containsKey("completedTasks"));
        assertTrue(result.containsKey("inProgressTasks"));
        assertTrue(result.containsKey("overdueTasks"));
        assertTrue(result.containsKey("completionRate"));
        assertTrue(result.containsKey("overdueRate"));

        assertEquals(3L, result.get("totalTasks"));
        assertEquals(1L, result.get("completedTasks"));
        assertEquals(1L, result.get("inProgressTasks"));
        assertEquals(1L, result.get("overdueTasks"));
        assertEquals(33.33, result.get("completionRate"));
        assertEquals(33.33, result.get("overdueRate"));
    }

    @Test
    void testGetProductivityMetricsWithZeroTasks() {
        // Given
        when(taskRepository.count()).thenReturn(0L);
        when(taskRepository.countByStatus(Status.COMPLETED)).thenReturn(0L);
        when(taskRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(0L);
        when(taskRepository.findOverdueTasks(any(LocalDate.class))).thenReturn(Arrays.asList());

        // When
        Map<String, Object> result = taskAnalyticsService.getProductivityMetrics();

        // Then
        assertNotNull(result);
        assertEquals(0L, result.get("totalTasks"));
        assertEquals(0L, result.get("completedTasks"));
        assertEquals(0L, result.get("inProgressTasks"));
        assertEquals(0L, result.get("overdueTasks"));
        assertEquals(0.0, result.get("completionRate"));
        assertEquals(0.0, result.get("overdueRate"));
    }
}
