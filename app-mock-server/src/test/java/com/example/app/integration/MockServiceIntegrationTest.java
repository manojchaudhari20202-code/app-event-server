package com.example.app.integration;

import com.example.app.model.Event;
import com.example.app.service.MockService;
import com.example.app.util.RandomDataGenerator;
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

/**
 * Integration Test Layer — MockService
 * Full Spring context + embedded Derby.
 * RocketMQ, RandomDataGenerator (scheduler) replaced by mocks.
 * Each test rolls back to leave the DB clean.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
class MockServiceIntegrationTest {

    @Autowired
    private MockService mockService;

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    @MockitoBean
    private RandomDataGenerator randomDataGenerator;

    // ── getEvent ─────────────────────────────────────────────────────────────

    @Test
    void getEvent_existingId_returnsEvent() {
        // Seeded EVENT_ID 1 exists (generated identity; query by known id via repo)
        List<Event> live = mockService.getLiveEvents();
        assertFalse(live.isEmpty(), "Seed data must contain at least one event");

        Long firstLiveId = live.get(0).getEventId();
        Event result = mockService.getEvent(String.valueOf(firstLiveId));

        assertNotNull(result);
        assertEquals(firstLiveId, result.getEventId());
    }

    @Test
    void getEvent_nonExistingId_returnsNull() {
        Event result = mockService.getEvent("99999");

        assertNull(result);
    }

    // ── getLiveEvents ────────────────────────────────────────────────────────

    @Test
    void getLiveEvents_returnsOnlyLiveEvents() {
        List<Event> liveEvents = mockService.getLiveEvents();

        assertFalse(liveEvents.isEmpty());
        assertTrue(liveEvents.stream().allMatch(Event::isEventStatus),
                "All events from getLiveEvents must have eventStatus=true");
    }

    // ── getEventById ─────────────────────────────────────────────────────────

    @Test
    void getEventById_existingId_returnsEvent() {
        List<Event> live = mockService.getLiveEvents();
        Long firstId = live.get(0).getEventId();

        Event result = mockService.getEventById(firstId);

        assertNotNull(result);
        assertEquals(firstId, result.getEventId());
    }

    @Test
    void getEventById_nonExistingId_returnsNull() {
        Event result = mockService.getEventById(99999L);

        assertNull(result);
    }
}
