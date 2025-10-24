package com.bunic.reportingframework.user.model;

import com.bunic.reportingframework.task.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNotifaction {

    private String message;
    private TaskStatus status;
    private String desc;
}
