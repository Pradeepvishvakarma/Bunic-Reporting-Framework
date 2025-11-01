package com.bunic.reportingframework.email;

import com.bunic.reportingframework.collection.model.Filter;
import com.bunic.reportingframework.collection.service.CollectionService;
import com.bunic.reportingframework.email.model.EmailSubscription;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.model.TaskScheduler;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.task.model.TaskType;
import com.bunic.reportingframework.task.scheduler.TaskManagerScheduler;
import com.bunic.reportingframework.task.service.TaskManagerService;
import com.bunic.reportingframework.user.model.Notification;
import com.bunic.reportingframework.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bunic.reportingframework.common.constant.Constant.THREE_STRING;

@Service
public class EmailManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailManagementService.class);

    @Autowired
    UserService userService;

    @Autowired
    TaskManagerService taskManagerService;

    @Autowired
    CollectionService collectionService;

    @Autowired
    TaskManagerDao taskManagerDao;

    @Autowired
    TaskManagerScheduler taskManagerScheduler;

    public List<EmailSubscription> getAllEmailSubscriptions(){
        var emailSubscriptions = new ArrayList<EmailSubscription>();
        var schedulers = taskManagerService.getSchedulers();
        schedulers.stream().filter(Objects::nonNull).sorted(Comparator.comparing(scheduler -> Integer.parseInt(scheduler.getSchedulerId())))
                .forEach(scheduler ->
                {
                        emailSubscriptions.add(toEmailSubscription(scheduler));
                }
        );
        return emailSubscriptions;
    }

    public List<EmailSubscription> getEmailSubscriptionsByUserId(String userId){
        var emailSubscriptions = new ArrayList<EmailSubscription>();
        var schedulers = taskManagerService.getSchedulers();
        schedulers.stream().filter(Objects::nonNull).filter(scheduler -> scheduler.getUserId().equalsIgnoreCase(userId)).sorted(Comparator.comparing(scheduler -> Integer.parseInt(scheduler.getSchedulerId())))
                .forEach(scheduler ->
                        {
                            emailSubscriptions.add(toEmailSubscription(scheduler));
                        }
                );
        return emailSubscriptions;
    }

    public EmailSubscription getEmailSubscriptionsBySchedulerId(String schedulerId){
        var emailSubscriptions = new EmailSubscription();
        var scheduler = taskManagerService.getSchedulerBySchedulerId(schedulerId);
        return toEmailSubscription(scheduler);
    }

    public EmailSubscription toEmailSubscription(TaskScheduler scheduler){
        var emailSubscription = new EmailSubscription();
        emailSubscription.setActive(scheduler.getActive().equalsIgnoreCase("Y"));
        emailSubscription.setReportCode((String)scheduler.getParams().get("report"));
        emailSubscription.setReportName(getReportName(scheduler));
        emailSubscription.setCronTriggerTime(scheduler.getCronTriggerTime());
        emailSubscription.setSchedulerId(scheduler.getSchedulerId());
        emailSubscription.setUserId(scheduler.getUserId());
        emailSubscription.setTimeZone(scheduler.getCronTimeZone());
        emailSubscription.setEnableBodyReport(scheduler.getParams().get("emailBodyReport") != null && (boolean)scheduler.getParams().get("emailBodyReport"));
        emailSubscription.setIncludeExcelAttachment(scheduler.getParams().get("includeExcelAttachment") != null && (boolean)scheduler.getParams().get("includeExcelAttachment"));
        return emailSubscription;
    }

    public String getReportName(TaskScheduler scheduler) {
        String reportName = null;
        try {
            var metadata = collectionService.getMetadataByCode((String)scheduler.getParams().get("report"));
            reportName = metadata.getName() != null ? metadata.getName() : metadata.getCode();
        } catch (Exception e){
            LOGGER.error("Unable to get Report Name");
        }
        return reportName;
    }

    public void updateSetting(String schedulerId, String field, boolean value) {
        EmailSubscription sub = getAllEmailSubscriptions().stream().filter(emailSubscription -> emailSubscription.getSchedulerId().equalsIgnoreCase(schedulerId)).findFirst().orElse(null);
        if (sub != null) {
            if ("enableBodyReport".equals(field)) sub.setEnableBodyReport(value);
            else if ("includeExcelAttachment".equals(field)) sub.setIncludeExcelAttachment(value);
            else if ("active".equals(field)) sub.setActive(value);
            save(sub);
        }
    }

    public void save(EmailSubscription sub) {
        var scheduler = toScheduler(sub);
        taskManagerDao.deleteSchedulerBySchedulerId(scheduler.getSchedulerId());
        taskManagerDao.saveScheduler(scheduler);
    }

    private TaskScheduler toScheduler(EmailSubscription sub) {
        var scheduler = new TaskScheduler();
        scheduler.setActive(sub.isActive() ? "Y" : "N");
        scheduler.setId(sub.getSchedulerId());
        scheduler.setSchedulerId(sub.getSchedulerId());
        scheduler.setType(TaskType.EMAIL_REPORT);
        scheduler.setCronTriggerTime(sub.getCronTriggerTime());
        scheduler.setUserId(sub.getUserId());
        scheduler.setParams(getParams(sub));
        scheduler.setCronTimeZone(sub.getTimeZone());
        scheduler.setCreatedBy("Pradeep Kumar");
        return scheduler;
    }

    public Map<String ,Object> getParams(EmailSubscription subscription){
        var params = new HashMap<String, Object>();
        params.put("report", subscription.getReportCode());
        params.put("filters", getFilters());
        params.put("emailBodyReport", subscription.isEnableBodyReport());
        params.put("includeExcelAttachment", subscription.isIncludeExcelAttachment());
        return params;
    }

    public List<Filter> getFilters(){
        var filters = new ArrayList<Filter>();
        filters.add(new Filter("sAsOfDate", "eq", "20251027"));
        return filters;
    }

    public Map<String, String> validateEmailSubscription(EmailSubscription emailSubscription, boolean isEmailOnboardingViaGui) {
        var validationMap = new HashMap<String, String>();
        if (emailSubscription == null) {
            validationMap.put("user", "New Email Subscription can not be null");
            return validationMap;
        } else {
            if (emailSubscription.getUserId() == null && !isEmailOnboardingViaGui) {
                validationMap.put("userId", "userId can not be empty");
            }
            if (emailSubscription.getSchedulerId() == null && !isEmailOnboardingViaGui) {
                validationMap.put("schedulerId", "schedulerId can not be empty");
            }
            if (emailSubscription.getReportCode() == null) {
                validationMap.put("reportCode", "report code can not be empty");
            }
            if (emailSubscription.getCronTriggerTime() == null) {
                validationMap.put("reportTimings", "report timings can not be empty");
            }
            if (emailSubscription.getTimeZone() == null) {
                validationMap.put("timezone", "time zone can not be empty");
            }
        }
        return validationMap;
    }

    public Notification onBoardEmail(EmailSubscription emailSubscription) {
        var message = new Notification();
        emailSubscription.setSchedulerId(String.valueOf((int)(Math.random() * 9999)));
        emailSubscription.setActive(true);
        var newScheduler = toScheduler(emailSubscription);
        taskManagerDao.saveScheduler(newScheduler);
        message.setMessage(String.format(THREE_STRING,"New Email Subscription Added for UserId ", newScheduler.getUserId(), "!"));
        message.setStatus(TaskStatus.COMPLETED);
        taskManagerScheduler.refreshSchedulers();
        return message;
    }

}
