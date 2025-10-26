package com.example.taskmanagement.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for API rate limiting using Bucket4j.
 * Implements token bucket algorithm for request throttling.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 2.0
 */
@Configuration
public class RateLimitConfig {

    @Value("${app.ratelimit.capacity:100}")
    private long capacity;

    @Value("${app.ratelimit.refill-tokens:100}")
    private long refillTokens;

    @Value("${app.ratelimit.refill-duration-minutes:1}")
    private long refillDuration;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Resolves or creates a bucket for a given key (e.g., IP address).
     *
     * @param key the unique identifier for the bucket
     * @return the bucket for rate limiting
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Creates a new bucket with configured capacity and refill rate.
     *
     * @return a new Bucket instance
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(capacity, 
            Refill.intervally(refillTokens, Duration.ofMinutes(refillDuration)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Gets the configured capacity.
     *
     * @return the bucket capacity
     */
    public long getCapacity() {
        return capacity;
    }

    /**
     * Gets the number of tokens refilled per period.
     *
     * @return the refill tokens
     */
    public long getRefillTokens() {
        return refillTokens;
    }

    /**
     * Gets the refill duration in minutes.
     *
     * @return the refill duration
     */
    public long getRefillDuration() {
        return refillDuration;
    }
}




