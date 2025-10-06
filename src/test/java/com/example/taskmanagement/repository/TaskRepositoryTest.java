package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TaskRepository.
 * 
 * <p>This class contains integration tests for the TaskRepository,
 * testing database operations and custom query methods using
 * an in-memory H2 database.</p>
 * 
 * @author Task Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        // Create test tasks
        task1 = new Task("Task 1", Priority.HIGH, Status.TODO);
        task1.setDescription("First test task");
        task1.setDueDate(LocalDate.now().plusDays(1));

        task2 = new Task("Task 2", Priority.MEDIUM, Status.IN_PROGRESS);
        task2.setDescription("Second test task");
        task2.setDueDate(LocalDate.now().plusDays(2));

        task3 = new Task("Task 3", Priority.LOW, Status.COMPLETED);
        task3.setDescription("Third test task");
        task3.setDueDate(LocalDate.now().minusDays(1));
        task3.markAsCompleted();

        // Persist test data
        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);
        entityManager.persistAndFlush(task3);
    }

    @Test
    void testFindByStatus() {
        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Task> result = taskRepository.findByStatus(Status.TODO, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Task 1", result.getContent().get(0).getTitle());
    }

    @Test
    void testFindByPriority() {
        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Task> result = taskRepository.findByPriority(Priority.HIGH, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Task 1", result.getContent().get(0).getTitle());
    }

    @Test
    void testFindByStatusAndPriority() {
        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Task> result = taskRepository.findByStatusAndPriority(
            Status.IN_PROGRESS, Priority.MEDIUM, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Task 2", result.getContent().get(0).getTitle());
    }

    @Test
    void testFindOverdueTasks() {
        // Create a task that is overdue but not completed
        final Task overdueTask = new Task("Overdue Task", Priority.MEDIUM, Status.TODO);
        overdueTask.setDueDate(LocalDate.now().minusDays(1));
        entityManager.persistAndFlush(overdueTask);

        final List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDate.now());

        assertEquals(1, overdueTasks.size());
        assertEquals("Overdue Task", overdueTasks.get(0).getTitle());
    }

    @Test
    void testFindTasksDueToday() {
        final Task todayTask = new Task("Today Task", Priority.MEDIUM, Status.TODO);
        todayTask.setDueDate(LocalDate.now());
        entityManager.persistAndFlush(todayTask);

        final List<Task> todayTasks = taskRepository.findTasksDueToday(LocalDate.now());

        assertEquals(1, todayTasks.size());
        assertEquals("Today Task", todayTasks.get(0).getTitle());
    }

    @Test
    void testSearchTasks() {
        final Pageable pageable = PageRequest.of(0, 10);
        final Page<Task> result = taskRepository.searchTasks("test", pageable);

        assertEquals(3, result.getTotalElements());
    }

    @Test
    void testCountByStatus() {
        final long todoCount = taskRepository.countByStatus(Status.TODO);
        final long completedCount = taskRepository.countByStatus(Status.COMPLETED);

        assertEquals(1, todoCount);
        assertEquals(1, completedCount);
    }

    @Test
    void testCountByPriority() {
        final long highCount = taskRepository.countByPriority(Priority.HIGH);
        final long mediumCount = taskRepository.countByPriority(Priority.MEDIUM);
        final long lowCount = taskRepository.countByPriority(Priority.LOW);

        assertEquals(1, highCount);
        assertEquals(1, mediumCount);
        assertEquals(1, lowCount);
    }

    @Test
    void testFindTasksCreatedBetween() {
        final LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        final LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        final Pageable pageable = PageRequest.of(0, 10);

        final Page<Task> result = taskRepository.findTasksCreatedBetween(
            startDate, endDate, pageable);

        // All tasks should be created today, so they should be in the range
        assertTrue(result.getTotalElements() >= 3);
    }
}
