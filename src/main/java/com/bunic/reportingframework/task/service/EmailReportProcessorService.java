package com.bunic.reportingframework.task.service;

import com.bunic.reportingframework.collection.dao.CollectionDao;
import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class EmailReportProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailReportProcessorService.class);

    @Autowired
    private TaskManagerDao taskManagerDao;

    @Autowired
    private CollectionDao collectionDao;

    public void setTaskFailure(Task task, Map<String, String> errors){
        task.setErrorMessage(String.join(",", errors.values()));
        task.setStatus(TaskStatus.FAILED);
        LOGGER.error("|Alert| - Task manager - email runner  - Problem to generate email report for schedulerId: {}, exception: {}", task.getId(), task.getErrorMessage());
    }

    public Metadata getMetadata(Task task){
        var metadataList = collectionDao.getAllMetadata();
        var reportMetadata = metadataList.stream().filter(metadata -> ((String)task.getParams().get("report")).equalsIgnoreCase(metadata.getCode())).findFirst();
        if(reportMetadata.isEmpty()){
            throw new RuntimeException("Metadata not found for report: " + task.getParams().get("report"));
        }
        return reportMetadata.get();
    }
    public void saveTask(Task task){
        task.setCompletedTime(new Date());
        taskManagerDao.saveTask(task);
    }
}
