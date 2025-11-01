package com.bunic.reportingframework.user.controller;

import com.bunic.reportingframework.email.EmailManagementService;
import com.bunic.reportingframework.email.model.EmailSubscription;
import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.bunic.reportingframework.common.constant.Constant.FOUR_STRING_WITH_SPACE;
import static com.bunic.reportingframework.common.constant.Constant.THREE_STRING_WITH_SPACE;

@Controller
public class UserProfileController {

    @Autowired
    UserService userService;

    @Autowired
    EmailManagementService emailManagementService;

    @GetMapping("/user-profile")
    public String getUserProfileForm(@RequestParam(defaultValue = "userManagement") String activeTab, Model model, HttpSession session) {
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("user", session.getAttribute("userId"));
        model.addAttribute("user", new User());
        model.addAttribute("emailSubscription", new EmailSubscription());
        model.addAttribute("userName", userService.getUserName(userService.getUserByUserId((String) session.getAttribute("userId"))));

        model.addAttribute("isAdmin", "USER");
        model.addAttribute("users", userService.getUserByUserId((String) session.getAttribute("userId")));
        model.addAttribute("emailSubscriptions", emailManagementService.getEmailSubscriptionsByUserId((String) session.getAttribute("userId")));
        model.addAttribute("message", session.getAttribute("message"));
        return "user-profile";
    }

    @PostMapping("/user-profile")
    public String processToUserProfile(@ModelAttribute User user, @RequestParam(defaultValue = "userManagement") String activeTab, Model model, HttpSession session) {
        var fetchedUser = userService.getUser(user.getUserId(), user.getPassword());
        if (fetchedUser != null) {
            model.addAttribute("activeTab", activeTab);
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
            model.addAttribute("users", List.of(fetchedUser));
            model.addAttribute("emailSubscriptions", emailManagementService.getEmailSubscriptionsByUserId(fetchedUser.getUserId()));
            session.setAttribute("userId", fetchedUser.getUserId());
            return "user-profile";
        }
        model.addAttribute("message", "Invalid credentials");
        model.addAttribute("status", "FAILED");
        return "login";
    }

    @GetMapping("/user-profile/edit")
    public String editUser(@RequestParam String userId, Model model, HttpSession session) {
        model.addAttribute("user", userService.getUserById(userId));
//        model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
//        model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
//        session.setAttribute("UserUpdateVia", "ADMIN");
//        model.addAttribute("UserUpdateVia", "ADMIN");
//        session.setAttribute("adminMessage", String.format(THREE_STRING_WITH_SPACE,"User ",userId," details updated successfully!"));
//        session.setAttribute("adminStatus", "COMPLETED");
//        model.addAttribute("adminMessage", session.getAttribute("adminMessage"));
//        model.addAttribute("adminStatus", session.getAttribute("adminStatus"));
//        session.setAttribute("activeTab","userManagement");

        model.addAttribute("user", userService.getUserById(userId));
//        model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
//        model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
//        session.setAttribute("UserUpdateVia", "ADMIN");
//        model.addAttribute("UserUpdateVia", "ADMIN");
        session.setAttribute("activeTab","userManagement");
        session.setAttribute("userId", userId);
        session.setAttribute("userName", userService.getUserName(userService.getUserByUserId(userId)));
        model.addAttribute("userName", userService.getUserName(userService.getUser(userId)));
        model.addAttribute("userId", userService.getUserById(userId));
        return "update-profile";  // reuse existing update page
    }

}

