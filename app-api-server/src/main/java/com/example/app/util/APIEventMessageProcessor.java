package com.example.app.util;

import com.example.app.model.Event;
import com.example.app.repository.APIRepository;
import com.example.app.service.APIService;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
//@RocketMQMessageListener(
//        topic = "event-topic",
//        consumerGroup = "event-consumer-group",
//        consumeMode = ConsumeMode.CONCURRENTLY,
//        consumeThreadNumber = 100,
//        consumeThreadMax = 100
//)
public class APIEventMessageProcessor implements RocketMQListener<Event> {

    private final Logger logger = LoggerFactory.getLogger(APIEventMessageProcessor.class);

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    APIService apiService;

    public void onMessage(Event event) {
        logger.info("RECEIVED (API SERVER) ::: {}", event);
        try {
            apiService.upsertEvent(event);
        } catch (Exception e) {
            logger.error("ERROR ::: ",e);
        }
    }

}