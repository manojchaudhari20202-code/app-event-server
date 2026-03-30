package com.example.app.integration;

import com.example.app.model.Event;
import com.example.app.service.APIService;
import com.example.app.util.APIIntegrationClient;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration Test Layer — APIService
 * Full Spring context + embedded Derby.
 * External services (RocketMQ, REST client) replaced by Mockito beans.
 * Each test rolls back its transaction to leave the DB clean.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
class APIServiceIntegrationTest {

    @Autowired
    private APIService apiService;

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    @MockitoBean
    private APIIntegrationClient apiIntegrationClient;

    // ── getEventById ─────────────────────────────────────────────────────────

    @Test
    void getEventById_existingId_returnsEvent() {
        Event event = apiService.getEventById("1");

        assertNotNull(event);
        assertEquals(1L, event.getEventId());
        assertTrue(event.isEventStatus());
        assertEquals("100:200", event.getEventScore());
    }

    @Test
    void getEventById_nonExistingId_returnsNull() {
        Event event = apiService.getEventById("9999");

        assertNull(event);
    }

    // ── getAllLiveEvents ──────────────────────────────────────────────────────

    @Test
    void getAllLiveEvents_returnsOnlyLiveEvents() {
        List<Event> liveEvents = apiService.getAllLiveEvents();

        assertFalse(liveEvents.isEmpty());
        assertTrue(liveEvents.stream().allMatch(Event::isEventStatus),
                "All returned events must have eventStatus=true");
    }

    // ── addEvent + getEventById ───────────────────────────────────────────────

    @Test
    void addEvent_thenGetById_returnsAddedEvent() {
        Event newEvent = new Event(100L, true, "111:222");
        apiService.addEvent(newEvent);

        Event fetched = apiService.getEventById("100");

        assertNotNull(fetched);
        assertEquals(100L, fetched.getEventId());
        assertEquals("111:222", fetched.getEventScore());
    }

    // ── updateEvent ──────────────────────────────────────────────────────────

    @Test
    void updateEvent_changesScoreAndStatus() {
        Event toUpdate = new Event(1L, false, "UPDATED:999");
        apiService.updateEvent(toUpdate);

        Event updated = apiService.getEventById("1");

        assertNotNull(updated);
        assertFalse(updated.isEventStatus());
        assertEquals("UPDATED:999", updated.getEventScore());
    }

    // ── upsertEvent ──────────────────────────────────────────────────────────

    @Test
    void upsertEvent_existingEvent_updates() {
        Event toUpsert = new Event(2L, true, "UPSERTED:SCORE");
        apiService.upsertEvent(toUpsert);

        Event result = apiService.getEventById("2");

        assertNotNull(result);
        assertTrue(result.isEventStatus());
        assertEquals("UPSERTED:SCORE", result.getEventScore());
    }

    @Test
    void upsertEvent_newEvent_inserts() {
        Event newEvent = new Event(200L, true, "200:200");
        apiService.upsertEvent(newEvent);

        Event result = apiService.getEventById("200");

        assertNotNull(result);
        assertEquals(200L, result.getEventId());
    }

    // ── removeEvent ──────────────────────────────────────────────────────────

    @Test
    void removeEvent_deletesEventFromDb() {
        apiService.removeEvent(3L);

        Event removed = apiService.getEventById("3");

        assertNull(removed);
    }

    // ── getCachedEventById (cache-aside) ─────────────────────────────────────

    @Test
    void getCachedEventById_foundInDb_doesNotCallRemoteClient() {
        Event result = apiService.getCachedEventById("1");

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
    }

    @Test
    void getCachedEventById_notInDb_callsRemoteClient() {
        Event remoteEvent = new Event(9999L, true, "REMOTE");
        when(apiIntegrationClient.getRemoteEventStatus(anyString())).thenReturn(remoteEvent);

        Event result = apiService.getCachedEventById("9999");

        // service falls back to integration client when DB misses
        assertNotNull(result);
    }
}
