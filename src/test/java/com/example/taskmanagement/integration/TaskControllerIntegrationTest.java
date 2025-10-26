package com.example.taskmanagement.integration;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.dto.TaskSearchRequest;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TaskController.
 * Tests all 10 endpoints with database interaction.
 * 
 * Endpoints tested:
 * 1. POST /api/tasks - Create task
 * 2. GET /api/tasks/{id} - Get task by ID
 * 3. GET /api/tasks - Get all tasks (paginated)
 * 4. PUT /api/tasks/{id} - Update task
 * 5. DELETE /api/tasks/{id} - Delete task
 * 6. PUT /api/tasks/{id}/complete - Complete task
 * 7. POST /api/tasks/search - Search tasks
 * 8. GET /api/tasks/search/quick - Quick search
 * 9. GET /api/tasks/status/{status} - Get tasks by status
 * 10. GET /api/tasks/overdue - Get overdue tasks
 */
public class TaskControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testCreateTask_Success() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value(request.getTitle()))
                .andExpect(jsonPath("$.data.description").value(request.getDescription()))
                .andExpect(jsonPath("$.data.priority").value(request.getPriority().name()))
                .andExpect(jsonPath("$.data.status").value(request.getStatus().name()))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        verifyApiResponseStructure(response, true);

        // Verify task was actually saved in database
        assertEquals(1, taskRepository.count());
    }

    @Test
    public void testCreateTask_ValidationFailure() throws Exception {
        TaskRequest request = new TaskRequest();
        // Missing required fields
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetTaskById_Success() throws Exception {
        // Create a task first
        TaskRequest request = createStandardTaskRequest();
        Long taskId = createTaskViaApi(request);

        // Get the task
        MvcResult result = mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(taskId))
                .andExpect(jsonPath("$.data.title").value(request.getTitle()))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        verifyApiResponseStructure(response, true);
    }

    @Test
    public void testGetTaskById_NotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllTasks_Success() throws Exception {
        // Create multiple tasks
        createTaskViaApi(createTaskRequest("Task 1", "Desc 1", Priority.HIGH, Status.TODO));
        createTaskViaApi(createTaskRequest("Task 2", "Desc 2", Priority.MEDIUM, Status.IN_PROGRESS));
        createTaskViaApi(createTaskRequest("Task 3", "Desc 3", Priority.LOW, Status.COMPLETED));

        MvcResult result = mockMvc.perform(get("/api/tasks")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        verifyApiResponseStructure(response, true);
    }

    @Test
    public void testGetAllTasks_Pagination() throws Exception {
        // Create 15 tasks
        for (int i = 1; i <= 15; i++) {
            createTaskViaApi(createTaskRequest("Task " + i, "Desc " + i, Priority.MEDIUM, Status.TODO));
        }

        // Get first page
        mockMvc.perform(get("/api/tasks")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(15))
                .andExpect(jsonPath("$.data.totalPages").value(3));

        // Get second page
        mockMvc.perform(get("/api/tasks")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(5))
                .andExpect(jsonPath("$.data.number").value(1));
    }

    @Test
    public void testUpdateTask_Success() throws Exception {
        // Create a task
        TaskRequest createRequest = createStandardTaskRequest();
        Long taskId = createTaskViaApi(createRequest);

        // Update the task
        TaskRequest updateRequest = createTaskRequest("Updated Title", "Updated Description", Priority.HIGH, Status.IN_PROGRESS);
        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(taskId))
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.description").value("Updated Description"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    public void testUpdateTask_NotFound() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/tasks/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteTask_Success() throws Exception {
        // Create a task
        TaskRequest request = createStandardTaskRequest();
        Long taskId = createTaskViaApi(request);

        // Verify task exists
        assertEquals(1, taskRepository.count());

        // Delete the task (204 No Content has no response body)
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        // Verify task was deleted
        assertEquals(0, taskRepository.count());
    }

    @Test
    public void testDeleteTask_NotFound() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCompleteTask_Success() throws Exception {
        // Create a TODO task
        TaskRequest request = createTaskRequest("Task to Complete", "Description", Priority.MEDIUM, Status.TODO);
        Long taskId = createTaskViaApi(request);

        // Complete the task
        mockMvc.perform(put("/api/tasks/{id}/complete", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(taskId))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").exists());
    }

    @Test
    public void testCompleteTask_NotFound() throws Exception {
        mockMvc.perform(put("/api/tasks/{id}/complete", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSearchTasks_Success() throws Exception {
        // Create tasks with different attributes
        createTaskViaApi(createTaskRequest("Bug Fix", "Fix critical bug", Priority.HIGH, Status.TODO));
        createTaskViaApi(createTaskRequest("Feature Request", "Implement new feature", Priority.MEDIUM, Status.IN_PROGRESS));
        createTaskViaApi(createTaskRequest("Documentation", "Update docs", Priority.LOW, Status.COMPLETED));

        // Search for tasks
        TaskSearchRequest searchRequest = new TaskSearchRequest();
        searchRequest.setSearchTerm("Bug");
        searchRequest.setPage(0);
        searchRequest.setSize(10);

        String json = objectMapper.writeValueAsString(searchRequest);

        MvcResult result = mockMvc.perform(post("/api/tasks/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        verifyApiResponseStructure(response, true);
    }

    @Test
    public void testSearchTasks_WithFilters() throws Exception {
        // Create tasks
        createTaskViaApi(createTaskRequest("Task 1", "Description", Priority.HIGH, Status.TODO));
        createTaskViaApi(createTaskRequest("Task 2", "Description", Priority.HIGH, Status.COMPLETED));
        createTaskViaApi(createTaskRequest("Task 3", "Description", Priority.LOW, Status.TODO));

        // Search by priority
        TaskSearchRequest searchRequest = new TaskSearchRequest();
        searchRequest.setPriority(Priority.HIGH);
        searchRequest.setPage(0);
        searchRequest.setSize(10);

        String json = objectMapper.writeValueAsString(searchRequest);

        mockMvc.perform(post("/api/tasks/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test
    public void testQuickSearch_Success() throws Exception {
        // Create tasks
        createTaskViaApi(createTaskRequest("Complete project documentation", "Write comprehensive docs", Priority.MEDIUM, Status.TODO));
        createTaskViaApi(createTaskRequest("Review code", "Code review for PR #123", Priority.HIGH, Status.IN_PROGRESS));

        // Quick search
        mockMvc.perform(get("/api/tasks/search/quick")
                .param("q", "documentation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    public void testGetTasksByStatus_Success() throws Exception {
        // Create tasks with different statuses
        createTaskViaApi(createTaskRequest("Task 1", "Desc", Priority.MEDIUM, Status.TODO));
        createTaskViaApi(createTaskRequest("Task 2", "Desc", Priority.MEDIUM, Status.TODO));
        createTaskViaApi(createTaskRequest("Task 3", "Desc", Priority.MEDIUM, Status.IN_PROGRESS));

        // Get TODO tasks
        mockMvc.perform(get("/api/tasks/status/{status}", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test
    public void testGetOverdueTasks_Success() throws Exception {
        // Create an overdue task (past due date)
        TaskRequest overdueRequest = createStandardTaskRequest();
        overdueRequest.setDueDate(LocalDate.now().minusDays(5));
        createTaskViaApi(overdueRequest);

        // Create a non-overdue task
        TaskRequest normalRequest = createStandardTaskRequest();
        normalRequest.setDueDate(LocalDate.now().plusDays(5));
        createTaskViaApi(normalRequest);

        // Get overdue tasks
        mockMvc.perform(get("/api/tasks/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    public void testFullCrudLifecycle() throws Exception {
        // CREATE
        TaskRequest createRequest = createStandardTaskRequest();
        Long taskId = createTaskViaApi(createRequest);
        assertNotNull(taskId);

        // READ
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(createRequest.getTitle()));

        // UPDATE
        TaskRequest updateRequest = createTaskRequest("Updated", "Updated Desc", Priority.HIGH, Status.IN_PROGRESS);
        String updateJson = objectMapper.writeValueAsString(updateRequest);
        
        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated"));

        // DELETE
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
    }
}

