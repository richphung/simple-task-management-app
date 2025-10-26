package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.service.SmartSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for smart suggestions functionality.
 * Provides endpoints to get intelligent task suggestions based on patterns.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Smart Suggestions", description = "AI-powered task suggestions and recommendations. " +
        "Get intelligent property suggestions based on task title patterns and historical data.")
@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(origins = "*")
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "REC_CATCH_EXCEPTION"}) // Intentional generic exception handling for REST controller
public class SuggestionsController extends BaseController {

    @Autowired
    private SmartSuggestionService smartSuggestionService;

    /**
     * Get smart suggestions for a task based on its title.
     *
     * @param title the task title
     * @return map of suggestions including priority, due date, status, etc. wrapped in ApiResponse
     */
    @GetMapping("/task")
    public ResponseEntity<ApiResponse<List<SmartSuggestionService.TaskSuggestion>>> getTaskSuggestions(@RequestParam String title) {
        try {
            List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(title);
            if (suggestions.isEmpty()) {
                return handleSuccess(suggestions);
            }
            return handleSuccess(suggestions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get smart suggestions for multiple tasks.
     *
     * @param titles array of task titles
     * @return map of suggestions for each title wrapped in ApiResponse
     */
    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<Map<String, List<SmartSuggestionService.TaskSuggestion>>>> getMultipleTaskSuggestions(
            @RequestBody String[] titles) {
        try {
            Map<String, List<SmartSuggestionService.TaskSuggestion>> allSuggestions = new java.util.HashMap<>();
            
            for (String title : titles) {
                List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.generateSuggestions(title);
                allSuggestions.put(title, suggestions);
            }
            
            return handleSuccess(allSuggestions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get default task suggestions.
     *
     * @return list of default suggestions wrapped in ApiResponse
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<List<SmartSuggestionService.TaskSuggestion>>> getDefaultSuggestions() {
        try {
            List<SmartSuggestionService.TaskSuggestion> suggestions = smartSuggestionService.getDefaultSuggestions();
            return handleSuccess(suggestions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
