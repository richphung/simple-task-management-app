package com.example.taskmanagement.interceptor;

import com.example.taskmanagement.config.RateLimitConfig;
import com.example.taskmanagement.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitInterceptor.
 * 
 * @author Task Management Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @Mock
    private RateLimitConfig rateLimitConfig;

    @Mock
    private Bucket bucket;

    @Mock
    private ConsumptionProbe consumptionProbe;

    @InjectMocks
    private RateLimitInterceptor rateLimitInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Should allow request when within rate limit")
    void shouldAllowRequestWhenWithinRateLimit() throws Exception {
        // Arrange
        request.setRequestURI("/api/tasks");
        request.setRemoteAddr("192.168.1.1");
        
        when(rateLimitConfig.resolveBucket(anyString())).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(consumptionProbe);
        when(consumptionProbe.isConsumed()).thenReturn(true);
        when(consumptionProbe.getRemainingTokens()).thenReturn(99L);

        // Act
        boolean result = rateLimitInterceptor.preHandle(request, response, null);

        // Assert
        assertTrue(result);
        assertEquals("99", response.getHeader("X-Rate-Limit-Remaining"));
        verify(bucket).tryConsumeAndReturnRemaining(1);
    }

    @Test
    @DisplayName("Should reject request when rate limit exceeded")
    void shouldRejectRequestWhenRateLimitExceeded() {
        // Arrange
        request.setRequestURI("/api/tasks");
        request.setRemoteAddr("192.168.1.1");
        
        when(rateLimitConfig.resolveBucket(anyString())).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(consumptionProbe);
        when(consumptionProbe.isConsumed()).thenReturn(false);
        when(consumptionProbe.getNanosToWaitForRefill()).thenReturn(60_000_000_000L); // 60 seconds

        // Act & Assert
        RateLimitExceededException exception = assertThrows(
            RateLimitExceededException.class,
            () -> rateLimitInterceptor.preHandle(request, response, null)
        );

        assertTrue(exception.getMessage().contains("Too many requests"));
        assertTrue(exception.getMessage().contains("60 seconds"));
        assertEquals("60", response.getHeader("X-Rate-Limit-Retry-After-Seconds"));
    }

    @Test
    @DisplayName("Should skip rate limiting for actuator endpoints")
    void shouldSkipRateLimitingForActuator() throws Exception {
        // Arrange
        request.setRequestURI("/actuator/health");

        // Act
        boolean result = rateLimitInterceptor.preHandle(request, response, null);

        // Assert
        assertTrue(result);
        verify(rateLimitConfig, never()).resolveBucket(anyString());
        verify(bucket, never()).tryConsumeAndReturnRemaining(anyInt());
    }

    @Test
    @DisplayName("Should use X-Forwarded-For header when present")
    void shouldUseXForwardedForHeader() throws Exception {
        // Arrange
        request.setRequestURI("/api/tasks");
        request.addHeader("X-Forwarded-For", "203.0.113.1, 198.51.100.1");
        request.setRemoteAddr("192.168.1.1");
        
        when(rateLimitConfig.resolveBucket("203.0.113.1")).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(consumptionProbe);
        when(consumptionProbe.isConsumed()).thenReturn(true);
        when(consumptionProbe.getRemainingTokens()).thenReturn(99L);

        // Act
        rateLimitInterceptor.preHandle(request, response, null);

        // Assert
        verify(rateLimitConfig).resolveBucket("203.0.113.1");
    }

    @Test
    @DisplayName("Should use remote address when X-Forwarded-For not present")
    void shouldUseRemoteAddressWhenNoXForwardedFor() throws Exception {
        // Arrange
        request.setRequestURI("/api/tasks");
        request.setRemoteAddr("192.168.1.1");
        
        when(rateLimitConfig.resolveBucket("192.168.1.1")).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(consumptionProbe);
        when(consumptionProbe.isConsumed()).thenReturn(true);
        when(consumptionProbe.getRemainingTokens()).thenReturn(99L);

        // Act
        rateLimitInterceptor.preHandle(request, response, null);

        // Assert
        verify(rateLimitConfig).resolveBucket("192.168.1.1");
    }

    @Test
    @DisplayName("Should handle empty X-Forwarded-For header")
    void shouldHandleEmptyXForwardedFor() throws Exception {
        // Arrange
        request.setRequestURI("/api/tasks");
        request.addHeader("X-Forwarded-For", "");
        request.setRemoteAddr("192.168.1.1");
        
        when(rateLimitConfig.resolveBucket("192.168.1.1")).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(consumptionProbe);
        when(consumptionProbe.isConsumed()).thenReturn(true);
        when(consumptionProbe.getRemainingTokens()).thenReturn(99L);

        // Act
        rateLimitInterceptor.preHandle(request, response, null);

        // Assert
        verify(rateLimitConfig).resolveBucket("192.168.1.1");
    }

    @Test
    @DisplayName("Should set retry-after header when rate limit exceeded")
    void shouldSetRetryAfterHeader() {
        // Arrange
        request.setRequestURI("/api/tasks");
        request.setRemoteAddr("192.168.1.1");
        
        when(rateLimitConfig.resolveBucket(anyString())).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(consumptionProbe);
        when(consumptionProbe.isConsumed()).thenReturn(false);
        when(consumptionProbe.getNanosToWaitForRefill()).thenReturn(30_000_000_000L); // 30 seconds

        // Act & Assert
        assertThrows(
            RateLimitExceededException.class,
            () -> rateLimitInterceptor.preHandle(request, response, null)
        );

        assertEquals("30", response.getHeader("X-Rate-Limit-Retry-After-Seconds"));
    }

    @Test
    @DisplayName("Should allow requests for different endpoints")
    void shouldAllowRequestsForDifferentEndpoints() throws Exception {
        // Arrange
        request.setRequestURI("/api/tasks/123");
        request.setRemoteAddr("192.168.1.1");
        
        when(rateLimitConfig.resolveBucket(anyString())).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(consumptionProbe);
        when(consumptionProbe.isConsumed()).thenReturn(true);
        when(consumptionProbe.getRemainingTokens()).thenReturn(99L);

        // Act
        boolean result = rateLimitInterceptor.preHandle(request, response, null);

        // Assert
        assertTrue(result);
    }
}




