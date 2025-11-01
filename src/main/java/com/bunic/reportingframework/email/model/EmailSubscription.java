package com.bunic.reportingframework.email.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmailSubscription {

    private String schedulerId;
    private String userId;
    private String reportName;
    private String reportCode;
    private boolean enableBodyReport;
    private boolean includeExcelAttachment;
    private String cronTriggerTime;
    private String timeZone;
    private boolean active;
}
