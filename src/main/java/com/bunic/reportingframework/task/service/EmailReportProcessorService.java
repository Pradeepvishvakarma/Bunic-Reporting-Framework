package com.bunic.reportingframework.task.service;

import com.bunic.reportingframework.collection.model.GroupReport;
import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.collection.model.PivotConfig;
import com.bunic.reportingframework.collection.service.CollectionService;
import com.bunic.reportingframework.common.util.CommonUtil;
import com.bunic.reportingframework.email.model.EmailProperties;
import com.bunic.reportingframework.email.service.EmailSender;
import com.bunic.reportingframework.exception.BunicException;
import com.bunic.reportingframework.exception.BunicUnauthorizedException;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskScheduler;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import freemarker.template.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailReportProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailReportProcessorService.class);

    @Value("${bunic.reportingframework.email.processor.from}")
    public String businessEmailId;

    @Autowired
    private TaskManagerDao taskManagerDao;

    @Autowired
    private CollectionService collectionService;

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

    public User getUser(Task task) throws BunicUnauthorizedException {
        var userId = task.getUserId();
        var user = userService.getUserByUserId(userId);
        if(user == null) {
            throw new BunicUnauthorizedException("request is not authorised");
        }
        return user;
    }

    public Map<String, Object> getEmailTemplateData(Task task, Metadata metadata, List<DBObject> data, User user) throws Exception {
        var pivotConfig = getPivotConfig(task, metadata);
        var reportDataResponse = pivotTableService.getReportDataResponse(metadata, pivotConfig, data);

        try {

            System.out.println("reportDataResponse = " + objectMapper.writeValueAsString(reportDataResponse));
        } catch (Exception e) {
            throw new RuntimeException("Task manager - email runner - problem on prepare email report data " + e.getMessage());
        }

        var emailTemplateData = getReportEmailProperty(task, metadata, null, user);

        if(pivotConfig != null){
            emailTemplateData.put("isNonPivotReport", true);
            emailTemplateData.put("pivotedReportData", reportDataResponse);
            preparePivotEmailTemplateData(emailTemplateData, pivotConfig);
        } else {
            emailTemplateData.put("isNonPivotReport", false);
            emailTemplateData.put("reportData", reportDataResponse);
        }
        pivotTableService.generateExcel(emailTemplateData, data, metadata, task);
        return emailTemplateData;
    }

    public Map<String, Object> getReportEmailProperty(Task task, Metadata metadata, TaskScheduler scheduler, User user){
        var map = new HashMap<String, Object>();

        map.put("scheduledTriggerTime", getScheduledTriggerTime(scheduler));
        map.put("reportDate", getReportDate(task));
        map.put("emailId", user.getEmailId());
        map.put("taskId", task.getId());
        map.put("filePath", task.getPath());
        map.put("generatedDate", new Date());

        var metadataEmailProperties = metadata.getEmailReportProperties();
        if(metadataEmailProperties != null && !metadataEmailProperties.isEmpty()){
            map.put("reportName",metadataEmailProperties.get("reportName"));
            map.put("legends", metadata.getLegends());
            map.put("subject", metadataEmailProperties.get("subject"));
            map.put("emailBodyReport", metadataEmailProperties.get("emailBodyReport"));
            map.put("includeExcelAttachment", metadataEmailProperties.get("includeExcelAttachment"));
            map.put("attachmentFileName", metadataEmailProperties.get("attachmentFileName"));
        }

        var taskEmailProp = task.getParams();
        if (taskEmailProp != null && !taskEmailProp.isEmpty()){
            map.put("reportName", CommonUtil.getFieldValue("reportName", taskEmailProp, map.get("reportName")));
            map.put("legends", CommonUtil.getFieldValue("legends", taskEmailProp, map.get("legends")));
            map.put("subject", CommonUtil.getFieldValue("subject", taskEmailProp, map.get("subject")));
            map.put("emailBodyReport", CommonUtil.getFieldValue("emailBodyReport", taskEmailProp, map.get("emailBodyReport")));
            map.put("attachmentFileName", CommonUtil.getFieldValue("attachmentFileName", taskEmailProp, map.get("attachmentFileName")));
            map.put("includeExcelAttachment", CommonUtil.getFieldValue("includeExcelAttachment", taskEmailProp, map.get("includeExcelAttachment")));
        }

        var reportDisplayName = (String) CommonUtil.getFieldValue("reportName", map, metadata.getName());
        map.put("reportDisplayName", reportDisplayName);
        return map;
    }

    private String getScheduledTriggerTime(TaskScheduler scheduler) {
        if(scheduler != null && scheduler.getCronTriggerTime() != null){
            var expression = scheduler.getCronTriggerTime().split("\\s+");
            return String.format("%s $s", LocalDateTime.now(Clock.systemDefaultZone())
                    .withHour(Integer.parseInt(expression[2]))
                    .withMinute(Integer.parseInt(expression[1]))
                    .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")), scheduler.getCronTimeZone());
        }
        return String.format("%s %s", LocalDateTime.now(Clock.systemDefaultZone()).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")), "IST");
    }

    private void preparePivotEmailTemplateData(Map<String, Object> emailTemplateData, PivotConfig pivotConfig){
        if(pivotConfig.isEnabled() && pivotConfig.getRowGroup() != null && !pivotConfig.getRowGroup().isEmpty() && (pivotConfig.getColumnGroup() == null || pivotConfig.getColumnGroup().isEmpty())){
            emailTemplateData.put("isPivotEnableRowGrouping", true);
        } else if(!pivotConfig.isEnabled() && pivotConfig.getRowGroup() != null && !pivotConfig.getRowGroup().isEmpty() && (pivotConfig.getColumnGroup() == null || pivotConfig.getColumnGroup().isEmpty())){
            emailTemplateData.put("isPivotDisableRowGrouping", true);
        } else if(pivotConfig.isEnabled() && pivotConfig.getRowGroup() != null && !pivotConfig.getRowGroup().isEmpty() && pivotConfig.getColumnGroup() != null && !pivotConfig.getColumnGroup().isEmpty()){
            emailTemplateData.put("isPivotEnableColumnGrouping", true);
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

    public void sendEmail(StringWriter content, Map<String, Object> emailTemplateData) throws BunicException {
        EmailProperties emailProperties = getEmailReportProperties(emailTemplateData);
        emailProperties.setContent(content);
        emailSender.sendEmail(emailProperties);
    }

    private EmailProperties getEmailReportProperties(Map<String, Object> emailTemplateData){
        EmailProperties emailProperties = new EmailProperties();
        emailProperties.setFrom(businessEmailId);
        emailProperties.setMailIds((String) emailTemplateData.get("emailId"));
        emailProperties.setFilePath((String) emailTemplateData.get("filePath"));
        emailProperties.setAttachmentName((String) emailTemplateData.get("attachmentFileName"));
        emailProperties.setSubject((String) emailTemplateData.get("subject"));
        emailProperties.setHasAttachment((boolean) emailTemplateData.get("includeExcelAttachment"));
        return emailProperties;
    }

    public void validateGroupReportParams(GroupReport groupReport){
        if (groupReport == null){
            throw new IllegalArgumentException("Group report configuration cannot be null");
        }
        if (groupReport.getRows() == null || groupReport.getRows().isEmpty()){
            throw new IllegalArgumentException("Group report rows configuration cannot be null or empty");
        }
        var invalidRows = groupReport.getRows().stream().filter(row -> row.getCols().isEmpty() || (row.getCols()
                        .stream()
                        .filter(col -> StringUtils.isBlank(col.getReport()))
                        .toList()
                        .isEmpty()))
                .toList();
        if ( invalidRows.isEmpty()){
            throw new IllegalArgumentException("Rows and Cols cannot be empty");
        }
    }

    public Metadata getMetadataByCode(User user, String reportCode) throws BunicException {
        return collectionService.getMetadataByCode(user, reportCode);
    }
}
