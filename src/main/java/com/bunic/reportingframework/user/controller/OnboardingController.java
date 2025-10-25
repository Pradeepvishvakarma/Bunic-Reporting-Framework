package com.bunic.reportingframework.user.controller;

import com.bunic.reportingframework.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bunic.reportingframework.user.model.User;

import static com.bunic.reportingframework.common.constant.Constant.*;

@Controller
public class OnboardingController {

    @Autowired
    UserService userService;

    @GetMapping("/")
    public String showLoginFormOnStartUp(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/onboarding")
    public String showOnboardingForm(Model model) {
        model.addAttribute("user", new User());
        return "onboarding";
    }

    @PostMapping("/onboarding")
    public String processOnboarding(@ModelAttribute User user, Model model, HttpSession session) {
        var isUserOnboardingViaGui = true;
        var validationParams = userService.validateUser(user, isUserOnboardingViaGui);

        if (session.getAttribute("UserAddVia") != null && ((String) session.getAttribute("UserAddVia")).equalsIgnoreCase("ADMIN")){
            if (validationParams.isEmpty()) {
                var message = userService.onBoardUser(user);
                if (message.getStatus().name().equalsIgnoreCase("COMPLETED")){
                    message.setMessage(String.format(THREE_STRING_WITH_SPACE, "User ",userService.getUserName(user), " Added Successfully"));
                }
                if (message.getStatus().name().equalsIgnoreCase("FAILED")){
                    message.setMessage(String.format(FIVE_STRING_WITH_SPACE, "Failed to Add User ",userService.getUserName(user)," because Email Id ",user.getEmailId() , " already exists"));
                }
                model.addAttribute("message", message.getMessage());
                model.addAttribute("status", message.getStatus().name());
                model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
                model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
                model.addAttribute("users", userService.getAllUsers());
                return "admin-profile";
            }
        } else {
            if (validationParams.isEmpty()) {
                var message = userService.onBoardUser(user);
                model.addAttribute("message", message.getMessage());
                model.addAttribute("status", message.getStatus().name());
                model.addAttribute("desc", message.getDesc());
                return "onboarding-success";
            }
        }
        return "onboarding-success";
    }
}

