package com.example.app.unit;

import com.example.app.graphql.APIController;
import com.example.app.model.Event;
import com.example.app.service.APIService;
import com.example.app.util.APIIntegrationClient;
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
 * Unit Test Layer — APIController (GraphQL)
 * Validates query delegation: controller → service/client, no Spring context.
 */
@ExtendWith(MockitoExtension.class)
class APIControllerUnitTest {

    @Mock
    private APIService apiService;

    @Mock
    private APIIntegrationClient apiIntegrationClient;

    @InjectMocks
    private APIController apiController;

    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = new Event(1L, true, "100:200");
    }

    // ── eventById ────────────────────────────────────────────────────────────

    @Test
    void eventById_triggersRemoteCallThenReturnsCachedEvent() {
        when(apiService.getCachedEventById("1")).thenReturn(sampleEvent);

        Event result = apiController.eventById("1");

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
        // remote status call must happen before cache lookup
        verify(apiIntegrationClient).getRemoteEventStatus("1");
        verify(apiService).getCachedEventById("1");
    }

    @Test
    void eventById_returnsNullWhenServiceReturnsNull() {
        when(apiService.getCachedEventById("99")).thenReturn(null);

        Event result = apiController.eventById("99");

        assertNull(result);
        verify(apiIntegrationClient).getRemoteEventStatus("99");
    }

    // ── liveEvents ───────────────────────────────────────────────────────────

    @Test
    void liveEvents_returnsAllLiveEventsFromService() {
        List<Event> liveEvents = Arrays.asList(
                new Event(1L, true, "100:200"),
                new Event(3L, true, "500:600"));
        when(apiService.getAllLiveEvents()).thenReturn(liveEvents);

        List<Event> result = apiController.liveEvents();

        assertEquals(2, result.size());
        verify(apiService).getAllLiveEvents();
        verifyNoInteractions(apiIntegrationClient);
    }

    @Test
    void liveEvents_returnsEmptyListWhenNoLiveEvents() {
        when(apiService.getAllLiveEvents()).thenReturn(List.of());

        List<Event> result = apiController.liveEvents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
