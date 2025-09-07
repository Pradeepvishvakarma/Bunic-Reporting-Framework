package com.bunic.reportingframework.task.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrandTotal {
    private Map<String, BigDecimal> rowGrandTotal;
    private Map<String, BigDecimal> columnGrandTotal;
}
