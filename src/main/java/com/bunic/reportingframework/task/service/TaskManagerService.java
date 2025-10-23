package com.bunic.reportingframework.task.service;

import com.bunic.reportingframework.exception.BunicInvalidConfigurationException;
import com.bunic.reportingframework.task.dao.TaskManagerDao;
import com.bunic.reportingframework.task.dto.TaskDto;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskScheduler;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.task.model.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class TaskManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerService.class);

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

}
