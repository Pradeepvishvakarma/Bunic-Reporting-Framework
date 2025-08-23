package com.bunic.reportingframework.collection.model;

import java.util.Map;

public class NumberFormat {

	private String rule;
	private String field;
	private Map<String, String> values;
	
	public NumberFormat() {
		this.rule = NumberFormatRule.NORMAL.getRule();
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}
}
