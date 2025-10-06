package com.example.taskmanagement.aspect;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskAudit;
import com.example.taskmanagement.repository.TaskAuditRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Aspect for auditing task changes.
 * Automatically creates audit records when tasks are modified.
 */
@Aspect
@Component
public class TaskAuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(TaskAuditAspect.class);

    @Autowired
    private TaskAuditRepository taskAuditRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Audit task creation.
     *
     * @param joinPoint the join point
     * @param result the created task
     */
    @AfterReturning(pointcut = "execution(* com.example.taskmanagement.service.TaskService.createTask(..))", 
                    returning = "result")
    public void auditTaskCreation(JoinPoint joinPoint, Object result) {
        if (result instanceof Task) {
            Task task = (Task) result;
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Auditing task creation for task ID: {}", task.getId());
                }
                
                TaskAudit audit = new TaskAudit(task.getId(), task.getTitle(), "CREATED");
                audit.setNewValues(serializeTask(task));
                audit.setChangedBy("SYSTEM");
                
                taskAuditRepository.save(audit);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to audit task creation for task ID: {}", task.getId(), e);
                }
            }
        }
    }

    /**
     * Audit task updates.
     *
     * @param joinPoint the join point
     * @param result the updated task
     */
    @AfterReturning(pointcut = "execution(* com.example.taskmanagement.service.TaskService.updateTask(..))", 
                    returning = "result")
    public void auditTaskUpdate(JoinPoint joinPoint, Object result) {
        if (result instanceof Task) {
            Task task = (Task) result;
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Auditing task update for task ID: {}", task.getId());
                }
                
                TaskAudit audit = new TaskAudit(task.getId(), task.getTitle(), "UPDATED");
                audit.setNewValues(serializeTask(task));
                audit.setChangedBy("SYSTEM");
                
                taskAuditRepository.save(audit);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to audit task update for task ID: {}", task.getId(), e);
                }
            }
        }
    }

    /**
     * Audit task completion.
     *
     * @param joinPoint the join point
     * @param result the completed task
     */
    @AfterReturning(pointcut = "execution(* com.example.taskmanagement.service.TaskService.completeTask(..))", 
                    returning = "result")
    public void auditTaskCompletion(JoinPoint joinPoint, Object result) {
        if (result instanceof Task) {
            Task task = (Task) result;
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Auditing task completion for task ID: {}", task.getId());
                }
                
                TaskAudit audit = new TaskAudit(task.getId(), task.getTitle(), "COMPLETED");
                audit.setNewValues(serializeTask(task));
                audit.setChangedBy("SYSTEM");
                
                taskAuditRepository.save(audit);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to audit task completion for task ID: {}", task.getId(), e);
                }
            }
        }
    }

    /**
     * Audit task deletion.
     *
     * @param joinPoint the join point
     */
    @AfterReturning(pointcut = "execution(* com.example.taskmanagement.service.TaskService.deleteTask(..))")
    public void auditTaskDeletion(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Long) {
                Long taskId = (Long) args[0];
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Auditing task deletion for task ID: {}", taskId);
                }
                
                TaskAudit audit = new TaskAudit(taskId, "DELETED_TASK", "DELETED");
                audit.setChangedBy("SYSTEM");
                
                taskAuditRepository.save(audit);
            }
        } catch (Exception e) {
            logger.error("Failed to audit task deletion", e);
        }
    }

    /**
     * Audit task deletion for testing purposes.
     *
     * @param joinPoint the join point (can be null for testing)
     * @param taskId the task ID
     */
    public void auditTaskDeletion(JoinPoint joinPoint, Long taskId) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Auditing task deletion for task ID: {}", taskId);
            }
            
            TaskAudit audit = new TaskAudit(taskId, "DELETED_TASK", "DELETED");
            audit.setChangedBy("SYSTEM");
            
            taskAuditRepository.save(audit);
        } catch (Exception e) {
            logger.error("Failed to audit task deletion", e);
        }
    }

    /**
     * Serialize task to JSON for audit storage.
     *
     * @param task the task to serialize
     * @return JSON string representation of the task
     * @throws JsonProcessingException if serialization fails
     */
    private String serializeTask(Task task) throws JsonProcessingException {
        return objectMapper.writeValueAsString(task);
    }
}
