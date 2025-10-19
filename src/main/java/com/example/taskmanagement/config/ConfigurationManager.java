package com.example.taskmanagement.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;

/**
 * Centralized configuration management utility.
 * Provides access to application configuration and cache management.
 */
@Component
public class ConfigurationManager {

    private final ApplicationConfig.ApplicationProperties applicationProperties;
    private final CacheManager cacheManager;

    @Autowired
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring DI pattern - framework manages object lifecycle")
    public ConfigurationManager(ApplicationConfig.ApplicationProperties applicationProperties,
                               CacheManager cacheManager) {
        this.applicationProperties = applicationProperties;
        this.cacheManager = cacheManager;
    }

    /**
     * Get application name.
     */
    public String getApplicationName() {
        return applicationProperties.getName();
    }

    /**
     * Get application version.
     */
    public String getApplicationVersion() {
        return applicationProperties.getVersion();
    }

    /**
     * Get application description.
     */
    public String getApplicationDescription() {
        return applicationProperties.getDescription();
    }

    /**
     * Get cache configuration.
     */
    public ApplicationConfig.ApplicationProperties.CacheSettings getCacheSettings() {
        return applicationProperties.getCache();
    }

    /**
     * Get pagination configuration.
     */
    public ApplicationConfig.ApplicationProperties.PaginationSettings getPaginationSettings() {
        return applicationProperties.getPagination();
    }

    /**
     * Get validation configuration.
     */
    public ApplicationConfig.ApplicationProperties.ValidationSettings getValidationSettings() {
        return applicationProperties.getValidation();
    }

    /**
     * Clear all caches.
     */
    public void clearAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    /**
     * Clear specific cache by name.
     */
    public void clearCache(String cacheName) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Get cache statistics for monitoring.
     */
    public String getCacheStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("Cache Statistics:\n");
        
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            stats.append("Cache: ").append(cacheName).append("\n");
            // Add more detailed statistics if needed
        }
        
        return stats.toString();
    }

    /**
     * Check if cache is enabled.
     */
    public boolean isCacheEnabled() {
        return cacheManager != null;
    }

    /**
     * Get default page size from configuration.
     */
    public int getDefaultPageSize() {
        return getPaginationSettings().getDefaultPageSize();
    }

    /**
     * Get maximum page size from configuration.
     */
    public int getMaxPageSize() {
        return getPaginationSettings().getMaxPageSize();
    }

    /**
     * Get default sort field from configuration.
     */
    public String getDefaultSortBy() {
        return getPaginationSettings().getDefaultSortBy();
    }

    /**
     * Get default sort direction from configuration.
     */
    public String getDefaultSortDirection() {
        return getPaginationSettings().getDefaultSortDirection();
    }

    /**
     * Get maximum title length from configuration.
     */
    public int getMaxTitleLength() {
        return getValidationSettings().getMaxTitleLength();
    }

    /**
     * Get maximum description length from configuration.
     */
    public int getMaxDescriptionLength() {
        return getValidationSettings().getMaxDescriptionLength();
    }

    /**
     * Get maximum notes length from configuration.
     */
    public int getMaxNotesLength() {
        return getValidationSettings().getMaxNotesLength();
    }

    /**
     * Validate page size against configuration limits.
     */
    public int validatePageSize(int pageSize) {
        if (pageSize <= 0) {
            return getDefaultPageSize();
        }
        return Math.min(pageSize, getMaxPageSize());
    }

    /**
     * Validate sort field against allowed values.
     */
    public String validateSortField(String sortField) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return getDefaultSortBy();
        }
        
        // Add validation logic for allowed sort fields
        String[] allowedFields = {"id", "title", "priority", "status", "dueDate", "createdAt", "updatedAt"};
        for (String allowedField : allowedFields) {
            if (allowedField.equalsIgnoreCase(sortField)) {
                return allowedField;
            }
        }
        
        return getDefaultSortBy();
    }

    /**
     * Validate sort direction.
     */
    public String validateSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return getDefaultSortDirection();
        }
        
        if ("ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection)) {
            return sortDirection.toUpperCase(Locale.ROOT);
        }
        
        return getDefaultSortDirection();
    }
}
