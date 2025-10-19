package com.example.taskmanagement.service;

import com.example.taskmanagement.constants.TaskConstants;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.util.TaskStatisticsCalculator;
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
import java.util.HashMap;
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

    @Mock
    private TaskStatisticsCalculator statisticsCalculator;

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
            List<Task> allTasks = Arrays.asList(task1, task2, task3);
            
            Map<String, Object> mockStats = new HashMap<>();
            mockStats.put("totalTasks", 3L);
            mockStats.put("completedTasks", 1L);
            mockStats.put("overdueTasks", 2L);
            mockStats.put("completionRate", 33.33);
            mockStats.put("overdueRate", 66.67);
            
            Map<Status, Long> statusCountsEnum = new HashMap<>();
            statusCountsEnum.put(Status.TODO, 1L);
            statusCountsEnum.put(Status.IN_PROGRESS, 1L);
            statusCountsEnum.put(Status.COMPLETED, 1L);
            mockStats.put("statusCounts", statusCountsEnum);
            
            Map<Priority, Long> priorityCountsEnum = new HashMap<>();
            priorityCountsEnum.put(Priority.HIGH, 1L);
            priorityCountsEnum.put(Priority.MEDIUM, 1L);
            priorityCountsEnum.put(Priority.LOW, 1L);
            mockStats.put("priorityCounts", priorityCountsEnum);

            when(taskRepository.findAll()).thenReturn(allTasks);
            when(statisticsCalculator.calculateComprehensiveStats(allTasks)).thenReturn(mockStats);

            // When
            Map<String, Object> result = taskAnalyticsService.getTaskStatistics();

            // Then
            assertNotNull(result);
            assertTrue(result.containsKey("statusCounts"));
            assertTrue(result.containsKey("priorityCounts"));
            assertTrue(result.containsKey("overdueTasks"));
            assertTrue(result.containsKey("totalTasks"));
            assertTrue(result.containsKey("completionRate"));

            assertEquals(3L, result.get("totalTasks"));
            assertEquals(2L, result.get("overdueTasks"));
            assertEquals(33.33, (Double) result.get("completionRate"), 0.01);
            
            // Verify that status/priority counts are converted to enum names
            @SuppressWarnings("unchecked")
            Map<String, Long> statusCounts = (Map<String, Long>) result.get("statusCounts");
            assertTrue(statusCounts.containsKey("TODO"));
            assertTrue(statusCounts.containsKey("IN_PROGRESS"));
            assertTrue(statusCounts.containsKey("COMPLETED"));
        }

    @Test
    void testGetTaskStatisticsForDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Task> tasks = Arrays.asList(task1, task2, task3);
        
        Map<Status, Long> statusCountsEnum = new HashMap<>();
        statusCountsEnum.put(Status.TODO, 1L);
        statusCountsEnum.put(Status.IN_PROGRESS, 1L);
        statusCountsEnum.put(Status.COMPLETED, 1L);
        
        Map<Priority, Long> priorityCountsEnum = new HashMap<>();
        priorityCountsEnum.put(Priority.HIGH, 1L);
        priorityCountsEnum.put(Priority.MEDIUM, 1L);
        priorityCountsEnum.put(Priority.LOW, 1L);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalTasks", 3L);
        mockStats.put("completedTasks", 1L);
        mockStats.put("overdueTasks", 1L);
        mockStats.put("completionRate", 33.33);
        mockStats.put("overdueRate", 33.33);
        mockStats.put("statusCounts", statusCountsEnum);
        mockStats.put("priorityCounts", priorityCountsEnum);

        when(taskRepository.findTasksCreatedBetween(any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(tasks));
        when(statisticsCalculator.calculateComprehensiveStats(tasks)).thenReturn(mockStats);

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
        List<Task> allTasks = Arrays.asList(task1, task2, task3);
        
        Map<Status, Long> statusCountsEnum = new HashMap<>();
        statusCountsEnum.put(Status.TODO, 1L);
        statusCountsEnum.put(Status.IN_PROGRESS, 1L);
        statusCountsEnum.put(Status.COMPLETED, 1L);
        
        Map<Priority, Long> priorityCountsEnum = new HashMap<>();
        priorityCountsEnum.put(Priority.HIGH, 1L);
        priorityCountsEnum.put(Priority.MEDIUM, 1L);
        priorityCountsEnum.put(Priority.LOW, 1L);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalTasks", 3L);
        mockStats.put("completedTasks", 1L);
        mockStats.put("overdueTasks", 1L);
        mockStats.put("completionRate", 33.33);
        mockStats.put("overdueRate", 33.33);
        mockStats.put("statusCounts", statusCountsEnum);
        mockStats.put("priorityCounts", priorityCountsEnum);

        when(taskRepository.findAll()).thenReturn(allTasks);
        when(taskRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(1L);
        when(statisticsCalculator.calculateComprehensiveStats(allTasks)).thenReturn(mockStats);

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
        List<Task> emptyTasks = Arrays.asList();
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalTasks", 0L);
        mockStats.put("completedTasks", 0L);
        mockStats.put("overdueTasks", 0L);
        mockStats.put("completionRate", 0.0);
        mockStats.put("overdueRate", 0.0);
        mockStats.put("statusCounts", new HashMap<Status, Long>());
        mockStats.put("priorityCounts", new HashMap<Priority, Long>());

        when(taskRepository.findAll()).thenReturn(emptyTasks);
        when(statisticsCalculator.calculateComprehensiveStats(emptyTasks)).thenReturn(mockStats);

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
