package com.bunic.reportingframework.collection.model;

public enum NumberFormatRule {

	NORMAL("normal"), POSITIVE_NEGATIVE("positive-negative"), BREACH_STYLING("breach-styling");
	
	private final String rule;

	private NumberFormatRule(String rule) {
		this.rule = rule;
	}
	
	public String getRule() {
		return rule;
	}
	
	
}
