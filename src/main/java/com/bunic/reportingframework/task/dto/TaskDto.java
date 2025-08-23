package com.bunic.reportingframework.task.dto;

import com.bunic.reportingframework.task.model.TaskType;
import lombok.Builder;
import lombok.Data;

import java.lang.Object;
import java.util.Map;

@Data
@Builder
public class TaskDto {

    private String id;
    private TaskType type;
    private Map<String, Object> params;
    private String referenceId;
}
