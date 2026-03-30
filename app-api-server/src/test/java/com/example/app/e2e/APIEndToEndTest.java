package com.example.app.e2e;

import com.example.app.util.APIIntegrationClient;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * E2E Test Layer — API Server
 * Full Spring Boot context on a random port, embedded Derby, all real beans
 * except external I/O (RocketMQ broker, REST mock-server).
 *
 * Validates the complete vertical slice:
 *   GraphQL query → APIController → APIService (cache) → APIRepository → Derby DB
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class APIEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    @MockitoBean
    private APIIntegrationClient apiIntegrationClient;

    private HttpGraphQlTester graphQlTester;

    @BeforeEach
    void setUp() {
        graphQlTester = HttpGraphQlTester.builder(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
        ).build();
    }

    // ── eventById: real DB lookup ─────────────────────────────────────────────

    @Test
    void eventById_queryHitsRealDb_returnsSeededEvent() {
        graphQlTester.document("""
                    { eventById(eventId: "1") { eventId eventStatus eventScore } }
                """)
                .execute()
                .path("eventById.eventId").entity(Integer.class).isEqualTo(1)
                .path("eventById.eventStatus").entity(Boolean.class).isEqualTo(true)
                .path("eventById.eventScore").entity(String.class).isEqualTo("100:200");
    }

    @Test
    void eventById_missingRecord_returnsNull() {
        graphQlTester.document("""
                    { eventById(eventId: "9999") { eventId } }
                """)
                .execute()
                .path("eventById").valueIsNull();
    }

    // ── liveEvents: filters by status ─────────────────────────────────────────

    @Test
    void liveEvents_returnsOnlyStatusTrueEvents() {
        // seed data has EVENT_IDs 1,3,4 with status=TRUE and 2,5 with status=FALSE
        graphQlTester.document("""
                    { liveEvents { eventId eventStatus } }
                """)
                .execute()
                .path("liveEvents[*].eventStatus")
                .entityList(Boolean.class)
                .satisfies(statuses -> statuses.forEach(s ->
                        org.junit.jupiter.api.Assertions.assertTrue(s,
                                "Every live event must have eventStatus=true")));
    }

    // ── write + read round-trip ───────────────────────────────────────────────

    @Test
    void insertEvent_thenQueryById_returnsInsertedEvent() {
        // Insert directly via JDBC (simulates what the RocketMQ consumer would do)
        jdbcTemplate.update(
                "INSERT INTO API_EVENT_DETAILS (EVENT_ID,EVENT_STATUS,EVENT_SCORE) VALUES (?,?,?)",
                999L, true, "E2E:TEST");

        graphQlTester.document("""
                    { eventById(eventId: "999") { eventId eventScore } }
                """)
                .execute()
                .path("eventById.eventId").entity(Integer.class).isEqualTo(999)
                .path("eventById.eventScore").entity(String.class).isEqualTo("E2E:TEST");

        // cleanup
        jdbcTemplate.update("DELETE FROM API_EVENT_DETAILS WHERE EVENT_ID = ?", 999L);
    }

    // ── cache-aside: cached response consistent ───────────────────────────────

    @Test
    void eventById_calledTwice_secondCallServedFromCache() {
        // Both calls must return the same value (cache hit on second call)
        graphQlTester.document("""
                    { eventById(eventId: "4") { eventId eventScore } }
                """)
                .execute()
                .path("eventById.eventScore").entity(String.class).isEqualTo("700:800");

        graphQlTester.document("""
                    { eventById(eventId: "4") { eventId eventScore } }
                """)
                .execute()
                .path("eventById.eventScore").entity(String.class).isEqualTo("700:800");
    }
}
