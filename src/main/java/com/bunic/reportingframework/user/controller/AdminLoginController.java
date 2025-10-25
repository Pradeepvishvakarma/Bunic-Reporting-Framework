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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import static com.bunic.reportingframework.common.constant.Constant.*;

@Controller
public class AdminLoginController {

    @Autowired
    UserService userService;

    @GetMapping("/admin-profile")
    public String adminProfileForm(HttpSession session, Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isAdmin", "ADMIN");
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
        model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
        if(session.getAttribute("adminMessage") != null){
            model.addAttribute("status", session.getAttribute("adminStatus"));
            model.addAttribute("message", session.getAttribute("adminMessage"));
        } else {
            model.addAttribute("message", session.getAttribute("message"));
        }
        return "admin-profile";
    }

    @PostMapping("/admin-profile")
    public String processingAdminProfileForm(@ModelAttribute User user, Model model) {
        var fetchedUser = userService.isUserAvailable(user.getUserId(), user.getPassword());
        model.addAttribute("message", fetchedUser ? "Login Successfully" : "Invalid credentials");
        model.addAttribute("status", fetchedUser ? "COMPLETED" : "FAILED");
        var isAdmin = userService.isAdminUser(user.getUserId());
        return fetchedUser && isAdmin ? "admin-profile" : "login";
    }

    @GetMapping("/logout")
    public String clearSession(SessionStatus status, HttpSession session) {
        session.invalidate();
        status.setComplete();
        return "redirect:/login";
    }

    @PostMapping("/admin-profile/delete")
    public String deleteUser(@RequestParam String userId, Model model, HttpSession session) {
        userService.deleteUser(userId);
        model.addAttribute("message", "User deleted successfully!");
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
        model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
        model.addAttribute("status", "COMPLETED");
        model.addAttribute("message", String.format(THREE_STRING_WITH_SPACE,"User ",userId," deleted successfully!"));
        return "admin-profile";
    }

    @GetMapping("/admin-profile/edit")
    public String editUser(@RequestParam String userId, Model model, HttpSession session) {
        model.addAttribute("user", userService.getUserById(userId));
        model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
        model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
        session.setAttribute("UserUpdateVia", "ADMIN");
        model.addAttribute("UserUpdateVia", "ADMIN");
        session.setAttribute("adminMessage", String.format(THREE_STRING_WITH_SPACE,"User ",userId," details updated successfully!"));
        session.setAttribute("adminStatus", "COMPLETED");
        model.addAttribute("adminMessage", session.getAttribute("adminMessage"));
        model.addAttribute("adminStatus", session.getAttribute("adminStatus"));
        return "update-profile";  // reuse existing update page
    }

    @PostMapping("/admin-profile/toggle-admin")
    public String toggleAdmin(@RequestParam String userId, Model model, HttpSession session) {
        userService.toggleAdminAccess(userId);
        model.addAttribute("message", "User Type updated successfully!");
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
        model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
        model.addAttribute("status", "COMPLETED");
        var updateUser = userService.getUserByUserId(userId);
        var adminAccessPermissionType = updateUser.getUserType();
        var permission = adminAccessPermissionType.equalsIgnoreCase("ADMIN") ? " Granted" : " Revoked";
        model.addAttribute("message", String.format(FOUR_STRING_WITH_SPACE,"User ",userId, permission, " Admin Access!"));
        return "admin-profile";
    }

    @GetMapping("/addUser")
    public String addUser(HttpSession session, Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isAdmin", "ADMIN");
        model.addAttribute("users", userService.getAllUsers());
        session.setAttribute("UserUpdateVia", "ADMIN");
        model.addAttribute("UserUpdateVia", "ADMIN");
        session.setAttribute("UserAddVia", "ADMIN");
        model.addAttribute("UserAddVia", "ADMIN");
        model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
        model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
        System.out.println("session.getAttribute(\"adminMessage\") details :- "+ session.getAttribute("adminMessage"));
        if(session.getAttribute("adminMessage") != null){
            model.addAttribute("status", session.getAttribute("adminStatus"));
            model.addAttribute("message", session.getAttribute("adminMessage"));
        } else {
            model.addAttribute("message", session.getAttribute("message"));
        }
        return "onboarding";
    }

}

