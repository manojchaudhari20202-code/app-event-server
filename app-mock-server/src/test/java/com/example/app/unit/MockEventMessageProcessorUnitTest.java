package com.example.app.unit;

import com.example.app.model.Event;
import com.example.app.service.MockService;
import com.example.app.util.MockEventMessageProcessor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit Test Layer — MockEventMessageProcessor (RocketMQ Consumer on "mock-topic")
 * Verifies that consumed event IDs are resolved to Events and forwarded to "event-topic".
 */
@ExtendWith(MockitoExtension.class)
class MockEventMessageProcessorUnitTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @Mock
    private MockService mockService;

    @InjectMocks
    private MockEventMessageProcessor processor;

    private Event resolvedEvent;

    @BeforeEach
    void setUp() {
        resolvedEvent = new Event(1L, true, "100:200");
    }

    // ── onMessage ─────────────────────────────────────────────────────────────

    @Test
    void onMessage_resolvesEventAndForwardsToEventTopic() {
        when(mockService.getEventById(1L)).thenReturn(resolvedEvent);

        processor.onMessage(1L);

        verify(mockService).getEventById(1L);
        verify(rocketMQTemplate).convertAndSend(MockEventMessageProcessor.EVENT_TOPIC, resolvedEvent);
    }

    @Test
    void onMessage_rocketMQThrowsException_doesNotPropagate() {
        when(mockService.getEventById(2L)).thenReturn(new Event(2L, false, "300:400"));
        doThrow(new RuntimeException("broker down"))
                .when(rocketMQTemplate).convertAndSend(anyString(), any(Event.class));

        // must swallow the exception and log it
        processor.onMessage(2L);

        verify(mockService).getEventById(2L);
    }

    @Test
    void onMessage_multipleIds_eachForwardedIndependently() {
        Event event2 = new Event(2L, false, "300:400");
        when(mockService.getEventById(1L)).thenReturn(resolvedEvent);
        when(mockService.getEventById(2L)).thenReturn(event2);

        processor.onMessage(1L);
        processor.onMessage(2L);

        verify(rocketMQTemplate).convertAndSend(MockEventMessageProcessor.EVENT_TOPIC, resolvedEvent);
        verify(rocketMQTemplate).convertAndSend(MockEventMessageProcessor.EVENT_TOPIC, event2);
    }
}
