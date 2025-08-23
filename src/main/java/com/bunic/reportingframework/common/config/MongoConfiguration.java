package com.bunic.reportingframework.common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

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
