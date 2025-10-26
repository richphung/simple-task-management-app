package com.example.taskmanagement.integration;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Phase 2 Security Hardening features.
 * Tests rate limiting, input validation (XSS, SQL injection), and security headers.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SecurityHardeningIntegrationTest extends BaseIntegrationTest {

    // ==================== Security Headers Tests ====================

    @Test
    public void testSecurityHeaders_Present() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    public void testSecurityHeaders_AllEndpoints() throws Exception {
        // Test POST endpoint
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStandardTaskRequest())))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-Content-Type-Options"));
    }

    // ==================== XSS Protection Tests ====================

    @Test
    public void testXssProtection_ScriptTagInTitle() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setTitle("<script>alert('XSS')</script>");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testXssProtection_JavascriptInDescription() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setDescription("javascript:alert('XSS')");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testXssProtection_OnErrorInNotes() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setNotes("<img src=x onerror=alert('XSS')>");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testXssProtection_IframeTag() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setTitle("<iframe src='malicious.com'></iframe>");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testXssProtection_OnClickEvent() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setDescription("<div onclick='malicious()'>Click me</div>");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    // ==================== SQL Injection Protection Tests ====================

    @Test
    public void testSqlInjection_OrStatement() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setTitle("Test' OR '1'='1");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testSqlInjection_UnionSelect() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setDescription("Test'; SELECT * FROM tasks; --");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testSqlInjection_DropTable() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setNotes("Test'; DROP TABLE tasks; --");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testSqlInjection_CommentInjection() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setTitle("Test /* comment */ injection");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testSqlInjection_ExecCommand() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setDescription("Test'; EXEC sp_executesql");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    // ==================== Valid Input Tests (Negative Tests) ====================

    @Test
    public void testValidInput_NoFalsePositives() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setTitle("Valid task title with normal text");
        request.setDescription("This is a description with some 'quotes' and numbers 123");
        request.setNotes("Notes with dashes - and underscores_test");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testValidInput_SpecialCharacters() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setTitle("Task with special chars: @#$%&*()[]{}");
        request.setDescription("Email: test@example.com, Phone: (555) 123-4567");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== Rate Limiting Tests ====================

    @Test
    public void testRateLimiting_NormalUsage() throws Exception {
        // Make a few requests (well under limit)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-Rate-Limit-Remaining"));
        }
    }

    @Test
    public void testRateLimiting_HeadersPresent() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Rate-Limit-Remaining"));
    }

    // Note: Testing actual rate limit exceeded is difficult in integration tests
    // as it would require making 100+ requests, which would slow down tests.
    // This is better tested in dedicated load/stress tests or unit tests.

    // ==================== CORS Tests ====================

    @Test
    public void testCors_OptionsRequest() throws Exception {
        mockMvc.perform(options("/api/tasks")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    public void testCors_ActualRequest() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    // ==================== Combined Attack Tests ====================

    @Test
    public void testCombinedAttack_XssAndSqlInjection() throws Exception {
        TaskRequest request = createStandardTaskRequest();
        request.setTitle("<script>alert('XSS')</script>'; DROP TABLE tasks; --");
        request.setDescription("javascript:alert(document.cookie)");
        request.setNotes("<iframe src='evil.com'></iframe>");

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testUpdate_XssProtection() throws Exception {
        // First create a valid task
        Long taskId = createTaskViaApi(createStandardTaskRequest());

        // Try to update with XSS
        TaskRequest updateRequest = createStandardTaskRequest();
        updateRequest.setTitle("<script>alert('XSS')</script>");

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    public void testBulkOperations_InputValidation() throws Exception {
        TaskRequest request1 = createTaskRequest("Valid task", "Description", Priority.MEDIUM, Status.TODO);
        TaskRequest request2 = createTaskRequest("<script>XSS</script>", "Description", Priority.HIGH, Status.TODO);

        mockMvc.perform(post("/api/tasks/bulk/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Arrays.asList(request1, request2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}




