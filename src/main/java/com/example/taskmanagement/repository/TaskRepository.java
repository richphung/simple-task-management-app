package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Task entity operations.
 * 
 * <p>This interface extends JpaRepository to provide basic CRUD operations
 * and defines custom query methods for task-specific operations including
 * search, filtering, and analytics.</p>
 * 
 * @author Task Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Finds tasks by status with pagination.
     * 
     * @param status the task status
     * @param pageable pagination information
     * @return page of tasks with the specified status
     */
    Page<Task> findByStatus(Status status, Pageable pageable);

    /**
     * Finds tasks by priority with pagination.
     * 
     * @param priority the task priority
     * @param pageable pagination information
     * @return page of tasks with the specified priority
     */
    Page<Task> findByPriority(Priority priority, Pageable pageable);

    /**
     * Finds tasks by status and priority with pagination.
     * 
     * @param status the task status
     * @param priority the task priority
     * @param pageable pagination information
     * @return page of tasks with the specified status and priority
     */
    Page<Task> findByStatusAndPriority(Status status, Priority priority, Pageable pageable);

    /**
     * Finds overdue tasks.
     * 
     * @param currentDate the current date
     * @return list of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate);

    /**
     * Finds tasks due today.
     * 
     * @param today the current date
     * @return list of tasks due today
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate = :today " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findTasksDueToday(@Param("today") LocalDate today);

    /**
     * Searches tasks by title or description containing the given text.
     * 
     * @param searchText the text to search for
     * @param pageable pagination information
     * @return page of tasks matching the search criteria
     */
    @Query("SELECT t FROM Task t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<Task> searchTasks(@Param("searchText") String searchText, Pageable pageable);

    /**
     * Counts tasks by status.
     * 
     * @param status the task status
     * @return count of tasks with the specified status
     */
    long countByStatus(Status status);

    /**
     * Counts tasks by priority.
     * 
     * @param priority the task priority
     * @return count of tasks with the specified priority
     */
    long countByPriority(Priority priority);

    /**
     * Finds tasks created between the specified dates.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of tasks created between the specified dates
     */
    @Query("SELECT t FROM Task t WHERE t.createdAt >= :startDate AND t.createdAt < :endDate")
    Page<Task> findTasksCreatedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    // ==================== OPTIMIZED REPOSITORY METHODS ====================

    /**
     * Finds tasks by multiple statuses with pagination.
     * More efficient than multiple individual queries.
     * 
     * @param statuses list of task statuses
     * @param pageable pagination information
     * @return page of tasks with any of the specified statuses
     */
    Page<Task> findByStatusIn(List<Status> statuses, Pageable pageable);

    /**
     * Finds tasks by multiple priorities with pagination.
     * More efficient than multiple individual queries.
     * 
     * @param priorities list of task priorities
     * @param pageable pagination information
     * @return page of tasks with any of the specified priorities
     */
    Page<Task> findByPriorityIn(List<Priority> priorities, Pageable pageable);

    /**
     * Advanced search with multiple criteria.
     * Combines multiple search conditions in a single query.
     * 
     * @param searchTerm search term for title/description
     * @param statuses list of statuses to filter by
     * @param priorities list of priorities to filter by
     * @param dueDateFrom start date for due date range
     * @param dueDateTo end date for due date range
     * @param pageable pagination information
     * @return page of tasks matching all criteria
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:statuses IS NULL OR t.status IN :statuses) AND " +
           "(:priorities IS NULL OR t.priority IN :priorities) AND " +
           "(:dueDateFrom IS NULL OR t.dueDate >= :dueDateFrom) AND " +
           "(:dueDateTo IS NULL OR t.dueDate <= :dueDateTo)")
    Page<Task> findTasksWithAdvancedSearch(
        @Param("searchTerm") String searchTerm,
        @Param("statuses") List<Status> statuses,
        @Param("priorities") List<Priority> priorities,
        @Param("dueDateFrom") LocalDate dueDateFrom,
        @Param("dueDateTo") LocalDate dueDateTo,
        Pageable pageable
    );

    /**
     * Finds overdue tasks with pagination.
     * More efficient than loading all overdue tasks into memory.
     * 
     * @param currentDate the current date
     * @param pageable pagination information
     * @return page of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    Page<Task> findOverdueTasksPaged(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    /**
     * Finds tasks due within a specific date range.
     * Useful for upcoming tasks and deadline management.
     * 
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @param pageable pagination information
     * @return page of tasks due within the date range
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate >= :startDate AND t.dueDate <= :endDate " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    Page<Task> findTasksDueBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    /**
     * Bulk update task status for multiple tasks.
     * More efficient than individual updates.
     * 
     * @param taskIds list of task IDs to update
     * @param status new status to set
     * @param updatedAt timestamp of the update
     * @return number of updated tasks
     */
    @Modifying
    @Query("UPDATE Task t SET t.status = :status, t.updatedAt = :updatedAt WHERE t.id IN :taskIds")
    int bulkUpdateTaskStatus(
        @Param("taskIds") List<Long> taskIds,
        @Param("status") Status status,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Bulk update task priority for multiple tasks.
     * More efficient than individual updates.
     * 
     * @param taskIds list of task IDs to update
     * @param priority new priority to set
     * @param updatedAt timestamp of the update
     * @return number of updated tasks
     */
    @Modifying
    @Query("UPDATE Task t SET t.priority = :priority, t.updatedAt = :updatedAt WHERE t.id IN :taskIds")
    int bulkUpdateTaskPriority(
        @Param("taskIds") List<Long> taskIds,
        @Param("priority") Priority priority,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Bulk delete tasks by IDs.
     * More efficient than individual deletes.
     * 
     * @param taskIds list of task IDs to delete
     * @return number of deleted tasks
     */
    @Modifying
    @Query("DELETE FROM Task t WHERE t.id IN :taskIds")
    int bulkDeleteTasks(@Param("taskIds") List<Long> taskIds);

    /**
     * Counts tasks by multiple statuses.
     * More efficient than multiple individual count queries.
     * 
     * @param statuses list of statuses to count
     * @return map of status to count
     */
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.status IN :statuses GROUP BY t.status")
    List<Object[]> countTasksByStatuses(@Param("statuses") List<Status> statuses);

    /**
     * Counts tasks by multiple priorities.
     * More efficient than multiple individual count queries.
     * 
     * @param priorities list of priorities to count
     * @return map of priority to count
     */
    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.priority IN :priorities GROUP BY t.priority")
    List<Object[]> countTasksByPriorities(@Param("priorities") List<Priority> priorities);

    /**
     * Gets task statistics summary without loading full entities.
     * Optimized for analytics with minimal data transfer.
     * 
     * @return list of [status, priority, count] arrays
     */
    @Query("SELECT t.status, t.priority, COUNT(t) FROM Task t GROUP BY t.status, t.priority")
    List<Object[]> getTaskStatisticsSummary();

    /**
     * Finds tasks with high priority and approaching deadlines.
     * Useful for urgent task management.
     * 
     * @param currentDate current date
     * @param daysAhead number of days ahead to check
     * @param pageable pagination information
     * @return page of high priority tasks due soon
     */
    @Query("SELECT t FROM Task t WHERE t.priority = 'HIGH' " +
           "AND t.dueDate BETWEEN :currentDate AND :futureDate " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    Page<Task> findHighPriorityTasksDueSoon(
        @Param("currentDate") LocalDate currentDate,
        @Param("futureDate") LocalDate futureDate,
        Pageable pageable
    );

    /**
     * Finds recently completed tasks.
     * Useful for completion tracking and analytics.
     * 
     * @param completedAfter date after which tasks were completed
     * @param pageable pagination information
     * @return page of recently completed tasks
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'COMPLETED' " +
           "AND t.completedAt >= :completedAfter")
    Page<Task> findRecentlyCompletedTasks(
        @Param("completedAfter") LocalDateTime completedAfter,
        Pageable pageable
    );

    /**
     * Finds tasks by title similarity using database-level text search.
     * More efficient than loading all tasks and filtering in memory.
     * 
     * @param searchTerm search term for title similarity
     * @return list of similar tasks (limited to 10 results)
     */
    @Query(value = "SELECT t FROM Task t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY LENGTH(t.title) - LENGTH(REPLACE(LOWER(t.title), LOWER(:searchTerm), '')) DESC",
           nativeQuery = false)
    List<Task> findSimilarTasksByTitle(@Param("searchTerm") String searchTerm);

    /**
     * Gets task completion rate for a specific date range.
     * Optimized query for analytics.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return array with [total_tasks, completed_tasks]
     */
    @Query("SELECT COUNT(t), SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) " +
           "FROM Task t WHERE t.createdAt >= :startDate AND t.createdAt < :endDate")
    Object[] getCompletionRateForDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Finds tasks that need attention (overdue or high priority due soon).
     * Single query for dashboard/notification purposes.
     * 
     * @param currentDate current date
     * @param futureDate future date for "due soon" check
     * @param pageable pagination information
     * @return page of tasks needing attention
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(t.dueDate < :currentDate AND t.status NOT IN ('COMPLETED', 'CANCELLED')) OR " +
           "(t.priority = 'HIGH' AND t.dueDate BETWEEN :currentDate AND :futureDate " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED'))")
    Page<Task> findTasksNeedingAttention(
        @Param("currentDate") LocalDate currentDate,
        @Param("futureDate") LocalDate futureDate,
        Pageable pageable
    );

    /**
     * Gets task distribution by status and priority.
     * Single query for comprehensive analytics.
     * 
     * @return list of [status, priority, count] arrays
     */
    @Query("SELECT t.status, t.priority, COUNT(t) FROM Task t " +
           "GROUP BY t.status, t.priority ORDER BY t.status, t.priority")
    List<Object[]> getTaskDistributionByStatusAndPriority();
}
