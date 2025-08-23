package com.bunic.reportingframework.collection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
public class NumberFormat {

	private String rule;
	private String field;
	private Map<String, String> values;
	
	public NumberFormat() {
		this.rule = NumberFormatRule.NORMAL.getRule();
	}
}
