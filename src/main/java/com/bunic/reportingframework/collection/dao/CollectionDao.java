package com.bunic.reportingframework.collection.dao;

import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.user.service.UserService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CollectionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionDao.class);

    @Autowired
    @Qualifier("primaryTemplate")
    private MongoTemplate primaryTemplate;

    @Autowired
    private UserService userService;

    public List<Metadata> getAllMetadata(){
        LOGGER.info("Read all collection metadata");
        return primaryTemplate.findAll(Metadata.class, "ReportingFrameworkMetadata");
    }

    public void SaveAllMetadata(List<Metadata> metadataList) {
        primaryTemplate.dropCollection("ReportingFrameworkMetadata");
        LOGGER.info("Save all collection metadata");
        for (Metadata metadata : metadataList) {
            primaryTemplate.save(metadata, "ReportingFrameworkMetadata");
        }
    }

    public List<DBObject> getData(Metadata metadata, Task task) {
        try {
            Aggregation aggregation = Aggregation.newAggregation(getPipeLine(metadata).stream()
                    .map(stage -> (AggregationOperation) context -> stage)
                    .collect(Collectors.toList()));

            var results = primaryTemplate.aggregate(aggregation, metadata.getStore().getCollection(), Document.class);
            var data = results.getMappedResults().stream()
                    .map(BasicDBObject::new).map(doc -> (DBObject) doc)
                    .toList();
            return filterData(data, metadata, task);
        } catch (Exception e){
            throw  new RuntimeException(String.format("Invalid pipeline configure for report %s ", metadata.getName()));
        }
    }


    private List<DBObject> filterData(List<DBObject> rawData, Metadata metadata, Task task){
        var user = userService.getUserByUserId(task.getUserId());
        if(metadata.getAccessLevel() == null || user.getAccessLevel().equalsIgnoreCase("GLOBAL")){
            return rawData;
        }
        var accessLevel = metadata.getAccessLevel().toLowerCase();
        if(user.getEntitlementAccess().containsKey(accessLevel)){
//            var ss = (List<String>) user.getEntitlementAccess().get(accessLevel);
//            rawData.stream().filter(dbObject -> ss.contains(dbObject.containsField()
        }
        return rawData;
    }

    private List<Document> getPipeLine(Metadata metadata) throws Exception{
        if(metadata == null || metadata.getStore() == null || metadata.getStore().getQueryFile() == null){
            throw new RuntimeException("Metadata or query file path is null");
        }
        var queryFilePath = String.format("%s%s%s","src/main/resources/query/",metadata.getStore().getQueryFile(),".json");
        String json = new String(Files.readAllBytes(Paths.get(queryFilePath)), StandardCharsets.UTF_8);

        return Document.parse("{\"pipeline\":" + json + "}").getList("pipeline", Document.class);
    }



}
