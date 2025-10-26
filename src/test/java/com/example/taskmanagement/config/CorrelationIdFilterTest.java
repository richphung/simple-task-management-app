package com.example.taskmanagement.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CorrelationIdFilter.
 * 
 * @author Task Management Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    private CorrelationIdFilter correlationIdFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        correlationIdFilter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        
        // Clean up MDC before each test
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate correlation ID when not provided in request")
    void shouldGenerateCorrelationIdWhenNotProvided() throws ServletException, IOException {
        // Act
        correlationIdFilter.doFilter(request, response, filterChain);

        // Assert
        String correlationId = response.getHeader("X-Correlation-ID");
        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());
        
        // Should be a valid UUID format
        assertTrue(correlationId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    @DisplayName("Should use correlation ID from request header when provided")
    void shouldUseCorrelationIdFromRequest() throws ServletException, IOException {
        // Arrange
        String providedCorrelationId = "test-correlation-123";
        request.addHeader("X-Correlation-ID", providedCorrelationId);

        // Act
        correlationIdFilter.doFilter(request, response, filterChain);

        // Assert
        String responseCorrelationId = response.getHeader("X-Correlation-ID");
        assertEquals(providedCorrelationId, responseCorrelationId);
    }

    @Test
    @DisplayName("Should add correlation ID to response headers")
    void shouldAddCorrelationIdToResponseHeaders() throws ServletException, IOException {
        // Act
        correlationIdFilter.doFilter(request, response, filterChain);

        // Assert
        assertNotNull(response.getHeader("X-Correlation-ID"));
    }

    @Test
    @DisplayName("Should clean up MDC after request processing")
    void shouldCleanUpMdcAfterRequest() throws ServletException, IOException {
        // Act
        correlationIdFilter.doFilter(request, response, filterChain);

        // Assert
        assertNull(MDC.get("correlationId"));
    }

    @Test
    @DisplayName("Should generate different correlation IDs for different requests")
    void shouldGenerateDifferentCorrelationIds() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        MockFilterChain filterChain2 = new MockFilterChain();

        // Act
        correlationIdFilter.doFilter(request, response, filterChain);
        correlationIdFilter.doFilter(request2, response2, filterChain2);

        // Assert
        String correlationId1 = response.getHeader("X-Correlation-ID");
        String correlationId2 = response2.getHeader("X-Correlation-ID");
        
        assertNotNull(correlationId1);
        assertNotNull(correlationId2);
        assertNotEquals(correlationId1, correlationId2);
    }

    @Test
    @DisplayName("Should handle empty correlation ID header")
    void shouldHandleEmptyCorrelationIdHeader() throws ServletException, IOException {
        // Arrange
        request.addHeader("X-Correlation-ID", "");

        // Act
        correlationIdFilter.doFilter(request, response, filterChain);

        // Assert
        String correlationId = response.getHeader("X-Correlation-ID");
        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());
    }

    @Test
    @DisplayName("Should handle null correlation ID header")
    void shouldHandleNullCorrelationIdHeader() throws ServletException, IOException {
        // Act (no header added, will be null)
        correlationIdFilter.doFilter(request, response, filterChain);

        // Assert
        String correlationId = response.getHeader("X-Correlation-ID");
        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());
    }

    @Test
    @DisplayName("Should call filter chain")
    void shouldCallFilterChain() throws ServletException, IOException {
        // Arrange
        final boolean[] chainCalled = {false};
        MockFilterChain customChain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) {
                chainCalled[0] = true;
            }
        };

        // Act
        correlationIdFilter.doFilter(request, response, customChain);

        // Assert
        assertTrue(chainCalled[0]);
    }

    @Test
    @DisplayName("Should return current correlation ID from MDC")
    void shouldReturnCurrentCorrelationIdFromMdc() {
        // Arrange
        String testCorrelationId = "test-123";
        MDC.put("correlationId", testCorrelationId);

        try {
            // Act
            String result = CorrelationIdFilter.getCurrentCorrelationId();

            // Assert
            assertEquals(testCorrelationId, result);
        } finally {
            // Cleanup
            MDC.clear();
        }
    }

    @Test
    @DisplayName("Should return null when no correlation ID in MDC")
    void shouldReturnNullWhenNoCorrelationIdInMdc() {
        // Arrange
        MDC.clear();

        // Act
        String result = CorrelationIdFilter.getCurrentCorrelationId();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should preserve correlation ID through multiple filters")
    void shouldPreserveCorrelationIdThroughFilters() throws ServletException, IOException {
        // Arrange
        String providedCorrelationId = "persistent-123";
        request.addHeader("X-Correlation-ID", providedCorrelationId);

        final String[] capturedCorrelationId = {null};
        MockFilterChain customChain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) {
                capturedCorrelationId[0] = MDC.get("correlationId");
            }
        };

        // Act
        correlationIdFilter.doFilter(request, response, customChain);

        // Assert
        assertEquals(providedCorrelationId, capturedCorrelationId[0]);
        assertEquals(providedCorrelationId, response.getHeader("X-Correlation-ID"));
    }
}




