package com.bunic.reportingframework.task.runner;

import com.bunic.reportingframework.collection.dao.CollectionDao;
import com.bunic.reportingframework.collection.model.CollectionRequest;
import com.bunic.reportingframework.collection.model.Filter;
import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.common.util.CommonUtil;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.task.model.TaskType;
import com.bunic.reportingframework.task.service.EmailReportProcessorService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            var request = getCollectionRequest(task);
            var metadata = getMetadata(task, request);
            prepareAndSendReport(metadata, task, request);
        } catch (Exception e) {
            LOGGER.error("|Alert| - Task manager - email runner  - Problem to generate email report for taskId: {}, exception: {}", task.getId(), e.getMessage(), e);
            errors.put("errorMessage", e.getMessage());
            emailReportProcessorService.setTaskFailure(task, errors);
        }
        task.setCompletedTime(new Date());
        task.setStatus(TaskStatus.COMPLETED);
        emailReportProcessorService.saveTask(task);
    }

    private CollectionRequest getCollectionRequest(Task task) {
        var filters = objectMapper.convertValue(task.getParams().get("filters"),
                new TypeReference<List<Filter>>() {
                }
        );
        var report = (String) task.getParams().get("report");
        return new CollectionRequest(report, filters);
    }

    private void prepareAndSendReport(Metadata metadata, Task task, CollectionRequest request) throws Exception {
        task.setPath(createTaskLocation(task.getId()));
        var data = getData(metadata, task, request);
        var emailTemplateData = emailReportProcessorService.getEmailTemplateData(task, metadata, request, data);
        var emailTemplate = getEmailTemplate(metadata);
        StringWriter htmlReport = emailReportProcessorService.prepareReportHtml(emailTemplate, emailTemplateData);
        emailReportProcessorService.sendEmail(htmlReport, emailTemplateData);
    }

    private String getEmailTemplate(Metadata metadata) {
        var emailTemplate = (String) CommonUtil.getFieldValue("emailTemplate", metadata.getEmailReportProperties(), "default_template.ftlh");
        LOGGER.info("Email template configured: {} ", emailTemplate);
        return emailTemplate;
    }

    private Metadata getMetadata(Task task, CollectionRequest request) {
        return emailReportProcessorService.getMetadata(task);
    }

    private List<DBObject> getData(Metadata metadata, Task task, CollectionRequest request) throws IOException {
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

    private String createTaskLocation(String taskId) {
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
                throw new RuntimeException("Task manager - task runner - Problem to create report folder path " + e.getMessage());
            }
        }
        return reportFolderPath;
    }
}
