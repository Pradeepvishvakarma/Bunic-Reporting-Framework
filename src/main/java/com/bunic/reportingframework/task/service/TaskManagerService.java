package com.bunic.reportingframework.task.service;

import com.bunic.reportingframework.collection.model.Filter;
import com.bunic.reportingframework.common.util.CommonUtil;
import com.bunic.reportingframework.email.model.EmailSubscription;
import com.bunic.reportingframework.exception.BunicInvalidConfigurationException;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.dto.TaskDto;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskScheduler;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.task.model.TaskType;
import com.bunic.reportingframework.user.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.bunic.reportingframework.common.constant.Constant.THREE_STRING_WITH_SPACE;

@Service
public class TaskManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerService.class);

    private static final List<String> TIMEZONES = Arrays.asList(TimeZone.getAvailableIDs());
    private static final String MSG_INVALID_CRON_EXPRESSION = "Scheduler Id {} configured with invalid cronTriggerTime: {} or cronTimeZone: {}";

    private final KafkaTemplate<String, Task> emailTaskKafkaTemplate;

    @Autowired
    private PivotTableService pivotTableService;

    @Autowired
    public TaskManagerService(KafkaTemplate<String, Task> emailTaskKafkaTemplate) {
        this.emailTaskKafkaTemplate = emailTaskKafkaTemplate;
    }

    @Autowired
    private TaskManagerDao taskManagerDao;

    @Value("bunic.reportingframework.email.report.path")
    private String reportFilePath;

    public Map<String, Object> getTaskParams(TaskScheduler scheduler) {
        var params = scheduler.getParams();
        if (null != params && !params.isEmpty()) {
            return params;
        }
        return params;
    }

    public void saveTask(TaskScheduler scheduler, Task task) throws Exception {
        saveAndSchedulerTask(task);
    }
    public TaskDto saveTask(TaskDto taskDto) throws Exception {
        Task task = new Task();
        task.setUserId(taskDto.getUserId());
        BeanUtils.copyProperties(taskDto, task);
        Task daoTask = saveAndSchedulerTask(task);
        BeanUtils.copyProperties(daoTask, taskDto);
        return taskDto;
    }

    public String getTimesByCronTriggerTime(String cronTriggerTime){
        return "";
    }

    public Task saveAndSchedulerTask(Task task) throws Exception {
        task.setStatus(TaskStatus.PENDING);
        task.setDateTime(new Date());
        taskManagerDao.saveTask(task);
        schedule(task);
        return task;
    }

    private void schedule(Task task) throws Exception {
        if(null == task){
            throw new BunicInvalidConfigurationException("Task to schedule cannot be null");
        }
        LOGGER.info("Task manager publishing task {}", task);
        sendMessage(task);
    }

    private void sendMessage(Task task) throws Exception {
        LOGGER.info("Task manager send message task {} {}", task.getType(), task.getId());
        CompletableFuture<SendResult<String, Task>> future;
        if(TaskType.EMAIL_REPORT.equals(task.getType())){
            future = emailTaskKafkaTemplate.send("bunic-email-topic-test", task.getId(), task);
        } else {
            throw new BunicInvalidConfigurationException("Task type is Invalid");
        }
        LOGGER.info("Task Manager Service - sendMessage task {} completed", task);
        future.whenComplete((result, ex) -> {
            if(null == ex){
                LOGGER.info("Task manager published message {}", task);
            } else {
                throw new KafkaException(String.format("Task Manager Service - error while publishing task [%s] due to [%s]", task.getType(), ex.getMessage()), ex);
            }
        });
    }

    public Task getTaskById(String taskId){
        return taskManagerDao.getTaskById(taskId);
    }

    public List<TaskScheduler> getSchedulers(){
        return taskManagerDao.getSchedulers();
    }

    public TaskScheduler getSchedulerBySchedulerId(String schedulerId){
        return taskManagerDao.findSchedulerBySchedulerId(schedulerId);
    }

    public void triggerEmailOnAdminRequest(String schedulerId, Notification notifaction) {
        var scheduler = taskManagerDao.findSchedulerBySchedulerId(schedulerId);
        if(scheduler == null || scheduler.getActive().equalsIgnoreCase("N")){
            notifaction.setStatus(TaskStatus.FAILED);
            notifaction.setMessage(String.format(THREE_STRING_WITH_SPACE,"Email Subscription is Disable for SchedulerId ", schedulerId, " - Enable it to Generate Email!"));
            return;
        }
        try {
            LOGGER.info("trigger email on admin trigger - Scheduler {} - running triggerTime: {} with timeZone: {}", scheduler.getId(), scheduler.getCronTriggerTime(), scheduler.getCronTimeZone());
            var task = CommonUtil.toTask(scheduler);
            task.setParams(getTaskParams(scheduler));
            notifaction.setMessage(String.format(THREE_STRING_WITH_SPACE, "for scheduler id ", schedulerId, " - Email Generated successfully!"));
            notifaction.setStatus(TaskStatus.COMPLETED);
            saveTask(scheduler, task);
        } catch (Exception e) {
            LOGGER.error("trigger email on admin trigger - Scheduler {} - error for schedulerId: {}, report: {}, exception: {}, timestamp: {}",
                    scheduler.getSchedulerId(), scheduler.getUserId(), scheduler.getParams().get("report"),
                    getTimesByCronTriggerTime(scheduler.getCronTriggerTime()), e);
        }

    }

    public boolean isValidScheduler(TaskScheduler scheduler){
        if(scheduler == null){
            return false;
        }
        if(!CronExpression.isValidExpression(scheduler.getCronTriggerTime()) || !TIMEZONES.contains(scheduler.getCronTimeZone())){
            LOGGER.error(MSG_INVALID_CRON_EXPRESSION, scheduler.getId(), scheduler.getCronTriggerTime(), scheduler.getCronTimeZone());
            return false;
        }
        return true;
    }

    public String deleteSchedulerBySchedulerId(String schedulerId) {
        taskManagerDao.deleteSchedulerBySchedulerId(schedulerId);
        return "Email Subscription deleted successfully!";
    }

    public void updateSchedulerId(TaskScheduler scheduler, EmailSubscription emailSubscription) {
        var newScheduler = new TaskScheduler();
        newScheduler.setSchedulerId(emailSubscription.getSchedulerId());
        newScheduler.setType(TaskType.EMAIL_REPORT);
        newScheduler.setActive(scheduler.getActive());
        newScheduler.setParams(prepareParams(emailSubscription));
        newScheduler.setUserId(scheduler.getUserId());
        newScheduler.setId(scheduler.getId());
        newScheduler.setCreatedBy("Pradeep");
        newScheduler.setCronTriggerTime(emailSubscription.getCronTriggerTime());
        newScheduler.setCronTimeZone(emailSubscription.getTimeZone());

        System.out.println("existing scheduler :- "+scheduler);
        System.out.println("update email subs  :- "+emailSubscription);
        System.out.println("new scheduler :- "+newScheduler);

        taskManagerDao.deleteSchedulerBySchedulerId(scheduler.getSchedulerId());
        taskManagerDao.saveScheduler(newScheduler);

    }

    public Map<String, Object> prepareParams(EmailSubscription emailSubscription){
        var report = emailSubscription.getReportCode();
        var emailBodyReport = emailSubscription.isEnableBodyReport();
        var includeExcelAttachment = emailSubscription.isIncludeExcelAttachment();
        var filters = getFilters();
        var map = new HashMap<String, Object>();
        map.put("report", report);
        map.put("emailBodyReport", emailBodyReport);
        map.put("includeExcelAttachment", includeExcelAttachment);
        map.put("filters", filters);
        return map;
    }

    public List<Filter> getFilters(){
        var filters = new ArrayList<Filter>();
        filters.add(new Filter("sAsOfDate", "eq", "20251027"));
        return filters;
    }
}
