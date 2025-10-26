package com.example.taskmanagement.integration;

import com.example.taskmanagement.TaskManagementApplication;
import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.repository.TaskAuditRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Base class for integration tests.
 * Provides common setup, utilities, and helper methods for testing REST endpoints.
 * 
 * Integration tests verify the complete flow from HTTP request to database and back,
 * testing all layers: Controller -> Service -> Repository -> Database.
 */
@SpringBootTest(
    classes = TaskManagementApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected TaskAuditRepository taskAuditRepository;

    /**
     * Clean up database before each test to ensure test isolation.
     */
    @BeforeEach
    public void setUp() {
        taskAuditRepository.deleteAll();
        taskRepository.deleteAll();
    }

    /**
     * Helper method to create a standard task request for testing.
     */
    protected TaskRequest createStandardTaskRequest() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Integration Test Task");
        request.setDescription("Test Description");
        request.setPriority(Priority.MEDIUM);
        request.setStatus(Status.TODO);
        request.setDueDate(LocalDate.now().plusDays(7));
        return request;
    }

    /**
     * Helper method to create a task request with custom values.
     */
    protected TaskRequest createTaskRequest(String title, String description, Priority priority, Status status) {
        TaskRequest request = new TaskRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPriority(priority);
        request.setStatus(status);
        request.setDueDate(LocalDate.now().plusDays(7));
        return request;
    }

    /**
     * Helper method to create a task via API and return its ID.
     */
    protected Long createTaskViaApi(TaskRequest request) throws Exception {
        String json = objectMapper.writeValueAsString(request);
        
        MvcResult result = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> apiResponse = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> data = (Map<String, Object>) apiResponse.get("data");
        
        return ((Number) data.get("id")).longValue();
    }

    /**
     * Helper method to extract data from ApiResponse wrapper.
     */
    protected <T> T extractDataFromApiResponse(String jsonResponse, Class<T> dataClass) throws Exception {
        Map<String, Object> apiResponse = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        Object data = apiResponse.get("data");
        
        // Convert data back to JSON and then to target type
        String dataJson = objectMapper.writeValueAsString(data);
        return objectMapper.readValue(dataJson, dataClass);
    }

    /**
     * Helper method to extract data from ApiResponse wrapper with TypeReference.
     */
    protected <T> T extractDataFromApiResponse(String jsonResponse, TypeReference<T> typeReference) throws Exception {
        Map<String, Object> apiResponse = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        Object data = apiResponse.get("data");
        
        // Convert data back to JSON and then to target type
        String dataJson = objectMapper.writeValueAsString(data);
        return objectMapper.readValue(dataJson, typeReference);
    }

    /**
     * Helper method to verify ApiResponse structure.
     */
    protected void verifyApiResponseStructure(String jsonResponse, boolean shouldBeSuccess) throws Exception {
        Map<String, Object> apiResponse = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        
        // Verify required fields exist
        assert apiResponse.containsKey("success");
        assert apiResponse.containsKey("data");
        assert apiResponse.containsKey("message");
        assert apiResponse.containsKey("timestamp");
        assert apiResponse.containsKey("status");
        
        // Verify success flag
        assert apiResponse.get("success").equals(shouldBeSuccess);
    }

    /**
     * Helper method to wait for async operations (if needed).
     */
    protected void waitForAsync(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

