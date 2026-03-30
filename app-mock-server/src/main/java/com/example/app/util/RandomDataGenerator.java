package com.example.app.util;

import com.example.app.model.Event;
import com.example.app.repository.MockRepository;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class RandomDataGenerator {

    private final Logger logger = LoggerFactory.getLogger(RandomDataGenerator.class);

    public static final String EVENT_TOPIC = "event-topic";

    @Autowired
    private MockRepository apiRepository;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Transactional
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS )
    public void prepareLiveMockData() {
        for (int i = 0; i < 3; i++) {
            extractedInserts();
        }
        extractedUpdate();
    }

    private void extractedUpdate() {
        Event eventToUpdate = apiRepository.getEventById(apiRepository.getRandonEventId());
        eventToUpdate.setEventStatus(false);
        apiRepository.updateEvent(eventToUpdate);
        processEventData(eventToUpdate);
    }

    private void extractedInserts() {
        Event event = new Event();
        event.setEventStatus(true);
        event.setEventScore(ThreadLocalRandom.current().nextInt(1000) + ":" + ThreadLocalRandom.current().nextInt(1000));
        event.setEventId(apiRepository.addEvent(event));
        processEventData(event);
    }

    public void processEventData(Event event) {
        logger.info("RANDOM DATA BATCH ::: {} ",event);
        rocketMQTemplate.convertAndSend(EVENT_TOPIC, event);
    }


}