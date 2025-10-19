package com.bunic.reportingframework.task.service;

import com.bunic.reportingframework.collection.dao.CollectionDao;
import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.collection.model.PivotConfig;
import com.bunic.reportingframework.email.model.EmailProperties;
import com.bunic.reportingframework.email.service.EmailSender;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private UserService userService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private PivotTableService pivotTableService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setTaskFailure(Task task, Map<String, String> errors){
        task.setErrorMessage(String.join(",", errors.values()));
        task.setStatus(TaskStatus.FAILED);
        LOGGER.error("|Alert| - Task manager - email runner  - Problem to generate email report for schedulerId: {}, exception: {}", task.getId(), task.getErrorMessage());
    }

    public Metadata getMetadata(Task task){
        var user = userService.getUserByUserId(task.getUserId());
        if(user == null){
            throw new RuntimeException("request is not authorised");
        }
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

    private PivotConfig getPivotConfig(Task task, Metadata metadata) {
        if (task != null && task.getParams() != null && task.getParams().get("pivotConfig") != null) {
            return objectMapper.convertValue(task.getParams().get("pivotConfig"), PivotConfig.class);
        }
        return metadata.getPivotConfig();
    }

    public User getUser(Task task){
        var userId = task.getUserId();
        var user = userService.getUserByUserId(userId);
        if(user == null) {
            throw new RuntimeException("request is not authorised");
        }
        return user;
    }

    public Map<String, Object> getEmailTemplateData(Task task, Metadata metadata, List<DBObject> data, User user) throws Exception {
        var pivotConfig = getPivotConfig(task, metadata);
        var reportDataResponse = pivotTableService.getReportDataResponse(metadata, task, pivotConfig, data);

        try {

            System.out.println("reportDataResponse = " + objectMapper.writeValueAsString(reportDataResponse));
        } catch (Exception e) {
            throw new RuntimeException("Task manager - email runner - problem on prepare email report data " + e.getMessage());
        }

        var dataMap = new HashMap<String, Object>();
        dataMap.put("taskId", task.getId());
        dataMap.put("filePath", task.getPath());
        dataMap.put("generatedDate", new Date());
        dataMap.put("columns", metadata.getColumns());
        dataMap.put("reportName",metadata.getEmailReportProperties().get("reportName"));
        dataMap.put("legends", metadata.getLegends());
        dataMap.put("reportDate", getReportDate(task));
        dataMap.put("subject", metadata.getEmailReportProperties().get("reportName"));
        dataMap.put("emailBodyReport", metadata.getEmailReportProperties().get("emailBodyReport"));
        dataMap.put("emailId", user.getEmailId());
        dataMap.put("attachmentFileName", metadata.getEmailReportProperties().get("attachmentFileName"));

        if(pivotConfig != null){
            dataMap.put("isNonPivotReport", true);
            dataMap.put("pivotedReportData", reportDataResponse);
            preparePivotEmailTemplateData(dataMap, pivotConfig);
        } else {
            dataMap.put("isNonPivotReport", false);
            dataMap.put("reportData", reportDataResponse);
        }
        pivotTableService.generateExcel(data, metadata, task);
        return dataMap;
    }
    private void preparePivotEmailTemplateData(Map<String, Object> emailTemplateData, PivotConfig pivotConfig){
        if(pivotConfig.isEnabled() && pivotConfig.getRowGroup() != null && !pivotConfig.getRowGroup().isEmpty() && (pivotConfig.getColumnGroup() == null || pivotConfig.getColumnGroup().isEmpty())){
            emailTemplateData.put("isPivotEnableRowGrouping", true);
        }
    }
    private String getReportDate(Task task){
        return null;
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
        emailProperties.setFrom("bunic.corporation.ltd@gmail.com");
        emailProperties.setMailIds((String) emailTemplateData.get("emailId"));
        emailProperties.setFilePath((String) emailTemplateData.get("filePath"));
        emailProperties.setAttachmentName((String) emailTemplateData.get("attachmentFileName"));
        emailProperties.setSubject((String) emailTemplateData.get("subject"));
//        emailProperties.setE
        return emailProperties;
    }
}
