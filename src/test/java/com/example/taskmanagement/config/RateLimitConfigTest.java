package com.example.taskmanagement.config;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitConfig.
 * 
 * @author Task Management Team
 * @version 1.0
 */
class RateLimitConfigTest {

    private RateLimitConfig rateLimitConfig;

    @BeforeEach
    void setUp() {
        rateLimitConfig = new RateLimitConfig();
        // Set test values using reflection
        ReflectionTestUtils.setField(rateLimitConfig, "capacity", 100L);
        ReflectionTestUtils.setField(rateLimitConfig, "refillTokens", 100L);
        ReflectionTestUtils.setField(rateLimitConfig, "refillDuration", 1L);
    }

    @Test
    @DisplayName("Should create new bucket for new key")
    void shouldCreateNewBucketForNewKey() {
        Bucket bucket = rateLimitConfig.resolveBucket("test-client-1");
        assertNotNull(bucket);
    }

    @Test
    @DisplayName("Should return same bucket for same key")
    void shouldReturnSameBucketForSameKey() {
        String key = "test-client-2";
        Bucket bucket1 = rateLimitConfig.resolveBucket(key);
        Bucket bucket2 = rateLimitConfig.resolveBucket(key);
        
        assertSame(bucket1, bucket2);
    }

    @Test
    @DisplayName("Should create different buckets for different keys")
    void shouldCreateDifferentBucketsForDifferentKeys() {
        Bucket bucket1 = rateLimitConfig.resolveBucket("client-1");
        Bucket bucket2 = rateLimitConfig.resolveBucket("client-2");
        
        assertNotSame(bucket1, bucket2);
    }

    @Test
    @DisplayName("Should consume tokens from bucket")
    void shouldConsumeTokensFromBucket() {
        Bucket bucket = rateLimitConfig.resolveBucket("test-client-3");
        
        // Try to consume 1 token
        boolean consumed = bucket.tryConsume(1);
        assertTrue(consumed);
        
        // Available tokens should be less than capacity
        long availableTokens = bucket.getAvailableTokens();
        assertTrue(availableTokens < 100);
    }

    @Test
    @DisplayName("Should reject when capacity exceeded")
    void shouldRejectWhenCapacityExceeded() {
        Bucket bucket = rateLimitConfig.resolveBucket("test-client-4");
        
        // Consume all tokens
        boolean consumed = bucket.tryConsume(100);
        assertTrue(consumed);
        
        // Try to consume more
        boolean rejectedConsumption = bucket.tryConsume(1);
        assertFalse(rejectedConsumption);
    }

    @Test
    @DisplayName("Should return configured capacity")
    void shouldReturnConfiguredCapacity() {
        assertEquals(100L, rateLimitConfig.getCapacity());
    }

    @Test
    @DisplayName("Should return configured refill tokens")
    void shouldReturnConfiguredRefillTokens() {
        assertEquals(100L, rateLimitConfig.getRefillTokens());
    }

    @Test
    @DisplayName("Should return configured refill duration")
    void shouldReturnConfiguredRefillDuration() {
        assertEquals(1L, rateLimitConfig.getRefillDuration());
    }

    @Test
    @DisplayName("Should handle multiple concurrent clients")
    void shouldHandleMultipleConcurrentClients() {
        Bucket bucket1 = rateLimitConfig.resolveBucket("client-1");
        Bucket bucket2 = rateLimitConfig.resolveBucket("client-2");
        Bucket bucket3 = rateLimitConfig.resolveBucket("client-3");
        
        // Each bucket should be independent
        bucket1.tryConsume(50);
        bucket2.tryConsume(75);
        bucket3.tryConsume(25);
        
        assertEquals(50, bucket1.getAvailableTokens());
        assertEquals(25, bucket2.getAvailableTokens());
        assertEquals(75, bucket3.getAvailableTokens());
    }

    @Test
    @DisplayName("Should cache buckets for performance")
    void shouldCacheBucketsForPerformance() {
        String key = "cached-client";
        
        // First call creates bucket
        Bucket bucket1 = rateLimitConfig.resolveBucket(key);
        bucket1.tryConsume(10);
        
        // Second call should return cached bucket with state
        Bucket bucket2 = rateLimitConfig.resolveBucket(key);
        
        // Should have consumed tokens
        assertEquals(90, bucket2.getAvailableTokens());
        assertSame(bucket1, bucket2);
    }
}




