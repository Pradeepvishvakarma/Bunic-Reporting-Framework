package com.bunic.reportingframework.task.dao;

import com.bunic.reportingframework.email.model.EmailSubscription;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.task.model.TaskScheduler;
import com.bunic.reportingframework.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TaskManagerDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerDao.class);

    @Autowired
    @Qualifier("primaryTemplate")
    private MongoTemplate primaryTemplate;

    public List<TaskScheduler> getActiveSchedulers(){
        LOGGER.info("Read configured active schedulers");
        Query findQuery = new Query();
        findQuery.addCriteria(Criteria.where("active").is("Y"));
        return primaryTemplate.find(findQuery, TaskScheduler.class, "ReportingFrameworkScheduler");
    }

    public List<TaskScheduler> getSchedulers(){
        LOGGER.info("Read configured schedulers");
        return primaryTemplate.findAll(TaskScheduler.class, "ReportingFrameworkScheduler");
    }

//    public void updateSchedulerBySchedulerId(String schedulerId){
//        LOGGER.info("Update scheduler by schedulerId: {}",schedulerId);
//        Query findQuery = new Query();
//        findQuery.addCriteria(Criteria.where("schedulerId").is(schedulerId));
//        return primaryTemplate.upsert(findQuery, );
////        findAll(TaskScheduler.class, "ReportingFrameworkScheduler");
//    }

    public void saveTask(Task task){
        LOGGER.info("Task Manager save task {}", task);
        primaryTemplate.save(task, "ReportingFrameworkTask");
    }

    public Task getTaskById(String taskId){
        LOGGER.info("Get Task by id: {}", taskId);
        Query findQuery = new Query();
        findQuery.addCriteria(Criteria.where("_id").is(taskId));
        return primaryTemplate.findOne(findQuery, Task.class, "ReportingFrameworkTask");
    }

    public TaskScheduler findSchedulerBySchedulerId(String schedulerId) {
        LOGGER.info("Get TaskScheduler by scheduler id: {}", schedulerId);
        Query findQuery = new Query();
        findQuery.addCriteria(Criteria.where("schedulerId").is(schedulerId));
        return primaryTemplate.findOne(findQuery, TaskScheduler.class, "ReportingFrameworkScheduler");
    }

    public void deleteSchedulerBySchedulerId(String schedulerId) {
        LOGGER.info("delete requested Task scheduler {}", schedulerId);
        Query query = new Query();
        query.addCriteria(Criteria.where("schedulerId").is(schedulerId));
        primaryTemplate.remove(query, "ReportingFrameworkScheduler");
    }

    public void saveScheduler(TaskScheduler scheduler) {
        LOGGER.info("Task Manager save scheduler {}", scheduler);
        primaryTemplate.save(scheduler, "ReportingFrameworkScheduler");
    }
}
