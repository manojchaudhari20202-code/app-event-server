package com.example.app.integration;

import com.example.app.model.Event;
import com.example.app.service.MockService;
import com.example.app.util.RandomDataGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test Layer — MockController (REST)
 * Full Spring context; MockService is mocked to keep tests fast and isolated.
 * Validates HTTP method, content-type, request/response JSON mapping, and status codes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class MockControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private MockService mockService;

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    @MockitoBean
    private RandomDataGenerator randomDataGenerator;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // ── POST /api/event ───────────────────────────────────────────────────────

    @Test
    void postEvent_existingId_returns200WithEvent() throws Exception {
        Event event = new Event(1L, true, "100:200");
        when(mockService.getEvent("1")).thenReturn(event);

        mockMvc.perform(post("/api/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"1\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.eventStatus").value(true))
                .andExpect(jsonPath("$.eventScore").value("100:200"));
    }

    @Test
    void postEvent_unknownId_returns200WithNullBody() throws Exception {
        when(mockService.getEvent("9999")).thenReturn(null);

        mockMvc.perform(post("/api/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"9999\""))
                .andExpect(status().isOk());
    }

    // ── POST /api/liveEvents ──────────────────────────────────────────────────

    @Test
    void postLiveEvents_returnsJsonArrayOfLiveEvents() throws Exception {
        List<Event> events = Arrays.asList(
                new Event(1L, true, "100:200"),
                new Event(3L, true, "500:600"));
        when(mockService.getLiveEvents()).thenReturn(events);

        mockMvc.perform(post("/api/liveEvents")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventId").value(1))
                .andExpect(jsonPath("$[1].eventId").value(3));
    }

    @Test
    void postLiveEvents_emptyList_returnsEmptyJsonArray() throws Exception {
        when(mockService.getLiveEvents()).thenReturn(List.of());

        mockMvc.perform(post("/api/liveEvents")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void postEvent_inactiveEvent_returns200WithFalseStatus() throws Exception {
        Event inactive = new Event(2L, false, "300:400");
        when(mockService.getEvent("2")).thenReturn(inactive);

        mockMvc.perform(post("/api/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"2\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventStatus").value(false))
                .andExpect(jsonPath("$.eventScore").value("300:400"));
    }
}
