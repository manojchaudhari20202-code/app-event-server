package com.example.app.model;

import java.io.Serializable;

public class Event implements Serializable {

    private Long eventId;
    private boolean eventStatus;
    private String eventScore;

    public Event() {
    }

    public Event(Long eventId, boolean eventStatus, String eventScore) {
        this.eventId = eventId;
        this.eventStatus = eventStatus;
        this.eventScore = eventScore;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public boolean isEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(boolean eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String getEventScore() {
        return eventScore;
    }

    public void setEventScore(String eventScore) {
        this.eventScore = eventScore;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", eventStatus=" + eventStatus +
                ", eventScore='" + eventScore + '\'' +
                '}';
    }


}