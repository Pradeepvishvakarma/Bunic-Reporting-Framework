package com.bunic.reportingframework.user.service;

import com.bunic.reportingframework.user.dao.UserDao;
import com.bunic.reportingframework.user.model.User;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    public User getUserByUserId(String userId) {
        var user = userDao.getUser(userId);
        if (user == null) {
            LOGGER.info("User not found for userId {}", userId);
        } else {
            LOGGER.info("UserId {} found User {}", userId, user);
        }
        return user;
    }

    public String addUser(User user){
        var validationParams = validateUser(user);
        if(validationParams.isEmpty()) {
            var existingUser = getUserByUserId(user.getUserId());
            if(existingUser != null){
                return String.format("User %s already exists", user.getUserId());
            }
            user.setAccessLevel(StringUtil.isNotBlank(user.getAccessLevel())? user.getAccessLevel() : "GLOBAL");
            userDao.saveUser(user);
            LOGGER.info("User added Successfully");
            return "User added Successfully";
        }
        return String.format(String.join(", ",validationParams.values()));
    }

    private Map<String, String> validateUser(User user){
        var validationMap = new HashMap<String, String>();
        if (user == null) {
            validationMap.put("user", "requested user can not be null");
            return validationMap;
        } else {
            if (user.getUserId() == null) {
                validationMap.put("userId", "userId can not be empty");
            }
            if (user.getName() == null) {
                validationMap.put("name", "name can not be empty");
            }
            if (user.getEmailId() == null) {
                validationMap.put("emailId", "emailId can not be empty");
            }
        }
        return validationMap;
    }
}
