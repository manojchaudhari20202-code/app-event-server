package com.example.app;

public class LiveEvent {

    private String eventId;
    private Boolean live;
    private String currentScore;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Boolean getLive() {
        return live;
    }

    public void setLive(Boolean live) {
        this.live = live;
    }

    public String getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(String currentScore) {
        this.currentScore = currentScore;
    }

    @Override
    public String toString() {
        return "LiveEvent{" +
                "eventId='" + eventId + '\'' +
                ", live=" + live +
                ", currentScore='" + currentScore + '\'' +
                '}';
    }
}
