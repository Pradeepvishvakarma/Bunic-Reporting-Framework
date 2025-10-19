package com.bunic.reportingframework.common.controller;

import com.bunic.reportingframework.common.model.Event;
import com.bunic.reportingframework.common.service.EventPublisherService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {
    private final EventPublisherService publisherService;

    public EventController(EventPublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @GetMapping("/trigger-event")
    @Operation(summary = "Event publisher")
    public String triggerEvent(Event event) {
        publisherService.publishEvent(event);
        return "Event Published!";
    }
}