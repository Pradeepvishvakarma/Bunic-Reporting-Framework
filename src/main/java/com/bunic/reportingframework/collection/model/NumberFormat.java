package com.bunic.reportingframework.collection.model;

import lombok.*;

import java.util.Map;


@Getter
@Setter
public class NumberFormat {

	private String rule;
	private String field;
	private Map<String, String> values;

    public NumberFormat() {
		this.rule = NumberFormatRule.NORMAL.getRule();
	}

    public NumberFormat(String rule, String field, Map<String, String> values) {
        this.rule = rule;
        this.field = field;
        this.values = values;
    }
}
