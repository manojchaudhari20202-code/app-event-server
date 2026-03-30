package com.example.app;

import com.example.app.LiveEvent;
import com.example.app.LiveEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class LiveEventServiceTest {

    private LiveEventService service;

    @BeforeEach
    void setup() {
        service = new LiveEventService();
    }

    @Test
    void shouldAddEventWhenStatusTrue() {
        service.updateCache("101", true);
        LiveEvent event = service.getFromCache("101");
        assertNotNull(event);
        assertTrue(event.getLive());
    }

    @Test
    void shouldRemoveEventWhenStatusFalse() {
        service.updateCache("101", true);
        service.updateCache("101", false);
        assertNull(service.getFromCache("101"));
    }

    @Test
    void shouldReturnAllLiveEvents() {
        service.updateCache("101", true);
        service.updateCache("102", true);
        List<LiveEvent> events = service.getAllEventsFromLiveCache();
        assertEquals(2, events.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoEvents() {
        assertTrue(service.getAllEventsFromLiveCache().isEmpty());
    }

}
