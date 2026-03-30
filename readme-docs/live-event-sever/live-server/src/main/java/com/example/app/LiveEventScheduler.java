package com.example.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Component
public class LiveEventScheduler {

    private final Logger logger = LoggerFactory.getLogger(LiveEventScheduler.class);

    @Autowired
    private LiveEventService liveEventService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private final RestClient restClient;

    @Value("${rocketmq.producer.topic}")
    private String topic;

    public LiveEventScheduler(@Value("${mock.server.endpoint}") String mockServerEndpoint) {
        this.restClient = RestClient.builder().baseUrl(mockServerEndpoint).build();
    }

    @Scheduled(fixedRate=10, timeUnit = TimeUnit.SECONDS)
    public void prepareLiveMockData() throws JsonProcessingException {
        liveEventService.getAllEventIdsFromLiveCache().forEach(eventId -> {
            try {
                sendRequestForScoreUpdate(eventId);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendRequestForScoreUpdate(String eventId) throws JsonProcessingException {
        String result = this.restClient.post()
                .uri("/eventScore")
                .body(eventId) // Spring handles the JSON conversion automatically
                .retrieve()
                .body(String.class);
        logger.info("SENDING EVENT ::: {}", result);
        rocketMQTemplate.convertAndSend(topic, new ObjectMapper().readValue(result, LiveEvent.class));
    }

}
