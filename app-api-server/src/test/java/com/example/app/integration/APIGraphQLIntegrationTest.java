package com.example.app.integration;

import com.example.app.model.Event;
import com.example.app.service.APIService;
import com.example.app.util.APIIntegrationClient;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Integration Test Layer — GraphQL HTTP layer
 * Starts the full server on a random port; service + external dependencies mocked.
 * Exercises the GraphQL schema, query parsing, and JSON response mapping end-to-end.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class APIGraphQLIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private APIService apiService;

    @MockitoBean
    private APIIntegrationClient apiIntegrationClient;

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    private HttpGraphQlTester graphQlTester;

    @BeforeEach
    void setUp() {
        graphQlTester = HttpGraphQlTester.builder(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
        ).build();
    }

    // ── eventById ────────────────────────────────────────────────────────────

    @Test
    void eventById_returnsAllFields() {
        when(apiService.getCachedEventById("1")).thenReturn(new Event(1L, true, "100:200"));

        graphQlTester.document("""
                    { eventById(eventId: "1") { eventId eventStatus eventScore } }
                """)
                .execute()
                .path("eventById.eventId").entity(Integer.class).isEqualTo(1)
                .path("eventById.eventStatus").entity(Boolean.class).isEqualTo(true)
                .path("eventById.eventScore").entity(String.class).isEqualTo("100:200");
    }

    @Test
    void eventById_unknownId_returnsNullNode() {
        when(apiService.getCachedEventById("9999")).thenReturn(null);

        graphQlTester.document("""
                    { eventById(eventId: "9999") { eventId eventStatus } }
                """)
                .execute()
                .path("eventById").valueIsNull();
    }

    @Test
    void eventById_partialFieldSelection_onlyRequestedFieldsReturned() {
        when(apiService.getCachedEventById("2")).thenReturn(new Event(2L, false, "300:400"));

        graphQlTester.document("""
                    { eventById(eventId: "2") { eventStatus } }
                """)
                .execute()
                .path("eventById.eventStatus").entity(Boolean.class).isEqualTo(false);
    }

    // ── liveEvents ───────────────────────────────────────────────────────────

    @Test
    void liveEvents_returnsListOfEvents() {
        List<Event> events = Arrays.asList(
                new Event(1L, true, "100:200"),
                new Event(3L, true, "500:600"));
        when(apiService.getAllLiveEvents()).thenReturn(events);

        graphQlTester.document("""
                    { liveEvents { eventId eventScore } }
                """)
                .execute()
                .path("liveEvents").entityList(Object.class).hasSize(2);
    }

    @Test
    void liveEvents_emptyResult_returnsEmptyList() {
        when(apiService.getAllLiveEvents()).thenReturn(List.of());

        graphQlTester.document("""
                    { liveEvents { eventId } }
                """)
                .execute()
                .path("liveEvents").entityList(Object.class).hasSize(0);
    }
}
