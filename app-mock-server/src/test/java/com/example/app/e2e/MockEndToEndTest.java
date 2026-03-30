package com.example.app.e2e;

import com.example.app.model.Event;
import com.example.app.util.RandomDataGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E Test Layer — Mock Server
 * Full Spring Boot context on a random port, embedded Derby, all real beans
 * except the external RocketMQ broker and the scheduler (RandomDataGenerator).
 *
 * Validates the complete vertical slice:
 *   POST /api/event     → MockController → MockService → MockRepository → Derby DB
 *   POST /api/liveEvents → same chain, filtered by EVENT_STATUS=TRUE
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MockEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    @MockitoBean
    private RandomDataGenerator randomDataGenerator;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    // ── POST /api/event: real DB lookup ───────────────────────────────────────

    @Test
    void postEvent_existingId_returnsEventFromDb() {
        Long seededId = jdbcTemplate.queryForObject(
                "SELECT EVENT_ID FROM MOCK_EVENT_DETAILS FETCH FIRST 1 ROW ONLY", Long.class);
        assertNotNull(seededId);

        ResponseEntity<Event> response = restClient.post().uri("/api/event")
                .contentType(MediaType.APPLICATION_JSON)
                .body("\"" + seededId + "\"")
                .retrieve()
                .toEntity(Event.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(seededId, response.getBody().getEventId());
    }

    @Test
    void postEvent_unknownId_returns200() {
        ResponseEntity<String> response = restClient.post().uri("/api/event")
                .contentType(MediaType.APPLICATION_JSON)
                .body("\"99999\"")
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    // ── POST /api/liveEvents: filters by status ────────────────────────────────

    @Test
    void postLiveEvents_returnsOnlyStatusTrueEventsFromDb() {
        Event[] events = restClient.post().uri("/api/liveEvents")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Event[].class);

        assertNotNull(events);
        assertTrue(events.length > 0, "Expected at least one live event");
        for (Event e : events) {
            assertTrue(e.isEventStatus(), "Every returned event must have eventStatus=true");
        }
    }

    // ── write + read round-trip ────────────────────────────────────────────────

    @Test
    void insertEventDirectly_thenPostEvent_returnsInsertedEvent() {
        jdbcTemplate.update(
                "INSERT INTO MOCK_EVENT_DETAILS (EVENT_STATUS, EVENT_SCORE) VALUES (TRUE, 'E2E:TEST')");
        Long newId = jdbcTemplate.queryForObject(
                "SELECT MAX(EVENT_ID) FROM MOCK_EVENT_DETAILS", Long.class);
        assertNotNull(newId);

        ResponseEntity<Event> response = restClient.post().uri("/api/event")
                .contentType(MediaType.APPLICATION_JSON)
                .body("\"" + newId + "\"")
                .retrieve()
                .toEntity(Event.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("E2E:TEST", response.getBody().getEventScore());

        jdbcTemplate.update("DELETE FROM MOCK_EVENT_DETAILS WHERE EVENT_ID = ?", newId);
    }

    // ── Scheduler isolation: RandomDataGenerator is mocked ─────────────────────

    @Test
    void liveEvents_schedulerNotRunning_dataRemainsStable() {
        Event[] first = restClient.post().uri("/api/liveEvents")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Event[].class);

        Event[] second = restClient.post().uri("/api/liveEvents")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Event[].class);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.length, second.length,
                "Data must be stable without the scheduler running");
    }
}
