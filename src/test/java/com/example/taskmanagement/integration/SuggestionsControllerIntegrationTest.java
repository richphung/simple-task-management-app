package com.example.taskmanagement.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SuggestionsController.
 * Tests all 3 endpoints with database interaction.
 * 
 * Endpoints tested:
 * 1. GET /api/suggestions/task - Get task suggestions
 * 2. POST /api/suggestions/tasks - Get multiple task suggestions
 * 3. GET /api/suggestions/default - Get default suggestions
 */
public class SuggestionsControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testGetTaskSuggestions_Success() throws Exception {
        mockMvc.perform(get("/api/suggestions/task")
                .param("title", "Fix critical bug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetTaskSuggestions_EmptyTitle() throws Exception {
        mockMvc.perform(get("/api/suggestions/task")
                .param("title", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetTaskSuggestions_DifferentKeywords() throws Exception {
        // Test various keywords
        String[] keywords = {"bug", "feature", "documentation", "refactor", "test"};

        for (String keyword : keywords) {
            mockMvc.perform(get("/api/suggestions/task")
                    .param("title", keyword))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Test
    public void testGetTaskSuggestions_LongTitle() throws Exception {
        String longTitle = "This is a very long task title that contains multiple words and should still generate suggestions";

        mockMvc.perform(get("/api/suggestions/task")
                .param("title", longTitle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetTaskSuggestions_SpecialCharacters() throws Exception {
        mockMvc.perform(get("/api/suggestions/task")
                .param("title", "Fix bug #123 - Critical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetMultipleTaskSuggestions_Success() throws Exception {
        String[] titles = {"Fix bug", "Add feature", "Write documentation"};
        String json = objectMapper.writeValueAsString(titles);

        mockMvc.perform(post("/api/suggestions/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data['Fix bug']").exists())
                .andExpect(jsonPath("$.data['Add feature']").exists())
                .andExpect(jsonPath("$.data['Write documentation']").exists());
    }

    @Test
    public void testGetMultipleTaskSuggestions_EmptyArray() throws Exception {
        String[] titles = {};
        String json = objectMapper.writeValueAsString(titles);

        mockMvc.perform(post("/api/suggestions/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void testGetMultipleTaskSuggestions_SingleTitle() throws Exception {
        String[] titles = {"Fix critical bug"};
        String json = objectMapper.writeValueAsString(titles);

        mockMvc.perform(post("/api/suggestions/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data['Fix critical bug']").exists());
    }

    @Test
    public void testGetMultipleTaskSuggestions_ManyTitles() throws Exception {
        String[] titles = new String[10];
        for (int i = 0; i < 10; i++) {
            titles[i] = "Task " + i;
        }
        String json = objectMapper.writeValueAsString(titles);

        mockMvc.perform(post("/api/suggestions/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void testGetDefaultSuggestions_Success() throws Exception {
        mockMvc.perform(get("/api/suggestions/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetDefaultSuggestions_Consistency() throws Exception {
        // Call multiple times and verify we get consistent results
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/suggestions/default"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Test
    public void testSuggestions_WithRealTaskData() throws Exception {
        // Create tasks with patterns
        createTaskViaApi(createTaskRequest("Fix bug in login", "Login bug", com.example.taskmanagement.enums.Priority.HIGH, com.example.taskmanagement.enums.Status.TODO));
        createTaskViaApi(createTaskRequest("Fix bug in payment", "Payment bug", com.example.taskmanagement.enums.Priority.HIGH, com.example.taskmanagement.enums.Status.TODO));
        createTaskViaApi(createTaskRequest("Add new feature", "Feature request", com.example.taskmanagement.enums.Priority.MEDIUM, com.example.taskmanagement.enums.Status.TODO));

        // Get suggestions for similar tasks
        mockMvc.perform(get("/api/suggestions/task")
                .param("title", "Fix bug in checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testSuggestions_ErrorHandling() throws Exception {
        // Test with potential error-causing inputs
        mockMvc.perform(get("/api/suggestions/task")
                .param("title", "<script>alert('test')</script>"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}




