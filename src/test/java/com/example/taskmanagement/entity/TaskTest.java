package com.example.taskmanagement.entity;

import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Task entity.
 * 
 * <p>This class contains comprehensive unit tests for the Task entity,
 * testing all getters, setters, business logic methods, and validation.</p>
 * 
 * @author Task Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(task);
        assertNull(task.getId());
        assertNull(task.getTitle());
        assertNull(task.getDescription());
        assertNull(task.getPriority());
        assertNull(task.getStatus());
        assertNull(task.getDueDate());
        assertNull(task.getCompletedAt());
        assertNull(task.getNotes());
    }

    @Test
    void testParameterizedConstructor() {
        final String title = "Test Task";
        final Priority priority = Priority.HIGH;
        final Status status = Status.TODO;

        final Task newTask = new Task(title, priority, status);

        assertEquals(title, newTask.getTitle());
        assertEquals(priority, newTask.getPriority());
        assertEquals(status, newTask.getStatus());
    }

    @Test
    void testGettersAndSetters() {
        final Long id = 1L;
        final String title = "Test Task";
        final String description = "Test Description";
        final Priority priority = Priority.MEDIUM;
        final Status status = Status.IN_PROGRESS;
        final LocalDate dueDate = LocalDate.now().plusDays(7);
        final LocalDateTime completedAt = LocalDateTime.now();
        final String notes = "Test Notes";

        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setStatus(status);
        task.setDueDate(dueDate);
        task.setCompletedAt(completedAt);
        task.setNotes(notes);

        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(priority, task.getPriority());
        assertEquals(status, task.getStatus());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(completedAt, task.getCompletedAt());
        assertEquals(notes, task.getNotes());
    }

    @Test
    void testMarkAsCompleted() {
        task.setStatus(Status.IN_PROGRESS);
        task.setCompletedAt(null);

        task.markAsCompleted();

        assertEquals(Status.COMPLETED, task.getStatus());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    void testMarkAsInProgress() {
        task.setStatus(Status.TODO);
        task.setCompletedAt(LocalDateTime.now());

        task.markAsInProgress();

        assertEquals(Status.IN_PROGRESS, task.getStatus());
        assertNull(task.getCompletedAt());
    }

    @Test
    void testIsOverdueWhenTaskIsOverdue() {
        task.setDueDate(LocalDate.now().minusDays(1));
        task.setStatus(Status.TODO);

        assertTrue(task.isOverdue());
    }

    @Test
    void testIsOverdueWhenTaskIsNotOverdue() {
        task.setDueDate(LocalDate.now().plusDays(1));
        task.setStatus(Status.TODO);

        assertFalse(task.isOverdue());
    }

    @Test
    void testIsOverdueWhenTaskIsCompleted() {
        task.setDueDate(LocalDate.now().minusDays(1));
        task.setStatus(Status.COMPLETED);

        assertFalse(task.isOverdue());
    }

    @Test
    void testIsOverdueWhenTaskIsCancelled() {
        task.setDueDate(LocalDate.now().minusDays(1));
        task.setStatus(Status.CANCELLED);

        assertFalse(task.isOverdue());
    }

    @Test
    void testIsOverdueWhenDueDateIsNull() {
        task.setDueDate(null);
        task.setStatus(Status.TODO);

        assertFalse(task.isOverdue());
    }

    @Test
    void testToString() {
        task.setId(1L);
        task.setTitle("Test Task");
        task.setPriority(Priority.HIGH);
        task.setStatus(Status.IN_PROGRESS);
        task.setDueDate(LocalDate.of(2024, 12, 31));

        final String result = task.toString();

        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("title='Test Task'"));
        assertTrue(result.contains("priority=High"));
        assertTrue(result.contains("status=In Progress"));
        assertTrue(result.contains("dueDate=2024-12-31"));
    }
}
