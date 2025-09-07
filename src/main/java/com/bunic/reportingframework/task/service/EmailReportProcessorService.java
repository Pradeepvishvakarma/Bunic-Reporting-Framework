package com.bunic.reportingframework.task.service;

import com.bunic.reportingframework.collection.dao.CollectionDao;
import com.bunic.reportingframework.collection.model.CollectionRequest;
import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.email.model.EmailProperties;
import com.bunic.reportingframework.email.service.EmailSender;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.mongodb.DBObject;
import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailReportProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailReportProcessorService.class);

    @Autowired
    private TaskManagerDao taskManagerDao;

    @Autowired
    private CollectionDao collectionDao;

    @Autowired
    private Configuration configuration;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private PivotTableService pivotTableService;

    @Autowired
    private TaskManagerService taskManagerService;

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
        LOGGER.info("report: {} - metadata: {}", task.getParams().get("report"), reportMetadata.get());
        return reportMetadata.get();
    }
    public void saveTask(Task task){
        task.setCompletedTime(new Date());
        taskManagerDao.saveTask(task);
    }

    public Map<String, Object> getEmailTemplateData(Task task, Metadata metadata, CollectionRequest request, List<DBObject> data){
        var pivotConfig = metadata.getPivotConfig();
        var reportDataResponse = pivotTableService.getReportDataResponse(metadata, task, pivotConfig, data);
        System.out.println("reportDataResponse = " + reportDataResponse);
        var dataMap = new HashMap<String, Object>();
        dataMap.put("taskId", task.getId());
        dataMap.put("reportName", metadata.getName());
        dataMap.put("filePath", task.getPath());
        dataMap.put("reportDescription", metadata.getDescription());
        dataMap.put("generatedDate", new Date());
        dataMap.put("reportData", data);
        dataMap.put("columns", metadata.getColumns());
        return dataMap;
    }

    public StringWriter prepareReportHtml(String template, Map<String, Object> reportData){
        StringWriter ftlWriter = new StringWriter();
        try{
            configuration.getTemplate(template).process(reportData, ftlWriter);
        }catch (Exception e) {
            throw new RuntimeException("Task manager - email runner - problem on send email report " + e.getMessage());
        }
        return ftlWriter;
    }

    public void sendEmail(StringWriter content, Map<String, Object> emailTemplateData) {
        EmailProperties emailProperties = getEmailReportProperties(emailTemplateData);
        emailProperties.setContent(content);
        emailSender.sendEmail(emailProperties);
    }

    private EmailProperties getEmailReportProperties(Map<String, Object> emailTemplateData){
        EmailProperties emailProperties = new EmailProperties();
        emailProperties.setFrom("ss");
        emailProperties.setMailIds(List.of("pradeepv4919@gmail.com"));
        emailProperties.setFilePath((String) emailTemplateData.get("filePath"));
//        emailProperties.setE
        return emailProperties;
    }
}
