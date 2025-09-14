package com.bunic.reportingframework.task.model;

import com.bunic.reportingframework.collection.model.Column;
import com.bunic.reportingframework.collection.model.PivotConfig;
import com.mongodb.DBObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class PivotTableContext {

    private List<DBObject> dbObjects;
    private List<String> rowGroup;
    private List<String> columnGroup;
    private List<Column> columns;
    private boolean isColumnGrouping;
    private boolean isPivotEnabled;
    private boolean isPivotDisableRowGrouping;

    public PivotTableContext(List<DBObject> dbObjects, List<Column> columns, PivotConfig pivotConfig){
        this.dbObjects = dbObjects.stream().filter(Objects::nonNull).toList();
        this.isPivotEnabled = pivotConfig != null && pivotConfig.isEnabled();
        this.columns = columns.stream().filter(Objects::nonNull).toList();
        this.rowGroup = pivotConfig != null && pivotConfig.getRowGroup() != null && !pivotConfig.getRowGroup().isEmpty() ? pivotConfig.getRowGroup() : Collections.emptyList();
        this.columnGroup = isPivotEnabled && pivotConfig.getColumnGroup() != null && !pivotConfig.getColumnGroup().isEmpty() ? pivotConfig.getColumnGroup() : Collections.emptyList();
        this.isPivotDisableRowGrouping = pivotConfig != null && !pivotConfig.isEnabled()  &&  !rowGroup.isEmpty() && pivotConfig.getColumnGroup().isEmpty();
    }
}
