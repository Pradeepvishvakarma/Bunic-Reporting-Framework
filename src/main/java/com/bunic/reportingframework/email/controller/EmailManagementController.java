package com.bunic.reportingframework.email.controller;

import com.bunic.reportingframework.collection.service.CollectionService;
import com.bunic.reportingframework.email.EmailManagementService;
import com.bunic.reportingframework.email.model.EmailSubscription;
import com.bunic.reportingframework.task.scheduler.TaskManagerScheduler;
import com.bunic.reportingframework.task.service.TaskManagerService;
import com.bunic.reportingframework.user.model.User;
import com.bunic.reportingframework.user.model.Notification;
import com.bunic.reportingframework.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

import static com.bunic.reportingframework.common.constant.Constant.*;

@Controller
public class EmailManagementController {

    @Autowired
    UserService userService;

    @Autowired
    EmailManagementService emailManagementService;

    @Autowired
    CollectionService collectionService;

    @Autowired
    TaskManagerService taskManagerService;

    @Autowired
    TaskManagerScheduler taskManagerScheduler;

    @GetMapping("/email-management")
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

    @PostMapping("/email-management")
    public String processingAdminProfileForm(@ModelAttribute User user, Model model) {
        var fetchedUser = userService.isUserAvailable(user.getUserId(), user.getPassword());
        model.addAttribute("message", fetchedUser ? "Login Successfully" : "Invalid credentials");
        model.addAttribute("status", fetchedUser ? "COMPLETED" : "FAILED");
        var isAdmin = userService.isAdminUser(user.getUserId());
        return fetchedUser && isAdmin ? "admin-profile" : "login";
    }

    @PostMapping("/email-management/toggle-setting")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSetting(@RequestBody Map<String, Object> payload) {
        String schedulerId = (String) payload.get("schedulerId");
        String field = (String) payload.get("field");
        boolean value = (boolean) payload.get("value");

        emailManagementService.updateSetting(schedulerId, field, value);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "schedulerId", schedulerId,
                "field", field,
                "mess", getFieldMessage(field, schedulerId),
                "value", value
        ));
    }

    private Object getFieldMessage(String field, String schedulerId) {
        if(field.equalsIgnoreCase("active")){
            return String.format(THREE_STRING_WITH_SPACE, "Email Subscription for SchedulerId ",schedulerId, " is ");
        } else if(field.equalsIgnoreCase("emailBodyReport") || field.equalsIgnoreCase("enableBodyReport")){
            return String.format(THREE_STRING_WITH_SPACE, "Email Body for SchedulerId ",schedulerId, " is ");
        } else if(field.equalsIgnoreCase("includeExcelAttachment")){
            return String.format(THREE_STRING_WITH_SPACE, "Excel Attachment for SchedulerId ",schedulerId, " is ");
        }
        return null;
    }

    @PostMapping("/email-management/triggerEmail")
    public String deleteUser(@RequestParam String schedulerId, Model model, HttpSession session) {
        var notification = new Notification();
        taskManagerService.triggerEmailOnAdminRequest(schedulerId, notification);
        if (session.getAttribute("isAdmin") != null && ((String) session.getAttribute("isAdmin")).equalsIgnoreCase("ADMIN")) {
            model.addAttribute("message", notification.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("emailSubscriptions", emailManagementService.getAllEmailSubscriptions());
            model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
            model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
            model.addAttribute("status", notification.getStatus().name());
            model.addAttribute("activeTab", "emailManagement");
            session.setAttribute("activeTab","emailManagement");

            session.setAttribute("adminMessage", notification.getMessage());
            session.setAttribute("adminStatus", notification.getStatus().name());
            model.addAttribute("adminMessage", session.getAttribute("adminMessage"));
            model.addAttribute("adminStatus", session.getAttribute("adminStatus"));
            return "redirect:/admin-profile?activeTab=emailManagement";
        }
        else {
            model.addAttribute("message", notification.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("emailSubscriptions", emailManagementService.getAllEmailSubscriptions());
            model.addAttribute("status", notification.getStatus().name());
            model.addAttribute("activeTab", "emailManagement");
            session.setAttribute("activeTab","emailManagement");
            return "redirect:/user-profile?activeTab=emailManagement";
        }

    }

    @PostMapping("/email-management/delete")
    public String deleteEmailSubscription(@RequestParam String schedulerId, Model model, HttpSession session) {
        taskManagerService.deleteSchedulerBySchedulerId(schedulerId);
        System.out.println("session.getAttribute(UserAddVia) details :- "+session.getAttribute("UserAddVia"));
        if (session.getAttribute("isAdmin") != null && ((String) session.getAttribute("isAdmin")).equalsIgnoreCase("ADMIN")){
            model.addAttribute("message", "Email Subscription deleted successfully!");
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("emailSubscriptions", emailManagementService.getAllEmailSubscriptions());
            model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
            model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
            model.addAttribute("status", "COMPLETED");
            model.addAttribute("message", String.format(THREE_STRING_WITH_SPACE,"Email Subscription ",schedulerId," deleted successfully!"));
            session.setAttribute("adminMessage", String.format(THREE_STRING_WITH_SPACE,"Email Subscription ",schedulerId," deleted successfully!"));
            session.setAttribute("adminStatus", "COMPLETED");
            model.addAttribute("adminMessage", session.getAttribute("adminMessage"));
            model.addAttribute("adminStatus", session.getAttribute("adminStatus"));
            taskManagerScheduler.refreshSchedulers();
            return "redirect:/admin-profile?activeTab=emailManagement";
        }
        else {
            model.addAttribute("message", "Email Subscription deleted successfully!");
            model.addAttribute("users", userService.getAllUsers());
//            model.addAttribute("emailSubscription", emailManagementService.getEmailSubscriptionsByUserId());
            model.addAttribute("status", "COMPLETED");
            model.addAttribute("message", String.format(THREE_STRING_WITH_SPACE,"Email Subscription ",schedulerId," deleted successfully!"));
            taskManagerScheduler.refreshSchedulers();
            return "redirect:/user-profile?activeTab=emailManagement";
        }
    }

    @GetMapping("/addEmail")
    public String addEmailSubscription(HttpSession session, Model model) {
        model.addAttribute("user", new EmailSubscription());
        model.addAttribute("isAdmin", "ADMIN");
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("emailSubscriptions", emailManagementService.getAllEmailSubscriptions());
        session.setAttribute("UserUpdateVia", "ADMIN");
        model.addAttribute("UserUpdateVia", "ADMIN");
        session.setAttribute("UserAddVia", "ADMIN");
        model.addAttribute("UserAddVia", "ADMIN");
        model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
        model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
        if(session.getAttribute("adminMessage") != null){
            model.addAttribute("status", session.getAttribute("adminStatus"));
            model.addAttribute("message", session.getAttribute("adminMessage"));
        } else {
            model.addAttribute("message", session.getAttribute("message"));
        }
        return "add-new-email";
    }

    @GetMapping("/add-new-email")
    public String showAddEmailForm(HttpSession session, Model model) {
        model.addAttribute("emailSubscription", new EmailSubscription());
        return "add-new-email";
    }

    @PostMapping("/add-new-email")
    public String processEmailAdd(@ModelAttribute EmailSubscription emailSubscription, Model model, HttpSession session) {
        var isEmailOnboardingViaGui = true;
        var validationParams = emailManagementService.validateEmailSubscription(emailSubscription, isEmailOnboardingViaGui);
        System.out.println("session.getAttribute(\"isAdmin\"):- "+session.getAttribute("isAdmin"));
        if (session.getAttribute("isAdmin") != null && ((String) session.getAttribute("isAdmin")).equalsIgnoreCase("ADMIN")){
            if (validationParams.isEmpty()) {
                var message = emailManagementService.onBoardEmail(emailSubscription);
                model.addAttribute("message", message.getMessage());
                model.addAttribute("status", message.getStatus().name());
                model.addAttribute("adminUserId", session.getAttribute("adminUserId"));
                model.addAttribute("adminUserName", session.getAttribute("adminUserName"));
                model.addAttribute("users", userService.getAllUsers());
                model.addAttribute("emailSubscriptions", emailManagementService.getAllEmailSubscriptions());

                session.setAttribute("adminMessage", message.getMessage());
                session.setAttribute("adminStatus", message.getStatus().name());
                model.addAttribute("adminMessage", session.getAttribute("adminMessage"));
                model.addAttribute("adminStatus", session.getAttribute("adminStatus"));

                return "redirect:/admin-profile?activeTab=emailManagement";
            }
        } else {
            if (validationParams.isEmpty()) {
                var message = emailManagementService.onBoardEmail(emailSubscription);
                model.addAttribute("message", message.getMessage());
                model.addAttribute("status", message.getStatus().name());
                model.addAttribute("users", session.getAttribute("userId"));
                model.addAttribute("emailSubscription", emailManagementService.getEmailSubscriptionsByUserId((String) session.getAttribute("userId")));
                model.addAttribute("userId", emailSubscription.getUserId());
                return "redirect:/user-profile?activeTab=emailManagement";
            }
        }
        return "onboarding-success";
    }
}

