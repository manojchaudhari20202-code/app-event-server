package com.example.app.unit;

import com.example.app.model.Event;
import com.example.app.repository.APIRepository;
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
 * Unit Test Layer — APIService
 * Pure Mockito: no Spring context, no DB, no network.
 * Validates business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class APIServiceUnitTest {

    @Mock
    private APIRepository apiRepository;

    @Mock
    private APIIntegrationClient apiIntegrationClient;

    @InjectMocks
    private APIService apiService;

    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = new Event(1L, true, "100:200");
    }

    // ── getCachedEventById ───────────────────────────────────────────────────

    @Test
    void getCachedEventById_foundInRepo_returnsEventWithoutCallingClient() {
        when(apiRepository.getEventById(1L)).thenReturn(sampleEvent);

        Event result = apiService.getCachedEventById("1");

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
        assertTrue(result.isEventStatus());
        verify(apiIntegrationClient, never()).getRemoteEventStatus(any());
    }

    @Test
    void getCachedEventById_notInRepo_fallsBackToIntegrationClient() {
        Event remoteEvent = new Event(1L, true, "REMOTE");
        when(apiRepository.getEventById(1L)).thenReturn(null);
        when(apiIntegrationClient.getRemoteEventStatus("1")).thenReturn(remoteEvent);

        Event result = apiService.getCachedEventById("1");

        assertNotNull(result);
        assertEquals("REMOTE", result.getEventScore());
        verify(apiIntegrationClient).getRemoteEventStatus("1");
    }

    // ── getEventById ─────────────────────────────────────────────────────────

    @Test
    void getEventById_delegatesToRepository() {
        when(apiRepository.getEventById(1L)).thenReturn(sampleEvent);

        Event result = apiService.getEventById("1");

        assertEquals(sampleEvent, result);
        verify(apiRepository).getEventById(1L);
    }

    // ── getAllLiveEvents ──────────────────────────────────────────────────────

    @Test
    void getAllLiveEvents_returnsListFromRepository() {
        List<Event> events = Arrays.asList(
                new Event(1L, true, "100:200"),
                new Event(3L, true, "500:600"));
        when(apiRepository.getAllLiveEvents()).thenReturn(events);

        List<Event> result = apiService.getAllLiveEvents();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Event::isEventStatus));
    }

    // ── addEvent ─────────────────────────────────────────────────────────────

    @Test
    void addEvent_delegatesToRepository() {
        apiService.addEvent(sampleEvent);

        verify(apiRepository).addEvent(sampleEvent);
    }

    // ── updateEvent ──────────────────────────────────────────────────────────

    @Test
    void updateEvent_delegatesToRepository() {
        sampleEvent.setEventScore("999:999");
        apiService.updateEvent(sampleEvent);

        verify(apiRepository).updateEvent(sampleEvent);
    }

    // ── upsertEvent ──────────────────────────────────────────────────────────

    @Test
    void upsertEvent_delegatesToRepository() {
        apiService.upsertEvent(sampleEvent);

        verify(apiRepository).upsertEvent(sampleEvent);
    }

    // ── removeEvent ──────────────────────────────────────────────────────────

    @Test
    void removeEvent_delegatesToRepository() {
        apiService.removeEvent(1L);

        verify(apiRepository).removeEvent(1L);
    }
}
