package com.example.taskmanagement.controller;

import com.example.taskmanagement.service.SmartSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for smart suggestions functionality.
 * Provides endpoints to get intelligent task suggestions based on patterns.
 */
@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(origins = "*")
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "REC_CATCH_EXCEPTION"}) // Intentional generic exception handling for REST controller
public class SuggestionsController {

    @Autowired
    private SmartSuggestionService smartSuggestionService;

    /**
     * Get smart suggestions for a task based on its title.
     *
     * @param title the task title
     * @return map of suggestions including priority, due date, status, etc.
     */
    @GetMapping("/task")
    public ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> getTaskSuggestions(@RequestParam String title) {
        try {
            List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(title);
            if (suggestions.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get smart suggestions for multiple tasks.
     *
     * @param titles array of task titles
     * @return map of suggestions for each title
     */
    @PostMapping("/tasks")
    public ResponseEntity<Map<String, List<SmartSuggestionService.TaskSuggestion>>> getMultipleTaskSuggestions(
            @RequestBody String[] titles) {
        try {
            Map<String, List<SmartSuggestionService.TaskSuggestion>> allSuggestions = new java.util.HashMap<>();
            
            for (String title : titles) {
                List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(title);
                allSuggestions.put(title, suggestions);
            }
            
            return ResponseEntity.ok(allSuggestions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get default task suggestions.
     *
     * @return list of default suggestions
     */
    @GetMapping("/default")
    public ResponseEntity<List<SmartSuggestionService.TaskSuggestion>> getDefaultSuggestions() {
        try {
            List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.getDefaultSuggestions();
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
