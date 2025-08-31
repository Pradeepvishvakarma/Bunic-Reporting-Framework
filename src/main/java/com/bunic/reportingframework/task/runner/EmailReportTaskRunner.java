package com.bunic.reportingframework.task.runner;

import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.task.model.TaskType;
import com.bunic.reportingframework.task.service.EmailReportProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailReportTaskRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailReportTaskRunner.class);

    @Autowired
    private EmailReportProcessorService emailReportProcessorService;

    @Autowired
    private ObjectMapper objectMapper;

    public void run(Task task)  {
        var errors = new HashMap<String, String>();
        validateTask(task, errors);

        if(!errors.isEmpty()){
            emailReportProcessorService.setTaskFailure(task, errors);
            emailReportProcessorService.saveTask(task);
            return;
        }

        LOGGER.info("Task manager - email runner  - executing email report task for taskId: {}", task.getId());
        try{
            var metadata = getMetadata(task);
            System.out.println("Metadata: " + metadata);
        }catch (Exception e){
            LOGGER.error("|Alert| - Task manager - email runner  - Problem to generate email report for taskId: {}, exception: {}", task.getId(), e.getMessage());
            errors.put("errorMessage", e.getMessage());
            emailReportProcessorService.setTaskFailure(task, errors);
        }

        task.setStatus(TaskStatus.COMPLETED);


    }

    private Metadata getMetadata(Task task){
        return emailReportProcessorService.getMetadata(task);
    }

    private void validateTask(Task task, Map<String, String> errors){
        if (task == null) {
            errors.put("task", "Task cannot be null.");
        } else {
            if (task.getType() != null && !task.getType().equals(TaskType.EMAIL_REPORT)) {
                errors.put("type", "Invalid task type. Expected EMAIL_REPORT.");
            }
            if (task.getParams().isEmpty()) {
                errors.put("params", "Task params cannot be empty. one or more params are required.");
            } else {
//                List<Filter> filters = objectMapper.readValue((JsonParser) task.getParams().get("filters"),
//                        new TypeReference<List<Filter>>() {});
                String report = (String) task.getParams().get("report");

//                if(filters == null || filters.isEmpty()) {
//                    errors.put("filters", "Task filters cannot be null or empty.");
//                }
                if(StringUtils.isBlank(report)) {
                    errors.put("report", "Task report cannot be empty.");
                }
            }

        }
    }
}
