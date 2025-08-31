package com.bunic.reportingframework.common.listener;

import com.bunic.reportingframework.common.model.EventObject;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MyCustomEventListener {

    @EventListener
    public void handleMyCustomEvent(EventObject event) {
        System.out.println("📢 Event Received: " + event.getEventName());
        event.getAttributes().forEach((k, v) ->
                System.out.println("   -> " + k + " : " + v)
        );
    }
}