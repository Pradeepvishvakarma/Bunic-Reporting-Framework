package com.bunic.reportingframework.common.model;

import com.bunic.reportingframework.common.service.EventPublisherService;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
@Setter
public class Event extends ApplicationEvent {

    private String eventName;
    private Map<String, Object> attributes;

    public Event(Object source, String eventName, Map<String, Object> attributes) {
        super(source);
        this.eventName = eventName;
        this.attributes = attributes;
    }

    public Event(EventPublisherService source, Event event) {
        super(source);
        this.eventName = event.getEventName();
        this.attributes = event.getAttributes();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Event [Name=").append(this.eventName);
        sb.append(", attributes=").append(this.attributes);
        sb.append("]");
        return sb.toString();
    }
}
