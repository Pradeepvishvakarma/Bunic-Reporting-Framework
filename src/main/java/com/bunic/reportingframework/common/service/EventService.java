package com.bunic.reportingframework.common.service;

import com.bunic.reportingframework.common.model.Event;
import com.bunic.reportingframework.common.model.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    private final String topicName;
    private final KafkaTemplate<String, Event> KafkaTemplate;
    private final Executor eventKafkaJobExecutor;
    private final Map<String, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private final Map<String, List<EventListener>> queueListeners = new ConcurrentHashMap<>();

    public EventService(String topicName, KafkaTemplate<String, Event> kafkaTemplate, Executor eventKafkaJobExecutor){
        this.topicName = topicName;
        this.KafkaTemplate = kafkaTemplate;
        this.eventKafkaJobExecutor = eventKafkaJobExecutor;
    }

    public void publish(Event event){
        LOGGER.info("Event Publishing event {}", event.getEventName());
        this.sendMessage(event, this.topicName);
    }

    public void subscribe(String eventName, EventListener eventListener){
        this.listeners.computeIfAbsent(eventName, (k) -> new CopyOnWriteArrayList<>());
        this.listeners.get(eventName).add(eventListener);
    }

    public void subscribeQueue(String eventName, EventListener eventListener){
        this.queueListeners.computeIfAbsent(eventName, (k) -> new CopyOnWriteArrayList<>());
        this.queueListeners.get(eventName).add(eventListener);
    }

    public void sendMessage(Event event, String topicName){
        CompletableFuture<SendResult<String, Event>> future = this.KafkaTemplate.send(topicName, event);
        future.whenComplete((result, ex) -> {
            if(null == ex){
                LOGGER.info("Event Published message = [{}]", event.getEventName());
            } else {
               throw new KafkaException(String.format("Event error while publishing event=[%s] due to : %s", event.getEventName(), ex.getMessage()), ex);
            }
        });
    }

}
