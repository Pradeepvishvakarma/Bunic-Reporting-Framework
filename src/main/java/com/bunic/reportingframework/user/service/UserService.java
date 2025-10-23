package com.bunic.reportingframework.user.service;

import com.bunic.reportingframework.user.dao.UserDao;
import com.bunic.reportingframework.user.model.User;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    public User getUserByUserId(String userId) {
        var user = userDao.getUserByUserId(userId);
        if (user == null) {
            LOGGER.info("User not found for userId {}", userId);
        } else {
            LOGGER.info("UserId {} found User {}", userId, user);
        }
        return user;
    }

    public String registerUser(User user){
        var validationParams = validateUser(user);
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


    public List<User> getAllUsers() {
        return userDao.getUsers();
    }

    public Optional<User> getUserById(String userId) {
        return Optional.of(userDao.getUserByUserId(userId));
    }

//    public String updateUser(String id, User updatedUser) {
//        return userRepository.findById(id).map(existing -> {
//            existing.setFullName(updatedUser.getFullName());
//            existing.setEmail(updatedUser.getEmail());
//            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
//                existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
//            }
//            existing.setRole(updatedUser.getRole());
//            userRepository.save(existing);
//            return "User updated successfully!";
//        }).orElse("User not found!");
//    }

    public String deleteUser(String userId) {
        if (userDao.getUserByUserId(userId) != null) {
            return "User not found!";
        }
        userDao.deleteUserByUserId(userId);
        return "User deleted successfully!";
    }
}
