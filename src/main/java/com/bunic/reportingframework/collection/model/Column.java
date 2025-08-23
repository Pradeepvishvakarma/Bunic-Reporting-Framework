package com.bunic.reportingframework.collection.model;

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
//	private NumberFormat numberFormat;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getAggFunc() {
		return aggFunc;
	}
	public void setAggFunc(String aggFunc) {
		this.aggFunc = aggFunc;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public boolean isMandatoryFilters() {
		return mandatoryFilters;
	}
	public void setMandatoryFilters(boolean mandatoryFilters) {
		this.mandatoryFilters = mandatoryFilters;
	}
	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
//	public NumberFormat getNumberFormat() {
//		return numberFormat;
//	}
//	public void setNumberFormat(NumberFormat numberFormat) {
//		this.numberFormat = numberFormat;
//	}
}
