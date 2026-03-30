package com.example.app.unit;

import com.example.app.controller.MockController;
import com.example.app.model.Event;
import com.example.app.service.MockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test Layer — MockController (REST)
 * Validates controller delegation to MockService without a Servlet context.
 */
@ExtendWith(MockitoExtension.class)
class MockControllerUnitTest {

    @Mock
    private MockService mockService;

    @InjectMocks
    private MockController mockController;

    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = new Event(1L, true, "100:200");
    }

    // ── POST /event ───────────────────────────────────────────────────────────

    @Test
    void event_delegatesStringIdToService_returnsEvent() {
        when(mockService.getEvent("1")).thenReturn(sampleEvent);

        Event result = mockController.event("1");

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
        verify(mockService).getEvent("1");
    }

    @Test
    void event_unknownId_returnsNull() {
        when(mockService.getEvent("999")).thenReturn(null);

        Event result = mockController.event("999");

        assertNull(result);
    }

    // ── POST /liveEvents ─────────────────────────────────────────────────────

    @Test
    void liveEvents_returnsAllLiveEventsFromService() {
        List<Event> events = Arrays.asList(
                new Event(1L, true, "100:200"),
                new Event(3L, true, "500:600"));
        when(mockService.getLiveEvents()).thenReturn(events);

        List<Event> result = mockController.liveEvents();

        assertEquals(2, result.size());
        verify(mockService).getLiveEvents();
    }

    @Test
    void liveEvents_emptyResult_returnsEmptyList() {
        when(mockService.getLiveEvents()).thenReturn(List.of());

        List<Event> result = mockController.liveEvents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
