package com.bunic.reportingframework.user.controller;

import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @GetMapping("/getUser")
    @Operation(summary = "get user by userId")
    public User getUser(@RequestParam String userId) {
        return userService.getUserByUserId(userId);
    }

    @PostMapping("/addUser")
    @Operation(summary = "add user")
    public String addUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @GetMapping
    @Operation(summary = "get All users")
    @PostMapping("/getAllUsers")
    public List<User> listUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/update/{id}")
    @Operation(summary = "Update user by userId")
    public void updateUser(@PathVariable Long id, User user) {
//        userService.updateUser(id, user);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "get user by userId")
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }
}