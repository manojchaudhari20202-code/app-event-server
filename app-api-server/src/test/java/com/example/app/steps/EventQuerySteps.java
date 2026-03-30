package com.example.app.steps;

import com.example.app.model.Event;
import com.example.app.service.APIService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BDD Step Definitions — Event Query feature
 * Uses the real Spring APIService backed by embedded Derby (test profile).
 */
public class EventQuerySteps {

    @Autowired
    private APIService apiService;

    private Event lastEvent;
    private List<Event> lastLiveEvents;

    @Given("the API server is running with seeded event data")
    public void the_api_server_is_running_with_seeded_event_data() {
        // Spring context is already started by CucumberSpringConfiguration;
        // the embedded Derby DB is pre-seeded via data-test.sql.
    }

    @When("I query the GraphQL endpoint for event with id {string}")
    public void i_query_the_graphql_endpoint_for_event_with_id(String id) {
        lastEvent = apiService.getEventById(id);
    }

    @Then("the response event id is {int}")
    public void the_response_event_id_is(int id) {
        assertNotNull(lastEvent, "Expected an event but got null");
        assertEquals((long) id, lastEvent.getEventId());
    }

    @Then("the response event status is {word}")
    public void the_response_event_status_is(String status) {
        assertNotNull(lastEvent);
        assertEquals(Boolean.parseBoolean(status), lastEvent.isEventStatus());
    }

    @Then("the response event score is {string}")
    public void the_response_event_score_is(String score) {
        assertNotNull(lastEvent, "Expected an event but got null");
        assertEquals(score, lastEvent.getEventScore());
    }

    @Then("the response event is null")
    public void the_response_event_is_null() {
        assertNull(lastEvent, "Expected null but got an event");
    }

    @When("I query the GraphQL endpoint for all live events")
    public void i_query_the_graphql_endpoint_for_all_live_events() {
        lastLiveEvents = apiService.getAllLiveEvents();
    }

    @Then("all returned events have status true")
    public void all_returned_events_have_status_true() {
        assertNotNull(lastLiveEvents);
        assertFalse(lastLiveEvents.isEmpty(), "Live events list should not be empty");
        assertTrue(lastLiveEvents.stream().allMatch(Event::isEventStatus),
                "Every returned event must have eventStatus=true");
    }

    @And("the response event score is {string}")
    public void and_the_response_event_score_is(String score) {
        the_response_event_score_is(score);
    }
}
