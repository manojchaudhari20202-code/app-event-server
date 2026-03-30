package com.example.app.unit;

import com.example.app.model.Event;
import com.example.app.service.APIService;
import com.example.app.util.APIEventMessageProcessor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit Test Layer — APIEventMessageProcessor (RocketMQ Consumer)
 * Verifies that incoming Event messages from "event-topic" are upserted via APIService.
 */
@ExtendWith(MockitoExtension.class)
class APIEventMessageProcessorUnitTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @Mock
    private APIService apiService;

    @InjectMocks
    private APIEventMessageProcessor processor;

    private Event incomingEvent;

    @BeforeEach
    void setUp() {
        incomingEvent = new Event(1L, true, "100:200");
    }

    // ── onMessage ─────────────────────────────────────────────────────────────

    @Test
    void onMessage_callsUpsertOnService() {
        processor.onMessage(incomingEvent);

        verify(apiService).upsertEvent(incomingEvent);
    }

    @Test
    void onMessage_serviceThrowsException_doesNotPropagate() {
        doThrow(new RuntimeException("DB error")).when(apiService).upsertEvent(any());

        // should swallow the exception and log it
        processor.onMessage(incomingEvent);

        verify(apiService).upsertEvent(incomingEvent);
    }

    @Test
    void onMessage_nullEvent_handledGracefully() {
        doThrow(new NullPointerException()).when(apiService).upsertEvent(null);

        // must not throw out of the listener
        processor.onMessage(null);

        verify(apiService).upsertEvent(null);
    }

    @Test
    void onMessage_multipleEvents_eachUpsertedIndependently() {
        Event event2 = new Event(2L, false, "300:400");

        processor.onMessage(incomingEvent);
        processor.onMessage(event2);

        verify(apiService).upsertEvent(incomingEvent);
        verify(apiService).upsertEvent(event2);
    }
}
