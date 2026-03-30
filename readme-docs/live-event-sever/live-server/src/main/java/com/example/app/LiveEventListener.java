package com.example.app;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "${rocketmq.producer.topic}",
        consumerGroup = "${rocketmq.producer.group}",
        consumeMode = ConsumeMode.CONCURRENTLY,
        consumeThreadNumber = 100,
        consumeThreadMax = 100
)
public class LiveEventListener implements RocketMQListener<LiveEvent> {

    private final Logger logger = LoggerFactory.getLogger(LiveEventListener.class);

    @Autowired
    LiveEventService liveEventService;

    @Value("${rocketmq.producer.dead-topic}")
    private String topic;


    public void onMessage(LiveEvent event) {
        logger.info("RECEIVED EVENT ::: {}", event);
        LiveEvent liveEvent = liveEventService.getFromCache(event.getEventId());
        liveEvent.setCurrentScore(event.getCurrentScore());
    }

}
