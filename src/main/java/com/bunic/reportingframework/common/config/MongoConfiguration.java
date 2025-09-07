package com.bunic.reportingframework.common.config;

import com.bunic.reportingframework.collection.model.Metadata;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Component;
import org.bson.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MongoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoConfiguration.class);

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @PostConstruct
    public void init() {
        LOGGER.info("mongodb configuration initialized");
    }

    @Bean("primaryTemplate")
    public MongoTemplate primaryMongoTemplate(@Value("${bunic.reportingframework.database.primary.name}") String databaseName) {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(formattedUri(databaseName)));
    }

    private String formattedUri(String databaseName) {
        return MessageFormat.format(uri, databaseName);
    }
}
