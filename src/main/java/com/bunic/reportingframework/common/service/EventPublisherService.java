package com.bunic.reportingframework.common.service;

import com.bunic.reportingframework.common.model.Event;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherService {
    private final ApplicationEventPublisher publisher;

    public EventPublisherService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishEvent(Event event) {
        Event newEvent = new Event(this, event);
        publisher.publishEvent(newEvent);
    }
}