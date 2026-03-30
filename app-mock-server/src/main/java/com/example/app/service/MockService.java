package com.example.app.service;

import com.example.app.model.Event;
import com.example.app.repository.MockRepository;
import com.example.app.util.RandomDataGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

//- Transactional Business Logic
@Service
public class MockService {

    private final Logger logger = LoggerFactory.getLogger(MockService.class);

    private static final String MOCK_TOPIC = "mock-topic";

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Autowired
    MockRepository mockRepository;

    @Transactional(readOnly = true)
    public Event getEvent(String eventId) {
        Long eventLongId = Long.parseLong(eventId);
        logger.trace("SENDING EVENT TO INTERNAL TOPIC ::: {}",eventLongId);
        sendMessageToInternalTopic(eventLongId);
        return mockRepository.getEventById(eventLongId);
    }

    @Transactional(readOnly = true)
    public List<Event> getLiveEvents() {
        List<Event> events = mockRepository.getAllLiveEvents();
        performLongRunningTask(events);
        return events;
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long eventId) {
        return mockRepository.getEventById(eventId);
    }

    @Async
    private void performLongRunningTask(List<Event> events) {
        logger.trace("SENDING EVENTS TO INTERNAL TOPIC ::: {}",events);
        events.forEach(event -> sendMessageToInternalTopic(event.getEventId()));
    }

    private void sendMessageToInternalTopic(Long eventId){
        rocketMQTemplate.convertAndSend(MOCK_TOPIC,eventId);
    }

}
