package com.example.taskmanagement.controller;

import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.service.TaskAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.EnumMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AnalyticsController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskAnalyticsService taskAnalyticsService;

    private Map<Status, Long> statusCounts;
    private Map<Priority, Long> priorityCounts;

    @BeforeEach
    void setUp() {
        statusCounts = new EnumMap<>(Status.class);
        statusCounts.put(Status.TODO, 5L);
        statusCounts.put(Status.IN_PROGRESS, 3L);
        statusCounts.put(Status.COMPLETED, 2L);

        priorityCounts = new EnumMap<>(Priority.class);
        priorityCounts.put(Priority.HIGH, 4L);
        priorityCounts.put(Priority.MEDIUM, 3L);
        priorityCounts.put(Priority.LOW, 3L);
    }

    @Test
    void testGetTaskCountsByStatus() throws Exception {
        Map<String, Object> stats = new java.util.HashMap<>();
        Map<String, Long> statusCountsMap = new java.util.HashMap<>();
        statusCountsMap.put("TODO", 5L);
        statusCountsMap.put("IN_PROGRESS", 3L);
        statusCountsMap.put("COMPLETED", 2L);
        stats.put("statusCounts", statusCountsMap);
        when(taskAnalyticsService.getTaskStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/analytics/status-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.TODO").value(5))
                .andExpect(jsonPath("$.data.IN_PROGRESS").value(3))
                .andExpect(jsonPath("$.data.COMPLETED").value(2));
    }

    @Test
    void testGetTaskCountsByPriority() throws Exception {
        Map<String, Object> stats = new java.util.HashMap<>();
        Map<String, Long> priorityCountsMap = new java.util.HashMap<>();
        priorityCountsMap.put("HIGH", 4L);
        priorityCountsMap.put("MEDIUM", 3L);
        priorityCountsMap.put("LOW", 3L);
        stats.put("priorityCounts", priorityCountsMap);
        when(taskAnalyticsService.getTaskStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/analytics/priority-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.HIGH").value(4))
                .andExpect(jsonPath("$.data.MEDIUM").value(3))
                .andExpect(jsonPath("$.data.LOW").value(3));
    }

    @Test
    void testGetDashboardAnalytics() throws Exception {
        Map<String, Object> stats = new java.util.HashMap<>();
        Map<String, Long> statusCountsMap = new java.util.HashMap<>();
        statusCountsMap.put("TODO", 5L);
        statusCountsMap.put("IN_PROGRESS", 3L);
        statusCountsMap.put("COMPLETED", 2L);
        Map<String, Long> priorityCountsMap = new java.util.HashMap<>();
        priorityCountsMap.put("HIGH", 4L);
        priorityCountsMap.put("MEDIUM", 3L);
        priorityCountsMap.put("LOW", 3L);
        stats.put("statusCounts", statusCountsMap);
        stats.put("priorityCounts", priorityCountsMap);
        stats.put("totalTasks", 10L);
        stats.put("completedTasks", 2L);
        stats.put("completionRate", 20.0);
        when(taskAnalyticsService.getTaskStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statusCounts.TODO").value(5))
                .andExpect(jsonPath("$.data.statusCounts.IN_PROGRESS").value(3))
                .andExpect(jsonPath("$.data.statusCounts.COMPLETED").value(2))
                .andExpect(jsonPath("$.data.priorityCounts.HIGH").value(4))
                .andExpect(jsonPath("$.data.priorityCounts.MEDIUM").value(3))
                .andExpect(jsonPath("$.data.priorityCounts.LOW").value(3))
                .andExpect(jsonPath("$.data.totalTasks").value(10))
                .andExpect(jsonPath("$.data.completedTasks").value(2))
                .andExpect(jsonPath("$.data.completionRate").value(20.0));
    }
}
