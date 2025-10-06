package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
