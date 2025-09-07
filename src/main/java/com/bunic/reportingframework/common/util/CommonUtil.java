package com.bunic.reportingframework.common.util;

import java.util.Map;

public class CommonUtil {

    public static Object getFieldValue(String field, Map<String, Object> prop, Object defaultValue) {
        if(!prop.isEmpty() && prop.get(field) != null) {
            return prop.get(field);
        }
        return defaultValue;
    }
}
