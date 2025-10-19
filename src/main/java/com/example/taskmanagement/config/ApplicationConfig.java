package com.example.taskmanagement.config;

import com.example.taskmanagement.constants.TaskConstants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Centralized application configuration.
 * Consolidates all application-specific settings and properties.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Application properties configuration.
     * Maps application.properties values to a configuration class.
     */
    @Bean
    @ConfigurationProperties(prefix = "app")
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }

    /**
     * Method validation post processor for validation annotations.
     * Enables method-level validation in controllers and services.
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    /**
     * Development profile configuration.
     * Optimized settings for development environment.
     */
    @Configuration
    @Profile("dev")
    static class DevConfig {
        // Development-specific configuration can be added here
    }

    /**
     * Production profile configuration.
     * Optimized settings for production environment.
     */
    @Configuration
    @Profile("prod")
    static class ProdConfig {
        // Production-specific configuration can be added here
    }

    /**
     * Test profile configuration.
     * Optimized settings for testing environment.
     */
    @Configuration
    @Profile("test")
    static class TestConfig {
        // Test-specific configuration can be added here
    }

    /**
     * Application properties class.
     * Contains all configurable application properties.
     */
    public static class ApplicationProperties {
        
        private String name = TaskConstants.APP_NAME;
        private String version = TaskConstants.APP_VERSION;
        private String description = TaskConstants.APP_DESCRIPTION;
        
        // Cache settings
        private CacheSettings cache = new CacheSettings();
        
        // Pagination settings
        private PaginationSettings pagination = new PaginationSettings();
        
        // Validation settings
        private ValidationSettings validation = new ValidationSettings();

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Spring ConfigurationProperties pattern - framework uses these for property injection")
        public CacheSettings getCache() { return cache; }
        @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring ConfigurationProperties pattern - framework uses these for property injection")
        public void setCache(CacheSettings cache) { this.cache = cache; }
        
        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Spring ConfigurationProperties pattern - framework uses these for property injection")
        public PaginationSettings getPagination() { return pagination; }
        @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring ConfigurationProperties pattern - framework uses these for property injection")
        public void setPagination(PaginationSettings pagination) { this.pagination = pagination; }
        
        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Spring ConfigurationProperties pattern - framework uses these for property injection")
        public ValidationSettings getValidation() { return validation; }
        @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring ConfigurationProperties pattern - framework uses these for property injection")
        public void setValidation(ValidationSettings validation) { this.validation = validation; }

        /**
         * Cache configuration settings.
         */
        public static class CacheSettings {
            private int maxSize = 1000;
            private int expireAfterWriteMinutes = 10;
            private int expireAfterAccessMinutes = 5;
            private boolean recordStats = true;

            // Getters and setters
            public int getMaxSize() { return maxSize; }
            public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
            
            public int getExpireAfterWriteMinutes() { return expireAfterWriteMinutes; }
            public void setExpireAfterWriteMinutes(int expireAfterWriteMinutes) { 
                this.expireAfterWriteMinutes = expireAfterWriteMinutes; 
            }
            
            public int getExpireAfterAccessMinutes() { return expireAfterAccessMinutes; }
            public void setExpireAfterAccessMinutes(int expireAfterAccessMinutes) { 
                this.expireAfterAccessMinutes = expireAfterAccessMinutes; 
            }
            
            public boolean isRecordStats() { return recordStats; }
            public void setRecordStats(boolean recordStats) { this.recordStats = recordStats; }
        }

        /**
         * Pagination configuration settings.
         */
        public static class PaginationSettings {
            private int defaultPageSize = 10;
            private int maxPageSize = 100;
            private String defaultSortBy = TaskConstants.DEFAULT_SORT_FIELD;
            private String defaultSortDirection = TaskConstants.DEFAULT_SORT_DIRECTION;

            // Getters and setters
            public int getDefaultPageSize() { return defaultPageSize; }
            public void setDefaultPageSize(int defaultPageSize) { this.defaultPageSize = defaultPageSize; }
            
            public int getMaxPageSize() { return maxPageSize; }
            public void setMaxPageSize(int maxPageSize) { this.maxPageSize = maxPageSize; }
            
            public String getDefaultSortBy() { return defaultSortBy; }
            public void setDefaultSortBy(String defaultSortBy) { this.defaultSortBy = defaultSortBy; }
            
            public String getDefaultSortDirection() { return defaultSortDirection; }
            public void setDefaultSortDirection(String defaultSortDirection) { 
                this.defaultSortDirection = defaultSortDirection; 
            }
        }

        /**
         * Validation configuration settings.
         */
        public static class ValidationSettings {
            private int maxTitleLength = TaskConstants.MAX_TITLE_LENGTH;
            private int maxDescriptionLength = TaskConstants.MAX_DESCRIPTION_LENGTH;
            private int maxNotesLength = TaskConstants.MAX_NOTES_LENGTH;

            // Getters and setters
            public int getMaxTitleLength() { return maxTitleLength; }
            public void setMaxTitleLength(int maxTitleLength) { this.maxTitleLength = maxTitleLength; }
            
            public int getMaxDescriptionLength() { return maxDescriptionLength; }
            public void setMaxDescriptionLength(int maxDescriptionLength) { 
                this.maxDescriptionLength = maxDescriptionLength; 
            }
            
            public int getMaxNotesLength() { return maxNotesLength; }
            public void setMaxNotesLength(int maxNotesLength) { this.maxNotesLength = maxNotesLength; }
        }
    }
}
