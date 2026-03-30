package com.example.app;

import java.util.concurrent.ThreadLocalRandom;

public class MockEvent {

    private final String eventId;
    private String currentScore;
    private final Boolean live;

    public MockEvent(String eventId) {
        this.eventId = eventId;
        this.live = true;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCurrentScore() {
        return ThreadLocalRandom.current().nextInt(1000) + ":" + ThreadLocalRandom.current().nextInt(100);
    }

    public Boolean getLive() {
        return live;
    }
}
