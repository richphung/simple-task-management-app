package com.example.taskmanagement.config;

import com.example.taskmanagement.constants.TaskConstants;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching in the application.
 * Uses Caffeine for high-performance in-memory caching.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure the cache manager with Caffeine.
     *
     * @return configured CacheManager
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure Caffeine cache settings
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)                    // Maximum number of entries
                .expireAfterWrite(10, TimeUnit.MINUTES) // Expire after 10 minutes of write
                .expireAfterAccess(5, TimeUnit.MINUTES) // Expire after 5 minutes of access
                .recordStats()                        // Enable statistics
        );
        
        // Set cache names using constants
        cacheManager.setCacheNames(Arrays.asList(
            TaskConstants.CACHE_TASKS,
            TaskConstants.CACHE_TASK_BY_ID,
            TaskConstants.CACHE_TASK_ANALYTICS,
            TaskConstants.CACHE_SUGGESTIONS
        ));
        
        return cacheManager;
    }

    /**
     * Configure a specific cache for task analytics.
     * This cache has different settings optimized for analytics data.
     *
     * @return CaffeineCacheManager for analytics
     */
    @Bean("analyticsCacheManager")
    public CacheManager analyticsCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)                     // Smaller cache for analytics
                .expireAfterWrite(30, TimeUnit.MINUTES) // Longer expiration for analytics
                .recordStats()
        );
        
        cacheManager.setCacheNames(Arrays.asList(
            TaskConstants.CACHE_TASK_ANALYTICS,
            "productivity-metrics", 
            "trends"
        ));
        
        return cacheManager;
    }

    /**
     * Configure a cache for search results.
     * This cache is optimized for frequently searched data.
     *
     * @return CaffeineCacheManager for search
     */
    @Bean("searchCacheManager")
    public CacheManager searchCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)                     // Medium cache for search results
                .expireAfterWrite(15, TimeUnit.MINUTES) // Moderate expiration
                .expireAfterAccess(10, TimeUnit.MINUTES) // Shorter access expiration
                .recordStats()
        );
        
        cacheManager.setCacheNames(Arrays.asList(
            "search-results", 
            "filtered-tasks", 
            TaskConstants.CACHE_SUGGESTIONS
        ));
        
        return cacheManager;
    }
}
