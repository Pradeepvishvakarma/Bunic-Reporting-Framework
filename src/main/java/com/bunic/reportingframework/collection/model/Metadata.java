package com.bunic.reportingframework.collection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {
	private String name;
	private String code;
	private String description;
    private Store store;
    private PivotConfig pivotConfig;
    private List<Column> columns;
    private List<String> legends;
    private String accessLevel;
    private Map<String, Object> emailReportProperties;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Metadata{");
        sb.append("name:'").append(name).append('\'');
        sb.append(", code:'").append(code).append('\'');
        sb.append(", description:'").append(description).append('\'');
        sb.append(", pivotConfig:'").append(pivotConfig).append('\'');
        sb.append(", columns:'").append(columns).append('\'');
        sb.append(", legends:'").append(legends).append('\'');
        sb.append(", emailReportProperties:'").append(emailReportProperties).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
