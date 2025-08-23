package com.bunic.reportingframework.task.model;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class Task {

    private String id;
    private String userId;
    private List<String> users;
    private TaskType type;
    private Map<String, Object> params;
    private String referenceId;
    private Date dateTime;
    private Date updatedTime;
    private Date completedTime;
    private TaskStatus status;
    private String path;
    private String errorMessage;
}
