package com.bunic.reportingframework.user.dao;

import com.bunic.reportingframework.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
    private static final String COLLECTION_USERS = "Users";

    @Autowired
    @Qualifier("primaryTemplate")
    private MongoTemplate primaryTemplate;

    public User getUserByUserId(String userId){
        LOGGER.info("fetch user {} from db", userId);
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        return primaryTemplate.findOne(query, User.class, COLLECTION_USERS);
    }

    public User getUserByEmailId(String emailId){
        LOGGER.info("fetch user {} from db", emailId);
        Query query = new Query();
        query.addCriteria(Criteria.where("emailId").is(emailId));
        return primaryTemplate.findOne(query, User.class, COLLECTION_USERS);
    }

    public void saveUser(User user) {
        LOGGER.info("Add requested user {}", user);
        primaryTemplate.save(user, COLLECTION_USERS);
    }

    public void deleteUserByUserId(String userId) {
        LOGGER.info("delete requested userId {}", userId);
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        primaryTemplate.remove(query, COLLECTION_USERS);
    }

    public List<User> getUsers(){
        LOGGER.info("fetch all users from db");
        return primaryTemplate.findAll(User.class, COLLECTION_USERS);
    }

    public User getUserByUserIdAndPassword(String userId, String password) {
        LOGGER.info("fetch user by userId: {}, password: {} from db", userId, password);
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId).and("password").is(password));
        return primaryTemplate.findOne(query, User.class, COLLECTION_USERS);
    }

    public User getUserByUserType(String userType) {
        LOGGER.info("fetch user by userType: {} from db", userType);
        Query query = new Query();
        query.addCriteria(Criteria.where("userType").is(userType));
        return primaryTemplate.findOne(query, User.class, COLLECTION_USERS);
    }

    public List<User> getAdminUsers() {
        LOGGER.info("fetch All admin users from db");
        Query query = new Query();
        query.addCriteria(Criteria.where("userType").is("ADMIN"));
        return primaryTemplate.find(query, User.class, COLLECTION_USERS);
    }

    public void updateUserTypeByUserId(String userId, User user) {
        String newAccessType = "ADMIN".equals(user.getUserType()) ? "USER" : "ADMIN";
        Update update = new Update().set("userType", newAccessType);
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        primaryTemplate.updateFirst(query, update, User.class, COLLECTION_USERS);
    }
}
