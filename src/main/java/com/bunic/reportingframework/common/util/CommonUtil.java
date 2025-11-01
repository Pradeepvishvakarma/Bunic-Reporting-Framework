package com.bunic.reportingframework.common.util;

import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskScheduler;
import com.bunic.reportingframework.task.model.TaskStatus;

import java.util.Map;

public class CommonUtil {

    public static Object getFieldValue(String field, Map<String, Object> prop, Object defaultValue) {
        if(!prop.isEmpty() && prop.get(field) != null) {
            return prop.get(field);
        }
        return defaultValue;
    }

    public static Task toTask(TaskScheduler scheduler){
        var task = new Task();
        task.setType(scheduler.getType());
        task.setReferenceId(scheduler.getId());
        task.setStatus(TaskStatus.PENDING);
        task.setUserId(scheduler.getUserId());
        return task;
    }
}
