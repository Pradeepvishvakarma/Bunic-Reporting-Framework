package com.bunic.reportingframework.task.runner;

import com.bunic.reportingframework.collection.dao.CollectionDao;
import com.bunic.reportingframework.collection.model.Filter;
import com.bunic.reportingframework.collection.model.GroupColumn;
import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.common.util.CommonUtil;
import com.bunic.reportingframework.exception.BunicException;
import com.bunic.reportingframework.exception.BunicRuntimeException;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.task.model.TaskType;
import com.bunic.reportingframework.task.service.EmailReportProcessorService;
import com.bunic.reportingframework.user.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static com.bunic.reportingframework.common.constant.Constant.FILE_SAPERATOR;

@Component
public class EmailReportTaskRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailReportTaskRunner.class);

    @Autowired
    private EmailReportProcessorService emailReportProcessorService;

    @Autowired
    private CollectionDao collectionDao;

    @Value("${bunic.reportingframework.email.processor.file.path}")
    public String emailReportPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void run(Task task) throws Exception {
        var errors = new HashMap<String, String>();
        validateTask(task, errors);

        if (!errors.isEmpty()) {
            emailReportProcessorService.setTaskFailure(task, errors);
            emailReportProcessorService.saveTask(task);
            return;
        }

        LOGGER.info("Task manager - email runner  - executing email report task for taskId: {}", task.getId());
        try {
            var user = emailReportProcessorService.getUser(task);
            var metadata = emailReportProcessorService.getMetadataByCode(user, (String) task.getParams().get("report"));
            if (metadata.isGroupReportEnabled()){
                prepareAndSendGroupReport(metadata, task, user);
            } else {
                prepareAndSendReport(metadata, task, user);
            }
        } catch (Exception e) {
            LOGGER.error("|Alert| - Task manager - email runner  - Problem to generate email report for taskId: {}, exception: {}", task.getId(), e.getMessage(), e);
            errors.put("errorMessage", e.getMessage());
            emailReportProcessorService.setTaskFailure(task, errors);
        }
        task.setCompletedTime(new Date());
        task.setStatus(TaskStatus.COMPLETED);
        emailReportProcessorService.saveTask(task);
    }

    private void prepareAndSendReport(Metadata metadata, Task task, User user) throws Exception {
        task.setPath(createTaskLocation(task.getId()));
        var data = getData(metadata, task);
        var emailTemplateData = emailReportProcessorService.getEmailTemplateData(task, metadata, data, user);
        var emailTemplate = getEmailTemplate(metadata);
        StringWriter htmlReport = emailReportProcessorService.prepareReportHtml(emailTemplate, emailTemplateData);
        emailReportProcessorService.sendEmail(htmlReport, emailTemplateData);
    }

    private void prepareAndSendGroupReport(Metadata metadata, Task task, User user) throws Exception {
        task.setPath(createTaskLocation(task.getId()));
        emailReportProcessorService.validateGroupReportParams(metadata.getGroupReport());

        var groupReportData = new ArrayList<Map<String, Object>>();
        var groupReportContent = new TreeMap<Integer, List<StringWriter>>();

        var groupReport = metadata.getGroupReport();
        var rows = groupReport.getRows();

        for (int i = 0; !rows.isEmpty() && i < rows.size(); i++) {
            var htmlReports = new ArrayList<StringWriter>();

            for (GroupColumn col : rows.get(i).getCols()){
                var childReportMetadata = emailReportProcessorService.getMetadataByCode(user, col.getReport());
                var data = getData(childReportMetadata, task);
                var collReportData = emailReportProcessorService.getEmailTemplateData(task, childReportMetadata, data, user);
                var collEmailTemplate = getEmailTemplate(childReportMetadata, collReportData, col);
                updateReportWidth(col, collReportData);
                updateReportTitle(childReportMetadata, col, collReportData);
                groupReportData.add(collReportData);

                var htmlReport = emailReportProcessorService.prepareReportHtml(collEmailTemplate, collReportData);
                htmlReports.add(htmlReport);
            }
            groupReportContent.put(i, htmlReports);
        }

        var groupReportEmailProps = emailReportProcessorService.getReportEmailProperty(task, metadata, null, user);
        groupReportEmailProps.put("groupReportContent", groupReportContent);
        String emailTemplate = (String) CommonUtil.getFieldValue("emailTemplate", metadata.getEmailReportProperties(), "group_report_template.ftlh");
        StringWriter htmlGroupReport = emailReportProcessorService.prepareReportHtml(emailTemplate, groupReportEmailProps);
        emailReportProcessorService.sendEmail(htmlGroupReport, groupReportEmailProps);
    }

    private void updateReportTitle(Metadata metadata, GroupColumn col, Map<String, Object> collReportData) {
        if(col.getConfig() != null && col.getConfig().getTitle() != null){
            collReportData.put("reportTitle", col.getConfig().getTitle());
        } else {
            collReportData.put("reportTitle", metadata.getName());
        }
    }

    private void updateReportWidth(GroupColumn col, Map<String, Object> collReportData) {
        if(col.getConfig() != null && col.getConfig().getWidth() != null){
            collReportData.put("width", col.getConfig().getWidth());
        }
    }

    private String getEmailTemplate(Metadata metadata) {
        var emailTemplate = (String) CommonUtil.getFieldValue("emailTemplate", metadata.getEmailReportProperties(), "default_template.ftlh");
        LOGGER.info("Email template configured: {} ", emailTemplate);
        return emailTemplate;
    }

    private String getEmailTemplate(Metadata metadata, Map<String, Object> collReportData, GroupColumn col) {
        if(col.getConfig() != null && col.getConfig().getEmailTemplate() != null){
            var emailTemplate = col.getConfig().getEmailTemplate();
            LOGGER.info("Email template configured: {} for child report: {} in group report", emailTemplate, metadata.getCode());
            return emailTemplate;
        }
        var emailTemplate = (boolean) collReportData.get("isNonPivotReport") ? "pivot_template.ftlh" : "non_pivot_template.ftlh";
        LOGGER.info("Email template configured: {} for child report: {} in group report", emailTemplate, metadata.getCode());
        return emailTemplate;
    }

    private List<DBObject> getData(Metadata metadata, Task task) throws BunicRuntimeException {
        return collectionDao.getData(metadata, task);
    }

    private void validateTask(Task task, Map<String, String> errors) throws IOException {
        if (task == null) {
            errors.put("task", "Task cannot be null.");
        } else {
            if (task.getType() != null && !task.getType().equals(TaskType.EMAIL_REPORT)) {
                errors.put("type", "Invalid task type. Expected EMAIL_REPORT.");
            }
            if (task.getParams().isEmpty()) {
                errors.put("params", "Task params cannot be empty. one or more params are required.");
            } else {
                List<Filter> filters = objectMapper.readValue(
                        objectMapper.writeValueAsString(task.getParams().get("filters")),
                        new TypeReference<List<Filter>>() {
                        }
                );
                String report = (String) task.getParams().get("report");
                if (filters == null || filters.isEmpty()) {
                    errors.put("filters", "Task filters cannot be null or empty.");
                }
                if (StringUtils.isBlank(report)) {
                    errors.put("report", "Task report cannot be empty.");
                }
            }
        }
    }

    private String createTaskLocation(String taskId) throws BunicRuntimeException {
        String reportFolderPath = "";
        if (StringUtils.isBlank(taskId)) {
            throw new RuntimeException("Task manager - task runner - TaskId cannot be null");
        }

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        if (emailReportPath != null) {
            reportFolderPath = String.format("%s%s%s%s%s%s%s%s", emailReportPath, FILE_SAPERATOR, currentYear,
                    FILE_SAPERATOR, currentMonth, FILE_SAPERATOR, taskId, FILE_SAPERATOR);
            try {
                int fromIndex = reportFolderPath.indexOf(FILE_SAPERATOR);
                while (fromIndex != -1) {
                    int toIndex = reportFolderPath.indexOf(FILE_SAPERATOR, fromIndex + 1);
                    Path folderPath = Paths.get(reportFolderPath.substring(0, toIndex + 1));
                    if (!Files.exists(folderPath)) {
                        Files.createDirectories(folderPath);
                    }
                    fromIndex = toIndex;
                }
                int toIndex = reportFolderPath.lastIndexOf(FILE_SAPERATOR);
                LOGGER.info("Task manager - task runner - Created report folder path: {}", reportFolderPath);
            } catch (Exception e) {
                throw new BunicRuntimeException("Task manager - task runner - Problem to create report folder path " + e.getMessage(), e);
            }
        }
        return reportFolderPath;
    }
}
