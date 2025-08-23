package com.bunic.reportingframework.task.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum TaskType {
    EMAIL_REPORT("EMAIL_REPORT", "emailReportTaskRunner");

    private final String value;
    private final String runner;
    private static final Map<String, TaskType> taskTypes = new HashMap<>();

    static {
        for (TaskType taskType:TaskType.values()){
            taskTypes.put(taskType.getValue(), taskType);
        }
    }

    private TaskType(String value, String runner) {
        this.value = value;
        this.runner = runner;
    }

    public static TaskType get(String key) {
        return taskTypes.get(key);
    }
}
