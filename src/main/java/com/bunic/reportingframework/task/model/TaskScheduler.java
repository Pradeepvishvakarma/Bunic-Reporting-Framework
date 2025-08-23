package com.bunic.reportingframework.task.model;

import com.bunic.reportingframework.common.model.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class TaskScheduler {

    private String id;
    private String schedulerId;
    private String userId;
    private List<String> users;
    private Map<String, Object> params;
    private TaskType type;
    private String cronTriggerTime;
    private String cronTimeZone;
    private String active;
    private String createdBy;
    private Event event;
}
