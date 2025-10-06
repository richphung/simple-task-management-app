package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.dto.TaskSearchRequest;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TaskController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskResponse taskResponse;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setPriority(Priority.HIGH);
        taskResponse.setStatus(Status.TODO);
        taskResponse.setDueDate(LocalDate.now().plusDays(1));
        taskResponse.setCreatedAt(LocalDateTime.now());
        taskResponse.setUpdatedAt(LocalDateTime.now());

        taskRequest = new TaskRequest();
        taskRequest.setTitle("Test Task");
        taskRequest.setDescription("Test Description");
        taskRequest.setPriority(Priority.HIGH);
        taskRequest.setStatus(Status.TODO);
        taskRequest.setDueDate(LocalDate.now().plusDays(1));
    }

    @Test
    void testCreateTask() throws Exception {
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void testGetTaskById() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(Optional.of(taskResponse));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void testGetTaskByIdNotFound() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllTasks() throws Exception {
        Page<TaskResponse> taskPage = new PageImpl<>(Arrays.asList(taskResponse), PageRequest.of(0, 10), 1);
        when(taskService.getAllTasks(anyInt(), anyInt())).thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testUpdateTask() throws Exception {
        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(Optional.of(taskResponse));

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void testUpdateTaskNotFound() throws Exception {
        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTask() throws Exception {
        when(taskService.deleteTask(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteTaskNotFound() throws Exception {
        when(taskService.deleteTask(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCompleteTask() throws Exception {
        TaskResponse completedTask = new TaskResponse();
        completedTask.setId(1L);
        completedTask.setTitle("Test Task");
        completedTask.setStatus(Status.COMPLETED);
        completedTask.setCompletedAt(LocalDateTime.now());

        when(taskService.completeTask(1L)).thenReturn(Optional.of(completedTask));

        mockMvc.perform(put("/api/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void testCompleteTaskNotFound() throws Exception {
        when(taskService.completeTask(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tasks/1/complete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchTasks() throws Exception {
        Page<TaskResponse> taskPage = new PageImpl<>(Arrays.asList(taskResponse), PageRequest.of(0, 10), 1);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(taskPage);

        TaskSearchRequest searchRequest = new TaskSearchRequest();
        searchRequest.setSearchTerm("test");
        searchRequest.setPage(0);
        searchRequest.setSize(10);

        mockMvc.perform(post("/api/tasks/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testQuickSearch() throws Exception {
        Page<TaskResponse> taskPage = new PageImpl<>(Arrays.asList(taskResponse), PageRequest.of(0, 10), 1);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/search/quick")
                .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetTasksByStatus() throws Exception {
        Page<TaskResponse> taskPage = new PageImpl<>(Arrays.asList(taskResponse), PageRequest.of(0, 10), 1);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/status/TODO")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetOverdueTasks() throws Exception {
        Page<TaskResponse> taskPage = new PageImpl<>(Arrays.asList(taskResponse), PageRequest.of(0, 10), 1);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(taskPage);

        mockMvc.perform(get("/api/tasks/overdue")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
