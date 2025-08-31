package com.bunic.reportingframework.common.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Data
public class EventObject {
    private final String eventName;
    private final Map<String, Object> attributes;

    public EventObject(String eventName, Map<String, Object> attributes) {
        this.eventName = eventName;
        this.attributes = attributes;
    }
}