package com.example.app.unit;

import com.example.app.model.Event;
import com.example.app.repository.MockRepository;
import com.example.app.util.RandomDataGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test Layer — RandomDataGenerator (@Scheduled batch producer)
 * Verifies that prepareLiveMockData inserts 3 events, updates 1, and
 * sends all 4 to "event-topic" via RocketMQTemplate.
 */
@ExtendWith(MockitoExtension.class)
class RandomDataGeneratorUnitTest {

    @Mock
    private MockRepository apiRepository;

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @InjectMocks
    private RandomDataGenerator randomDataGenerator;

    @BeforeEach
    void setUp() {
        // Simulate addEvent returning a generated key for 3 inserts
        when(apiRepository.addEvent(any(Event.class))).thenReturn(10L, 11L, 12L);
        // Provide a random event id and its event for the update
        when(apiRepository.getRandonEventId()).thenReturn(1L);
        when(apiRepository.getEventById(1L)).thenReturn(new Event(1L, true, "100:200"));
    }

    // ── prepareLiveMockData ──────────────────────────────────────────────────

    @Test
    void prepareLiveMockData_insertsThreeNewEvents() {
        randomDataGenerator.prepareLiveMockData();

        verify(apiRepository, times(3)).addEvent(any(Event.class));
    }

    @Test
    void prepareLiveMockData_updatesOneRandomEvent() {
        randomDataGenerator.prepareLiveMockData();

        verify(apiRepository, times(1)).updateEvent(any(Event.class));
        // Updated event must be set to inactive
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(apiRepository).updateEvent(captor.capture());
        assertFalse(captor.getValue().isEventStatus(), "Updated event must be inactive");
    }

    @Test
    void prepareLiveMockData_sendsFourMessagesToEventTopic() {
        randomDataGenerator.prepareLiveMockData();

        // 3 inserts + 1 update = 4 messages to event-topic
        verify(rocketMQTemplate, times(4))
                .convertAndSend(eq(RandomDataGenerator.EVENT_TOPIC), any(Event.class));
    }

    @Test
    void prepareLiveMockData_insertedEventsHaveScoreInCorrectFormat() {
        randomDataGenerator.prepareLiveMockData();

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(apiRepository, times(3)).addEvent(captor.capture());
        List<Event> insertedEvents = captor.getAllValues();

        insertedEvents.forEach(event -> {
            assertTrue(event.isEventStatus(), "New events should be live");
            assertNotNull(event.getEventScore());
            assertTrue(event.getEventScore().contains(":"),
                    "Score format must be 'N:M' but was: " + event.getEventScore());
        });
    }

    // ── processEventData ─────────────────────────────────────────────────────

    @Test
    void processEventData_sendsEventToRocketMQ() {
        Event event = new Event(5L, true, "500:600");

        randomDataGenerator.processEventData(event);

        verify(rocketMQTemplate).convertAndSend(RandomDataGenerator.EVENT_TOPIC, event);
    }
}
