package com.bunic.reportingframework.task.model;

import com.mongodb.DBObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NonPivotData {
    private String id;
    private DBObject values;
    private Map<String, String> breachDetails;
}
