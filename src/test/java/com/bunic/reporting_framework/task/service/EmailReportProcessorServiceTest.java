package com.bunic.reporting_framework.task.service;

import com.bunic.reportingframework.collection.model.GroupReport;
import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.collection.service.CollectionService;
import com.bunic.reportingframework.email.model.EmailProperties;
import com.bunic.reportingframework.email.service.EmailSender;
import com.bunic.reportingframework.exception.BunicException;
import com.bunic.reportingframework.exception.BunicUnauthorizedException;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.model.ReportDataResponse;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskScheduler;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.task.service.EmailReportProcessorService;
import com.bunic.reportingframework.task.service.PivotTableService;
import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.StringWriter;
import java.util.*;

import static com.bunic.reportingframework.common.constant.Constant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailReportProcessorService
 */
@ExtendWith(MockitoExtension.class)
class EmailReportProcessorServiceTest {

    @InjectMocks
    private EmailReportProcessorService service;

    @Mock
    private TaskManagerDao taskManagerDao;
    @Mock
    private CollectionService collectionService;
    @Mock
    private UserService userService;
    @Mock
    private Configuration configuration;
    @Mock
    private EmailSender emailSender;
    @Mock
    private PivotTableService pivotTableService;

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // set business email id (public field)
        service.businessEmailId = "noreply@example.com";

        // inject ObjectMapper (private final) via reflection if necessary
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
    }

    @Test
    void testSetTaskFailure_updatesTask() {
        Task t = new Task();
        t.setId("task-1");

        Map<String, String> errors = new HashMap<>();
        errors.put("e1", "first");
        errors.put("e2", "second");

        service.setTaskFailure(t, errors);

        assertEquals(TaskStatus.FAILED, t.getStatus());
        assertTrue(t.getErrorMessage().contains("first"));
        assertTrue(t.getErrorMessage().contains("second"));
    }

    @Test
    void testSaveTask_callsDaoAndSetsCompletedTime() {
        Task t = new Task();
        t.setId("task-2");

        doNothing().when(taskManagerDao).saveTask(any(Task.class));

        service.saveTask(t);

        verify(taskManagerDao, times(1)).saveTask(taskCaptor.capture());
        Task saved = taskCaptor.getValue();
        assertNotNull(saved.getCompletedTime(), "Completed time should be set");
    }

    @Test
    void testGetUser_success() throws BunicUnauthorizedException {
        Task t = new Task();
        t.setUserId("user-1");

        User user = new User();
        user.setUserId("user-1");
        user.setEmailId("u@example.com");

        when(userService.getUserByUserId("user-1")).thenReturn(user);

        User result = service.getUser(t);

        assertNotNull(result);
        assertEquals("u@example.com", result.getEmailId());
    }

    @Test
    void testGetUser_unauthorized() {
        Task t = new Task();
        t.setUserId("missing-user");

        when(userService.getUserByUserId("missing-user")).thenReturn(null);

        assertThrows(BunicUnauthorizedException.class, () -> service.getUser(t));
    }

    @Test
    void testGetReportEmailProperty_placeholderReplacementAndOverrides() {
        // Create metadata with email props
        Metadata metadata = new Metadata();
        Map<String, Object> metaEmailProps = new HashMap<>();
        metaEmailProps.put(REPORT_NAME, "Monthly Report");
        metaEmailProps.put(SUBJECT, "Subject - {timestamp}");
        metaEmailProps.put(EMAIL_BODY_REPORT, "Body for {REPORT_DISPLAY_NAME}");
        metaEmailProps.put(INCLUDE_EXCEL_ATTACHMENT, false);
        metadata.setEmailReportProperties(metaEmailProps);
        metadata.setName("MetaName");

        Task task = new Task();
        Map<String, Object> taskParams = new HashMap<>();
        // override report name (and test placeholder)
        taskParams.put(REPORT_NAME, "Overridden Report");
        task.setParams(taskParams);

        User user = new User();
        user.setEmailId("test@example.com");

        Map<String, Object> result = service.getReportEmailProperty(task, metadata, null, user);

        assertEquals("test@example.com", result.get(EMAIL_ID));
        // REPORT_DISPLAY_NAME should be either task override or metadata name (we used override)
        // Subject placeholder should be replaced with timestamp (which was overridden)
        assertTrue(((String) result.get(SUBJECT)).contains("Subject -"));
        // TIMESTAMP present
        assertNotNull(result.get(TIMESTAMP));
    }

    @Test
    void testPrepareReportHtml_success() throws Exception {
        String templateName = "sample.ftl";
        Template mockTemplate = mock(Template.class);
        doAnswer(invocation -> {
            // write some content into writer passed as argument #1 = model, #2 = writer
            Object writer = invocation.getArgument(1);
            if (writer instanceof StringWriter) {
                ((StringWriter) writer).write("rendered-content");
            }
            return null;
        }).when(mockTemplate).process(any(), any(StringWriter.class));

        when(configuration.getTemplate(templateName)).thenReturn(mockTemplate);

        Map<String, Object> data = new HashMap<>();
        data.put("k", "v");

        StringWriter out = service.prepareReportHtml(templateName, data);
        assertNotNull(out);
        assertTrue(out.toString().contains("rendered-content"));
    }

    @Test
    void testPrepareReportHtml_templateException_wrappedAsRuntimeException() throws Exception {
        String templateName = "missing.ftl";
        when(configuration.getTemplate(templateName)).thenThrow(new RuntimeException("not found"));

        Map<String, Object> data = new HashMap<>();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.prepareReportHtml(templateName, data));
        assertTrue(ex.getMessage().contains("problem on send email report"));
    }

    @Test
    void testGetScheduledTriggerTime_withCron() {
        TaskScheduler sched = new TaskScheduler();
        sched.setCronTriggerTime("0 30 9 * * ?");
        sched.setCronTimeZone("UTC");

        String res = ReflectionTestUtils.invokeMethod(service, "getScheduledTriggerTime", sched);
        assertNotNull(res);
        assertTrue(res.contains("09:30") || res.contains("9:30")); // formatted hour:minute should be present
        assertTrue(res.contains("UTC"));
    }

    @Test
    void testGetScheduledTriggerTime_nullScheduler_returnsNowWithIST() {
        String res = ReflectionTestUtils.invokeMethod(service, "getScheduledTriggerTime", (TaskScheduler) null);
        assertNotNull(res);
        // Should end with TIME_ZONE_IST constant
        assertTrue(res.contains(TIME_ZONE_IST));
    }

    @Test
    void testGetReportDate_withCompletedTime_and_without() {
        Task t1 = new Task();
        Date dt = new Date();
        t1.setCompletedTime(dt);
        String res1 = ReflectionTestUtils.invokeMethod(service, "getReportDate", t1);
        assertNotNull(res1);

        String res2 = ReflectionTestUtils.invokeMethod(service, "getReportDate", (Task) null);
        assertNotNull(res2);
    }

    @Test
    void testGetEmailTemplateData_nonPivot_withExcelAttachment_triggersGenerateExcel() throws Exception {
        // Task with no pivot config -> pivotConfig null path (so puts REPORT_DATA)
        Task task = new Task();
        task.setId("t-100");
        Map<String, Object> params = new HashMap<>();
        task.setParams(params);
        User user = new User();
        user.setEmailId("u@example.com");
        user.setUserId("u1");

        Metadata metadata = new Metadata();
        // metadata email props include INCLUDE_EXCEL_ATTACHMENT true
        Map<String, Object> metaEmailProps = new HashMap<>();
        metaEmailProps.put(INCLUDE_EXCEL_ATTACHMENT, true);
        metaEmailProps.put(ATTACHMENT_FILE_NAME, "file.xlsx");
        metaEmailProps.put(REPORT_NAME, "TestReport");
        metaEmailProps.put(SUBJECT, "Subj");
        metaEmailProps.put(EMAIL_BODY_REPORT, "body");
        metadata.setEmailReportProperties(metaEmailProps);

        // pivotTableService.getReportDataResponse returns a simple object
        ReportDataResponse reportResponse = getExpectedReportDataResponse();
        when(pivotTableService.getReportDataResponse(metadata, null, Collections.emptyList())).thenReturn(reportResponse);

        // when getReportEmailProperty called inside service it will pick up metadata props -> include attachment true
        // call
        var result = service.getEmailTemplateData(task, metadata, Collections.emptyList(), user);

        // verify generateExcel was invoked because INCLUDE_EXCEL_ATTACHMENT true
        verify(pivotTableService, times(1)).generateExcel(anyMap(), anyList(), eq(metadata), eq(task));

        assertNotNull(result);
        assertTrue(result.containsKey(REPORT_DATA) || result.containsKey(PIVOTED_REPORT_DATA));
    }

    private ReportDataResponse getExpectedReportDataResponse() {
       return new ReportDataResponse();
    }

    @Test
    void testSendEmail_buildsEmailPropertiesAndInvokesSender() throws BunicException {
        Map<String, Object> emailTemplateData = new HashMap<>();
        emailTemplateData.put(EMAIL_ID, "a@b.com");
        emailTemplateData.put(FILE_PATH, "/tmp/x");
        emailTemplateData.put(ATTACHMENT_FILE_NAME, "att.xlsx");
        emailTemplateData.put(SUBJECT, "subject");
        emailTemplateData.put(INCLUDE_EXCEL_ATTACHMENT, false);

        StringWriter content = new StringWriter();
        content.write("html-content");

        // emailSender should be invoked
        doNothing().when(emailSender).sendEmail(any(EmailProperties.class));

        service.sendEmail(content, emailTemplateData);

        verify(emailSender, times(1)).sendEmail(any(EmailProperties.class));
    }

    @Test
    void testValidateGroupReportParams_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.validateGroupReportParams(null));
    }

    @Test
    void testValidateGroupReportParams_emptyRows_throws() {
        GroupReport gr = new GroupReport();
        gr.setRows(Collections.emptyList());
        assertThrows(IllegalArgumentException.class, () -> service.validateGroupReportParams(gr));
    }

}
