package com.example.app.util;

import com.example.app.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class APIIntegrationClient {

    private final Logger logger = LoggerFactory.getLogger(APIIntegrationClient.class);

        private final RestClient restClient;

        public APIIntegrationClient() {
            this.restClient = RestClient.builder().baseUrl("http://localhost:9090/api").build();;
            try {
                getAsyncRemoteLiveEvents();
            } catch (Exception e) {
                logger.error("ERROR TRY AFTER SOME TIME ::: ", e);
            }
        }

    public Event getRemoteEventStatus(String eventId) {
        String result = this.restClient.post()
                .uri("/event")
                .body(eventId) // Spring handles the JSON conversion automatically
                .retrieve()
                .body(String.class);
        logger.info("RESULT (EVENT) ::: {}", result);
        Event event = new Event();
        event.setEventStatus(true);
        return event;
    }

    public void getAsyncRemoteLiveEvents() {
        this.restClient.post()
                .uri("/liveEvents")
                .retrieve()
                .body(String.class);
    }

}
