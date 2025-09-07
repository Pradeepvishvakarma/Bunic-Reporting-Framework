package com.bunic.reportingframework.task.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDataResponse {
    private List<NonPivotData> nonPivotedData;
    private List<PivotData> pivotedData;
    private GrandTotal grandTotal;

}
