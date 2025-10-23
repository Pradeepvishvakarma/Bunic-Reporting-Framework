package com.bunic.reportingframework.task.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PivotData {

    private String id;
    private int level;
    private Map<String, BigDecimal> rowGroupValues;
    private Map<String, Map<String, BigDecimal>> columnGroupValues;
    private List<NonPivotData> pivotDisableRowGroupValues;
    private Map<String, String> rowGroupedBreachDetails;
    private List<Map<String, Map<String, String>>> columnGroupedBreachDetails;
    private String breachDetailsByLevel;
}
