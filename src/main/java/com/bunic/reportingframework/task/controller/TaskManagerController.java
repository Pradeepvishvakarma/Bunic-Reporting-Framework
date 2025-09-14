package com.bunic.reportingframework.task.controller;

import com.bunic.reportingframework.task.dto.TaskDto;
import com.bunic.reportingframework.task.scheduler.TaskManagerScheduler;
import com.bunic.reportingframework.task.service.TaskManagerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task-manager")
public class TaskManagerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerController.class);

    @Autowired
    TaskManagerService taskManagerService;

    @Autowired
    TaskManagerScheduler taskManagerScheduler;

    @GetMapping("/hello")
    public String helloWorld() {
        return "Welcome to hello World";
    }

    @GetMapping("/refresh-schedulers")
    @ResponseBody
    @Operation(summary = "configure Schedulers")
    public void refresh() throws Exception {
        taskManagerScheduler.refreshSchedulers();
    }

    @GetMapping("/save")
    @ResponseBody
    @Operation(summary = "save task")
    public TaskDto saveTask(@RequestBody TaskDto task, HttpServletRequest request) throws Exception {
        LOGGER.info("Task Manager - save task - request from: {}", task);
        taskManagerService.saveTask(task);
        return task;
    }
}