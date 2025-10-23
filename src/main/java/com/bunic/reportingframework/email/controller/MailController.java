package com.bunic.reportingframework.email.controller;

import com.bunic.reportingframework.email.service.EmailSender;
import com.bunic.reportingframework.exception.BunicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private EmailSender emailService;

    @PostMapping("/send")
    public String sendMail(@RequestParam String toEmail,
                           @RequestParam String subject,
                           @RequestParam String body) throws BunicException {
        emailService.sendEmail(null);
        return "Mail sent to " + toEmail;
    }
}
