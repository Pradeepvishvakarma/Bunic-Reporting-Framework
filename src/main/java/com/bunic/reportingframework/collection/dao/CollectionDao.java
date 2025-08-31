package com.bunic.reportingframework.collection.dao;

import com.bunic.reportingframework.collection.model.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CollectionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionDao.class);

    @Autowired
    @Qualifier("primaryTemplate")
    private MongoTemplate primaryTemplate;

    public List<Metadata> getAllMetadata(){
        LOGGER.info("Read collection metadata");
        return primaryTemplate.findAll(Metadata.class, "ReportingFrameworkMetadata");
    }
}
