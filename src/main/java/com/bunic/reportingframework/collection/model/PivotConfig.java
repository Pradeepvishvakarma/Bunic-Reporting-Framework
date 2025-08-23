package com.bunic.reportingframework.collection.model;

import java.util.List;

public class PivotConfig {

	boolean enabled;
	List<String> rowGroup;
	List<String> columnGroup;
	List<String> unusedColumns;
	List<String> valueColumns;
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public List<String> getRowGroup() {
		return rowGroup;
	}
	public void setRowGroup(List<String> rowGroup) {
		this.rowGroup = rowGroup;
	}
	public List<String> getColumnGroup() {
		return columnGroup;
	}
	public void setColumnGroup(List<String> columnGroup) {
		this.columnGroup = columnGroup;
	}
	public List<String> getUnusedColumns() {
		return unusedColumns;
	}
	public void setUnusedColumns(List<String> unusedColumns) {
		this.unusedColumns = unusedColumns;
	}
	public List<String> getValueColumns() {
		return valueColumns;
	}
	public void setValueColumns(List<String> valueColumns) {
		this.valueColumns = valueColumns;
	}
	public PivotConfig() {
		super();
	}
	
}
