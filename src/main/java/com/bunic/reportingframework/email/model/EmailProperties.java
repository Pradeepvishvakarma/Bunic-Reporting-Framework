package com.bunic.reportingframework.email.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.StringWriter;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailProperties {
    private String from;
    private String aliasName;
    private String replyTo;
    private List<String> mailIds;
    private String[] cc;
    private String[] bcc;
    private String subject;
    private StringWriter content;
    private String filePath;
    private boolean hasAttachment;
    private String attachmentAbsolutePath;
}
