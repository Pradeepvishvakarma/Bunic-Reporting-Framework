package com.bunic.reportingframework.common.constant;

import java.util.regex.Pattern;

public class Constant {

    private Constant(){
    }

    public static final String FILE_SAPERATOR = "/";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_STRING = "string";
    public static final String BREACH_TYPE_BREACH_STYLING = "breach-styling";
    public static final String BREACH_TYPE_POSITIVE_NEGATIVE = "positive-negative";
    public static final String BREACH_NUMBER_FORMAT_RED = "number-format-red";
    public static final String BREACH_NUMBER_FORMAT_GREEN = "number-format-green";
    public static final String BREACH_NUMBER_FORMAT_WHITE = "number-format-white";
    public static final String BREACH_NUMBER_FORMAT_DEFAULT = "number-format-default";
    public static final String BREACH_STRING_FORMAT_RED = "string-format-red";
    public static final String BREACH_STRING_FORMAT_GREEN = "string-format-green";
    public static final String BREACH_STRING_FORMAT_WHITE = "string-format-white";
    public static final String BREACH_STRING_FORMAT_DEFAULT = "string-format-default";
    public static final String DOT_ALL = ".*?";
    public static final Pattern EMAIL_PLACEHOLDER_PATTERN = Pattern.compile(String.format("\\{%s\\}", DOT_ALL), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    public static final String COMMA_SEPARATOR = ",";
    public static final String PIVOT_CONFIG = "pivotConfig";
    public static final String UNAUTHORIZED_REQUEST = "request is not authorised";
    public static final String IS_NON_PIVOT_REPORT = "isNonPivotReport";
    public static final String IS_PIVOT_ENABLE_ROW_GROUPING = "isPivotEnableRowGrouping";
    public static final String IS_PIVOT_DISABLE_ROW_GROUPING = "isPivotDisableRowGrouping";
    public static final String IS_PIVOT_ENABLE_COLUMN_GROUPING = "isPivotEnableColumnGrouping";
    public static final String INCLUDE_EXCEL_ATTACHMENT = "includeExcelAttachment";
    public static final String ATTACHMENT_FILE_NAME = "attachmentFileName";
    public static final String REPORT_DATA = "reportData";
    public static final String PIVOTED_REPORT_DATA = "pivotedReportData";
    public static final String SCHEDULED_TRIGGER_TIME = "scheduledTriggerTime";
    public static final String REPORT_DATE = "reportDate";
    public static final String TASK_ID = "taskId";
    public static final String FILE_PATH = "filePath";
    public static final String GENERATED_DATE = "generatedDate";
    public static final String REPORT_NAME = "reportName";
    public static final String LEGENDS = "legends";
    public static final String SUBJECT = "subject";
    public static final String EMAIL_BODY_REPORT = "emailBodyReport";
    public static final String EMAIL_ID = "emailId";
    public static final String REPORT_DISPLAY_NAME = "reportDisplayName";
    public static final String TIMESTAMP = "timestamp";
    public static final String DATE_TIME_FORMAT = "dd-MMM-yyyy HH:mm";
    public static final String DATE_FORMAT = "dd-MMM-yyyy";
    public static final String TWO_STRING_WITH_SPACE = "%s %s";
    public static final String TWO_STRING = "%s%s";
    public static final String THREE_STRING = "%s%s%s";
    public static final String TIME_ZONE_IST = "IST";
    public static final String EXCEL_FILE_EXTENSION = ".xlsx";
    public static final String EMAIL_FILE_EXTENSION = ".eml";

}
