package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.TaskAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for TaskAudit entity.
 * Provides data access methods for audit trail functionality.
 */
@Repository
public interface TaskAuditRepository extends JpaRepository<TaskAudit, Long> {

    /**
     * Find audit records for a specific task.
     *
     * @param taskId the ID of the task
     * @param pageable pagination information
     * @return page of audit records for the task
     */
    Page<TaskAudit> findByTaskIdOrderByChangeTimestampDesc(Long taskId, Pageable pageable);

    /**
     * Find audit records by action type.
     *
     * @param action the action type (CREATED, UPDATED, COMPLETED, DELETED)
     * @param pageable pagination information
     * @return page of audit records for the action
     */
    Page<TaskAudit> findByActionOrderByChangeTimestampDesc(String action, Pageable pageable);

    /**
     * Find audit records within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of audit records within the date range
     */
    Page<TaskAudit> findByChangeTimestampBetweenOrderByChangeTimestampDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find audit records for a specific task within a date range.
     *
     * @param taskId the ID of the task
     * @param startDate the start date
     * @param endDate the end date
     * @return list of audit records for the task within the date range
     */
    List<TaskAudit> findByTaskIdAndChangeTimestampBetweenOrderByChangeTimestampDesc(
            Long taskId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count audit records by action type.
     *
     * @param action the action type
     * @return count of audit records for the action
     */
    long countByAction(String action);

    /**
     * Find recent audit records for a specific task.
     *
     * @param taskId the ID of the task
     * @param limit the maximum number of records to return
     * @return list of recent audit records for the task
     */
    @Query("SELECT ta FROM TaskAudit ta WHERE ta.taskId = :taskId " +
           "ORDER BY ta.changeTimestamp DESC")
    List<TaskAudit> findRecentByTaskId(@Param("taskId") Long taskId, Pageable pageable);

    /**
     * Find audit records by user.
     *
     * @param changedBy the user who made the changes
     * @param pageable pagination information
     * @return page of audit records for the user
     */
    Page<TaskAudit> findByChangedByOrderByChangeTimestampDesc(String changedBy, Pageable pageable);

    /**
     * Get audit statistics for a specific task.
     *
     * @param taskId the ID of the task
     * @return array containing [total changes, last change timestamp]
     */
    @Query("SELECT COUNT(ta), MAX(ta.changeTimestamp) FROM TaskAudit ta WHERE ta.taskId = :taskId")
    Object[] getAuditStatisticsForTask(@Param("taskId") Long taskId);

    /**
     * Finds all audit records for a specific task ordered by timestamp descending.
     * This method is used by the tests.
     *
     * @param taskId the ID of the task
     * @return a list of audit records for the given task
     */
    List<TaskAudit> findByTaskIdOrderByChangeTimestampDesc(Long taskId);
}
