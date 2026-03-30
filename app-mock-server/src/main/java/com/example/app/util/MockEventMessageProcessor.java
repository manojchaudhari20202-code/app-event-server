package com.example.app.util;

import com.example.app.model.Event;
import com.example.app.repository.MockRepository;
import com.example.app.service.MockService;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "mock-topic",
        consumerGroup = "mock-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY,
        consumeThreadNumber = 100,
        consumeThreadMax = 100
)
public class MockEventMessageProcessor implements RocketMQListener<Long> {

    private final Logger logger = LoggerFactory.getLogger(MockEventMessageProcessor.class);

    public static final String EVENT_TOPIC = "event-topic";

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    MockService mockService;

    public void onMessage(Long eventId) {
        Event event = mockService.getEventById(eventId);
        logger.info("RECEIVED (MOCK SERVER) ::: {}", event);
        try {
            rocketMQTemplate.convertAndSend(EVENT_TOPIC,event);
        } catch (Exception e) {
            logger.error("ERROR ::: ",e);
        }
    }

}