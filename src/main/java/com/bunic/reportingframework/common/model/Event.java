package com.bunic.reportingframework.common.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Event {

    private String eventName;
    private Map<String, Object> attributes;

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Event [Name=").append(this.eventName);
        sb.append(", attributes=").append(this.attributes);
        sb.append("]");
        return sb.toString();
    }
}
