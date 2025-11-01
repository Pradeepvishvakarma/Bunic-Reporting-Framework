package com.bunic.reportingframework.task.scheduler;

import com.bunic.reportingframework.common.util.CommonUtil;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.service.TaskManagerService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

@Component
public class TaskManagerScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerScheduler.class);

    @Autowired
    private TaskManagerDao taskManagerDao;

    @Autowired
    private TaskManagerService taskManagerService;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    private List<ScheduledFuture<?>> runningJobs = new ArrayList<>();;
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

    public void refreshSchedulers(){
        LOGGER.info("refresh scheduler - initializing all schedulers");
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
            if (taskManagerService.isValidScheduler(scheduler)) {
                LOGGER.info("Task Manager Scheduler {} - scheduling triggerTime: {} with timeZone: {}", scheduler.getId(), scheduler.getCronTriggerTime(), scheduler.getCronTimeZone());
                runningJobs.add(taskScheduler.schedule(() -> {
                    try {
                        LOGGER.info("Task Manager Scheduler {} - running triggerTime: {} with timeZone: {}", scheduler.getId(), scheduler.getCronTriggerTime(), scheduler.getCronTimeZone());
                        var task = CommonUtil.toTask(scheduler);
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

}