package com.example.app.steps;

import com.example.app.model.Event;
import com.example.app.service.APIService;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BDD Step Definitions — Event Lifecycle (write operations).
 * Uses the real APIService backed by embedded Derby so add/update/upsert/remove
 * are exercised against a real transactional database.
 */
public class EventLifecycleSteps {

    @Autowired
    private APIService apiService;

    private Event workingEvent;
    private Event queryResult;
    private final List<Long> insertedIds = new ArrayList<>();

    @After
    public void cleanup() {
        insertedIds.forEach(id -> {
            try { apiService.removeEvent(id); } catch (Exception ignored) {}
        });
        insertedIds.clear();
    }

    // ── Given ────────────────────────────────────────────────────────────────

    @Given("a new event with id {long}, status {word}, and score {string}")
    public void a_new_event_with(Long id, String status, String score) {
        workingEvent = new Event(id, Boolean.parseBoolean(status), score);
        insertedIds.add(id);
    }

    @Given("event with id {long} exists in the store")
    public void event_with_id_exists(Long id) {
        Event e = apiService.getEventById(String.valueOf(id));
        assertNotNull(e, "Pre-condition failed: event " + id + " not found in store");
    }

    @Given("no event with id {long} exists in the store")
    public void no_event_with_id_exists(Long id) {
        Event e = apiService.getEventById(String.valueOf(id));
        assertNull(e, "Pre-condition failed: event " + id + " should not exist");
        insertedIds.add(id);
    }

    // ── When ─────────────────────────────────────────────────────────────────

    @When("the event is added to the store")
    public void the_event_is_added() {
        apiService.addEvent(workingEvent);
    }

    @When("the event score is updated to {string}")
    public void the_event_score_is_updated_to(String newScore) {
        workingEvent = apiService.getEventById(String.valueOf(workingEvent != null ? workingEvent.getEventId() : 1L));
        if (workingEvent == null) {
            workingEvent = new Event(1L, true, newScore);
        } else {
            workingEvent.setEventScore(newScore);
        }
        apiService.updateEvent(workingEvent);
    }

    @When("event with id {long}, status {word}, and score {string} is upserted")
    public void event_is_upserted(Long id, String status, String score) {
        Event event = new Event(id, Boolean.parseBoolean(status), score);
        apiService.upsertEvent(event);
        insertedIds.add(id);
    }

    @When("event with id {long} is removed")
    public void event_is_removed(Long id) {
        apiService.removeEvent(id);
    }

    // ── Then ─────────────────────────────────────────────────────────────────

    @Then("querying by id {long} returns the event with score {string}")
    public void querying_by_id_returns_event_with_score(Long id, String expectedScore) {
        queryResult = apiService.getEventById(String.valueOf(id));
        assertNotNull(queryResult, "Expected event with id " + id + " but got null");
        assertEquals(expectedScore, queryResult.getEventScore());
    }

    @Then("querying by id {long} returns null")
    public void querying_by_id_returns_null(Long id) {
        queryResult = apiService.getEventById(String.valueOf(id));
        assertNull(queryResult, "Expected null for event id " + id + " but found one");
    }
}
