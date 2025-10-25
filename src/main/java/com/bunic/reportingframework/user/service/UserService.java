package com.bunic.reportingframework.user.service;

import com.bunic.reportingframework.task.model.TaskStatus;
import com.bunic.reportingframework.user.dao.UserDao;
import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.model.UserNotifaction;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.bunic.reportingframework.common.constant.Constant.*;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    public User getUserByUserId(String userId) {
        var user = userDao.getUserByUserId(userId.toUpperCase());
        if (user == null) {
            LOGGER.warn("User not found for userId {}", userId);
        } else {
            LOGGER.info("UserId {} found User {}", userId, user);
        }
        return user;
    }

    public boolean isUserAvailable(String userId, String password) {
        var user = userDao.getUserByUserIdAndPassword(userId.toUpperCase(), password);
        if (user == null) {
            LOGGER.warn("User not found for userId: {} and provided password: {}", userId, password);
            return false;
        } else {
            LOGGER.info("UserId: {} found User: {}", userId, user);
            return true;
        }
    }

    public User getUser(String userId, String password) {
        var user = userDao.getUserByUserIdAndPassword(userId.toUpperCase(), password);
        if (user == null) {
            LOGGER.warn("User not found for userId: {} and provided password: {}", userId, password);
        } else {
            LOGGER.info("UserId: {} found User: {}", userId, user);
        }
        return user;
    }



    public String registerUser(User user){
        var validationParams = validateUser(user, false);
        if(validationParams.isEmpty()) {
            var existingUser = getUserByUserId(user.getUserId());
            if(existingUser != null){
                return String.format("User %s already exists", user.getUserId());
            }
            user.setAccessLevel(StringUtil.isNotBlank(user.getAccessLevel())? user.getAccessLevel() : "GLOBAL");
            userDao.saveUser(user);
            LOGGER.info("User registered Successfully");
            return "User registered Successfully";
        }
        return String.format(String.join(", ",validationParams.values()));
    }

    public Map<String, String> validateUser(User user, boolean isUserOnboardingViaGui) {
        var validationMap = new HashMap<String, String>();
        if (user == null) {
            validationMap.put("user", "requested user can not be null");
            return validationMap;
        } else {
            if (user.getUserId() == null && !isUserOnboardingViaGui) {
                validationMap.put("userId", "userId can not be empty");
            }
            if (user.getFirstName() == null) {
                validationMap.put("name", "name can not be empty");
            }
            if (user.getEmailId() == null) {
                validationMap.put("emailId", "emailId can not be empty");
            }
        }
        return validationMap;
    }


    public List<User> getAllUsers() {
        return userDao.getUsers();
    }

    public Optional<User> getUserById(String userId) {
        return Optional.of(userDao.getUserByUserId(userId));
    }

    public String deleteUser(String userId) {
        userDao.deleteUserByUserId(userId);
        return "User deleted successfully!";
    }

    public UserNotifaction onBoardUser(User user) {
        var message = new UserNotifaction();
        var  existingUser = userDao.getUserByEmailId(user.getEmailId());
        if (existingUser != null){
            message.setMessage(String.format(THREE_STRING,"User ", getUserName(user), " with emailId "+ user.getEmailId() + " already exists"));
            message.setStatus(TaskStatus.FAILED);
            return message;
        } else {
            user.setUserId(createUserId(user));
        }
        if(user.getAccessLevel() == null){
            user.setAccessLevel("GLOBAL");
        }
        if(user.getUserType() == null){
            user.setUserType("USER");
        }
        userDao.saveUser(user);
        message.setMessage(String.format(THREE_STRING,"Welcome aboard, ", getUserName(user), "!"));
        message.setStatus(TaskStatus.COMPLETED);
        message.setDesc(String.format(TWO_STRING, "Your user ID is ", user.getUserId()));
        return message;
    }

    public String getUserName(User user) {
        return String.format(TWO_STRING_WITH_SPACE, user.getFirstName(), user.getLastName());
    }

    public String createUserId(User user) {
        var firstNamePart = user.getFirstName().substring(0,1).toUpperCase();
        var lastNamePart = user.getLastName().substring(0,1).toUpperCase();
        var randomNumber = (int)(Math.random() * 99999);
        return firstNamePart + lastNamePart + randomNumber;
    }

    public void updateUserDetails(User existingUser, User newUserDetails) {
        newUserDetails.setUserId(newUserDetails.getUserId().toUpperCase());
        newUserDetails.setUserType(existingUser.getUserType() == null ? "USER" : existingUser.getUserType());
        userDao.deleteUserByUserId(existingUser.getUserId());
        userDao.saveUser(newUserDetails);
    }

    public boolean isAdminUser(String userId) {
        return false;
    }

    public User getAdminUser() {
        var adminUser = userDao.getUserByUserType("ADMIN");
        if (adminUser == null) {
            LOGGER.warn("Admin User not found");
        } else {
            LOGGER.info("Found Admin User: {}", adminUser);
        }
        return adminUser;
    }

    public void toggleAdminAccess(String userId) {
        User user = getUserByUserId(userId);
        if (user != null) {
            userDao.updateUserTypeByUserId(userId, user);
        }
    }
}
