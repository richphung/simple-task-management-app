package com.example.taskmanagement.integration;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TaskDuplicationController.
 * Tests all 2 endpoints with database interaction.
 * 
 * Endpoints tested:
 * 1. POST /api/tasks/{id}/duplicate - Duplicate task
 * 2. POST /api/tasks/{id}/custom - Duplicate task with custom modifications
 */
public class TaskDuplicationControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testDuplicateTask_Success() throws Exception {
        // Create original task
        TaskRequest originalRequest = createTaskRequest(
            "Original Task",
            "Original Description",
            Priority.HIGH,
            Status.IN_PROGRESS
        );
        originalRequest.setNotes("Important notes");
        Long originalTaskId = createTaskViaApi(originalRequest);

        // Duplicate the task
        mockMvc.perform(post("/api/tasks/{id}/duplicate", originalTaskId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.id").value(org.hamcrest.Matchers.not(originalTaskId)))
                .andExpect(jsonPath("$.data.title").value(org.hamcrest.Matchers.containsString("Original Task")))
                .andExpect(jsonPath("$.data.description").value("Original Description"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.status").value("TODO")); // Status should be reset to TODO

        // Verify both tasks exist
        assertEquals(2, taskRepository.count());
    }

    @Test
    public void testDuplicateTask_NotFound() throws Exception {
        mockMvc.perform(post("/api/tasks/{id}/duplicate", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDuplicateTask_MultipleTimesSamely() throws Exception {
        // Create original task
        TaskRequest originalRequest = createStandardTaskRequest();
        Long originalTaskId = createTaskViaApi(originalRequest);

        // Duplicate multiple times
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/tasks/{id}/duplicate", originalTaskId))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        // Verify 4 tasks exist (1 original + 3 duplicates)
        assertEquals(4, taskRepository.count());
    }

    @Test
    public void testDuplicateTask_PreservesOriginalData() throws Exception {
        // Create task with specific data
        TaskRequest originalRequest = createTaskRequest(
            "Complex Task",
            "Detailed description with important information",
            Priority.LOW,
            Status.COMPLETED
        );
        originalRequest.setNotes("These are important notes");
        originalRequest.setDueDate(LocalDate.now().plusDays(10));
        
        Long originalTaskId = createTaskViaApi(originalRequest);

        // Duplicate
        mockMvc.perform(post("/api/tasks/{id}/duplicate", originalTaskId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.description").value("Detailed description with important information"))
                .andExpect(jsonPath("$.data.priority").value("LOW"))
                .andExpect(jsonPath("$.data.notes").value("These are important notes"));

        // Verify original task is unchanged
        mockMvc.perform(get("/api/tasks/{id}", originalTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    public void testDuplicateTaskWithModifications_Success() throws Exception {
        // Create original task
        TaskRequest originalRequest = createTaskRequest(
            "Original Task",
            "Original Description",
            Priority.MEDIUM,
            Status.IN_PROGRESS
        );
        Long originalTaskId = createTaskViaApi(originalRequest);

        // Prepare modifications
        Map<String, Object> modifications = new HashMap<>();
        modifications.put("title", "Modified Duplicate Task");
        modifications.put("description", "Modified Description");
        modifications.put("priority", "HIGH");
        modifications.put("dueDate", LocalDate.now().plusDays(14).toString());

        String json = objectMapper.writeValueAsString(modifications);

        // Duplicate with modifications
        mockMvc.perform(post("/api/tasks/{id}/custom", originalTaskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Modified Duplicate Task"))
                .andExpect(jsonPath("$.data.description").value("Modified Description"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.status").value("TODO")); // Always reset to TODO

        assertEquals(2, taskRepository.count());
    }

    @Test
    public void testDuplicateTaskWithModifications_PartialModifications() throws Exception {
        // Create original task
        TaskRequest originalRequest = createTaskRequest(
            "Original Task",
            "Original Description",
            Priority.LOW,
            Status.COMPLETED
        );
        Long originalTaskId = createTaskViaApi(originalRequest);

        // Only modify title, keep other fields
        Map<String, Object> modifications = new HashMap<>();
        modifications.put("title", "New Title Only");

        String json = objectMapper.writeValueAsString(modifications);

        // Duplicate with partial modifications
        mockMvc.perform(post("/api/tasks/{id}/custom", originalTaskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("New Title Only"))
                .andExpect(jsonPath("$.data.description").value("Original Description"))
                .andExpect(jsonPath("$.data.priority").value("LOW"));
    }

    @Test
    public void testDuplicateTaskWithModifications_EmptyModifications() throws Exception {
        // Create original task
        TaskRequest originalRequest = createStandardTaskRequest();
        Long originalTaskId = createTaskViaApi(originalRequest);

        // Empty modifications
        Map<String, Object> modifications = new HashMap<>();
        String json = objectMapper.writeValueAsString(modifications);

        // Duplicate with empty modifications (should work like regular duplicate)
        mockMvc.perform(post("/api/tasks/{id}/custom", originalTaskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(2, taskRepository.count());
    }

    @Test
    public void testDuplicateTaskWithModifications_NotFound() throws Exception {
        Map<String, Object> modifications = new HashMap<>();
        modifications.put("title", "New Title");
        String json = objectMapper.writeValueAsString(modifications);

        mockMvc.perform(post("/api/tasks/{id}/custom", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDuplicateTaskWithModifications_AllFields() throws Exception {
        // Create original task
        TaskRequest originalRequest = createStandardTaskRequest();
        Long originalTaskId = createTaskViaApi(originalRequest);

        // Modify all fields
        Map<String, Object> modifications = new HashMap<>();
        modifications.put("title", "Completely New Title");
        modifications.put("description", "Completely New Description");
        modifications.put("priority", "HIGH");
        modifications.put("dueDate", LocalDate.now().plusDays(20).toString());
        modifications.put("notes", "Completely New Notes");

        String json = objectMapper.writeValueAsString(modifications);

        mockMvc.perform(post("/api/tasks/{id}/custom", originalTaskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Completely New Title"))
                .andExpect(jsonPath("$.data.description").value("Completely New Description"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.notes").value("Completely New Notes"));
    }

    @Test
    public void testDuplication_WorkflowScenario() throws Exception {
        // Create a template task
        TaskRequest templateRequest = createTaskRequest(
            "Weekly Report Template",
            "Complete weekly status report",
            Priority.MEDIUM,
            Status.TODO
        );
        templateRequest.setNotes("Include: Progress, Blockers, Next Steps");
        Long templateId = createTaskViaApi(templateRequest);

        // Duplicate for Week 1
        Map<String, Object> week1Mods = new HashMap<>();
        week1Mods.put("title", "Weekly Report - Week 1");
        week1Mods.put("dueDate", LocalDate.now().plusDays(7).toString());
        String week1Json = objectMapper.writeValueAsString(week1Mods);

        mockMvc.perform(post("/api/tasks/{id}/custom", templateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(week1Json))
                .andExpect(status().isCreated());

        // Duplicate for Week 2
        Map<String, Object> week2Mods = new HashMap<>();
        week2Mods.put("title", "Weekly Report - Week 2");
        week2Mods.put("dueDate", LocalDate.now().plusDays(14).toString());
        String week2Json = objectMapper.writeValueAsString(week2Mods);

        mockMvc.perform(post("/api/tasks/{id}/custom", templateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(week2Json))
                .andExpect(status().isCreated());

        // Verify 3 tasks exist (1 template + 2 duplicates)
        assertEquals(3, taskRepository.count());
    }

    @Test
    public void testDuplication_Performance() throws Exception {
        // Create original task
        TaskRequest originalRequest = createStandardTaskRequest();
        Long originalTaskId = createTaskViaApi(originalRequest);

        long startTime = System.currentTimeMillis();

        // Duplicate 50 times
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(post("/api/tasks/{id}/duplicate", originalTaskId))
                    .andExpect(status().isCreated());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert duplication completed in reasonable time (< 10 seconds)
        assertTrue(duration < 10000, "Duplication took too long: " + duration + "ms");
        assertEquals(51, taskRepository.count()); // 1 original + 50 duplicates
    }
}

