package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Service for providing smart suggestions based on existing task patterns.
 * Uses machine learning-like algorithms to suggest task properties.
 */
@Service
public class SmartSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(SmartSuggestionService.class);

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Generate smart suggestions for a new task based on title.
     *
     * @param title the task title
     * @return list of task suggestions
     */
    @Cacheable(value = "suggestions-cache", key = "#title")
    public List<TaskSuggestion> generateSuggestions(String title) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Generating suggestions for title: {}", title);
            }

            // Handle null or empty title
            if (title == null || title.trim().isEmpty()) {
                return getDefaultSuggestions();
            }

            List<TaskSuggestion> suggestions = new ArrayList<>();
            
            // Find similar tasks
            List<Task> similarTasks = findSimilarTasks(title);
            
            if (!similarTasks.isEmpty()) {
                for (Task task : similarTasks) {
                    TaskSuggestion suggestion = new TaskSuggestion();
                    suggestion.setSuggestedTitle(task.getTitle());
                    suggestion.setSuggestedDescription(task.getDescription());
                    suggestion.setSuggestedPriority(task.getPriority());
                    suggestion.setSuggestedStatus(task.getStatus());
                    suggestion.setSuggestedDueDate(task.getDueDate());
                    suggestion.setConfidenceScore(calculateConfidence(similarTasks, title));
                    suggestions.add(suggestion);
                }
            } else {
                // No similar tasks found, provide default suggestions
                suggestions.add(createDefaultSuggestion("Review Code", "Review pull requests for quality", Priority.HIGH, Status.TODO, LocalDate.now().plusDays(1)));
                suggestions.add(createDefaultSuggestion("Plan Sprint", "Outline tasks for the next sprint", Priority.MEDIUM, Status.TODO, LocalDate.now().plusDays(7)));
            }
            
            return suggestions;
            
        } catch (Exception e) {
            logger.error("Failed to generate suggestions for title: {}", title, e);
            return getDefaultSuggestions();
        }
    }

    /**
     * Find tasks similar to the given title.
     *
     * @param title the title to find similar tasks for
     * @return list of similar tasks
     */
    private List<Task> findSimilarTasks(String title) {
        // Handle null or empty title
        if (title == null || title.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Simple similarity based on common words
        String[] titleWords = title.toLowerCase(Locale.ENGLISH).split("\\s+");
        
        return taskRepository.findAll().stream()
            .filter(task -> task.getTitle() != null && calculateSimilarity(titleWords, task.getTitle().toLowerCase(Locale.ENGLISH).split("\\s+")) > 0.3)
            .sorted((t1, t2) -> Double.compare(
                calculateSimilarity(titleWords, t2.getTitle().toLowerCase(Locale.ENGLISH).split("\\s+")),
                calculateSimilarity(titleWords, t1.getTitle().toLowerCase(Locale.ENGLISH).split("\\s+"))
            ))
            .limit(5)
            .collect(Collectors.toList());
    }

    /**
     * Calculate similarity between two arrays of words.
     *
     * @param words1 first array of words
     * @param words2 second array of words
     * @return similarity score between 0 and 1
     */
    private double calculateSimilarity(String[] words1, String[] words2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }




    /**
     * Inner class representing a task suggestion.
     */
    public static class TaskSuggestion {
        private String suggestedTitle;
        private String suggestedDescription;
        private Priority suggestedPriority;
        private Status suggestedStatus;
        private LocalDate suggestedDueDate;
        private double confidenceScore;

        // Getters and Setters
        public String getSuggestedTitle() {
            return suggestedTitle;
        }

        public void setSuggestedTitle(String suggestedTitle) {
            this.suggestedTitle = suggestedTitle;
        }

        public String getSuggestedDescription() {
            return suggestedDescription;
        }

        public void setSuggestedDescription(String suggestedDescription) {
            this.suggestedDescription = suggestedDescription;
        }

        public Priority getSuggestedPriority() {
            return suggestedPriority;
        }

        public void setSuggestedPriority(Priority suggestedPriority) {
            this.suggestedPriority = suggestedPriority;
        }

        public Status getSuggestedStatus() {
            return suggestedStatus;
        }

        public void setSuggestedStatus(Status suggestedStatus) {
            this.suggestedStatus = suggestedStatus;
        }

        public LocalDate getSuggestedDueDate() {
            return suggestedDueDate;
        }

        public void setSuggestedDueDate(LocalDate suggestedDueDate) {
            this.suggestedDueDate = suggestedDueDate;
        }

        public double getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
        }
    }

    /**
     * Calculate confidence score for suggestions.
     *
     * @param similarTasks list of similar tasks
     * @param title the task title
     * @return confidence score between 0 and 1
     */
    private double calculateConfidence(List<Task> similarTasks, String title) {
        if (similarTasks.isEmpty()) {
            return 0.0;
        }
        
        // Calculate average similarity
        double averageSimilarity = similarTasks.stream()
            .mapToDouble(task -> calculateSimilarity(
                title.toLowerCase(Locale.ENGLISH).split("\\s+"),
                task.getTitle().toLowerCase(Locale.ENGLISH).split("\\s+")
            ))
            .average()
            .orElse(0.0);
        
        // Adjust confidence based on number of similar tasks
        double countFactor = Math.min(similarTasks.size() / 5.0, 1.0);
        
        return averageSimilarity * countFactor;
    }

    /**
     * Get default suggestions when no similar tasks are found.
     *
     * @return list of default suggestions
     */
    public List<TaskSuggestion> getDefaultSuggestions() {
        List<TaskSuggestion> suggestions = new ArrayList<>();
        suggestions.add(createDefaultSuggestion("Review Code", "Review pull requests for quality", Priority.HIGH, Status.TODO, LocalDate.now().plusDays(1)));
        suggestions.add(createDefaultSuggestion("Plan Sprint", "Outline tasks for the next sprint", Priority.MEDIUM, Status.TODO, LocalDate.now().plusDays(7)));
        return suggestions;
    }

    /**
     * Create a default suggestion with the given parameters.
     *
     * @param title the suggested title
     * @param description the suggested description
     * @param priority the suggested priority
     * @param status the suggested status
     * @param dueDate the suggested due date
     * @return the created TaskSuggestion
     */
    private TaskSuggestion createDefaultSuggestion(String title, String description, Priority priority, Status status, LocalDate dueDate) {
        TaskSuggestion suggestion = new TaskSuggestion();
        suggestion.setSuggestedTitle(title);
        suggestion.setSuggestedDescription(description);
        suggestion.setSuggestedPriority(priority);
        suggestion.setSuggestedStatus(status);
        suggestion.setSuggestedDueDate(dueDate);
        suggestion.setConfidenceScore(0.5); // Default confidence for default suggestions
        return suggestion;
    }
}
