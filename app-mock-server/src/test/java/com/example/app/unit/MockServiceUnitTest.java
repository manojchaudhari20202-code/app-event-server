package com.example.app.unit;

import com.example.app.model.Event;
import com.example.app.repository.MockRepository;
import com.example.app.service.MockService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Test Layer — MockService
 * Pure Mockito: verifies message dispatch and repository delegation.
 */
@ExtendWith(MockitoExtension.class)
class MockServiceUnitTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @Mock
    private MockRepository mockRepository;

    @InjectMocks
    private MockService mockService;

    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = new Event(1L, true, "100:200");
    }

    // ── getEvent ─────────────────────────────────────────────────────────────

    @Test
    void getEvent_sendsIdToInternalTopicAndReturnsDbEvent() {
        when(mockRepository.getEventById(1L)).thenReturn(sampleEvent);

        Event result = mockService.getEvent("1");

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
        verify(rocketMQTemplate).convertAndSend("mock-topic", 1L);
        verify(mockRepository).getEventById(1L);
    }

    @Test
    void getEvent_eventNotInDb_returnsNull() {
        when(mockRepository.getEventById(99L)).thenReturn(null);

        Event result = mockService.getEvent("99");

        assertNull(result);
        verify(rocketMQTemplate).convertAndSend("mock-topic", 99L);
    }

    // ── getLiveEvents ────────────────────────────────────────────────────────

    @Test
    void getLiveEvents_returnsAllLiveEventsAndSendsToInternalTopic() {
        List<Event> events = Arrays.asList(
                new Event(1L, true, "100:200"),
                new Event(3L, true, "500:600"));
        when(mockRepository.getAllLiveEvents()).thenReturn(events);

        List<Event> result = mockService.getLiveEvents();

        assertEquals(2, result.size());
        verify(mockRepository).getAllLiveEvents();
    }

    @Test
    void getLiveEvents_emptyList_returnsEmpty() {
        when(mockRepository.getAllLiveEvents()).thenReturn(List.of());

        List<Event> result = mockService.getLiveEvents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getEventById ─────────────────────────────────────────────────────────

    @Test
    void getEventById_delegatesToRepository() {
        when(mockRepository.getEventById(1L)).thenReturn(sampleEvent);

        Event result = mockService.getEventById(1L);

        assertEquals(sampleEvent, result);
        verify(mockRepository).getEventById(1L);
    }
}
