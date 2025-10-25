package com.bunic.reportingframework.user.controller;

import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserLoginController {

    @Autowired
    UserService userService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute User user, Model model) {
        var fetchedUser = userService.getUser(user.getUserId(), user.getPassword());
        if (fetchedUser != null) {
            model.addAttribute("message", "Login Successfully");
            model.addAttribute("status", "COMPLETED");
            if(fetchedUser.getUserType().equalsIgnoreCase("ADMIN")){
                model.addAttribute("isAdmin", "ADMIN");
                return "admin-profile";
            }
            return "update-profile";
        }
        model.addAttribute("message", "Invalid credentials");
        model.addAttribute("status", "FAILED");
        return "login";
    }

    @PostMapping("/update-profile")
    public String process2Login(@ModelAttribute User user, Model model, HttpSession session) {
        var fetchedUser = userService.getUser(user.getUserId(), user.getPassword());
        if (fetchedUser != null) {
            model.addAttribute("message", "Login Successfully");
            model.addAttribute("status", "COMPLETED");
            if(fetchedUser.getUserType().equalsIgnoreCase("ADMIN")){
                model.addAttribute("isAdmin", "ADMIN");
                session.setAttribute("adminUserId", fetchedUser.getUserId());
                session.setAttribute("adminUserName", userService.getUserName(fetchedUser));
                session.setAttribute("users", userService.getAllUsers());
                return "redirect:/admin-profile";
            }
            model.addAttribute("user", userService.getUserById(user.getUserId().toUpperCase()));
            model.addAttribute("userId", fetchedUser.getUserId());
            model.addAttribute("userName", userService.getUserName(fetchedUser));
            return "update-profile";
        }
        model.addAttribute("message", "Invalid credentials");
        model.addAttribute("status", "FAILED");
        return "login";
    }

    @GetMapping("/update-profile")
    public String updateProfileForm(Model model) {
        model.addAttribute("user", new User());
        return "update-profile";
    }

    @PostMapping("/update-profile/update")
    public String processingUpdateProfileForm(@ModelAttribute User user, Model model, HttpSession session) {
        var isAdminRequest = (String) session.getAttribute("UserUpdateVia");
        System.out.println("isAdminRequest value :- "+isAdminRequest);
        var existingUser = userService.getUserById(user.getUserId().toUpperCase());
        if (existingUser.isPresent()) {
            userService.updateUserDetails(existingUser.get(), user);
            model.addAttribute("message", "Profile Updated Successfully");
            model.addAttribute("status", "COMPLETED");
            if(isAdminRequest != null && isAdminRequest.equalsIgnoreCase("ADMIN")){
                return "redirect:/admin-profile";
            }
            return "login";
        }
        model.addAttribute("message", "User Details not found");
        model.addAttribute("status", "FAILED");
        if(isAdminRequest != null && isAdminRequest.equalsIgnoreCase("ADMIN")){
            return "redirect:/admin-profile";
        }
        return "login";
    }

    @GetMapping("/update-profile/update")
    public String processingUpdateProfileForm(Model model) {
        return "update-profile/update";
    }
}

