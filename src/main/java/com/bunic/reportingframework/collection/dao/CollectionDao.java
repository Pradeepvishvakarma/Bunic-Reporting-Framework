package com.bunic.reportingframework.collection.dao;

import com.bunic.reportingframework.collection.model.Metadata;
import com.mongodb.DBObject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import java.io.IOException;
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

    public List<DBObject> getData(Metadata metadata) throws IOException {
        if(metadata == null || metadata.getStore() == null || metadata.getStore().getQueryFile() == null){
            throw new RuntimeException("Metadata or query file path is null");
        }
        var databaseName = metadata.getStore().getDatabase();
        var collectionName = metadata.getStore().getCollection();

        var queryFilePath = String.format("%s%s%s","src/main/resources/query/",metadata.getStore().getQueryFile(),".json");
        String json = new String(Files.readAllBytes(Paths.get(queryFilePath)), StandardCharsets.UTF_8);

        List<Document> pipeline = Document.parse("{\"pipeline\":" + json + "}").getList("pipeline", Document.class);

        Aggregation aggregation = Aggregation.newAggregation((List<? extends AggregationOperation>) pipeline.stream()
                .map(stage -> (AggregationOperation) context -> stage)
                .collect(Collectors.toList()));

        AggregationResults<Document> results = primaryTemplate.aggregate(aggregation, collectionName, Document.class);
        List<Document> data = results.getMappedResults();
        return results.getMappedResults().stream()
                .map(document -> com.mongodb.BasicDBObject.parse(document.toJson()))
                .collect(Collectors.toList());
    }

}
