package com.bunic.reportingframework.task.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum TaskType {
    EMAIL_REPORT("EMAIL_REPORT");
    private final String type;

    TaskType(String emailReport) {
        this.type = emailReport;
    }
}
