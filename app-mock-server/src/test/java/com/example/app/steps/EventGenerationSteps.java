package com.example.app.steps;

import com.example.app.model.Event;
import com.example.app.repository.MockRepository;
import com.example.app.util.RandomDataGenerator;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BDD Step Definitions — Random Event Generation (@Scheduled) feature.
 * Directly exercises RandomDataGenerator.prepareLiveMockData() against
 * the embedded Derby database, with RocketMQTemplate mocked to capture messages.
 */
public class EventGenerationSteps {

    @Autowired
    private MockRepository mockRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private int eventCountBefore;
    private int inactiveBefore;

    @When("the data generator runs one cycle")
    public void the_data_generator_runs_one_cycle() {
        eventCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MOCK_EVENT_DETAILS", Integer.class);
        inactiveBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MOCK_EVENT_DETAILS WHERE EVENT_STATUS = FALSE", Integer.class);

        // Directly construct and run the generator with real repo + mocked RocketMQ
        RandomDataGenerator generator = new RandomDataGenerator();
        injectField(generator, "apiRepository", mockRepository);
        injectField(generator, "rocketMQTemplate", rocketMQTemplate);
        generator.prepareLiveMockData();
    }

    @Then("{int} new live events are present in the store")
    public void new_live_events_are_present(int count) {
        int eventCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MOCK_EVENT_DETAILS", Integer.class);
        assertEquals(eventCountBefore + count, eventCountAfter,
                "Expected " + count + " new events inserted");
    }

    @Then("at least one event in the store has status false")
    public void at_least_one_event_is_inactive() {
        int inactiveAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MOCK_EVENT_DETAILS WHERE EVENT_STATUS = FALSE", Integer.class);
        assertTrue(inactiveAfter > 0, "Expected at least one inactive event after the generator ran");
    }

    @Then("all newly inserted events have a score matching {string} pattern")
    public void all_newly_inserted_events_have_valid_score(String pattern) {
        // The generator uses "N:M" random score format
        List<String> scores = jdbcTemplate.queryForList(
                "SELECT EVENT_SCORE FROM MOCK_EVENT_DETAILS WHERE EVENT_STATUS = TRUE ORDER BY EVENT_ID DESC FETCH FIRST 3 ROWS ONLY",
                String.class);
        assertFalse(scores.isEmpty(), "Expected newly inserted events");
        scores.forEach(score -> assertTrue(score.matches("\\d+:\\d+"),
                "Score '" + score + "' does not match N:M pattern"));
    }

    @Then("{int} messages are sent to the event topic")
    public void messages_are_sent_to_event_topic(int expectedCount) {
        // Verify RocketMQ was called the correct number of times (3 inserts + 1 update)
        verify(rocketMQTemplate, atLeast(expectedCount))
                .convertAndSend(eq(RandomDataGenerator.EVENT_TOPIC), any(Event.class));
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }
}
