package com.bunic.reportingframework.user.controller;

import com.bunic.reportingframework.collection.service.CollectionService;
import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @GetMapping("/getUser")
    @ResponseBody
    @Operation(summary = "get user by userId")
    public User getUser(@RequestParam String userId) {
        return userService.getUserByUserId(userId);
    }

    @PostMapping("/addUser")
    @ResponseBody
    @Operation(summary = "get user by userId")
    public String addUser(@RequestBody User user) {
        return userService.addUser(user);
    }
}