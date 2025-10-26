package com.example.taskmanagement.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Swagger/OpenAPI documentation endpoints.
 * Verifies that Swagger UI and OpenAPI documentation are accessible.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SwaggerIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testSwaggerUiIsAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testOpenApiDocsAreAccessible() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.openapi").value("3.0.1"))
                .andExpect(jsonPath("$.info.title").exists())
                .andExpect(jsonPath("$.info.version").exists())
                .andExpect(jsonPath("$.paths").exists());
    }

    @Test
    public void testOpenApiContainsAllTags() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[?(@.name == 'Task Management')]").exists())
                .andExpect(jsonPath("$.tags[?(@.name == 'Bulk Operations')]").exists())
                .andExpect(jsonPath("$.tags[?(@.name == 'Analytics & Reporting')]").exists())
                .andExpect(jsonPath("$.tags[?(@.name == 'Data Export')]").exists())
                .andExpect(jsonPath("$.tags[?(@.name == 'Smart Suggestions')]").exists())
                .andExpect(jsonPath("$.tags[?(@.name == 'Task Duplication')]").exists())
                .andExpect(jsonPath("$.tags[?(@.name == 'Audit Trail')]").exists());
    }

    @Test
    public void testOpenApiContainsTaskEndpoints() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/tasks']").exists())
                .andExpect(jsonPath("$.paths['/api/tasks/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/tasks/bulk/create']").exists())
                .andExpect(jsonPath("$.paths['/api/analytics/dashboard']").exists());
    }

    @Test
    public void testOpenApiContainsSchemas() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.TaskRequest").exists())
                .andExpect(jsonPath("$.components.schemas.TaskResponse").exists())
                .andExpect(jsonPath("$.components.schemas.TaskSearchRequest").exists())
                .andExpect(jsonPath("$.components.schemas.ApiResponse").exists());
    }

    @Test
    public void testTaskRequestSchemaHasDescriptions() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.TaskRequest.properties.title.description").exists())
                .andExpect(jsonPath("$.components.schemas.TaskRequest.properties.priority.description").exists())
                .andExpect(jsonPath("$.components.schemas.TaskRequest.properties.status.description").exists());
    }

    @Test
    public void testEndpointHasOperationSummary() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/tasks'].post.summary").value("Create a new task"))
                .andExpect(jsonPath("$.paths['/api/tasks'].post.description").exists())
                .andExpect(jsonPath("$.paths['/api/tasks'].post.tags[0]").value("Task Management"));
    }
}




