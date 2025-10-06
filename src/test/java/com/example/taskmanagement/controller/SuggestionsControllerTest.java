package com.example.taskmanagement.controller;

import com.example.taskmanagement.service.SmartSuggestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SuggestionsController}.
 */
@ExtendWith(MockitoExtension.class)
class SuggestionsControllerTest {

    @Mock
    private SmartSuggestionService suggestionService;

    @InjectMocks
    private SuggestionsController suggestionsController;

    private SmartSuggestionService.TaskSuggestion testSuggestion1;
    private SmartSuggestionService.TaskSuggestion testSuggestion2;
    private List<SmartSuggestionService.TaskSuggestion> testSuggestions;

    @BeforeEach
    void setUp() {
        testSuggestion1 = new SmartSuggestionService.TaskSuggestion();
        testSuggestion1.setSuggestedTitle("Review code changes");
        testSuggestion1.setSuggestedDescription("Review pull request for quality");
        testSuggestion1.setConfidenceScore(0.8);

        testSuggestion2 = new SmartSuggestionService.TaskSuggestion();
        testSuggestion2.setSuggestedTitle("Write documentation");
        testSuggestion2.setSuggestedDescription("Create API documentation");
        testSuggestion2.setConfidenceScore(0.6);

        testSuggestions = Arrays.asList(testSuggestion1, testSuggestion2);
    }

    @Test
    void testGetTaskSuggestionsWithValidTitle() {
        // Given
        String title = "code review";
        when(suggestionService.generateSuggestions(title)).thenReturn(testSuggestions);

        // When
        ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> response = 
                suggestionsController.getTaskSuggestions(title);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(testSuggestions, response.getBody());
        
        verify(suggestionService).generateSuggestions(title);
    }

    @Test
    void testGetTaskSuggestionsWithEmptySuggestions() {
        // Given
        String title = "unique task";
        when(suggestionService.generateSuggestions(title)).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> response = 
                suggestionsController.getTaskSuggestions(title);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(suggestionService).generateSuggestions(title);
    }

    @Test
    void testGetTaskSuggestionsWithNullTitle() {
        // Given
        when(suggestionService.generateSuggestions(null)).thenReturn(testSuggestions);

        // When
        ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> response = 
                suggestionsController.getTaskSuggestions(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        verify(suggestionService).generateSuggestions(null);
    }

    @Test
    void testGetTaskSuggestionsWithEmptyTitle() {
        // Given
        when(suggestionService.generateSuggestions("")).thenReturn(testSuggestions);

        // When
        ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> response = 
                suggestionsController.getTaskSuggestions("");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        verify(suggestionService).generateSuggestions("");
    }

    @Test
    void testGetTaskSuggestionsWithSpecialCharacters() {
        // Given
        String title = "code-review & testing!";
        when(suggestionService.generateSuggestions(title)).thenReturn(testSuggestions);

        // When
        ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> response = 
                suggestionsController.getTaskSuggestions(title);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        verify(suggestionService).generateSuggestions(title);
    }

    @Test
    void testGetDefaultSuggestions() {
        // Given
        when(suggestionService.getDefaultSuggestions()).thenReturn(testSuggestions);

        // When
        ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> response = 
                suggestionsController.getDefaultSuggestions();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(testSuggestions, response.getBody());
        
        verify(suggestionService).getDefaultSuggestions();
    }

    @Test
    void testGetDefaultSuggestionsWithEmptyList() {
        // Given
        when(suggestionService.getDefaultSuggestions()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> response = 
                suggestionsController.getDefaultSuggestions();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        
        verify(suggestionService).getDefaultSuggestions();
    }

    @Test
    void testGetTaskSuggestionsWithLongTitle() {
        // Given
        String longTitle = "This is a very long task title that should still work with the suggestion service";
        when(suggestionService.generateSuggestions(longTitle)).thenReturn(testSuggestions);

        // When
        ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> response = 
                suggestionsController.getTaskSuggestions(longTitle);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        verify(suggestionService).generateSuggestions(longTitle);
    }

    @Test
    void testGetTaskSuggestionsWithNumbersInTitle() {
        // Given
        String titleWithNumbers = "Task 123 for project 456";
        when(suggestionService.generateSuggestions(titleWithNumbers)).thenReturn(testSuggestions);

        // When
        ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> response = 
                suggestionsController.getTaskSuggestions(titleWithNumbers);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        verify(suggestionService).generateSuggestions(titleWithNumbers);
    }
}
