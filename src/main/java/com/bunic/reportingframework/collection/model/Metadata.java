package com.bunic.reportingframework.collection.model;

public class Metadata {
	private String name;
	private String code;
	private String description;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Metadata{");
        sb.append("name:'").append(name).append('\'');
        sb.append(", code:'").append(code).append('\'');
        sb.append(", description:'").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
