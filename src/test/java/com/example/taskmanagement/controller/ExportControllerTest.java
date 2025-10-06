package com.example.taskmanagement.controller;

import com.example.taskmanagement.service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExportController}.
 */
@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

    @Mock
    private ExportService exportService;

    @InjectMocks
    private ExportController exportController;

    private byte[] testCsvData;
    private byte[] testJsonData;

    @BeforeEach
    void setUp() {
        testCsvData = "ID,Title,Description\n1,Test Task,Test Description".getBytes();
        testJsonData = "{\"tasks\":[{\"id\":1,\"title\":\"Test Task\"}]}".getBytes();
    }

    @Test
    void testExportTasksWithCsvFormat() {
        // Given
        when(exportService.exportTasksToCsv(null)).thenReturn(testCsvData);

        // When
        ResponseEntity<byte[]> response = exportController.exportTasks("csv");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCsvData, response.getBody());
        
        HttpHeaders headers = response.getHeaders();
        assertTrue(headers.getContentDisposition().toString().contains("attachment"));
        assertTrue(headers.getContentDisposition().toString().contains(".csv"));
        assertEquals("text/csv;charset=utf-8", headers.getContentType().toString());
        
        verify(exportService).exportTasksToCsv(null);
        verify(exportService, never()).exportTasksToJson();
    }

    @Test
    void testExportTasksWithJsonFormat() {
        // Given
        when(exportService.exportTasksToJson(null)).thenReturn(testJsonData);

        // When
        ResponseEntity<byte[]> response = exportController.exportTasks("json");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testJsonData, response.getBody());
        
        HttpHeaders headers = response.getHeaders();
        assertTrue(headers.getContentDisposition().toString().contains("attachment"));
        assertTrue(headers.getContentDisposition().toString().contains(".json"));
        assertEquals("application/json;charset=utf-8", headers.getContentType().toString());
        
        verify(exportService).exportTasksToJson(null);
        verify(exportService, never()).exportTasksToCsv();
    }

    @Test
    void testExportTasksWithDefaultFormat() {
        // Given
        when(exportService.exportTasksToJson(null)).thenReturn(testJsonData);

        // When
        ResponseEntity<byte[]> response = exportController.exportTasks("json");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testJsonData, response.getBody());
        
        verify(exportService).exportTasksToJson(null);
        verify(exportService, never()).exportTasksToCsv();
    }

    @Test
    void testExportTasksWithUnsupportedFormat() {
        // Given
        String unsupportedFormat = "xml";

        // When
        ResponseEntity<byte[]> response = exportController.exportTasks(unsupportedFormat);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        String errorMessage = new String(response.getBody());
        assertTrue(errorMessage.contains("Unsupported format"));
        assertTrue(errorMessage.contains("csv"));
        assertTrue(errorMessage.contains("json"));
        
        verify(exportService, never()).exportTasksToCsv();
        verify(exportService, never()).exportTasksToJson();
    }

    @Test
    void testExportTasksWithCaseInsensitiveFormat() {
        // Given
        when(exportService.exportTasksToCsv(null)).thenReturn(testCsvData);

        // When
        ResponseEntity<byte[]> response = exportController.exportTasks("CSV");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCsvData, response.getBody());
        
        verify(exportService).exportTasksToCsv(null);
    }

    @Test
    void testExportAnalytics() {
        // Given
        when(exportService.exportAnalyticsToJson()).thenReturn(testJsonData);

        // When
        ResponseEntity<byte[]> response = exportController.exportAnalytics();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testJsonData, response.getBody());
        
        HttpHeaders headers = response.getHeaders();
        assertTrue(headers.getContentDisposition().toString().contains("attachment"));
        assertTrue(headers.getContentDisposition().toString().contains(".json"));
        assertEquals("application/json;charset=utf-8", headers.getContentType().toString());
        
        verify(exportService).exportAnalyticsToJson();
    }

    @Test
    void testGetAvailableFormats() {
        // When
        ResponseEntity<java.util.Map<String, String[]>> response = exportController.getAvailableFormats();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        java.util.Map<String, String[]> formats = response.getBody();
        assertNotNull(formats);
        assertTrue(formats.containsKey("taskFormats"));
        assertTrue(formats.containsKey("analyticsFormats"));
        assertTrue(formats.containsKey("supportedFilters"));
        
        String[] taskFormats = formats.get("taskFormats");
        assertEquals(2, taskFormats.length);
        assertTrue(java.util.Arrays.asList(taskFormats).contains("csv"));
        assertTrue(java.util.Arrays.asList(taskFormats).contains("json"));
        
        String[] analyticsFormats = formats.get("analyticsFormats");
        assertEquals(1, analyticsFormats.length);
        assertTrue(java.util.Arrays.asList(analyticsFormats).contains("json"));
    }
}
