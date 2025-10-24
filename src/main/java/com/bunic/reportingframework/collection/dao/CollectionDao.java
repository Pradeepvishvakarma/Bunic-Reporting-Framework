package com.bunic.reportingframework.collection.dao;

import com.bunic.reportingframework.collection.model.Metadata;
import com.bunic.reportingframework.exception.BunicRuntimeException;
import com.bunic.reportingframework.task.model.Task;
import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.service.UserService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang3.StringUtils;
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

import static com.bunic.reportingframework.common.constant.Constant.THREE_STRING;

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

    public List<DBObject> getData(Metadata metadata, Task task, User user) throws BunicRuntimeException {
        try {
            Aggregation aggregation = Aggregation.newAggregation(getPipeLine(metadata).stream()
                    .map(stage -> (AggregationOperation) context -> stage)
                    .collect(Collectors.toList()));

            var results = primaryTemplate.aggregate(aggregation, metadata.getStore().getCollection(), Document.class);
            var data = results.getMappedResults().stream()
                    .map(BasicDBObject::new).map(doc -> (DBObject) doc)
                    .toList();
            LOGGER.info("fetched {} records from DB for report {}", data.size(), metadata.getName());
            var filteredData = filterData(data, user);
            LOGGER.info("after applying access level filter {} records are available for report {}", filteredData.size(), metadata.getName());
            return filteredData;
        } catch (Exception e){
            throw new BunicRuntimeException(String.format("Invalid pipeline configure for report %s ", metadata.getName()), e);
        }
    }

    private List<DBObject> filterData(List<DBObject> rawData, User user){
        if(user.getAccessLevel().equalsIgnoreCase("GLOBAL")){
            return rawData;
        }
        var accessLevel = user.getAccessLevel();
        if(StringUtils.equalsIgnoreCase(accessLevel, "REGION")){
            var regionAccess = user.getRegionAccess();
            rawData = rawData.stream().filter(dbObject -> {
                if(regionAccess.contains(dbObject.get("region")) || regionAccess.contains(dbObject.get("REGION"))){
                    return true;
                }
                return false;
            }).toList();
        } else if (StringUtils.equalsIgnoreCase(accessLevel, "DESK") || StringUtils.equalsIgnoreCase(accessLevel, "COUNTRY")) {
            var deskAccess = user.getDeskAccess();
            rawData = rawData.stream().filter(dbObject -> deskAccess.contains(dbObject.get("desk")) || deskAccess.contains(dbObject.get("country"))).toList();
        }
        return rawData;
    }

    private List<Document> getPipeLine(Metadata metadata) throws Exception{
        if(metadata == null || metadata.getStore() == null || metadata.getStore().getQueryFile() == null){
            throw new RuntimeException("Metadata or query file path is null");
        }
        var queryFilePath = String.format(THREE_STRING,"src/main/resources/query/",metadata.getStore().getQueryFile(),".json");
        String json = new String(Files.readAllBytes(Paths.get(queryFilePath)), StandardCharsets.UTF_8);

        return Document.parse("{\"pipeline\":" + json + "}").getList("pipeline", Document.class);
    }



}
