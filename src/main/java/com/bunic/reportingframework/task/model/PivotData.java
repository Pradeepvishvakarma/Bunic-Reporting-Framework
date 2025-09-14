package com.bunic.reportingframework.task.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PivotData {

    private String id;
    private int level;
    private Map<String, BigDecimal> rowGroupValues;
    private Map<String, Map<String, BigDecimal>> columnGroupValues;
    private Map<String, String> rowGroupedBreachDetails;
    private String breachDetailsByLevel;
}
