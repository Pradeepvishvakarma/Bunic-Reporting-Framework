package com.bunic.reportingframework.email.service;

import com.bunic.reportingframework.email.model.EmailProperties;
import com.bunic.reportingframework.exception.BunicException;
import com.bunic.reportingframework.exception.BunicRuntimeException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(EmailProperties emailProperties) throws BunicException {
        if ( emailProperties != null && emailProperties.getMailIds() != null) {
            try {
                LOGGER.info("Email runner - start composing email for requested excel report");
                MimeMessage msg = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true);

                setAddresses(emailProperties, helper);
                mailSender.send(msg);
            } catch (Exception e) {
                throw new BunicRuntimeException("Email runner - problem on send email report "+ e.getMessage(), e);
            }
        }
    }

    private void setAddresses(EmailProperties emailProperties, MimeMessageHelper helper) throws Exception {
        helper.setFrom("bunic.corporation.ltd@gmail.com");
        helper.setTo(emailProperties.getMailIds());
        helper.setSubject(emailProperties.getSubject());
        helper.setText(String.valueOf(emailProperties.getContent()), true);

        var newPath = String.format("%s%s%s", emailProperties.getFilePath(), emailProperties.getAttachmentName(), ".eml");
        if (emailProperties.isHasAttachment()){
            var newExcelPath = String.format("%s%s%s", emailProperties.getFilePath(), emailProperties.getAttachmentName(), ".xlsx");
            helper.addAttachment(String.format("%s%s",emailProperties.getAttachmentName(),".xlsx"), new File(newExcelPath));
        }

        MimeMessage mimeMessage = helper.getMimeMessage();
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(newPath)) {
            mimeMessage.writeTo(fos);
        } catch (IOException e) {
            throw new BunicRuntimeException("Email runner - problem on composing email file"+ e.getMessage(), e);
        }
    }
}
