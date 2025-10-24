package com.bunic.reportingframework.user.controller;

import com.bunic.reportingframework.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bunic.reportingframework.user.model.User;

@Controller
public class OnboardingController {

    @Autowired
    UserService userService;

    @GetMapping("/onboarding")
    public String showOnboardingForm(Model model) {
        model.addAttribute("user", new User());
        return "onboarding";
    }

    @PostMapping("/onboarding")
    public String processOnboarding(@ModelAttribute User user, Model model) {
        System.out.println("user details: " + user);
        var isUserOnboardingViaGui = true;
        var validationParams = userService.validateUser(user, isUserOnboardingViaGui);
        System.out.println("validationParams: " + validationParams);
        if (validationParams.isEmpty()) {
            var message = userService.onBoardUser(user);
            model.addAttribute("message", message.getMessage());
            model.addAttribute("status", message.getStatus().name());
            model.addAttribute("desc", message.getDesc());
            return "onboarding-success";
        }
        return "onboarding-success";
    }
}

