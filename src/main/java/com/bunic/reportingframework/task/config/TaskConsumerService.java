package com.bunic.reportingframework.task.config;

import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.runner.EmailReportTaskRunner;
import com.bunic.reportingframework.task.service.TaskManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TaskConsumerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskConsumerService.class);

    @Autowired
    private EmailReportTaskRunner emailReportTaskRunner;

    @Autowired
    private TaskManagerService taskManagerService;

    @KafkaListener(topics = "bunic-email-topic", groupId = "bunic-email-group")
    public void consumeTask(String taskId) {
        LOGGER.info("Received TaskId from Kafka Consumer: {}", taskId);
        Task task = taskManagerService.getTaskById(taskId);
        emailReportTaskRunner.run(task);
    }
}