package com.bunic.reportingframework.collection.model;

import java.util.List;

public class Store {

	private String type;
	private String database;
	private String value;
	private String queryFile;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getQueryFile() {
		return queryFile;
	}
	public void setQueryFile(String queryFile) {
		this.queryFile = queryFile;
	}
}
