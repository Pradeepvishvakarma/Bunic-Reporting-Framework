package com.bunic.reportingframework.user.dao;

import com.bunic.reportingframework.collection.model.Metadata;
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
public class UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
    private static final String COLLECTION_USERS = "Users";

    @Autowired
    @Qualifier("primaryTemplate")
    private MongoTemplate primaryTemplate;

    public User getUser(String userId){
        LOGGER.info("fetch userId {} from db", userId);
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return primaryTemplate.findOne(query, User.class, COLLECTION_USERS);
    }

    public void saveUser(User user) {
        LOGGER.info("Add requested user {}", user);
        primaryTemplate.save(user, COLLECTION_USERS);
    }
}
