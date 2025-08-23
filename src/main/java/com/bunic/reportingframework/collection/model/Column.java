package com.bunic.reportingframework.collection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column {
 	private Long id;
	private String title;
	private String field;
	private String type;
	private String format;
	private String aggFunc;
	private int order;
	private boolean mandatoryFilters;
	private boolean hidden;
	private NumberFormat numberFormat;
}
