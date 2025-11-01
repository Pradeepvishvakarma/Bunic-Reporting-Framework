package com.bunic.reporting_framework.task.controller;

import com.bunic.reportingframework.task.controller.TaskManagerController;
import com.bunic.reportingframework.task.scheduler.TaskManagerScheduler;
import com.bunic.reportingframework.task.service.TaskManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskManagerControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private TaskManagerController controller;

    @Mock
    private TaskManagerService taskManagerService;

    @Mock
    private TaskManagerScheduler taskManagerScheduler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testHelloWorld() throws Exception {
        mockMvc.perform(get("/task-manager/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to hello World"));
    }

    @Test
    void testRefreshSchedulers() throws Exception {
        doNothing().when(taskManagerScheduler).refreshSchedulers();

        mockMvc.perform(get("/task-manager/refresh-schedulers"))
                .andExpect(status().isOk());

        verify(taskManagerScheduler, times(1)).refreshSchedulers();
    }

}
