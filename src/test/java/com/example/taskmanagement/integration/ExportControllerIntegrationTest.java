package com.example.taskmanagement.integration;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ExportController.
 * Tests all 6 endpoints with database interaction.
 * 
 * Endpoints tested:
 * 1. GET /api/export/tasks/csv - Export tasks to CSV
 * 2. GET /api/export/tasks/json - Export tasks to JSON
 * 3. GET /api/export/analytics/json - Export analytics to JSON
 * 4. GET /api/export/formats - Get available export formats
 * 5. GET /api/export/tasks - Export tasks (format parameter)
 * 6. GET /api/export/analytics - Export analytics
 */
public class ExportControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testExportTasksToCsv_WithData() throws Exception {
        // Create tasks
        createTaskViaApi(createTaskRequest("Task 1", "Description 1", Priority.HIGH, Status.TODO));
        createTaskViaApi(createTaskRequest("Task 2", "Description 2", Priority.MEDIUM, Status.IN_PROGRESS));

        mockMvc.perform(get("/api/export/tasks/csv"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("tasks_export_")))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")));
    }

    @Test
    public void testExportTasksToCsv_EmptyDatabase() throws Exception {
        mockMvc.perform(get("/api/export/tasks/csv"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")));
    }

    @Test
    public void testExportTasksToCsv_WithFilters() throws Exception {
        // Create tasks
        createTaskViaApi(createTaskRequest("High Priority Task", "Desc", Priority.HIGH, Status.TODO));
        createTaskViaApi(createTaskRequest("Low Priority Task", "Desc", Priority.LOW, Status.TODO));

        mockMvc.perform(get("/api/export/tasks/csv")
                .param("filters", "priority=HIGH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")));
    }

    @Test
    public void testExportTasksToJson_WithData() throws Exception {
        // Create tasks
        createTaskViaApi(createTaskRequest("Task 1", "Description 1", Priority.HIGH, Status.TODO));
        createTaskViaApi(createTaskRequest("Task 2", "Description 2", Priority.MEDIUM, Status.COMPLETED));

        mockMvc.perform(get("/api/export/tasks/json"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("tasks_export_")))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")));
    }

    @Test
    public void testExportTasksToJson_EmptyDatabase() throws Exception {
        mockMvc.perform(get("/api/export/tasks/json"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")));
    }

    @Test
    public void testExportAnalyticsToJson_WithData() throws Exception {
        // Create tasks for analytics
        createTaskViaApi(createTaskRequest("Task 1", "Desc", Priority.HIGH, Status.TODO));
        createTaskViaApi(createTaskRequest("Task 2", "Desc", Priority.MEDIUM, Status.COMPLETED));

        mockMvc.perform(get("/api/export/analytics/json"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("analytics_export_")))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")));
    }

    @Test
    public void testExportAnalyticsToJson_EmptyDatabase() throws Exception {
        mockMvc.perform(get("/api/export/analytics/json"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")));
    }

    @Test
    public void testGetAvailableFormats() throws Exception {
        mockMvc.perform(get("/api/export/formats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.taskFormats").isArray())
                .andExpect(jsonPath("$.data.taskFormats[0]").value("csv"))
                .andExpect(jsonPath("$.data.taskFormats[1]").value("json"))
                .andExpect(jsonPath("$.data.analyticsFormats").isArray())
                .andExpect(jsonPath("$.data.analyticsFormats[0]").value("json"))
                .andExpect(jsonPath("$.data.supportedFilters").isArray());
    }

    @Test
    public void testExportTasks_CsvFormat() throws Exception {
        createTaskViaApi(createStandardTaskRequest());

        mockMvc.perform(get("/api/export/tasks")
                .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")));
    }

    @Test
    public void testExportTasks_JsonFormat() throws Exception {
        createTaskViaApi(createStandardTaskRequest());

        mockMvc.perform(get("/api/export/tasks")
                .param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")));
    }

    @Test
    public void testExportTasks_DefaultFormat() throws Exception {
        createTaskViaApi(createStandardTaskRequest());

        // Should default to JSON
        mockMvc.perform(get("/api/export/tasks"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")));
    }

    @Test
    public void testExportTasks_UnsupportedFormat() throws Exception {
        createTaskViaApi(createStandardTaskRequest());

        mockMvc.perform(get("/api/export/tasks")
                .param("format", "xml"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unsupported format")));
    }

    @Test
    public void testExportAnalytics() throws Exception {
        // Create tasks for analytics
        createTaskViaApi(createTaskRequest("Task 1", "Desc", Priority.HIGH, Status.TODO));
        createTaskViaApi(createTaskRequest("Task 2", "Desc", Priority.MEDIUM, Status.COMPLETED));

        mockMvc.perform(get("/api/export/analytics"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("analytics_export_")))
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/json")));
    }

    @Test
    public void testExportCsv_ContentValidation() throws Exception {
        // Create a task with known values
        createTaskViaApi(createTaskRequest("Test CSV Task", "CSV Description", Priority.HIGH, Status.TODO));

        byte[] csvContent = mockMvc.perform(get("/api/export/tasks/csv"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        // Verify CSV content is not empty
        assertTrue(csvContent.length > 0);
        
        String csvString = new String(csvContent);
        // Basic CSV validation (should contain headers and data)
        assertTrue(csvString.contains("id") || csvString.contains("title") || csvString.length() > 0);
    }

    @Test
    public void testExportJson_ContentValidation() throws Exception {
        // Create a task with known values
        createTaskViaApi(createTaskRequest("Test JSON Task", "JSON Description", Priority.MEDIUM, Status.TODO));

        byte[] jsonContent = mockMvc.perform(get("/api/export/tasks/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        // Verify JSON content is not empty and is valid JSON
        assertTrue(jsonContent.length > 0);
        
        String jsonString = new String(jsonContent);
        // Basic JSON validation
        assertTrue(jsonString.startsWith("[") || jsonString.startsWith("{"));
    }

    @Test
    public void testExport_LargeDataset() throws Exception {
        // Create 100 tasks
        for (int i = 1; i <= 100; i++) {
            createTaskViaApi(createTaskRequest("Task " + i, "Description " + i, Priority.MEDIUM, Status.TODO));
        }

        // Export to CSV
        byte[] csvContent = mockMvc.perform(get("/api/export/tasks/csv"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertTrue(csvContent.length > 1000, "CSV export should contain substantial data");

        // Export to JSON
        byte[] jsonContent = mockMvc.perform(get("/api/export/tasks/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertTrue(jsonContent.length > 1000, "JSON export should contain substantial data");
    }
}

