package com.bunic.reportingframework.collection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PivotConfig {

	private boolean enabled;
	private List<String> rowGroup;
	private List<String> columnGroup;
	private List<String> unusedColumns;
	private List<String> valueColumns;
}
