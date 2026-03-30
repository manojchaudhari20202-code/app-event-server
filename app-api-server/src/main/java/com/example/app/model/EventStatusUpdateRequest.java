package com.example.app.model;

public class EventStatusUpdateRequest {

    private Long EventId;
    private EventStatus status;

    public Long getEventId() {
        return EventId;
    }

    public void setEventId(Long eventId) {
        EventId = eventId;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }
}
