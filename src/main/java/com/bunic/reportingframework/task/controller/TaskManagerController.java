package com.bunic.reportingframework.task.controller;

import com.bunic.reportingframework.task.service.TaskManagerService;
import io.swagger.v3.oas.annotations.Operation;
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

    @GetMapping("/hello")
    public String helloWorld() {
        return "Welcome to hello World";
    }

    @PostMapping("/excel")
    @ResponseBody
    @Operation(summary = "configure reports metadata")
    public void generateExcel() throws Exception {
        taskManagerService.createExcel();
    }
}