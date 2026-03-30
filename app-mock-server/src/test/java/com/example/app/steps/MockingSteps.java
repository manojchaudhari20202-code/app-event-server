package com.example.app.steps;

import com.example.app.model.Event;
import com.example.app.service.MockService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BDD Step Definitions — Mock Server REST API feature.
 * Uses the real MockService backed by embedded Derby (test profile).
 */
public class MockingSteps {

    @Autowired
    private MockService mockService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Event lastEvent;
    private List<Event> lastLiveEvents;

    @Given("the mock server is running with seeded event data")
    public void the_mock_server_is_running_with_seeded_event_data() {
        // Spring context started by CucumberSpringConfiguration; DB seeded via data-test.sql
    }

    @When("I POST to \"/api/event\" with the first seeded live event id")
    public void i_post_to_event_with_first_live_id() {
        Long liveId = jdbcTemplate.queryForObject(
                "SELECT EVENT_ID FROM MOCK_EVENT_DETAILS WHERE EVENT_STATUS = TRUE FETCH FIRST 1 ROW ONLY",
                Long.class);
        assertNotNull(liveId, "No live event found in seed data");
        lastEvent = mockService.getEvent(String.valueOf(liveId));
    }

    @When("I POST to \"/api/event\" with the first seeded inactive event id")
    public void i_post_to_event_with_first_inactive_id() {
        Long inactiveId = jdbcTemplate.queryForObject(
                "SELECT EVENT_ID FROM MOCK_EVENT_DETAILS WHERE EVENT_STATUS = FALSE FETCH FIRST 1 ROW ONLY",
                Long.class);
        assertNotNull(inactiveId, "No inactive event found in seed data");
        lastEvent = mockService.getEvent(String.valueOf(inactiveId));
    }

    @When("I POST to \"/api/event\" with id {string}")
    public void i_post_to_event_with_id(String id) {
        lastEvent = mockService.getEvent(id);
    }

    @When("I POST to \"/api/liveEvents\"")
    public void i_post_to_live_events() {
        lastLiveEvents = mockService.getLiveEvents();
    }

    @Then("the response event has status true")
    public void the_response_event_has_status_true() {
        assertNotNull(lastEvent, "Expected an event but got null");
        assertTrue(lastEvent.isEventStatus(), "Expected eventStatus=true");
    }

    @Then("the response event has status false")
    public void the_response_event_has_status_false() {
        assertNotNull(lastEvent, "Expected an event but got null");
        assertFalse(lastEvent.isEventStatus(), "Expected eventStatus=false");
    }

    @Then("the response event is empty")
    public void the_response_event_is_empty() {
        assertNull(lastEvent, "Expected null response for unknown id");
    }

    @Then("the response contains only events with status true")
    public void the_response_contains_only_live_events() {
        assertNotNull(lastLiveEvents);
        assertFalse(lastLiveEvents.isEmpty(), "Expected at least one live event");
        assertTrue(lastLiveEvents.stream().allMatch(Event::isEventStatus),
                "All returned events must have eventStatus=true");
    }
}
