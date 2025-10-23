package com.bunic.reportingframework.task.model;

import com.bunic.reportingframework.collection.model.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDataResponse {
    private List<Column> columns;
    private LinkedHashSet<String> columnGroupKeys;
    private List<NonPivotData> nonPivotedData;
    private List<PivotData> pivotedData;
    private GrandTotal grandTotal;
}
