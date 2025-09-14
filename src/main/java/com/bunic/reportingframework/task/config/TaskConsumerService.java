package com.bunic.reportingframework.task.config;

import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.task.runner.EmailReportTaskRunner;
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

    @KafkaListener(topics = "bunic-email-topic-test", groupId = "bunic-email-group-test-${random.uuid}")
    public void consumeTask(Task task) throws Exception {
        task.setStatus(TaskStatus.STARTED);
        LOGGER.info("Received TaskId from Kafka Consumer: {}", task);
        emailReportTaskRunner.run(task);
    }
}