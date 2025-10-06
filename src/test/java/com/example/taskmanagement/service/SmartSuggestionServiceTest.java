package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SmartSuggestionService}.
 */
@ExtendWith(MockitoExtension.class)
class SmartSuggestionServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SmartSuggestionService smartSuggestionService;

    private Task testTask1;
    private Task testTask2;
    private Task testTask3;
    private List<Task> testTasks;

    @BeforeEach
    void setUp() {
        testTask1 = new Task();
        testTask1.setId(1L);
        testTask1.setTitle("Review code changes");
        testTask1.setDescription("Review pull request for quality");
        testTask1.setStatus(Status.TODO);
        testTask1.setPriority(Priority.HIGH);
        testTask1.setDueDate(LocalDate.now().plusDays(1));
        testTask1.setCreatedAt(LocalDateTime.now());
        testTask1.setUpdatedAt(LocalDateTime.now());

        testTask2 = new Task();
        testTask2.setId(2L);
        testTask2.setTitle("Write documentation");
        testTask2.setDescription("Create API documentation");
        testTask2.setStatus(Status.IN_PROGRESS);
        testTask2.setPriority(Priority.MEDIUM);
        testTask2.setDueDate(LocalDate.now().plusDays(3));
        testTask2.setCreatedAt(LocalDateTime.now().minusDays(1));
        testTask2.setUpdatedAt(LocalDateTime.now());

        testTask3 = new Task();
        testTask3.setId(3L);
        testTask3.setTitle("Fix bugs");
        testTask3.setDescription("Fix critical bugs in production");
        testTask3.setStatus(Status.COMPLETED);
        testTask3.setPriority(Priority.HIGH);
        testTask3.setDueDate(LocalDate.now().minusDays(1));
        testTask3.setCreatedAt(LocalDateTime.now().minusDays(2));
        testTask3.setUpdatedAt(LocalDateTime.now());
        testTask3.setCompletedAt(LocalDateTime.now());

        testTasks = Arrays.asList(testTask1, testTask2, testTask3);
    }

    @Test
    void testGenerateSuggestionsWithSimilarTitle() {
        // Given
        when(taskRepository.findAll()).thenReturn(testTasks);
        String inputTitle = "code review";

        // When
        List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(inputTitle);

        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // Should find suggestions based on "code" keyword
        boolean foundCodeReviewSuggestion = suggestions.stream()
                .anyMatch(s -> s.getSuggestedTitle().toLowerCase().contains("code"));
        assertTrue(foundCodeReviewSuggestion);
        
        verify(taskRepository).findAll();
    }

    @Test
    void testGenerateSuggestionsWithExactMatch() {
        // Given
        when(taskRepository.findAll()).thenReturn(testTasks);
        String inputTitle = "Review code changes";

        // When
        List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(inputTitle);

        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // Should find exact match with high confidence
        boolean foundExactMatch = suggestions.stream()
                .anyMatch(s -> s.getSuggestedTitle().equals("Review code changes"));
        assertTrue(foundExactMatch);
        
        verify(taskRepository).findAll();
    }

    @Test
    void testGenerateSuggestionsWithNoMatches() {
        // Given
        when(taskRepository.findAll()).thenReturn(testTasks);
        String inputTitle = "completely different task";

        // When
        List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(inputTitle);

        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty()); // Should return default suggestions
        
        // Should contain default suggestions
        boolean foundDefaultSuggestion = suggestions.stream()
                .anyMatch(s -> s.getSuggestedTitle().contains("Review Code") || 
                             s.getSuggestedTitle().contains("Plan Sprint"));
        assertTrue(foundDefaultSuggestion);
        
        verify(taskRepository).findAll();
    }

    @Test
    void testGenerateSuggestionsWithEmptyRepository() {
        // Given
        when(taskRepository.findAll()).thenReturn(Arrays.asList());
        String inputTitle = "any title";

        // When
        List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(inputTitle);

        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty()); // Should return default suggestions
        
        // Should contain default suggestions
        boolean foundDefaultSuggestion = suggestions.stream()
                .anyMatch(s -> s.getSuggestedTitle().contains("Review Code") || 
                             s.getSuggestedTitle().contains("Plan Sprint"));
        assertTrue(foundDefaultSuggestion);
        
        verify(taskRepository).findAll();
    }

    @Test
    void testGenerateSuggestionsWithNullTitle() {
        // When
        List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(null);

        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty()); // Should return default suggestions
        
        verify(taskRepository, never()).findAll();
    }

    @Test
    void testGenerateSuggestionsWithEmptyTitle() {
        // When
        List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions("");

        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty()); // Should return default suggestions
        
        verify(taskRepository, never()).findAll();
    }

    @Test
    void testGenerateSuggestionsWithPartialMatch() {
        // Given
        when(taskRepository.findAll()).thenReturn(testTasks);
        String inputTitle = "code review";

        // When
        List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(inputTitle);

        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // Should find suggestions based on "code" keyword
        boolean foundCodeSuggestion = suggestions.stream()
                .anyMatch(s -> s.getSuggestedTitle().toLowerCase().contains("code"));
        assertTrue(foundCodeSuggestion);
        
        verify(taskRepository).findAll();
    }

    @Test
    void testGenerateSuggestionsConfidenceScores() {
        // Given
        when(taskRepository.findAll()).thenReturn(testTasks);
        String inputTitle = "code review";

        // When
        List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(inputTitle);

        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // All suggestions should have confidence scores between 0 and 1
        for (SmartSuggestionService.TaskSuggestion suggestion : suggestions) {
            assertTrue(suggestion.getConfidenceScore() >= 0.0);
            assertTrue(suggestion.getConfidenceScore() <= 1.0);
        }
        
        // Suggestions should be sorted by confidence score (descending)
        for (int i = 0; i < suggestions.size() - 1; i++) {
            assertTrue(suggestions.get(i).getConfidenceScore() >= suggestions.get(i + 1).getConfidenceScore());
        }
        
        verify(taskRepository).findAll();
    }

    @Test
    void testGenerateSuggestionsWithSpecialCharacters() {
        // Given
        when(taskRepository.findAll()).thenReturn(testTasks);
        String inputTitle = "code-review & testing!";

        // When
        List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(inputTitle);

        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        verify(taskRepository).findAll();
    }
}
