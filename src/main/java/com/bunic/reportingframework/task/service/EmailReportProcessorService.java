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

import java.io.StringWriter;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bunic.reportingframework.common.constant.Constant.*;

@Service
public class EmailReportProcessorService {

    public static final Logger LOGGER = LoggerFactory.getLogger(EmailReportProcessorService.class);

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
        task.setErrorMessage(String.join(COMMA_SEPARATOR, errors.values()));
        task.setStatus(TaskStatus.FAILED);
        LOGGER.error("|Alert| - Task manager - email runner  - Problem to generate email report for schedulerId: {}, exception: {}", task.getId(), task.getErrorMessage());
    }

    public void saveTask(Task task){
        task.setCompletedTime(new Date());
        taskManagerDao.saveTask(task);
    }

    private PivotConfig getPivotConfig(Task task, Metadata metadata) {
        if (task != null && task.getParams() != null && task.getParams().get(PIVOT_CONFIG) != null) {
            return objectMapper.convertValue(task.getParams().get(PIVOT_CONFIG), PivotConfig.class);
        }
        return metadata.getPivotConfig();
    }

    public User getUser(Task task) throws BunicUnauthorizedException {
        var userId = task.getUserId();
        var user = userService.getUserByUserId(userId);
        if(user == null) {
            throw new BunicUnauthorizedException(UNAUTHORIZED_REQUEST);
        }
        return user;
    }

    public Map<String, Object> getEmailTemplateData(Task task, Metadata metadata, List<DBObject> data, User user) throws Exception {
        var pivotConfig = getPivotConfig(task, metadata);
        var reportDataResponse = pivotTableService.getReportDataResponse(metadata, pivotConfig, data);

        var emailTemplateData = getReportEmailProperty(task, metadata, null, user);

        if(pivotConfig != null){
            emailTemplateData.put(IS_NON_PIVOT_REPORT, true);
            emailTemplateData.put(PIVOTED_REPORT_DATA, reportDataResponse);
            preparePivotEmailTemplateData(emailTemplateData, pivotConfig);
        } else {
            emailTemplateData.put(IS_NON_PIVOT_REPORT, false);
            emailTemplateData.put(REPORT_DATA, reportDataResponse);
        }
        if(emailTemplateData.containsKey(INCLUDE_EXCEL_ATTACHMENT) && (boolean) emailTemplateData.get(INCLUDE_EXCEL_ATTACHMENT)){
            prepareAndGenerateExcel(emailTemplateData, data, metadata, task);
        }
        return emailTemplateData;
    }

    private void prepareAndGenerateExcel(Map<String, Object> emailTemplateData, List<DBObject> data, Metadata metadata, Task task) throws Exception {
        pivotTableService.generateExcel(emailTemplateData, data, metadata, task);
    }

    public Map<String, Object> getReportEmailProperty(Task task, Metadata metadata, TaskScheduler scheduler, User user){
        var map = new HashMap<String, Object>();

        map.put(SCHEDULED_TRIGGER_TIME, getScheduledTriggerTime(scheduler));
        map.put(REPORT_DATE, getReportDate(task));
        map.put(EMAIL_ID, user.getEmailId());
        map.put(TASK_ID, task.getId());
        map.put(FILE_PATH, task.getPath());
        map.put(GENERATED_DATE, new Date());

        var metadataEmailProperties = metadata.getEmailReportProperties();
        if(metadataEmailProperties != null && !metadataEmailProperties.isEmpty()){
            map.put(REPORT_NAME,metadataEmailProperties.get(REPORT_NAME));
            map.put(LEGENDS, metadata.getLegends());
            map.put(SUBJECT, metadataEmailProperties.get(SUBJECT));
            map.put(EMAIL_BODY_REPORT, metadataEmailProperties.get(EMAIL_BODY_REPORT));
            map.put(INCLUDE_EXCEL_ATTACHMENT, metadataEmailProperties.get(INCLUDE_EXCEL_ATTACHMENT));
            map.put(ATTACHMENT_FILE_NAME, metadataEmailProperties.get(ATTACHMENT_FILE_NAME));
        }

        var taskEmailProp = task.getParams();
        if (taskEmailProp != null && !taskEmailProp.isEmpty()){
            map.put(REPORT_NAME, CommonUtil.getFieldValue(REPORT_NAME, taskEmailProp, map.get(REPORT_NAME)));
            map.put(LEGENDS, CommonUtil.getFieldValue(LEGENDS, taskEmailProp, map.get(LEGENDS)));
            map.put(SUBJECT, CommonUtil.getFieldValue(SUBJECT, taskEmailProp, map.get(SUBJECT)));
            map.put(EMAIL_BODY_REPORT, CommonUtil.getFieldValue(EMAIL_BODY_REPORT, taskEmailProp, map.get(EMAIL_BODY_REPORT)));
            map.put(ATTACHMENT_FILE_NAME, CommonUtil.getFieldValue(ATTACHMENT_FILE_NAME, taskEmailProp, map.get(ATTACHMENT_FILE_NAME)));
            map.put(INCLUDE_EXCEL_ATTACHMENT, CommonUtil.getFieldValue(INCLUDE_EXCEL_ATTACHMENT, taskEmailProp, map.get(INCLUDE_EXCEL_ATTACHMENT)));
        }

        var reportDisplayName = (String) CommonUtil.getFieldValue(REPORT_NAME, map, metadata.getName());
        map.put(REPORT_DISPLAY_NAME, reportDisplayName);
        map.put(TIMESTAMP, getTimeStamp());
        transformEmailProperty(map);
        return map;
    }

    private String getTimeStamp() {
        return LocalDateTime.now(Clock.systemDefaultZone()).format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    private void transformEmailProperty(Map<String, Object> emailProperties) {
        emailProperties.entrySet().forEach(entry -> {
            if (entry.getValue() != null && entry.getValue() instanceof String value) {
                var placeHolderMatcher = EMAIL_PLACEHOLDER_PATTERN.matcher(value);
                while (placeHolderMatcher.find()) {
                    var placeHolder = placeHolderMatcher.group();
                    String placeHolderKey = "";
                    if (StringUtils.isNotBlank(placeHolder)) {
                        placeHolderKey = placeHolder.replaceAll("[{}]*", "");
                    }
                    if (emailProperties.containsKey(placeHolderKey)) {
                        var updatedValue = ((String) emailProperties.get(entry.getKey())).replace(placeHolder, ((String) emailProperties.get(placeHolderKey)));
                        emailProperties.put(entry.getKey(), updatedValue);
                    }
                }
            }
        });
    }

    private String getScheduledTriggerTime(TaskScheduler scheduler) {
        if(scheduler != null && scheduler.getCronTriggerTime() != null){
            var expression = scheduler.getCronTriggerTime().split("\\s+");
            return String.format(TWO_STRING_WITH_SPACE, LocalDateTime.now(Clock.systemDefaultZone())
                    .withHour(Integer.parseInt(expression[2]))
                    .withMinute(Integer.parseInt(expression[1]))
                    .format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), scheduler.getCronTimeZone());
        }
        return String.format(TWO_STRING_WITH_SPACE, LocalDateTime.now(Clock.systemDefaultZone()).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), TIME_ZONE_IST);
    }

    private void preparePivotEmailTemplateData(Map<String, Object> emailTemplateData, PivotConfig pivotConfig){
        if(pivotConfig.isEnabled() && pivotConfig.getRowGroup() != null && !pivotConfig.getRowGroup().isEmpty() && (pivotConfig.getColumnGroup() == null || pivotConfig.getColumnGroup().isEmpty())){
            emailTemplateData.put(IS_PIVOT_ENABLE_ROW_GROUPING, true);
        } else if(!pivotConfig.isEnabled() && pivotConfig.getRowGroup() != null && !pivotConfig.getRowGroup().isEmpty() && (pivotConfig.getColumnGroup() == null || pivotConfig.getColumnGroup().isEmpty())){
            emailTemplateData.put(IS_PIVOT_DISABLE_ROW_GROUPING, true);
        } else if(pivotConfig.isEnabled() && pivotConfig.getRowGroup() != null && !pivotConfig.getRowGroup().isEmpty() && pivotConfig.getColumnGroup() != null && !pivotConfig.getColumnGroup().isEmpty()){
            emailTemplateData.put(IS_PIVOT_ENABLE_COLUMN_GROUPING, true);
        }
    }

    private String getReportDate(Task task){
        if (task != null && task.getCompletedTime() != null){
            return LocalDateTime.ofInstant(task.getCompletedTime().toInstant(), Clock.systemDefaultZone().getZone())
                    .format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        }
        return LocalDateTime.now(Clock.systemDefaultZone()).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
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
        emailProperties.setMailIds((String) emailTemplateData.get(EMAIL_ID));
        emailProperties.setFilePath((String) emailTemplateData.get(FILE_PATH));
        emailProperties.setAttachmentName((String) emailTemplateData.get(ATTACHMENT_FILE_NAME));
        emailProperties.setSubject((String) emailTemplateData.get(SUBJECT));
        emailProperties.setHasAttachment((boolean) emailTemplateData.get(INCLUDE_EXCEL_ATTACHMENT));
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
