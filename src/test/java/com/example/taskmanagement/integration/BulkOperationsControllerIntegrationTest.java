package com.example.taskmanagement.integration;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BulkOperationsController.
 * Tests all 4 endpoints with database interaction.
 * 
 * Endpoints tested:
 * 1. POST /api/tasks/bulk/create - Bulk create tasks
 * 2. PUT /api/tasks/bulk/status - Bulk update task status
 * 3. POST /api/tasks/bulk/delete - Bulk delete tasks
 * 4. PUT /api/tasks/bulk/complete - Bulk complete tasks
 */
public class BulkOperationsControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testBulkCreateTasks_Success() throws Exception {
        // Prepare bulk create request
        List<TaskRequest> requests = Arrays.asList(
            createTaskRequest("Task 1", "Description 1", Priority.HIGH, Status.TODO),
            createTaskRequest("Task 2", "Description 2", Priority.MEDIUM, Status.TODO),
            createTaskRequest("Task 3", "Description 3", Priority.LOW, Status.TODO)
        );

        String json = objectMapper.writeValueAsString(requests);

        mockMvc.perform(post("/api/tasks/bulk/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].title").value("Task 1"))
                .andExpect(jsonPath("$.data[1].title").value("Task 2"))
                .andExpect(jsonPath("$.data[2].title").value("Task 3"));

        // Verify tasks were created in database
        assertEquals(3, taskRepository.count());
    }

    @Test
    public void testBulkCreateTasks_EmptyList() throws Exception {
        List<TaskRequest> requests = Arrays.asList();
        String json = objectMapper.writeValueAsString(requests);

        mockMvc.perform(post("/api/tasks/bulk/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        assertEquals(0, taskRepository.count());
    }

    @Test
    public void testBulkCreateTasks_LargeVolume() throws Exception {
        // Create 50 tasks in bulk
        List<TaskRequest> requests = new java.util.ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            requests.add(createTaskRequest("Task " + i, "Description " + i, Priority.MEDIUM, Status.TODO));
        }

        String json = objectMapper.writeValueAsString(requests);

        mockMvc.perform(post("/api/tasks/bulk/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(50));

        assertEquals(50, taskRepository.count());
    }

    @Test
    public void testBulkUpdateTaskStatus_Success() throws Exception {
        // Create tasks first
        Long task1Id = createTaskViaApi(createTaskRequest("Task 1", "Desc", Priority.MEDIUM, Status.TODO));
        Long task2Id = createTaskViaApi(createTaskRequest("Task 2", "Desc", Priority.MEDIUM, Status.TODO));
        Long task3Id = createTaskViaApi(createTaskRequest("Task 3", "Desc", Priority.MEDIUM, Status.TODO));

        // Prepare bulk update request
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("taskIds", Arrays.asList(task1Id, task2Id, task3Id));
        updateRequest.put("status", "IN_PROGRESS");

        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/tasks/bulk/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isString())
                .andExpect(jsonPath("$.data").value(containsString("Updated status for 3 tasks")));

        // Verify status was updated
        mockMvc.perform(get("/api/tasks/{id}", task1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    public void testBulkUpdateTaskStatus_PartialSuccess() throws Exception {
        // Create some tasks
        Long task1Id = createTaskViaApi(createTaskRequest("Task 1", "Desc", Priority.MEDIUM, Status.TODO));
        Long task2Id = createTaskViaApi(createTaskRequest("Task 2", "Desc", Priority.MEDIUM, Status.TODO));

        // Include non-existent task IDs
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("taskIds", Arrays.asList(task1Id, task2Id, 999L, 1000L));
        updateRequest.put("status", "COMPLETED");

        String json = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/tasks/bulk/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    public void testBulkDeleteTasks_Success() throws Exception {
        // Create tasks first
        Long task1Id = createTaskViaApi(createTaskRequest("Task 1", "Desc", Priority.MEDIUM, Status.TODO));
        Long task2Id = createTaskViaApi(createTaskRequest("Task 2", "Desc", Priority.MEDIUM, Status.TODO));
        Long task3Id = createTaskViaApi(createTaskRequest("Task 3", "Desc", Priority.MEDIUM, Status.TODO));

        assertEquals(3, taskRepository.count());

        // Prepare bulk delete request
        List<Long> taskIds = Arrays.asList(task1Id, task2Id, task3Id);
        String json = objectMapper.writeValueAsString(taskIds);

        // 204 No Content has no response body
        mockMvc.perform(post("/api/tasks/bulk/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        // Verify tasks were deleted
        assertEquals(0, taskRepository.count());
    }

    @Test
    public void testBulkDeleteTasks_EmptyList() throws Exception {
        createTaskViaApi(createStandardTaskRequest());
        
        List<Long> taskIds = Arrays.asList();
        String json = objectMapper.writeValueAsString(taskIds);

        mockMvc.perform(post("/api/tasks/bulk/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        // Verify no tasks were deleted
        assertEquals(1, taskRepository.count());
    }

    @Test
    public void testBulkDeleteTasks_NonExistentIds() throws Exception {
        // Try to delete non-existent tasks
        List<Long> taskIds = Arrays.asList(999L, 1000L, 1001L);
        String json = objectMapper.writeValueAsString(taskIds);

        // 204 No Content has no response body
        mockMvc.perform(post("/api/tasks/bulk/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testBulkCompleteTasks_Success() throws Exception {
        // Create TODO tasks
        Long task1Id = createTaskViaApi(createTaskRequest("Task 1", "Desc", Priority.MEDIUM, Status.TODO));
        Long task2Id = createTaskViaApi(createTaskRequest("Task 2", "Desc", Priority.MEDIUM, Status.TODO));

        // Bulk complete
        List<Long> taskIds = Arrays.asList(task1Id, task2Id);
        String json = objectMapper.writeValueAsString(taskIds);

        mockMvc.perform(put("/api/tasks/bulk/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data[1].status").value("COMPLETED"));

        // Verify tasks were completed
        mockMvc.perform(get("/api/tasks/{id}", task1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").exists());
    }

    @Test
    public void testBulkCompleteTasks_MixedStatuses() throws Exception {
        // Create tasks with different statuses
        Long task1Id = createTaskViaApi(createTaskRequest("Task 1", "Desc", Priority.MEDIUM, Status.TODO));
        Long task2Id = createTaskViaApi(createTaskRequest("Task 2", "Desc", Priority.MEDIUM, Status.IN_PROGRESS));
        Long task3Id = createTaskViaApi(createTaskRequest("Task 3", "Desc", Priority.MEDIUM, Status.COMPLETED));

        // Bulk complete all
        List<Long> taskIds = Arrays.asList(task1Id, task2Id, task3Id);
        String json = objectMapper.writeValueAsString(taskIds);

        mockMvc.perform(put("/api/tasks/bulk/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    public void testBulkOperations_Performance() throws Exception {
        // Test performance of bulk operations
        long startTime = System.currentTimeMillis();

        // Bulk create 100 tasks
        List<TaskRequest> requests = new java.util.ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            requests.add(createTaskRequest("Task " + i, "Description " + i, Priority.MEDIUM, Status.TODO));
        }

        String json = objectMapper.writeValueAsString(requests);

        mockMvc.perform(post("/api/tasks/bulk/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert bulk operation completed in reasonable time (< 5 seconds)
        assertTrue(duration < 5000, "Bulk create took too long: " + duration + "ms");
        assertEquals(100, taskRepository.count());
    }

    @Test
    public void testBulkOperations_CompleteWorkflow() throws Exception {
        // 1. Bulk create tasks
        List<TaskRequest> requests = Arrays.asList(
            createTaskRequest("Task 1", "Desc", Priority.HIGH, Status.TODO),
            createTaskRequest("Task 2", "Desc", Priority.MEDIUM, Status.TODO),
            createTaskRequest("Task 3", "Desc", Priority.LOW, Status.TODO)
        );

        String createJson = objectMapper.writeValueAsString(requests);
        mockMvc.perform(post("/api/tasks/bulk/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isCreated());

        // Get all task IDs
        List<Long> allTaskIds = new java.util.ArrayList<>();
        taskRepository.findAll().forEach(task -> allTaskIds.add(task.getId()));

        // 2. Bulk update status
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("taskIds", allTaskIds);
        updateRequest.put("status", "IN_PROGRESS");
        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/tasks/bulk/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());

        // 3. Bulk complete
        String completeJson = objectMapper.writeValueAsString(allTaskIds);
        mockMvc.perform(put("/api/tasks/bulk/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(completeJson))
                .andExpect(status().isOk());

        // 4. Bulk delete
        mockMvc.perform(post("/api/tasks/bulk/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(completeJson))
                .andExpect(status().isNoContent());

        // Verify all operations completed
        assertEquals(0, taskRepository.count());
    }
}

