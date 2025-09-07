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
    private String level;
    private Map<String, BigDecimal> rowGroupValue;
    private Map<String, Map<String, BigDecimal>> columnGroupValue;
    private Map<String, String> rowGroupedBreachDetails;
}
