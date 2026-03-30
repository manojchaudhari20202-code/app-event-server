package com.example.app.service;

import com.example.app.model.Event;
import com.example.app.repository.APIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class)
class APIServiceTest {

    @Mock
    private APIRepository apiRepository;

    @InjectMocks
    private APIService apiService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setEventId(1L);
//        testEvent.setEventName("Test Event");
//        testEvent.setLive(true);
    }

    @Test
    void getEvent_shouldReturnEvent_whenEventExists() {
        // Given
        String eventId = "1";
        when(apiRepository.getEventById(Long.valueOf(eventId))).thenReturn(testEvent);

        // When
        Event result = apiService.getEventById(eventId);

        // Then
        assertNotNull(result);
        assertEquals(testEvent.getEventId(), result.getEventId());
        //assertEquals(testEvent.getEventName(), result.getEventName());
        verify(apiRepository, times(1)).getEventById(Long.valueOf(eventId));
    }

    @Test
    void getEvent_shouldReturnNull_whenEventDoesNotExist() {
        // Given
        String eventId = "99";
        when(apiRepository.getEventById(Long.valueOf(eventId))).thenReturn(null);

        // When
        Event result = apiService.getEventById(eventId);

        // Then
        assertNull(result);
        verify(apiRepository, times(1)).getEventById(Long.valueOf(eventId));
    }

    @Test
    void addEvent_shouldCallRepositoryAddEvent() {
        // Given
        doNothing().when(apiRepository).addEvent(testEvent);

        // When
        apiService.addEvent(testEvent);

        // Then
        verify(apiRepository, times(1)).addEvent(testEvent);
    }

    @Test
    void getAllLiveEvents_shouldReturnListOfEvents() {
        // Given
        Event anotherEvent = new Event();
        anotherEvent.setEventId(2L);
//        anotherEvent.setEventName("Another Live Event");
//        anotherEvent.setLive(true);
        List<Event> liveEvents = Arrays.asList(testEvent, anotherEvent);
        when(apiRepository.getAllLiveEvents()).thenReturn(liveEvents);

        // When
        List<Event> result = apiService.getAllLiveEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testEvent));
        assertTrue(result.contains(anotherEvent));
        verify(apiRepository, times(1)).getAllLiveEvents();
    }

    @Test
    void updateEvent_shouldCallRepositoryUpdateEvent() {
        // Given
        Event updatedEvent = new Event();
        updatedEvent.setEventId(1L);
//        updatedEvent.setEventName("Updated Event Name");
//        updatedEvent.setLive(false);
        doNothing().when(apiRepository).updateEvent(updatedEvent);

        // When
        apiService.updateEvent(updatedEvent);

        // Then
        verify(apiRepository, times(1)).updateEvent(updatedEvent);
    }

    @Test
    void removeEvent_shouldCallRepositoryRemoveEvent() {
        // Given
        Long eventIdToRemove = 1L;
        doNothing().when(apiRepository).removeEvent(eventIdToRemove);

        // When
        apiService.removeEvent(eventIdToRemove);

        // Then
        verify(apiRepository, times(1)).removeEvent(eventIdToRemove);
    }

    @Test
    void getCachedEvent_shouldReturnEvent_whenEventExists() {
        // Given
        String eventId = "1";
        when(apiRepository.getEventById(Long.valueOf(eventId))).thenReturn(testEvent);

        // When
        Event result = apiService.getCachedEventById(eventId);

        // Then
        assertNotNull(result);
        assertEquals(testEvent.getEventId(), result.getEventId());
        verify(apiRepository, times(1)).getEventById(Long.valueOf(eventId));
        // Note: Caching behavior itself is typically tested at a higher integration level
        // or by verifying cache interactions if a cache manager is mocked.
    }
}