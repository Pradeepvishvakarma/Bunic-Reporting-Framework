package com.bunic.reportingframework.task.scheduler;

import com.bunic.reportingframework.common.service.EventService;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskScheduler;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.task.service.TaskManagerService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

@Component
public class TaskManagerScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerScheduler.class);
    private static final List<String> TIMEZONES = Arrays.asList(TimeZone.getAvailableIDs());
    private static final String MSG_INVALID_CRON_EXPRESSION = "Scheduler Id {} configured with invalid cronTriggerTime: {} or cronTimeZone: {}";

    @Autowired
    private TaskManagerDao taskManagerDao;

    @Autowired
    private TaskManagerService taskManagerService;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    private EventService eventService;
    private List<ScheduledFuture<?>> runningJobs;
    private ScheduledFuture<?> runningTimer;

    @PostConstruct
    private void init(){
        initSchedulers();
    }

    private void initSchedulers(){
        LOGGER.info("initSchedulers - initializing schedulers");
        cancel();
        schedule();
    }

    private void cancel(){
        LOGGER.info("Task Manager Scheduler - cancel running tasks");
        runningJobs.forEach(job -> job.cancel(true));
    }

    private void schedule() {
        runningJobs = new ArrayList<>();
        taskManagerDao.getActiveSchedulers().forEach(scheduler -> {
            if (isValidScheduler(scheduler)) {
                LOGGER.info("Task Manager Scheduler {} - scheduling triggerTime: {} with timeZone: {}", scheduler.getId(), scheduler.getCronTriggerTime(), scheduler.getCronTimeZone());
                runningJobs.add(taskScheduler.schedule(() -> {
                    try {
                        LOGGER.info("Task Manager Scheduler {} - running triggerTime: {} with timeZone: {}", scheduler.getId(), scheduler.getCronTriggerTime(), scheduler.getCronTimeZone());
                        var task = toTask(scheduler);
                        task.setParams(taskManagerService.getTaskParams(scheduler));
                        taskManagerService.saveTask(scheduler, task);
                    } catch (Exception e){
                        LOGGER.error("Task Manager Scheduler {} - error for schedulerId: {}, report: {}, exception: {}, timestamp: {}",
                                scheduler.getSchedulerId(), scheduler.getUserId(), scheduler.getParams().get("report"),
                                taskManagerService.getTimesByCronTriggerTime(scheduler.getCronTriggerTime()), e);
                    }
                }, new CronTrigger(scheduler.getCronTriggerTime(), TimeZone.getTimeZone(scheduler.getCronTimeZone()))));
            }
        });
    }

    private boolean isValidScheduler(TaskScheduler scheduler){
        if(scheduler == null){
            return false;
        }
        if(!CronExpression.isValidExpression(scheduler.getCronTimeZone()) || !TIMEZONES.contains(scheduler.getCronTimeZone())){
            LOGGER.error(MSG_INVALID_CRON_EXPRESSION, scheduler.getId(), scheduler.getCronTriggerTime(), scheduler.getCronTimeZone());
            return false;
        }
        return true;
    }

    private Task toTask(TaskScheduler scheduler){
        var task = new Task();
        task.setType(scheduler.getType());
        task.setReferenceId(scheduler.getId());
        task.setStatus(TaskStatus.PENDING);
        return task;
    }
}
