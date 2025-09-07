package com.bunic.reportingframework.email.service;

import com.bunic.reportingframework.email.model.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);

//    @Value()
    private String from;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(EmailProperties emailProperties) {
        if ( emailProperties != null && emailProperties.getMailIds() != null) {
            try {
                LOGGER.info("Email runner - start composing email for requested excel report");
                MimeMessage msg = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true);

                setAddresses(emailProperties, helper);
            } catch (Exception e) {
                throw new RuntimeException("Email runner - problem on send email report " + e.getMessage(), e);
            }
        }
    }

    private void setAddresses(EmailProperties emailProperties, MimeMessageHelper helper) throws MessagingException {
        helper.setFrom(emailProperties.getFrom());
        helper.setTo(emailProperties.getMailIds().toArray(new String[0]));
        helper.setSubject("Pradeep Report");
        helper.setText(String.valueOf(emailProperties.getContent()), true);
        var newPath = String.format("%s%s", emailProperties.getFilePath(), "abc.eml");
        System.out.println("newPath: " + newPath);
        // Save the email as .eml file
        String emlPath = emailProperties.getFilePath(); // Add this property to EmailProperties
        MimeMessage mimeMessage = helper.getMimeMessage();
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(newPath)) {
            mimeMessage.writeTo(fos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    private String getAbsolutePath(){
//
//    }
}
